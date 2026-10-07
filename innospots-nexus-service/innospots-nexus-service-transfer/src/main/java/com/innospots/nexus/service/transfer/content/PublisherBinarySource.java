package com.innospots.nexus.service.transfer.content;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

import com.innospots.nexus.base.util.Checks;

/**
 * 基于 {@link Flow.Publisher} 的二进制源实现。
 */
public final class PublisherBinarySource implements BinarySource {

    private final Flow.Publisher<ByteBuffer> publisher;
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * 创建发布者二进制源。
     *
     * @param publisher 字节发布者
     */
    public PublisherBinarySource(Flow.Publisher<ByteBuffer> publisher) {
        this.publisher = Checks.notNull(publisher, "publisher");
    }

    @Override
    public Flow.Publisher<ByteBuffer> publisher() {
        if (closed.get()) {
            throw new IllegalStateException("binary source is closed");
        }
        return publisher;
    }

    @Override
    public void close() {
        closed.set(true);
    }

    /**
     * 从内存字节创建单订阅源。
     *
     * @param bytes 内容
     * @return 二进制源
     */
    public static PublisherBinarySource ofBytes(byte[] bytes) {
        Checks.notNull(bytes, "bytes");
        Flow.Publisher<ByteBuffer> publisher = subscriber -> subscriber.onSubscribe(new Flow.Subscription() {
            private boolean done;

            @Override
            public void request(long n) {
                if (done) {
                    return;
                }
                done = true;
                subscriber.onNext(ByteBuffer.wrap(bytes));
                subscriber.onComplete();
            }

            @Override
            public void cancel() {
                done = true;
            }
        });
        return new PublisherBinarySource(publisher);
    }
}
