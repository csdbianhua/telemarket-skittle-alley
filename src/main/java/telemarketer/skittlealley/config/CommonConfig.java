package telemarketer.skittlealley.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

/**
 * @author hason
 * @version 17-9-9
 */
@Configuration
public class CommonConfig {

    @Bean
    public static YamlPropertiesFactoryBean yamlPropertiesFactoryBean() {
        YamlPropertiesFactoryBean propertiesFactoryBean = new YamlPropertiesFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("/**/*.yml");
            propertiesFactoryBean.setResources(resources);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return propertiesFactoryBean;
    }

    /**
     * 用于解析yml配置，可使用${xx.xx}形式访问
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer configurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setProperties(yamlPropertiesFactoryBean().getObject());
        return configurer;
    }
}
