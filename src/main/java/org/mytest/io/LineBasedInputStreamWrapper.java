package org.mytest.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * InputStream包装器
 * <p>用于按行读取</p>
 */
public class LineBasedInputStreamWrapper extends AbstractInputStream {
    /**
     * 流End-of-File标记
     */
    private boolean eof = false;
    private final long maxDataLength;
    private long readDataLength = 0;
    private final InputStream internalInputStream;

    public LineBasedInputStreamWrapper(InputStream internalInputStream) {
        this(internalInputStream, -1);
    }

    public LineBasedInputStreamWrapper(InputStream internalInputStream, long maxDataLength) {
        this.internalInputStream = internalInputStream;
        this.maxDataLength = maxDataLength;
        if (0 == maxDataLength) {
            this.setEOF(true);
        }
    }

    private void addReadDataLength(long length) {
        if (-1 == length) {
            this.setEOF(true);
            return;
        }
        this.readDataLength += length;
        if (this.maxDataLength != -1 && this.readDataLength >= this.maxDataLength) {
            this.setEOF(true);
        }
    }

    public void setEOF(boolean eof) {
        this.eof = eof;
    }

    /**
     * 按行读取
     *
     * @return 单行字节数组
     * @throws IOException ..
     */
    public byte[] readLine() throws IOException {
        List<Byte> lineBytes = new LinkedList<>();
        boolean lastByteIsLF = false;

        while (true) {
            byte[] oneBytes = new byte[1];
            int readNumber = internalInputStream.read(oneBytes);
            addReadDataLength(oneBytes.length);

            if (0 == readNumber) {
                continue;
            }

            if (-1 == readNumber) {
                this.setEOF(true);
            }

            if (this.eof || oneBytes[0] == '\n') {
                int lineBytesSize = lineBytes.size();
                if (lastByteIsLF) {
                    lineBytesSize -= 1;
                }
                byte[] toByteArray = new byte[lineBytesSize];
                int i = 0;
                for (Byte eachByte : lineBytes) {
                    if (i > toByteArray.length - 1) {
                        break;
                    }
                    toByteArray[i++] = eachByte;
                }
                return toByteArray;
            }

            lineBytes.add(oneBytes[0]);
            lastByteIsLF = oneBytes[0] == '\r';
        }
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (this.eof) {
            return -1;
        }
        int readLength = this.internalInputStream.read(bytes, off, len);
        this.addReadDataLength(readLength);
        return readLength;
    }
}
