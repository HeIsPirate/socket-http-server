package org.mytest.http;

import java.io.IOException;

public interface HttpHandler {
    void handle(HttpExchange httpExchange) throws IOException;
}
