package shuaicj.hobby.great.free.will.http;

import java.net.Socket;

/**
 * The http proxy server task.
 *
 * @author shuaicj 2017/09/09
 */
public class HttpProxyServerTask implements Runnable {

    private final Socket socket;

    public HttpProxyServerTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
    }
}
