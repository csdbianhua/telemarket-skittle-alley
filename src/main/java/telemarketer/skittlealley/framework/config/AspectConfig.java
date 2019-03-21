package telemarketer.skittlealley.framework.config;

import org.aspectj.lang.Aspects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import telemarketer.skittlealley.framework.aspect.DrawGuessAspect;
import telemarketer.skittlealley.service.game.DrawGuess;
import telemarketer.skittlealley.web.websocket.DrawGuessWebSocket;

/**
 * Be careful.
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/9
 */
@Configuration
public class AspectConfig {


    @Bean
    public DrawGuessAspect drawGuessAspect(DrawGuess service, DrawGuessWebSocket webSocket) {
        DrawGuessAspect aspect = Aspects.aspectOf(DrawGuessAspect.class);
        aspect.setService(service);
        aspect.setWebSocket(webSocket);
        return aspect;
    }
}
