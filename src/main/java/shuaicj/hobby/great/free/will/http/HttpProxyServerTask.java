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
        Socket targetSocket = null;
        try {
            final InputStream clientInput = new BufferedInputStream(socket.getInputStream());
            HttpProxyClientHeader header = HttpProxyClientHeader.parseFrom(clientInput);
            targetSocket = new Socket(header.getHost(), header.getPort());

            OutputStream targetOutput = targetSocket.getOutputStream();
            logger.info(id + " {}\n{}", header, new String(header.getBytes()));

            if (header.isHttps()) { // if https, respond 200 to create tunnel, and do not forward header
                targetOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                targetOutput.flush();
            } else { // if http, forward header
                targetOutput.write(header.getBytes());
            }
            Future<?> future = pool.submit(() -> pipe(clientInput, targetOutput));

            InputStream targetInput = targetSocket.getInputStream();
            OutputStream clientOutput = socket.getOutputStream();
            pipe(targetInput, clientOutput);

            future.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(id + " shit happens", e);
        } finally {
            try {
                socket.close();
                if (targetSocket != null) {
                    targetSocket.close();
                }
            } catch (IOException e) {
                logger.error(id + " shit happens", e);
            }
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
