package org.mytest.http;

import org.apache.commons.collections4.CollectionUtils;
import org.mytest.util.MyThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * HttpServer
 */
@SuppressWarnings("ClassCanBeRecord")
public class HttpServer {
    private final int port;
    private final HttpHandler httpHandler;

    /**
     * 初始化
     *
     * @param port        端口
     * @param httpHandler 请求处理器
     */
    public HttpServer(int port, HttpHandler httpHandler) {
        this.port = port;
        this.httpHandler = httpHandler;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(this.port));
        // 循环接收请求
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket childSocket = serverSocket.accept();
            // 使用简单线程异步处理
            new MyThread(() -> {
                try {
                    // keep-alive, 继续执行
                    while (childSocket.isConnected()) {
                        // childSocket.setSoTimeout(300);
                        HttpExchange httpExchange = HttpExchange.createHttpExchange(childSocket);
                        // 当TCP没有payload时, httpExchange = null, 这是客户端关闭连接发送FIN包导致的,
                        // 因此这里直接close socket, 发送响应FIN包
                        if (null == httpExchange) {
                            System.out.println("关闭ChildSocket: " + childSocket.getPort());
                            childSocket.close();
                            break;
                        }
                        // 处理请求
                        this.httpHandler.handle(httpExchange);
                        // keep-alive处理: 不关闭socket
                        List<String> connectionHeaders = httpExchange.getRequestHeaders().get("connection");
                        if (CollectionUtils.isEmpty(connectionHeaders)
                                || !"keep-alive".equals(connectionHeaders.get(0))) {
                            childSocket.close();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
