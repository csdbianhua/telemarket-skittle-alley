package telemarketer.skittlealley.config;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Configuration
public class DataConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataConfiguration.class);

    @Bean
    public DataSource hsqldbDataSource() throws IOException {
        DataSourceBuilder builder = new DataSourceBuilder(ClassLoader.getSystemClassLoader());
        DataSource ds = builder.url("jdbc:hsqldb:file:data/db;hsqldb.write_delay=false;shutdown=true;").username("SA")
                .driverClassName("org.hsqldb.jdbcDriver").type(org.apache.tomcat.jdbc.pool.DataSource.class).build();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:sql/*.sql");
        char[] chars = new char[256];
        Arrays.sort(resources, Comparator.comparing(Resource::getFilename)); // 确保按顺序执行
        for (Resource resource : resources) {
            String s;
            try (Reader br = new InputStreamReader(resource.getInputStream(), "utf-8")) {
                StringBuilder sb = new StringBuilder(256);
                int count;
                while ((count = br.read(chars)) > 0) {
                    sb.append(new String(chars, 0, count));
                }
                s = sb.toString();
            }
            LOGGER.info("执行SQL文件:{}", resource.getFilename());
            try (Connection connection = ds.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(s);
            } catch (SQLException e) {
                LOGGER.warn("SQL文件[{}]语法错误", resource.getFilename(), e);
                throw new IllegalStateException(e);
            }
        }
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
