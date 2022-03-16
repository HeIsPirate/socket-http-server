package org.mytest.http;

import org.apache.commons.collections4.CollectionUtils;
import org.mytest.http.io.ChunkedInputStreamWrapper;
import org.mytest.http.io.HttpOutputStreamWrapper;
import org.mytest.io.LineBasedInputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.util.*;

/**
 * 请求上下文
 */
@SuppressWarnings("DanglingJavadoc")
public class HttpExchange {
    private String method;
    private URI uri;
    private Map<String, List<String>> requestHeaders = new HashMap<>();
    private final InputStream bodyInputStream;

    private int responseCode;
    private final Map<String, List<String>> responseHeaders = new HashMap<>();
    private final HttpOutputStreamWrapper outputStream;

    public HttpExchange(InputStream bodyInputStream, HttpOutputStreamWrapper outputStream) {
        this.bodyInputStream = bodyInputStream;
        this.outputStream = outputStream;
    }

    /**
     * 读取Socket, 生成{@link HttpExchange}
     *
     * @param socket 请求socket
     * @return 请求上下文
     * @throws IOException ..
     */
    public static HttpExchange createHttpExchange(Socket socket) throws IOException {
        LineBasedInputStreamWrapper httpInputStream = new LineBasedInputStreamWrapper(socket.getInputStream());
        Map<String, List<String>> requestHeaders = new HashMap<>();
        Optional<String> requestLine = Optional.empty();

        /**
         * 循环读取请求头行
         */
        while (true) {
            byte[] lineBytes = httpInputStream.readLine();
            String headerLineString = new String(lineBytes);
            // 请求头读取结束
            if (lineBytes.length == 0 || headerLineString.length() == 0) {
                break;
            }
            if (requestLine.isEmpty()) {
                requestLine = Optional.of(headerLineString);
                continue;
            }
            if (headerLineString.contains(":")) {
                int separatorIndex = headerLineString.indexOf(":");
                String key = headerLineString.substring(0, separatorIndex);
                String value = headerLineString.substring(separatorIndex + 1);
                requestHeaders.put(key.trim().toLowerCase(), Arrays.asList(value.trim().split(",")));
            }
        }

        // TCP没有payload
        if (requestLine.isEmpty()) {
            return null;
        }
        String[] requestLineArray = requestLine.get().split(" ");
        String method = requestLineArray[0];
        String uri = requestLineArray[1];

        List<String> contentLengths = requestHeaders.get("content-length");
        List<String> transferCoding = requestHeaders.get("transfer-coding");
        boolean bodyIsChunked = CollectionUtils.isNotEmpty(contentLengths)
                && "chunked".equals(transferCoding.get(0).trim());
        // 请求体InputStream
        InputStream bodyInputStream = bodyIsChunked
                ? new ChunkedInputStreamWrapper(httpInputStream)
                : new LineBasedInputStreamWrapper(httpInputStream,
                CollectionUtils.isEmpty(contentLengths) ? 0 : Long.parseLong(contentLengths.get(0)));

        HttpOutputStreamWrapper httpOutputStream = new HttpOutputStreamWrapper(socket.getOutputStream());

        HttpExchange httpExchange = new HttpExchange(bodyInputStream, httpOutputStream)
                .setRequestHeaders(requestHeaders)
                .setMethod(method)
                .setUri(URI.create(uri));

        httpOutputStream.setHttpExchange(httpExchange);

        return httpExchange;
    }

    @SuppressWarnings("unused")
    public String getMethod() {
        return method;
    }

    public HttpExchange setMethod(String method) {
        this.method = method;
        return this;
    }

    @SuppressWarnings("unused")
    public URI getUri() {
        return uri;
    }

    public HttpExchange setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public HttpExchange setRequestHeaders(Map<String, List<String>> requestHeaders) {
        this.requestHeaders = requestHeaders;
        return this;
    }

    public InputStream getInputStream() {
        return bodyInputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public HttpExchange setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public HttpExchange addResponseHeader(String key, String value) {
        this.getResponseHeaders().put(key, Collections.singletonList(value));
        return this;
    }
}
