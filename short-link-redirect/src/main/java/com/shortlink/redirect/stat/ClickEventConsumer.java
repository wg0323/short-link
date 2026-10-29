package com.shortlink.redirect.stat;

import com.shortlink.redirect.event.ClickEvent;
import com.shortlink.redirect.event.LocalQueueClickPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 埋点消费线程：从有界队列批量取数，凑够一批或等待 1 秒就落一次，
 * 把「每次点击一次写库」压缩成「一批一次写库」。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClickEventConsumer implements DisposableBean {

    private static final int MAX_BATCH = 500;

    private final LocalQueueClickPublisher publisher;

    private final ClickStatWriter writer;

    private volatile boolean running = true;

    private Thread worker;

    @PostConstruct
    public void start() {
        worker = new Thread(this::loop, "click-consumer");
        worker.setDaemon(true);
        worker.start();
        log.info("埋点消费线程已启动");
    }

    private void loop() {
        while (running) {
            try {
                ClickEvent first = publisher.poll(1, TimeUnit.SECONDS);
                if (first == null) {
                    continue;
                }
                List<ClickEvent> batch = new ArrayList<>(MAX_BATCH);
                batch.add(first);
                publisher.drainTo(batch, MAX_BATCH - 1);
                writer.write(batch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("埋点消费异常", e);
            }
        }
    }

    @Override
    public void destroy() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }
}
