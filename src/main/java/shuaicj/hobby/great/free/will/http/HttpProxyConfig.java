package shuaicj.hobby.great.free.will.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurations.
 *
 * @author shuaicj 2017/09/14
 */
@Configuration
public class HttpProxyConfig {

    @Bean
    public ExecutorService threadPool() {
        return Executors.newCachedThreadPool();
    }
}
