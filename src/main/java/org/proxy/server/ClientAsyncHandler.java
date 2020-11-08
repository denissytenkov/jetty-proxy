package org.proxy.server;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.ByteBuffer;

public class ClientAsyncHandler implements AsyncHandler {

    private HttpServletResponse response;
    private AsyncContext async;
    private ServletOutputStream out;

    public ClientAsyncHandler(HttpServletResponse response, AsyncContext async, ServletOutputStream out) {
        this.response = response;
        this.async = async;
        this.out = out;
    }

    @Override
    public void onThrowable(Throwable t) {
        response.setStatus(500);
    }

    @Override
    public AsyncHandler.State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        ByteBuffer content = bodyPart.getBodyByteBuffer();
        while (out.isReady()) {
            if (!content.hasRemaining()) {
                return State.CONTINUE;
            }
            out.write(content.get());
        }
        return State.CONTINUE;

    }

    @Override
    public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
        response.setStatus(responseStatus.getStatusCode());
        return State.CONTINUE;
    }

    @Override
    public State onHeadersReceived(HttpHeaders headers) throws Exception {
        headers.forEach(header -> response.addHeader(header.getKey(), header.getValue()));
        return State.CONTINUE;
    }

    @Override
    public Object onCompleted() throws Exception {
        async.complete();
        return new Object();
    }
}
