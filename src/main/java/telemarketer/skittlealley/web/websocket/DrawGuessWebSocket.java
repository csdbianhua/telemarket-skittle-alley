package telemarketer.skittlealley.web.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import telemarketer.skittlealley.framework.annotation.WSHandler;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.model.game.drawguess.DrawGuessContext;
import telemarketer.skittlealley.service.game.DrawGuess;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static telemarketer.skittlealley.util.Constant.GAME_WEB_SOCKET_PREFIX;

/**
 * 你画我猜WebSocket
 * <p>
 *
 * @author hason
 * @date 2017/2/6
 */
@WSHandler(GAME_WEB_SOCKET_PREFIX + DrawGuess.IDENTIFY)
public class DrawGuessWebSocket implements WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DrawGuessWebSocket.class);
    private static byte[] PING_MSG = "ping".getBytes(StandardCharsets.UTF_8);
    private final Map<String, WebSocketSession> clients = Collections.synchronizedMap(new LinkedHashMap<>());
    private final DrawGuess drawGuess;
    private final EmitterProcessor<MsgModel> processor = EmitterProcessor.create();
    private final FluxSink<MsgModel> sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);

    @Autowired
    public DrawGuessWebSocket(DrawGuess drawGuess) {
        this.drawGuess = drawGuess;
    }

    private void afterConnectionEstablished(WebSocketSession session) {
        String id = session.getId();
        clients.put(id, session);
        DrawGuessContext ctx = drawGuess.connected(session);
        JSONObject obj = new JSONObject();
        obj.put("info", session.getAttributes().get("info"));
        broadcast(transformToMsg(ApiResponse.code(DrawCode.USER_JOIN.getCode(), obj)), id);
        obj.put("players", clients.values()
                .stream()
                .map((w) -> w.getAttributes().get("info"))
                .collect(Collectors.toList()));
        obj.put("ctx", ctx);
        obj.put("assign", true);
        obj.put("timestamp", Instant.now().toEpochMilli());
        sendTo(transformToMsg(ApiResponse.code(DrawCode.USER_JOIN.getCode(), obj)), id);
    }

    private void afterConnectionClosed(WebSocketSession session) {
        String id = session.getId();
        clients.remove(id);
        drawGuess.closed(session);
        broadcast(transformToMsg(ApiResponse.code(DrawCode.USER_LEFT.getCode(), session.getAttributes().get("info"))),
                null);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} leave", session.getId());
        }
    }

    private void handleTextMessage(WebSocketSession session, WebSocketMessage message) {

        String result = drawGuess.handleRequest(message.getPayloadAsText(StandardCharsets.UTF_8), session);

        broadcast(result, null);
    }


    /**
     * 单独发送给某人
     *
     * @param msg 信息
     * @param id  id
     * @return mono
     */
    public void sendTo(String msg, String id) {
        sink.next(new MsgModel(msg).setToId(id));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MSG:{},id:{}", msg, id);
        }
    }


    /**
     * 广播给用户信息 可以除开某个id
     *
     * @param msg    信息
     * @param except 除开的id
     */
    public void broadcast(String msg, String except) {
        sink.next(new MsgModel(msg).setExcept(except));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MSG:{},except:{}", msg, except);
        }
    }


    public String transformToMsg(Object object) {
        return JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> flux = session.receive()
                .doOnNext(msg -> handleTextMessage(session, msg))
                .doOnError(throwable -> LOGGER.error("[DrawGuess]WebSocket error", throwable))
                .doOnTerminate(() -> afterConnectionClosed(session));
        Mono<Void> result = session.send(processor.filter(model -> model.isMatch(session.getId()))
                .map(model -> {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("send '{}' to {}", model.content, session.getId());
                    }
                    return session.textMessage(model.getContent());
                }))
                .then();
        afterConnectionEstablished(session);
        return result;
    }

    static class MsgModel {
        private final String content;
        private String except;
        private String toId;

        public MsgModel(String content) {
            this.content = content;
        }

        public boolean isMatch(String id) {
            if (toId != null) {
                return toId.equals(id);
            }
            return except == null || !except.equals(id);
        }

        public MsgModel setToId(String toId) {
            this.toId = toId;
            return this;
        }

        public String getContent() {
            return content;
        }


        public MsgModel setExcept(String except) {
            this.except = except;
            return this;
        }
    }
}
