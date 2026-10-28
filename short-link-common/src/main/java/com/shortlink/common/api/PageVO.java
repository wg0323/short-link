package com.shortlink.common.api;

import java.util.List;

/**
 * 分页响应的统一结构。
 */
public class PageVO<T> {

    private final long total;

    private final long page;

    private final long size;

    private final List<T> records;

    public PageVO(long total, long page, long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public long getPage() {
        return page;
    }

    public long getSize() {
        return size;
    }

    public List<T> getRecords() {
        return records;
    }
}
