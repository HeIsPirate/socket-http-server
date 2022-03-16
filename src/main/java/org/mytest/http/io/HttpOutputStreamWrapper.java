package org.mytest.http.io;

import org.mytest.http.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求响应流 包装器
 */
public class HttpOutputStreamWrapper extends OutputStream {
    private final OutputStream internalOutputStream;
    private HttpExchange httpExchange;
    private boolean writeHeadersFlag;

    public HttpOutputStreamWrapper(OutputStream internalOutputStream) {
        this.internalOutputStream = internalOutputStream;
    }

    @Override
    public void write(int b) throws IOException {
        this.write(new byte[]{(byte) b});
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        // 先输出响应头
        if (!writeHeadersFlag) {
            String responseLine = "HTTP/1.1 " + this.httpExchange.getResponseCode() + " OK\r\n";
            this.internalOutputStream.write(responseLine.getBytes());
            for (Map.Entry<String, List<String>> entry : this.httpExchange.getResponseHeaders().entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                String headerLineString = key + ":" + String.join(",", value);
                this.internalOutputStream.write((headerLineString + "\r\n").getBytes());
            }
            this.internalOutputStream.write(("\r\n").getBytes());
            writeHeadersFlag = true;
        }
        // 响应体
        this.internalOutputStream.write(bytes, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.internalOutputStream.flush();
    }

    @SuppressWarnings("UnusedReturnValue")
    public HttpOutputStreamWrapper setHttpExchange(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
        return this;
    }
}
