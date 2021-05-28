package telemarketer.skittlealley.framework.config;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import telemarketer.skittlealley.framework.annotation.WSHandler;

import java.util.Map;

/**
 * web 配置
 * <p>
 *
 * @author hason
 */
@Configuration(proxyBeanMethods = false)
public class WebConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);


    private final ApplicationContext context;

    public WebConfig(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
        return new WebSocketHandlerAdapter(webSocketService);
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }

    @Bean
    public HandlerMapping webSocketMapping() {
        String[] beans = context.getBeanNamesForAnnotation(WSHandler.class);

        Map<String, WebSocketHandler> map = Maps.newHashMapWithExpectedSize(beans.length);

        for (String bean : beans) {
            WebSocketHandler handler = context.getBean(bean, WebSocketHandler.class);
            String path = handler.getClass().getAnnotation(WSHandler.class).value();
            map.put(path, handler);
            LOGGER.info("[WebSocket]注册 WebSocket path {} 到 {}", path, bean);
        }
        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

}
