package org.mytest.http;

import java.io.IOException;

/**
 * 请求处理器
 */
public interface HttpHandler {
    /**
     * 请求处理方法
     *
     * @param httpExchange 请求上下文
     * @throws IOException ..
     */
    void handle(HttpExchange httpExchange) throws IOException;
}
