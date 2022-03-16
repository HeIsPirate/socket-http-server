package org.mytest.io;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractInputStream extends InputStream {
    @Override
    public int read() throws IOException {
        byte[] oneBytes = new byte[1];
        return this.read(oneBytes);
    }

    @Override
    public abstract int read(byte[] bytes, int off, int len) throws IOException;
}
