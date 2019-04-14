package telemarketer.skittlealley.web.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
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
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.service.drawguess.DrawGuess;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
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
@WSHandler(GAME_WEB_SOCKET_PREFIX + DrawGuess.IDENTIFY)
public class DrawGuessWebSocket extends BaseGameWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(DrawGuessWebSocket.class);
    private final DrawGuess drawGuess;

    @Autowired
    public DrawGuessWebSocket(DrawGuess drawGuess) {
        this.drawGuess = drawGuess;
    }

    @Override
    protected Flux<MsgModel> doAfterConnectionEstablished(WebSocketSession session) {

        return drawGuess.connected(session)
                .flatMapMany(context -> {
                    Object info = session.getAttributes().get("info");
                    MsgModel broadMsg = MsgModel.content(ApiResponse.code(DrawCode.USER_JOIN.getCode(),
                            ImmutableMap.of("info", info))).setExcept(session.getId());
                    HashMap<String, Object> json = Maps.newHashMapWithExpectedSize(5);
                    json.put("info", session.getId());
                    json.put("players", getAllOnlinePlayers());
                    json.put("ctx", context);
                    json.put("assign", true);
                    json.put("timestamp", Instant.now().toEpochMilli());
                    MsgModel toMsg = MsgModel.content(ApiResponse.code(DrawCode.USER_JOIN.getCode(), json)).setToId(session.getId());
                    if (log.isDebugEnabled()) {
                        log.debug("[DrawGuess] {} join", session.getId());
                    }
                    return Flux.just(broadMsg, toMsg);
                });

    }

    @Override
    protected Flux<MsgModel> doAfterConnectionClosed(WebSocketSession session) {
        if (log.isDebugEnabled()) {
            log.debug("[DrawGuess] {} leave", session.getId());
        }
        return drawGuess.closed(session);
    }

    private Map<String, Object> getAllOnlinePlayers() {
        return clients.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().getAttributes().get("info")));
    }


    @Override
    protected Flux<MsgModel> handleTextMessage(WebSocketSession session, WebSocketMessage message) {
        return drawGuess.handleRequest(message.getPayloadAsText(StandardCharsets.UTF_8), session);
    }


    /**
     * 单独发送给某人
     *
     * @param msg 信息
     * @param id  id
     * @return mono
     */
    public void sendTo(String msg, String id) {
        sink.next(MsgModel.content(msg).setToId(id));
    }


    /**
     * 广播给用户信息 可以除开某个id
     *
     * @param msg    信息
     * @param except 除开的id
     */
    public void broadcast(String msg, String except) {
        sink.next(MsgModel.content(msg).setExcept(except));
    }


    public String transformToMsg(Object object) {
        return JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }


}
