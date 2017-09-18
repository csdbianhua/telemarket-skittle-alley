package telemarketer.skittlealley.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import telemarketer.skittlealley.config.properties.DruidConfig;

import java.io.IOException;

/**
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Configuration
@EnableConfigurationProperties({DruidConfig.class})
public class DataConfig {

    @Bean(initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource(DruidConfig druidConfig) throws IOException {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(druidConfig.getUrl() + "?" + druidConfig.getOptions());
        ds.setUsername(druidConfig.getUser());
        ds.setPassword(druidConfig.getPwd());
        ds.setInitialSize(druidConfig.getInitialSize());
        ds.setMinIdle(druidConfig.getMinIdle());
        ds.setMaxActive(druidConfig.getMaxActive());
        ds.setMaxWait(druidConfig.getMaxWait());
        ds.setTimeBetweenEvictionRunsMillis(druidConfig.getTimeBetweenEvictionRunsMillis());
        ds.setMinEvictableIdleTimeMillis(druidConfig.getMinEvictableIdleTimeMillis());
        ds.setValidationQuery(druidConfig.getValidationQuery());
        ds.setTestWhileIdle(druidConfig.getTestWhileIdle());
        ds.setTestOnBorrow(druidConfig.getTestOnBorrow());
        ds.setTestOnReturn(druidConfig.getTestOnReturn());
        return ds;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPool() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 4);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 10);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(300);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
