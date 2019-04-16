package telemarketer.skittlealley.web.websocket;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.framework.annotation.WSHandler;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.model.game.noonesurvived.NosCode;
import telemarketer.skittlealley.service.nos.NoOneSurvived;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
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
        return gameService.connected(session)
                .flatMapMany(context -> {
                    Map<String, Object> json = Maps.newHashMapWithExpectedSize(3);
                    json.put("info", session.getId());
                    json.put("ctx", context);
                    json.put("users", clients.entrySet().stream()
                            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getAttributes().get("info"))));
                    json.put("timestamp", Instant.now().toEpochMilli());
                    MsgModel msgModel = MsgModel.content(ApiResponse.code(NosCode.USER_JOIN.getCode(), json)).setToId(session.getId());
                    if (log.isDebugEnabled()) {
                        log.debug("[Nos] {} join", session.getId());
                    }
                    return Flux.just(msgModel);
                });
    }

    @Override
    protected Flux<MsgModel> doAfterConnectionClosed(WebSocketSession session) {
        if (log.isDebugEnabled()) {
            log.debug("[Nos] {} leave", session.getId());
        }
        return gameService.closed(session);
    }

    @Override
    protected Flux<MsgModel> handleTextMessage(WebSocketSession session, WebSocketMessage message) {
        return gameService.handleRequest(message.getPayloadAsText(StandardCharsets.UTF_8), session);

    }

}
