package shuaicj.hobby.great.free.will.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.ToString;

/**
 * The target host.
 *
 * @author shuaicj 2017/09/10
 */
@ToString(of = {"host", "port"})
public class HttpProxyTarget {

    private String host;
    private int port;

    private final InputStream in;
    private final ByteArrayOutputStream consumedBytes;

    private HttpProxyTarget(InputStream in) {
        this.in = in;
        this.consumedBytes = new ByteArrayOutputStream();
    }

    private void init() throws IOException {
        String firstLine = readLine();
        for (String line = readLine(); line != null && !line.isEmpty(); line = readLine()) {
            if (line.startsWith("Host: ")) {
                String[] arr = line.split(":");
                host = arr[1].trim();
                try {
                    if (arr.length == 3) {
                        port = Integer.parseInt(arr[2]);
                    } else if (firstLine.startsWith("CONNECT ")) {
                        port = 443; // https
                    } else {
                        port = 80; // http
                    }
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
                break;
            }
        }
        if (host == null || port == 0) {
            throw new IOException("cannot find header \'Host\'");
        }
    }

    private String readLine() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int b = in.read(); b != -1; b = in.read()) {
            consumedBytes.write(b);
            builder.append((char) b);
            int len = builder.length();
            if (len >= 2 && builder.substring(len - 2).equals("\r\n")) {
                builder.delete(len - 2, len);
                return builder.toString();
            }
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    public static HttpProxyTarget parseFrom(InputStream in) throws IOException {
        HttpProxyTarget target = new HttpProxyTarget(in);
        target.init();
        return target;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public byte[] getConsumedBytes() {
        return consumedBytes.toByteArray();
    }
}
