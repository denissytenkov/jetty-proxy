package org.proxy.server;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class BodyConsumer implements Consumer<Optional<byte[]>> {
    private final ServletOutputStream out;
    private final AsyncContext async;

    public BodyConsumer(ServletOutputStream out, AsyncContext async) {
        this.out = out;
        this.async = async;
    }

    @Override
    public void accept(Optional<byte[]> data) {
        if (data.isEmpty()) {
            async.complete();
            System.out.println("Data complete");
        }
        data.ifPresent(bytes -> {
            try {
                System.out.println("Data");
                out.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
