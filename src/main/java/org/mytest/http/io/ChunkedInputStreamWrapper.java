package org.mytest.http.io;

import org.mytest.io.LineBasedInputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Chunked协议请求体
 * <p>继承{@link LineBasedInputStreamWrapper}是因为, chunked也需要按行读取</p>
 * <p>未完...</p>
 */
public class ChunkedInputStreamWrapper extends LineBasedInputStreamWrapper {
    private Long unReadChunkedLength;

    public ChunkedInputStreamWrapper(InputStream internalInputStream) {
        super(internalInputStream);
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (null == unReadChunkedLength) {
            unReadChunkedLength = getNextChunkedLength();
            if (0L == unReadChunkedLength) {
                this.setEOF(true);
                return -1;
            }
        }
        // len <= unread
        // 说明读取未结束
        if (unReadChunkedLength.compareTo((long) len) >= 0) {
            int readLength = super.read(bytes, off, len);
            this.unReadChunkedLength += len;
            return readLength;
        }

        // len > unread
        int read = super.read(bytes, off, Math.toIntExact(unReadChunkedLength));

        int remainingUnReadLength = (int) (len - unReadChunkedLength);
        int nextOff = (int) (off + unReadChunkedLength);
        unReadChunkedLength = null;

        // recursively read remaining bytes
        int readLeft = this.read(bytes, nextOff, remainingUnReadLength);
        return read + readLeft;
    }

    private long getNextChunkedLength() throws IOException {
        byte[] chunkedLengthBytes = this.readLine();
        String chunkedLengthString = new String(chunkedLengthBytes);
        return Long.parseLong(chunkedLengthString.trim());
    }
}
