package org.proxy.server;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpResponse;

public class RequestStartBodyHandler implements HttpResponse.BodyHandler<Void> {
    private final ServletOutputStream out;
    private final AsyncContext async;
    private final HttpServletResponse response;

    public RequestStartBodyHandler(ServletOutputStream out, AsyncContext async, HttpServletResponse response) {
        this.out = out;
        this.async = async;
        this.response = response;
    }

    @Override
    public HttpResponse.BodySubscriber<Void> apply(HttpResponse.ResponseInfo responseInfo) {
        System.out.println("Started");
        response.setStatus(responseInfo.statusCode());
        responseInfo.headers().map().forEach((k, v) -> response.addHeader(k, v.get(0)));

        return HttpResponse.BodySubscribers.ofByteArrayConsumer(new BodyConsumer(out, async));
    }
}
