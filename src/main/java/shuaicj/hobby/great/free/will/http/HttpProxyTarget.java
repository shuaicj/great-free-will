package shuaicj.hobby.great.free.will.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The target host.
 *
 * @author shuaicj 2017/09/10
 */
public class HttpProxyTarget {

    private String host;
    private int port;

    private final StringBuilder consumedLines;

    private HttpProxyTarget() {
        this.consumedLines = new StringBuilder();
    }

    public static HttpProxyTarget parse(InputStream in) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        HttpProxyTarget target = new HttpProxyTarget();

        String firstLine = reader.readLine();
        target.consumedLines.append(firstLine).append("\r\n");

        for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
            target.consumedLines.append(line).append("\r\n");
            if (line.startsWith("Host: ")) {
                String[] arr = line.split(":");
                target.host = arr[1].trim();
                try {
                    if (arr.length == 3) {
                        target.port = Integer.parseInt(arr[2]);
                    } else if (firstLine.startsWith("CONNECT ")) {
                        target.port = 443; // https
                    } else {
                        target.port = 80; // http
                    }
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
                break;
            }
        }

        if (target.host == null || target.port == 0) {
            throw new IOException("cannot find header \'Host\'");
        }

        return target;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getConsumedLines() {
        return consumedLines.toString();
    }
}
