package telemarketer.skittlealley.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import telemarketer.skittlealley.annotation.WSHandler;

/**
 * webSocket 配置
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/1/9
 */
@Service
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfig.class);


    private final ApplicationContext context;

    @Autowired
    public WebSocketConfig(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] beans = context.getBeanNamesForAnnotation(WSHandler.class);
        for (String bean : beans) {
            WebSocketHandler handler = context.getBean(bean, WebSocketHandler.class);
            String[] path = handler.getClass().getAnnotation(WSHandler.class).value();
            registry.addHandler(handler, path).setAllowedOrigins("*").withSockJS();
            LOGGER.info("[WebSocket]注册 WebSocket path {} 到 {}", path, bean);
        }
    }
}
