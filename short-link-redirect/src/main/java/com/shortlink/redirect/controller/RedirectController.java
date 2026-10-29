package com.shortlink.redirect.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.shortlink.common.util.Base62;
import com.shortlink.common.util.IpUtils;
import com.shortlink.redirect.bloom.BloomFilterService;
import com.shortlink.redirect.cache.LinkCacheService;
import com.shortlink.redirect.cache.LinkSourceUnavailableException;
import com.shortlink.redirect.entity.RedirectLink;
import com.shortlink.redirect.event.ClickEvent;
import com.shortlink.redirect.event.ClickEventPublisher;
import com.shortlink.redirect.support.IpRegionService;
import com.shortlink.redirect.support.UserAgentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 短链跳转。
 *
 * <p>链路：布隆过滤 → 本地缓存 → Redis → MySQL；命中后校验状态与时间窗，
 * 投递埋点事件，最后返回 302。整个链路不发起任何对外请求，因此不存在
 * 「服务端跟随重定向」这类 SSRF 面。</p>
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class RedirectController {

    /** Sentinel 资源名，流控与热点参数规则都挂到它上面。 */
    private static final String RESOURCE_REDIRECT = "redirect:click";

    private final BloomFilterService bloomFilterService;
    private final LinkCacheService linkCacheService;
    private final ClickEventPublisher clickEventPublisher;
    private final IpRegionService ipRegionService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/{code}")
    public ModelAndView redirect(@PathVariable String code, HttpServletRequest request,
                                 HttpServletResponse response) {
        Entry entry = null;
        try {
            // 热点参数取 code，用于拦单条爆款短链
            entry = SphU.entry(RESOURCE_REDIRECT, EntryType.IN, 1, code);
            RedirectLink link = resolve(code);
            if (link == null) {
                return errorView(404, "链接不存在", "请检查地址是否输入正确");
            }
            String invalidReason = checkAvailability(link, LocalDateTime.now());
            if (invalidReason != null) {
                return errorView(403, "链接无法访问", invalidReason);
            }
            if (link.getPasswordHash() != null) {
                return passwordView(code, null);
            }
            publishClick(link, request);
            return redirectTo(link.getOriginUrl(), response);
        } catch (LinkSourceUnavailableException e) {
            log.warn("缓存与回源均不可用 code={}", code);
            return errorView(503, "服务暂时不可用", "缓存与数据库都在恢复中，请稍后重试");
        } catch (BlockException e) {
            log.warn("跳转被 Sentinel 拦截 code={} rule={}", code, e.getRule() == null ? "-" : e.getRule().getResource());
            return errorView(429, "访问太频繁", "请稍后再试");
        } catch (Exception e) {
            log.error("跳转处理异常 code={}", code, e);
            return errorView(500, "服务暂时不可用", "请稍后重试");
        } finally {
            if (entry != null) {
                entry.exit(1, code);
            }
        }
    }

    @PostMapping("/p/{code}")
    public ModelAndView submitPassword(@PathVariable String code,
                                       @RequestParam("password") String password,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        try {
            RedirectLink link = resolve(code);
            if (link == null) {
                return errorView(404, "链接不存在", "请检查地址是否输入正确");
            }
            String invalidReason = checkAvailability(link, LocalDateTime.now());
            if (invalidReason != null) {
                return errorView(403, "链接无法访问", invalidReason);
            }
            if (link.getPasswordHash() == null) {
                return redirectTo(link.getOriginUrl(), response);
            }
            if (!passwordEncoder.matches(password, link.getPasswordHash())) {
                return passwordView(code, "访问密码不正确");
            }
            publishClick(link, request);
            return redirectTo(link.getOriginUrl(), response);
        } catch (LinkSourceUnavailableException e) {
            log.warn("缓存与回源均不可用 code={}", code);
            return errorView(503, "服务暂时不可用", "缓存与数据库都在恢复中，请稍后重试");
        } catch (Exception e) {
            log.error("密码校验异常 code={}", code, e);
            return errorView(500, "服务暂时不可用", "请稍后重试");
        }
    }

    private RedirectLink resolve(String code) {
        if (!Base62.isValidCode(code)) {
            return null;
        }
        if (!bloomFilterService.mightContain(code)) {
            return null;
        }
        return linkCacheService.get(code);
    }

    /**
     * 返回 null 表示可访问，否则返回不可访问的原因。
     */
    private String checkAvailability(RedirectLink link, LocalDateTime now) {
        if (link.getStatus() == null || link.getStatus() == RedirectLink.STATUS_DELETED) {
            return "该链接已不存在";
        }
        if (link.getStatus() == RedirectLink.STATUS_DISABLED) {
            return "该链接已被禁用";
        }
        if (link.getQuotaBlocked() != null && link.getQuotaBlocked() == 1) {
            return "该链接暂时无法访问，请稍后再试";
        }
        if (link.getExpireAt() != null && link.getExpireAt().isBefore(now)) {
            return "该链接已过期";
        }
        if (link.getStartAt() != null && now.isBefore(link.getStartAt())) {
            return "该链接尚未生效";
        }
        if (link.getEndAt() != null && now.isAfter(link.getEndAt())) {
            return "该链接已过生效时间";
        }
        return null;
    }

    private void publishClick(RedirectLink link, HttpServletRequest request) {
        String ip = IpUtils.getClientIp(request);
        String ua = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String[] region = ipRegionService.resolve(ip);
        clickEventPublisher.publish(new ClickEvent(link.getCode(), link.getUserId(), LocalDateTime.now(), ip,
                region[0], region[1], UserAgentParser.parse(ua),
                referer == null ? "" : referer, ua == null ? "" : ua));
    }

    private ModelAndView redirectTo(String url, HttpServletResponse response) {
        // 302 必须禁掉浏览器缓存，否则同一个短链的后续点击不会再回到服务端，统计就丢了
        response.setHeader("Cache-Control", "no-store");
        RedirectView view = new RedirectView(url);
        view.setContextRelative(false);
        view.setStatusCode(HttpStatus.FOUND);
        view.setExpandUriTemplateVariables(false);
        view.setExposeModelAttributes(false);
        return new ModelAndView(view);
    }

    private ModelAndView passwordView(String code, String error) {
        Map<String, Object> model = new HashMap<>(4);
        model.put("code", code);
        model.put("error", error);
        return new ModelAndView("password", model);
    }

    private ModelAndView errorView(int status, String title, String message) {
        Map<String, Object> model = new HashMap<>(4);
        model.put("status", status);
        model.put("title", title);
        model.put("message", message);
        ModelAndView view = new ModelAndView("error", model);
        view.setStatus(HttpStatus.valueOf(status));
        return view;
    }
}
