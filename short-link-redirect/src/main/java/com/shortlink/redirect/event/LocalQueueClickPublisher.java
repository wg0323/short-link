package com.shortlink.redirect.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 进程内有界队列投递。
 *
 * <p>跳转主链路只做一次入队，不等待落库；队列满时丢弃并计数，
 * 宁可少量统计数据丢失，也不能把 302 拖慢或阻塞。</p>
 */
@Component
public class LocalQueueClickPublisher implements ClickEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalQueueClickPublisher.class);

    private static final int CAPACITY = 20000;

    private final BlockingQueue<ClickEvent> queue = new ArrayBlockingQueue<>(CAPACITY);

    private final AtomicLong dropped = new AtomicLong();

    @Override
    public void publish(ClickEvent event) {
        if (!queue.offer(event)) {
            long total = dropped.incrementAndGet();
            if (total % 1000 == 1) {
                log.warn("埋点队列已满，累计丢弃 {} 条", total);
            }
        }
    }

    public ClickEvent take() throws InterruptedException {
        return queue.take();
    }

    public ClickEvent poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int drainTo(Collection<ClickEvent> target, int maxElements) {
        return queue.drainTo(target, maxElements);
    }

    public int size() {
        return queue.size();
    }

    public long getDropped() {
        return dropped.get();
    }
}
