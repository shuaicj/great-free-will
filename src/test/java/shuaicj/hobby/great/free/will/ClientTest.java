package shuaicj.hobby.great.free.will;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Mock a http client and test.
 *
 * @author shuaicj 2017/09/09
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ClientTest {

    @Value("${server.port}")
    int port;

    @Test
    public void request() throws Exception {
        logger.info(Request.Get("https://github.com")
                .viaProxy(new HttpHost("127.0.0.1", port))
                .execute()
                .returnContent()
                .asString());
    }
}
