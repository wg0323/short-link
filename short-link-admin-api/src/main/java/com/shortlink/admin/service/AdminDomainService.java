package com.shortlink.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shortlink.admin.dto.DomainRequest;
import com.shortlink.admin.dto.DomainVO;
import com.shortlink.admin.entity.Domain;
import com.shortlink.admin.mapper.DomainMapper;
import com.shortlink.common.api.ResultCode;
import com.shortlink.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 短链域名维护。域名只影响新生成的短链展示，不回溯已有短链。
 */
@Service
@RequiredArgsConstructor
public class AdminDomainService {

    private final DomainMapper domainMapper;

    public List<DomainVO> list() {
        return domainMapper.selectList(new LambdaQueryWrapper<Domain>()
                        .orderByDesc(Domain::getIsDefault)
                        .orderByAsc(Domain::getId))
                .stream()
                .map(item -> new DomainVO(item.getId(), item.getDomain(),
                        Integer.valueOf(1).equals(item.getIsDefault()), item.getStatus(),
                        domainMapper.countLinks(item.getId()), item.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(DomainRequest request) {
        String domainName = request.getDomain().trim().toLowerCase();
        if (domainMapper.selectCount(new LambdaQueryWrapper<Domain>()
                .eq(Domain::getDomain, domainName)) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "该域名已存在");
        }
        if (request.isDefaultFlag()) {
            clearDefault();
        }

        Domain domain = new Domain();
        domain.setDomain(domainName);
        domain.setIsDefault(request.isDefaultFlag() ? 1 : 0);
        domain.setStatus(1);
        try {
            domainMapper.insert(domain);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.PARAM_ERROR, "该域名已存在");
        }
    }

    public void updateStatus(long id, int status) {
        if (status != 0 && status != 1) {
            throw new BizException(ResultCode.PARAM_ERROR, "状态值不合法");
        }
        Domain domain = domainMapper.selectById(id);
        if (domain == null) {
            throw new BizException(ResultCode.NOT_FOUND, "域名不存在");
        }
        if (status == 0 && Integer.valueOf(1).equals(domain.getIsDefault())) {
            throw new BizException(ResultCode.PARAM_ERROR, "默认域名不能停用，请先切换默认域名");
        }
        Domain update = new Domain();
        update.setId(id);
        update.setStatus(status);
        domainMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(long id) {
        Domain domain = domainMapper.selectById(id);
        if (domain == null) {
            return;
        }
        if (Integer.valueOf(1).equals(domain.getIsDefault())) {
            throw new BizException(ResultCode.PARAM_ERROR, "默认域名不能删除");
        }
        if (domainMapper.countLinks(id) > 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "该域名下还有短链，不能删除");
        }
        domainMapper.deleteById(id);
    }

    private void clearDefault() {
        domainMapper.update(null, new LambdaUpdateWrapper<Domain>()
                .eq(Domain::getIsDefault, 1)
                .set(Domain::getIsDefault, 0));
    }
}
