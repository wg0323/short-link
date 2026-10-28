package com.shortlink.redirect.bloom;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.shortlink.redirect.entity.RedirectLink;
import com.shortlink.redirect.mapper.RedirectLinkMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 布隆过滤器：挡住「压根不存在」的短码，避免恶意扫链把请求打到 Redis 和 MySQL。
 *
 * <p>启动后异步加载全量短码，加载完成前不过滤（返回 true 放行）；
 * 加载完成后误判只可能导致一次多余的缓存查询，不会误杀真实链接。</p>
 */
@Component
public class BloomFilterService {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterService.class);

    private static final int BATCH_SIZE = 5000;

    private final RedirectLinkMapper linkMapper;

    private final long capacity;

    private final double fpp;

    private volatile BloomFilter<String> filter;

    private volatile boolean ready;

    /** 加载完成前收到的增量短码，加载完一并放入。 */
    private final Set<String> pending = ConcurrentHashMap.newKeySet();

    public BloomFilterService(RedirectLinkMapper linkMapper,
                              @Value("${shortlink.redirect.bloom-capacity:10000000}") long capacity,
                              @Value("${shortlink.redirect.bloom-fpp:0.0001}") double fpp) {
        this.linkMapper = linkMapper;
        this.capacity = capacity;
        this.fpp = fpp;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Thread loader = new Thread(this::load, "bloom-loader");
        loader.setDaemon(true);
        loader.start();
    }

    /**
     * false 表示一定不存在；true 表示可能存在（或尚未加载完成）。
     */
    public boolean mightContain(String code) {
        BloomFilter<String> current = filter;
        return current == null || current.mightContain(code);
    }

    public boolean isReady() {
        return ready;
    }

    /**
     * 新短链上线时由广播触发，避免被布隆过滤器误判为不存在。
     */
    public synchronized void put(String code) {
        if (filter != null) {
            filter.put(code);
        } else {
            pending.add(code);
        }
    }

    private void load() {
        long start = System.currentTimeMillis();
        BloomFilter<String> bloom = BloomFilter.create(
                Funnels.stringFunnel(Charsets.UTF_8), capacity, fpp);
        long lastId = 0;
        long total = 0;
        try {
            while (true) {
                List<RedirectLink> batch = linkMapper.selectCodesAfter(lastId, BATCH_SIZE);
                if (batch.isEmpty()) {
                    break;
                }
                for (RedirectLink link : batch) {
                    if (link.getCode() != null) {
                        bloom.put(link.getCode());
                    }
                    lastId = link.getId();
                }
                total += batch.size();
                if (batch.size() < BATCH_SIZE) {
                    break;
                }
            }
            synchronized (this) {
                for (String code : pending) {
                    bloom.put(code);
                }
                pending.clear();
                filter = bloom;
                ready = true;
            }
            log.info("布隆过滤器加载完成 codes={} cost={}ms", total, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("布隆过滤器加载失败，将退化为每次都查缓存", e);
        }
    }
}
