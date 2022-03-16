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
        // noinspection InfiniteLoopStatement
        while (true) {
            // BIO是阻塞式的
            // ServerSocket.accept()返回的socket, 对应一个客户端TCP连接
            Socket childSocket = serverSocket.accept();
            // 使用简单线程, 异步处理请求
            new MyThread(() -> {
                try {
                    // 当keep-alive时:
                    // _____________________________________________
                    // |Client                               Server|
                    // |      --------TCP Connection-------->      |
                    // |           ----request  1--->              |
                    // |           <---response 1----              |
                    // |           ----request  2--->              |
                    // |           <---response 3----              |
                    // |               ~~~more~~~                  |
                    // |      --------TCP Termination-------->     |
                    // _____________________________________________
                    //
                    // 因此需要继续执行: 读取&处理&响应socket
                    while (childSocket.isConnected()) {
                        // childSocket.setSoTimeout(300);
                        // 读取socket
                        HttpExchange httpExchange = HttpExchange.createHttpExchange(childSocket);
                        // 当TCP没有payload时, httpExchange = null, 这应该是客户端关闭连接发送FIN包导致的,
                        // 因此这里直接close socket, 发送响应FIN包
                        // (keep-alive时, 服务端不主动关闭连接, 由客户端(如浏览器)决定关闭连接)
                        // (调用close方法, 实质就是发送FIN包)
                        if (null == httpExchange) {
                            System.out.println("关闭ChildSocket: " + childSocket.getPort());
                            childSocket.close();
                            break;
                        }
                        // 处理请求
                        this.httpHandler.handle(httpExchange);
                        // 读取请求头的keep-alive, 判断是否不关闭socket
                        List<String> connectionHeaders = httpExchange.getRequestHeaders().get("connection");
                        if (CollectionUtils.isEmpty(connectionHeaders) || !"keep-alive".equals(connectionHeaders.get(0))) {
                            childSocket.close(); // 服务端主动关闭socket
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
