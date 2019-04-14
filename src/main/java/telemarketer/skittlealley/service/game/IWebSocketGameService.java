package telemarketer.skittlealley.service.game;

import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import telemarketer.skittlealley.model.GameCtx;
import telemarketer.skittlealley.model.MsgModel;

public interface IWebSocketGameService<T extends GameCtx> {

    Mono<T> connected(WebSocketSession session);

    Flux<MsgModel> closed(WebSocketSession session);

    Flux<MsgModel> handleRequest(String payloadAsText, WebSocketSession session);
}
