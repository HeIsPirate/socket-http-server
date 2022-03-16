import org.apache.commons.io.IOUtils;
import org.mytest.http.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SocketServerMain {
    public static void main(String[] args) throws IOException {
        System.out.println("线程是: " + ProcessHandle.current().pid());

        new HttpServer(888, exchange -> {
            String body = IOUtils.toString(exchange.getInputStream(), StandardCharsets.UTF_8);
            System.out.println("body: " + body);

            exchange.setResponseCode(200)
                    .addResponseHeader("Content-Type", "text/plain")
                    // 手动指定长度, 因为先输出响应头, 再输出响应体
                    // 而因为响应体可以多次输出, 导致响应体的长度无法通过代码计算出
                    .addResponseHeader("Content-Length", "3")
                    .getOutputStream().write("bye".getBytes());
        }).start();
    }
}
