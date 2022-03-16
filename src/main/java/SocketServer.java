import org.apache.commons.io.IOUtils;
import org.mytest.http.HttpServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ClassCanBeRecord")
public class SocketServer {
    public static void main(String[] args) throws IOException {
        System.out.println("线程是: " + ProcessHandle.current().pid());

        new HttpServer(888, exchange -> {
            String body = IOUtils.toString(exchange.getInputStream(), StandardCharsets.UTF_8);
            System.out.println("body: " + body);

            exchange.setResponseCode(200)
                    .addResponseHeader("Content-Type", "text/plain")
                    .addResponseHeader("Content-Length", "3")
                    .getOutputStream().write("bye".getBytes());
        }).start();
    }
}
