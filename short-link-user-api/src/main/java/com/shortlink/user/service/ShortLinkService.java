package com.shortlink.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.api.PageVO;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.cache.LinkCachePublisher;
import com.shortlink.common.exception.BizException;
import com.shortlink.common.security.CodePolicy;
import com.shortlink.common.security.UrlSecurityValidator;
import com.shortlink.common.util.DigestUtils;
import com.shortlink.user.dto.CreateLinkRequest;
import com.shortlink.user.dto.CreateLinkResult;
import com.shortlink.user.dto.LinkQuery;
import com.shortlink.user.dto.LinkVO;
import com.shortlink.user.dto.UpdateLinkRequest;
import com.shortlink.user.entity.Domain;
import com.shortlink.user.entity.ShortLink;
import com.shortlink.user.mapper.ShortLinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 短链的生成与管理。
 *
 * <p>幂等靠数据库唯一索引兜底，而不是先查后插：同一用户在同一域名下重复提交同一条长链接，
 * 第二次会命中 {@code uk_user_domain_hash}，此时直接把已有短链返回并标记 duplicated。</p>
 */
@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private final ShortLinkMapper shortLinkMapper;
    private final DomainService domainService;
    private final CodeGenerator codeGenerator;
    private final UrlSecurityValidator urlSecurityValidator;
    private final PasswordEncoder passwordEncoder;
    private final LinkCachePublisher linkCachePublisher;
    private final QuotaService quotaService;

    @Transactional(rollbackFor = Exception.class)
    public CreateLinkResult generate(long userId, CreateLinkRequest request) {
        URI uri = urlSecurityValidator.validate(request.getOriginUrl());
        String originUrl = uri.toString();
        Domain domain = domainService.getOrDefault(request.getDomainId());
        validateTimeWindow(request.getStartAt(), request.getEndAt(), request.getExpireAt());

        String customCode = StringUtils.hasText(request.getCode()) ? request.getCode().trim() : null;
        String urlHash = null;
        if (customCode != null) {
            CodePolicy.validateCustomCode(customCode);
        } else {
            urlHash = DigestUtils.sha256Hex(originUrl);
            ShortLink existing = findReusable(userId, domain.getId(), urlHash);
            if (existing != null) {
                return toResult(existing, domain, true);
            }
        }

        // 幂等复用不扣额度，确认要新建才计数
        quotaService.consumeGenerate(userId);

        ShortLink link = new ShortLink();
        link.setCode(customCode != null ? customCode : codeGenerator.nextCode());
        link.setDomainId(domain.getId());
        link.setUserId(userId);
        link.setOriginUrl(originUrl);
        link.setUrlHash(urlHash);
        link.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : "");
        link.setPasswordHash(StringUtils.hasText(request.getPassword())
                ? passwordEncoder.encode(request.getPassword()) : null);
        link.setStartAt(request.getStartAt());
        link.setEndAt(request.getEndAt());
        link.setExpireAt(request.getExpireAt());
        link.setStatus(ShortLink.STATUS_NORMAL);
        link.setClickCount(0L);
        link.setCreatedAt(LocalDateTime.now());
        link.setUpdatedAt(LocalDateTime.now());

        try {
            shortLinkMapper.insert(link);
        } catch (DuplicateKeyException e) {
            quotaService.refundGenerate(userId);
            if (customCode != null) {
                throw new BizException(ResultCode.CODE_OCCUPIED);
            }
            ShortLink existing = findReusable(userId, domain.getId(), urlHash);
            if (existing != null) {
                return toResult(existing, domain, true);
            }
            throw new BizException(ResultCode.CODE_OCCUPIED);
        }
        linkCachePublisher.publishAdd(link.getCode());
        return toResult(link, domain, false);
    }

    public PageVO<LinkVO> page(long userId, LinkQuery query) {
        Page<ShortLink> page = shortLinkMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), buildWrapper(userId, query));
        Map<Long, String> names = domainService.nameMap();
        List<LinkVO> records = page.getRecords().stream()
                .map(link -> toVO(link, names.getOrDefault(link.getDomainId(), "")))
                .collect(Collectors.toList());
        return new PageVO<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    public LinkVO detail(long userId, String code) {
        ShortLink link = requireOwned(userId, code);
        Map<Long, String> names = domainService.nameMap();
        return toVO(link, names.getOrDefault(link.getDomainId(), ""));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(long userId, String code, UpdateLinkRequest request) {
        ShortLink link = requireOwned(userId, code);
        validateTimeWindow(request.getStartAt(), request.getEndAt(), request.getExpireAt());

        LambdaUpdateWrapper<ShortLink> wrapper = new LambdaUpdateWrapper<ShortLink>()
                .eq(ShortLink::getId, link.getId());
        if (request.getRemark() != null) {
            wrapper.set(ShortLink::getRemark, request.getRemark().trim());
        }
        if (request.getPassword() != null) {
            wrapper.set(ShortLink::getPasswordHash, request.getPassword().isEmpty()
                    ? null : passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStartAt() != null) {
            wrapper.set(ShortLink::getStartAt, request.getStartAt());
        }
        if (request.getEndAt() != null) {
            wrapper.set(ShortLink::getEndAt, request.getEndAt());
        }
        if (request.getExpireAt() != null) {
            wrapper.set(ShortLink::getExpireAt, request.getExpireAt());
        }
        if (request.getStatus() != null) {
            wrapper.set(ShortLink::getStatus, request.getStatus());
        }
        wrapper.set(ShortLink::getUpdatedAt, LocalDateTime.now());
        shortLinkMapper.update(null, wrapper);
        linkCachePublisher.evict(code);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(long userId, String code) {
        ShortLink link = requireOwned(userId, code);
        shortLinkMapper.update(null, new LambdaUpdateWrapper<ShortLink>()
                .eq(ShortLink::getId, link.getId())
                .set(ShortLink::getStatus, ShortLink.STATUS_DELETED)
                // 释放幂等唯一索引的占位，否则同一长链接再也无法重新生成
                .set(ShortLink::getUrlHash, null)
                .set(ShortLink::getUpdatedAt, LocalDateTime.now()));
        linkCachePublisher.evict(code);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteBatch(long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<ShortLink> links = shortLinkMapper.selectList(new LambdaQueryWrapper<ShortLink>()
                .eq(ShortLink::getUserId, userId)
                .in(ShortLink::getId, ids)
                .ne(ShortLink::getStatus, ShortLink.STATUS_DELETED));
        if (links.isEmpty()) {
            return 0;
        }
        List<Long> targetIds = links.stream().map(ShortLink::getId).collect(Collectors.toList());
        shortLinkMapper.update(null, new LambdaUpdateWrapper<ShortLink>()
                .in(ShortLink::getId, targetIds)
                .set(ShortLink::getStatus, ShortLink.STATUS_DELETED)
                .set(ShortLink::getUrlHash, null)
                .set(ShortLink::getUpdatedAt, LocalDateTime.now()));
        links.forEach(link -> linkCachePublisher.evict(link.getCode()));
        return targetIds.size();
    }

    private ShortLink findReusable(long userId, long domainId, String urlHash) {
        return shortLinkMapper.selectOne(new LambdaQueryWrapper<ShortLink>()
                .eq(ShortLink::getUserId, userId)
                .eq(ShortLink::getDomainId, domainId)
                .eq(ShortLink::getUrlHash, urlHash)
                .ne(ShortLink::getStatus, ShortLink.STATUS_DELETED)
                .last("limit 1"));
    }

    private ShortLink requireOwned(long userId, String code) {
        ShortLink link = shortLinkMapper.selectOne(new LambdaQueryWrapper<ShortLink>()
                .eq(ShortLink::getCode, code)
                .eq(ShortLink::getUserId, userId)
                .ne(ShortLink::getStatus, ShortLink.STATUS_DELETED)
                .last("limit 1"));
        if (link == null) {
            throw new BizException(ResultCode.NOT_FOUND, "短链不存在或无权访问");
        }
        return link;
    }

    private LambdaQueryWrapper<ShortLink> buildWrapper(long userId, LinkQuery query) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ShortLink> wrapper = new LambdaQueryWrapper<ShortLink>()
                .eq(ShortLink::getUserId, userId);

        if (StringUtils.hasText(query.getRemark())) {
            wrapper.like(ShortLink::getRemark, query.getRemark().trim());
        }
        if (query.getDomainId() != null) {
            wrapper.eq(ShortLink::getDomainId, query.getDomainId());
        }

        String status = query.getStatus();
        if ("normal".equalsIgnoreCase(status)) {
            wrapper.eq(ShortLink::getStatus, ShortLink.STATUS_NORMAL)
                    .and(w -> w.isNull(ShortLink::getExpireAt).or().gt(ShortLink::getExpireAt, now));
        } else if ("expired".equalsIgnoreCase(status)) {
            wrapper.eq(ShortLink::getStatus, ShortLink.STATUS_NORMAL)
                    .isNotNull(ShortLink::getExpireAt)
                    .le(ShortLink::getExpireAt, now);
        } else if ("disabled".equalsIgnoreCase(status)) {
            wrapper.eq(ShortLink::getStatus, ShortLink.STATUS_DISABLED);
        } else if ("deleted".equalsIgnoreCase(status)) {
            wrapper.eq(ShortLink::getStatus, ShortLink.STATUS_DELETED);
        } else {
            wrapper.ne(ShortLink::getStatus, ShortLink.STATUS_DELETED);
        }

        if (query.getStartDate() != null) {
            wrapper.ge(ShortLink::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.lt(ShortLink::getCreatedAt, query.getEndDate().plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(ShortLink::getId);
        return wrapper;
    }

    private void validateTimeWindow(LocalDateTime startAt, LocalDateTime endAt, LocalDateTime expireAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new BizException(ResultCode.PARAM_ERROR, "生效结束时间必须晚于开始时间");
        }
        if (expireAt != null && expireAt.isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.PARAM_ERROR, "到期时间不能早于当前时间");
        }
    }

    private CreateLinkResult toResult(ShortLink link, Domain domain, boolean duplicated) {
        return new CreateLinkResult(link.getId(), link.getCode(),
                domainService.buildShortUrl(domain, link.getCode()), duplicated, link.getCreatedAt());
    }

    private LinkVO toVO(ShortLink link, String domainName) {
        return new LinkVO(link.getId(), link.getCode(),
                domainService.buildShortUrl(domainName, link.getCode()),
                link.getOriginUrl(), link.getRemark(), link.getDomainId(), domainName,
                deriveStatus(link), link.getPasswordHash() != null,
                link.getClickCount() == null ? 0L : link.getClickCount(),
                link.getStartAt(), link.getEndAt(), link.getExpireAt(), link.getCreatedAt());
    }

    private String deriveStatus(ShortLink link) {
        if (link.getStatus() == null) {
            return "normal";
        }
        if (link.getStatus() == ShortLink.STATUS_DELETED) {
            return "deleted";
        }
        if (link.getStatus() == ShortLink.STATUS_DISABLED) {
            return "disabled";
        }
        if (link.getExpireAt() != null && link.getExpireAt().isBefore(LocalDateTime.now())) {
            return "expired";
        }
        return "normal";
    }
}
