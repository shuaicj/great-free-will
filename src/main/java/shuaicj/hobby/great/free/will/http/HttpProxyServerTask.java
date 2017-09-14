package shuaicj.hobby.great.free.will.http;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The http proxy server task.
 *
 * @author shuaicj 2017/09/09
 */
@Slf4j
public class HttpProxyServerTask implements Runnable {

    private final String id;
    private final Socket socket;
    private final ExecutorService pool;

    public HttpProxyServerTask(String id, Socket socket, ExecutorService pool) {
        this.id = id;
        this.socket = socket;
        this.pool = pool;
    }

    @Override
    public void run() {
        try (final InputStream clientInput = new BufferedInputStream(socket.getInputStream());
             final OutputStream clientOutput = socket.getOutputStream()) {

            HttpProxyClientHeader header = HttpProxyClientHeader.parseFrom(clientInput);

            try (final Socket targetSocket = new Socket(header.getHost(), header.getPort())) {

                final OutputStream targetOutput = targetSocket.getOutputStream();
                final InputStream targetInput = targetSocket.getInputStream();

                logger.info(id + " {}", header);

                if (header.isHttps()) { // if https, respond 200 to create tunnel, and do not forward header
                    clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                    clientOutput.flush();
                } else { // if http, forward header
                    targetOutput.write(header.getBytes());
                }

                Future<?> future = pool.submit(() -> pipe(clientInput, targetOutput));
                pipe(targetInput, clientOutput);
                future.get();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(id + " shit happens", e);
        }
    }

    private void pipe(InputStream in, OutputStream out) {
        byte[] buf = new byte[4096];
        int len;
        try {
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            logger.error(id + " shit happens", e);
        }
    }
}
