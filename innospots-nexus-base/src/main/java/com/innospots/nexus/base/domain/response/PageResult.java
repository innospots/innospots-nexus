package com.innospots.nexus.base.domain.response;

import java.util.List;

/**
 * 分页 API 响应包装器。在构造时校验分页边界，并根据总记录数与每页大小计算总页数。
 *
 * @author Smars
 * @date 2026/09/13
 * @param <T> 记录类型
 * @see com.innospots.nexus.base.domain.data.DataPage
 */
public record PageResult<T>(
        List<T> records,
        long pageNo,
        long pageSize,
        long total,
        long pages
) {

    /**
     * 紧凑构造器校验分页参数并确保记录列表永不为 null。
     */
    public PageResult {
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        records = records == null ? List.of() : List.copyOf(records);
    }

    /**
     * 创建自动计算总页数的分页结果。
     *
     * @param records  记录列表
     * @param pageNo   从 1 开始的页码
     * @param pageSize 每页记录数
     * @param total    总记录数
     * @param <T>      记录类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long pageNo, long pageSize, long total) {
        return new PageResult<>(records, pageNo, pageSize, total, calculatePages(total, pageSize));
    }

    /**
     * 返回指定页码与大小的空分页结果。
     *
     * @param pageNo   页码
     * @param pageSize 每页记录数
     * @param <T>      记录类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return of(List.of(), pageNo, pageSize, 0);
    }

    /**
     * 判断是否有下一页。
     *
     * @return 有下一页时返回 {@code true}
     */
    public boolean hasNext() {
        return pageNo < pages;
    }

    /**
     * 判断是否有上一页。
     *
     * @return 有上一页时返回 {@code true}
     */
    public boolean hasPrevious() {
        return pageNo > 1 && pages > 0;
    }

    /**
     * 使用向上取整除法计算总页数。
     */
    private static long calculatePages(long total, long pageSize) {
        if (total <= 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }
}
