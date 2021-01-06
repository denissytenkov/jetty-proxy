package org.proxy.server;

import org.proxy.Main;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NonBlockingServlet extends HttpServlet {

    private ScheduledExecutorService executorService;

    public NonBlockingServlet() {

    }

    @Override
    public void init() {
        executorService = Executors.newScheduledThreadPool(1);
    }



    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AsyncContext async = request.startAsync();
        ServletOutputStream out = response.getOutputStream();
        executorService.schedule(() -> {
            System.out.println(Main.threadPool.toString());
            async.complete();
        }, 5, TimeUnit.SECONDS);
    }
}
