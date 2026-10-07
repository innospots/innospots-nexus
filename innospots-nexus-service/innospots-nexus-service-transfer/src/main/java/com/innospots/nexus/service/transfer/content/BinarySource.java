package com.innospots.nexus.service.transfer.content;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

/**
 * 只读二进制源。单订阅，消费方不得修改缓冲区内容。
 */
public interface BinarySource extends AutoCloseable {

    /**
     * 返回字节发布者。
     *
     * @return 发布者
     */
    Flow.Publisher<ByteBuffer> publisher();

    @Override
    void close();
}
