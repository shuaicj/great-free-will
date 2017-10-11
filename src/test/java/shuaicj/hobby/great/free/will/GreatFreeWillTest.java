package shuaicj.hobby.great.free.will;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test the project.
 *
 * @author shuaicj 2017/10/11
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GreatFreeWillTest {

    @Value("${client.daemon.port}")
    int port;

    @Test
    public void sock5() throws Exception {
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", port));
        HttpURLConnection conn = (HttpURLConnection) new URL("https://github.com").openConnection(proxy);
        assertThat(conn.getResponseCode()).isEqualTo(200);
    }
}

