package shuaicj.hobby.great.free.will.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple http proxy server.
 *
 * @author shuaicj 2017/09/09
 */
@Component
@Slf4j
public class HttpProxyServer {

    private final int port;
    private ExecutorService pool;
    private ServerSocket serverSocket;
    private boolean started;

    public HttpProxyServer(@Value("${proxy.port}") int port) {
        this.port = port;
    }

    public synchronized void start() throws IOException {
        if (!started) {
            pool = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(port);
            logger.info("HttpProxyServer started on port: {}", port);
            pool.submit(() -> {
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        socket.setKeepAlive(true);
                        pool.submit(new HttpProxyServerTask(socket));
                    } catch (IOException e) {
                        logger.error("Unhandled exception occurs!", e);
                    }
                }
            });
            started = true;
        }
    }

    public synchronized void stop() throws IOException {
        if (started) {
            pool.shutdownNow();
            serverSocket.close();
            started = false;
        }
    }
}
