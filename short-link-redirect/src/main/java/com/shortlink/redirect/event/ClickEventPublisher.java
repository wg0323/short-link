package com.shortlink.redirect.event;

/**
 * 埋点投递出口。一期用进程内有界队列，后续可替换为 MQ 实现而不改业务代码。
 */
public interface ClickEventPublisher {

    void publish(ClickEvent event);
}
