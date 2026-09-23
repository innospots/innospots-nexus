package com.innospots.nexus.base.domain.data;

import java.util.List;

/**
 * 不可变的分页数据容器。在构造时校验分页边界，并提供分页导航便捷方法。
 *
 * @author Smars
 * @date 2026/09/13
 * @param records  分页记录列表
 * @param pageNo   页码（从 1 开始）
 * @param pageSize 每页记录数
 * @param total    全部记录总数
 * @param pages    总页数（自动计算）
 * @see PageResult
 */
public record DataPage<T>(
        List<T> records,
        long pageNo,
        long pageSize,
        long total,
        long pages
) {

    /**
     * 紧凑构造器校验分页参数，并确保记录列表永不为 null。
     */
    public DataPage {
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
     * @param records  分页记录（null 安全）
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页记录数
     * @param total    全部记录总数
     */
    public static <T> DataPage<T> of(List<T> records, long pageNo, long pageSize, long total) {
        return new DataPage<>(records, pageNo, pageSize, total, calculatePages(total, pageSize));
    }

    /**
     * 返回指定页码与大小的空分页结果。
     */
    public static <T> DataPage<T> empty(long pageNo, long pageSize) {
        return of(List.of(), pageNo, pageSize, 0);
    }

    public boolean hasNext() {
        return pageNo < pages;
    }

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
