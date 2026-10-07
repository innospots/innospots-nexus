package com.innospots.nexus.service.transfer.content;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

import com.innospots.nexus.base.util.Checks;

/**
 * 内存二进制源辅助。仅用于请求内流式传输，不涉及持久化存储。
 */
public final class BinarySources {

    private BinarySources() {
    }

    /**
     * 将 {@link BinarySource} 全量物化为字节数组。
     *
     * @param source 二进制源
     * @return 字节数组
     */
    public static byte[] readAll(BinarySource source) {
        Checks.notNull(source, "source");
        List<byte[]> chunks = new ArrayList<>();
        int total = 0;
        CompletableFuture<Void> completed = new CompletableFuture<>();
        source.publisher().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                byte[] copy = new byte[item.remaining()];
                item.get(copy);
                chunks.add(copy);
            }

            @Override
            public void onError(Throwable throwable) {
                completed.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                completed.complete(null);
            }
        });
        completed.join();
        for (byte[] chunk : chunks) {
            total += chunk.length;
        }
        byte[] merged = new byte[total];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, merged, offset, chunk.length);
            offset += chunk.length;
        }
        return merged;
    }

    /**
     * 截取范围视图。
     *
     * @param source 原始源
     * @param range  读取范围
     * @return 范围视图源
     */
    public static BinarySource viewRange(BinarySource source, ByteRange range) {
        Checks.notNull(source, "source");
        Checks.notNull(range, "range");
        byte[] all = readAll(source);
        source.close();
        int start = Math.toIntExact(range.startInclusive());
        int length = Math.toIntExact(range.length());
        byte[] slice = new byte[length];
        System.arraycopy(all, start, slice, 0, length);
        return PublisherBinarySource.ofBytes(slice);
    }

    /**
     * 写入输出流并在结束时关闭源。
     *
     * @param source 二进制源
     * @param output 输出流
     */
    public static void writeTo(BinarySource source, OutputStream output) {
        Checks.notNull(source, "source");
        Checks.notNull(output, "output");
        try {
            drain(source, buffer -> {
                try {
                    if (buffer.hasArray()) {
                        output.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
                    } else {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        output.write(bytes);
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        } finally {
            source.close();
        }
    }

    /**
     * 消费全部块。
     *
     * @param source   二进制源
     * @param consumer 块消费者
     */
    public static void drain(BinarySource source, Consumer<ByteBuffer> consumer) {
        Checks.notNull(source, "source");
        Checks.notNull(consumer, "consumer");
        CompletableFuture<Void> completed = new CompletableFuture<>();
        source.publisher().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer item) {
                consumer.accept(item.asReadOnlyBuffer());
            }

            @Override
            public void onError(Throwable throwable) {
                completed.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                completed.complete(null);
            }
        });
        completed.join();
    }

    /**
     * 转为 {@link Flow.Publisher}，供响应式 adapter 使用。
     *
     * @param source 二进制源
     * @return 发布者
     */
    public static Flow.Publisher<ByteBuffer> toPublisher(BinarySource source) {
        Checks.notNull(source, "source");
        SubmissionPublisher<ByteBuffer> publisher = new SubmissionPublisher<>();
        CompletableFuture.runAsync(() -> {
            try {
                drain(source, publisher::submit);
                publisher.close();
            } catch (RuntimeException ex) {
                publisher.closeExceptionally(ex);
            } finally {
                source.close();
            }
        });
        return publisher;
    }
}
