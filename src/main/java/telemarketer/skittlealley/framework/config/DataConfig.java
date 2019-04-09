package telemarketer.skittlealley.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Configuration
public class DataConfig {


    @Bean
    public ThreadPoolExecutor threadPool() {
        return new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 4,
                Runtime.getRuntime().availableProcessors() * 6,
                5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
