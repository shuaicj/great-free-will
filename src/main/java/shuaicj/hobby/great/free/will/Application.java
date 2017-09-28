package shuaicj.hobby.great.free.will;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

/**
 * Spring boot app.
 *
 * @author shuaicj 2017/09/08
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Scope("prototype")
    @Profile("prod")
    public LoggingHandler loggingHandlerDefault() {
        return new LoggingHandler(LogLevel.INFO);
    }

    @Bean
    @Scope("prototype")
    @Profile("!prod")
    public LoggingHandler loggingHandlerProd() {
        return new LoggingHandler(LogLevel.WARN);
    }
}
