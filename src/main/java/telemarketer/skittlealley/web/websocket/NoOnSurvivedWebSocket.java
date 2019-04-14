package telemarketer.skittlealley.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.framework.annotation.WSHandler;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.service.game.NoOneSurvived;

import java.nio.charset.StandardCharsets;

import static telemarketer.skittlealley.util.Constant.GAME_WEB_SOCKET_PREFIX;

/**
 * 你画我猜WebSocket
 * <p>
 *
 * @author hason
 * @date 2017/2/6
 */
@WSHandler(GAME_WEB_SOCKET_PREFIX + NoOneSurvived.IDENTIFY)
public class NoOnSurvivedWebSocket extends BaseGameWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(NoOnSurvivedWebSocket.class);

    private final NoOneSurvived gameService;

    @Autowired
    public NoOnSurvivedWebSocket(NoOneSurvived gameService) {
        this.gameService = gameService;
    }

    @Override
    protected Flux<MsgModel> doAfterConnectionEstablished(WebSocketSession session) {
        return Flux.empty();
    }

    @Override
    protected Flux<MsgModel> doAfterConnectionClosed(WebSocketSession session) {
        return Flux.empty();
    }

    @Override
    protected Flux<MsgModel> handleTextMessage(WebSocketSession session, WebSocketMessage message) {

        return gameService.handleRequest(message.getPayloadAsText(StandardCharsets.UTF_8), session);

    }

}
