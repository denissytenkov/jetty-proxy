package org.proxy.server;

import javax.net.ssl.*;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;

public class NonBlockingServlet extends HttpServlet {
    private HttpClient asyncHttpClient;

    public NonBlockingServlet() throws SSLException {

    }

    @Override
    public void init() throws ServletException {
        try {
            asyncHttpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .executor(Executors.newFixedThreadPool(2))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .proxy(ProxySelector.getDefault())
                    .sslContext(SSLContext.getDefault())
                    .version(HttpClient.Version.HTTP_2)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new ServletException(e);
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
        ServletInputStream input = request.getInputStream();
        HttpRequest externalRequets = HttpRequest.newBuilder(URI.create("https://" + destinationHost + request.getRequestURI() + "?" + request.getQueryString()))
                .method(request.getMethod(), HttpRequest.BodyPublishers.ofInputStream(() -> input))
                .build();
        asyncHttpClient.sendAsync(externalRequets, HttpResponse.BodyHandlers.ofByteArray())
        .handle((clientResponse, ex) -> {
            if (ex != null) {
                ex.printStackTrace();
                return null;
            }
            response.setStatus(clientResponse.statusCode());
            clientResponse.headers().map().forEach((k, v) -> response.addHeader(k, v.get(0)));
            try {
                out.write(clientResponse.body());
            } catch (IOException e) {
                e.printStackTrace();
            }
            async.complete();
            return clientResponse;
        });
    }
}
