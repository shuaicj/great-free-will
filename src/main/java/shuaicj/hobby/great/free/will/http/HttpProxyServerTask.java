package shuaicj.hobby.great.free.will.http;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

/**
 * The http proxy server task.
 *
 * @author shuaicj 2017/09/09
 */
@Slf4j
public class HttpProxyServerTask implements Runnable {

    // static final String ERROR = "HTTP/1.1 500 Connection FAILED\r\n\r\n";

    private final String id;
    private final Socket socket;

    public HttpProxyServerTask(String id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    @Override
    public void run() {
        Socket targetSocket = null;
        try {
            final InputStream clientInput = new BufferedInputStream(socket.getInputStream());
            HttpProxyTarget target = HttpProxyTarget.parseFrom(clientInput);
            targetSocket = new Socket(target.getHost(), target.getPort());

            OutputStream targetOutput = targetSocket.getOutputStream();
            byte[] consumed = target.getConsumedBytes();
            logger.info(id + " {}\n{}", target, new String(consumed));
            targetOutput.write(consumed);


            // byte[] buf = new byte[4096];
            // int len;
            // while ((len = clientInput.read(buf)) != -1) {
            //     targetOutput.write(buf, 0, len);
            // }

            new Thread(() -> pipe(clientInput, targetOutput)).start();

            InputStream targetInput = targetSocket.getInputStream();
            OutputStream clientOutput = socket.getOutputStream();
            pipe(targetInput, clientOutput);
        } catch (IOException e) {
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
