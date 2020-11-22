package org.proxy.server;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.asynchttpclient.*;

import javax.net.ssl.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class NonBlockingServlet extends HttpServlet {
    private AsyncHttpClient asyncHttpClient;

    public NonBlockingServlet() throws SSLException {

    }

    @Override
    public void init() throws ServletException {
        try {
            AsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
                    .setSslContext(SslContextBuilder
                            .forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .clientAuth(ClientAuth.NONE)
                            .sslProvider(SslProvider.JDK).build())
                    .setMaxRequestRetry(1)
                    .setMaxConnections(500)
                    .setMaxConnectionsPerHost(200)
                    .build();
            asyncHttpClient = asyncHttpClient(config);
        } catch (SSLException e) {
            throw new SecurityException(e);
        }

    }



    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String destinationHost = request.getHeader("x-destination");

        AsyncContext async = request.startAsync();
        ServletOutputStream out = response.getOutputStream();
        ServletInputStream in = request.getInputStream();
        AtomicBoolean sync = new AtomicBoolean();
        Request externalRequets = Dsl.request(request.getMethod(), "https://" + destinationHost + request.getRequestURI() + "?" + request.getQueryString())
                .setBody(in)
                .build();
        ClientAsyncHandler clientAsyncHandler = new ClientAsyncHandler(response, async, out);
        in.setReadListener(new ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                sendSyncronized(sync, externalRequets, clientAsyncHandler);
            }

            @Override
            public void onAllDataRead() throws IOException { }

            @Override
            public void onError(Throwable t) {
                response.setStatus(500);
                async.complete();
            }
        });
        if (in.isReady()) {
            sendSyncronized(sync, externalRequets, clientAsyncHandler);
        }
    }

    private void sendSyncronized(AtomicBoolean isRequestSend, Request externalRequets, ClientAsyncHandler clientAsyncHandler) {
        if (!isRequestSend.compareAndExchange(false, true)) {
            asyncHttpClient.executeRequest(externalRequets, clientAsyncHandler).addListener(() -> {}, null);
        }
    }

}
