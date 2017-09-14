package shuaicj.hobby.great.free.will;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import shuaicj.hobby.great.free.will.http.HttpProxyServer;

/**
 * Mock a http client and test.
 *
 * @author shuaicj 2017/09/09
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ClientTest {

    @Value("${proxy.port}")
    int port;

    @Test
    public void request() throws Exception {
        logger.info("check this: {}",
                // Request.Get("https://github.com")
                Request.Get("http://www.baidu.com")
                // Request.Get("http://127.0.0.1:8080/hello")
                        .viaProxy(new HttpHost("127.0.0.1", port))
                        .execute()
                        // .returnResponse());
                        .returnContent());
    }
}
