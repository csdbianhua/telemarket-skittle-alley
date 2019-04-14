package telemarketer.skittlealley.service.nos;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.model.game.noonesurvived.NosContext;
import telemarketer.skittlealley.service.IWebSocketGameService;

/**
 * 无人生还
 *
 * @author hason
 */
@Service
public class NoOneSurvived implements IWebSocketGameService<NosContext> {

    public static final String IDENTIFY = "nos";


    @Override
    public Mono<NosContext> connected(WebSocketSession session) {
        return Mono.empty();
    }

    @Override
    public Flux<MsgModel> closed(WebSocketSession session) {
        return Flux.empty();
    }

    @Override
    public Flux<MsgModel> handleRequest(String payloadAsText, WebSocketSession session) {
        return Flux.empty();
    }
}
