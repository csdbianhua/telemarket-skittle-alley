package telemarketer.skittlealley.web.websocket;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import telemarketer.skittlealley.framework.annotation.WSHandler;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.model.game.drawguess.DrawGuessContext;
import telemarketer.skittlealley.service.game.DrawGuess;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static telemarketer.skittlealley.util.Constant.GAME_WEB_SOCKET_PREFIX;

/**
 * 你画我猜WebSocket
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/6
 */
@WSHandler(GAME_WEB_SOCKET_PREFIX + DrawGuess.IDENTIFY)
public class DrawGuessWebSocket extends TextWebSocketHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DrawGuessWebSocket.class);

    private static final Map<String, WebSocketSession> clients = Collections.synchronizedMap(new LinkedHashMap<>());
    private final DrawGuess drawGuess;
    private final ThreadPoolTaskExecutor threadPool;

    @Autowired
    public DrawGuessWebSocket(DrawGuess drawGuess, ThreadPoolTaskExecutor threadPool) {
        this.drawGuess = drawGuess;
        this.threadPool = threadPool;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = session.getId();
        clients.remove(id);
        drawGuess.closed(session);
        broadcast(transformToMsg(ApiResponse.code(DrawCode.USER_LEFT.getCode(), session.getAttributes().get("info"))),
                null);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        TextMessage result = null;
        try {
            String text = drawGuess.handleRequest(message.getPayload(), session);
            if (StringUtils.isNotBlank(text)) {
                result = new TextMessage(text);
            } else {
                return;
            }
        } catch (RuntimeException e) {
            LOGGER.error("[drawGuess]处理客户端消息异常", e);
        }
        broadcast(result, null);
    }

    /**
     * 单独发送给某人
     *
     * @param msg 信息
     * @param id  id
     */
    public void sendTo(TextMessage msg, String id) {
        WebSocketSession session = MapUtils.getObject(clients, id);
        if (session == null || !session.isOpen()) {
            LOGGER.warn("将要发送给的session已不存在,{}", id);
            return;
        }
        try {
            session.sendMessage(msg);
        } catch (IOException e) {
            LOGGER.error("[DrawGuess]发送消息\"{}\"出错", msg.getPayload(), e);
        }
    }

    /**
     * 广播给用户信息 可以除开某个id
     *
     * @param msg    信息
     * @param except 除开的id
     */
    public void broadcast(TextMessage msg, String except) {
        threadPool.execute(() -> {
            for (Map.Entry<String, WebSocketSession> entry : clients.entrySet()) {
                WebSocketSession se = entry.getValue();
                if (StringUtils.equals(except, entry.getKey()) || !se.isOpen()) {
                    continue;
                }
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (se) {
                    try {
                        se.sendMessage(msg);
                    } catch (IOException e) {
                        LOGGER.error("[DrawGuess]发送消息\"{}\"出错", msg.getPayload(), e);
                    }
                }
            }
        });

    }


    /**
     * 将对象转化为 {@link TextMessage}
     *
     * @param object 对象
     * @return 转化后的 {@link TextMessage}
     */
    public TextMessage transformToMsg(Object object) {
        return new TextMessage(JSONObject.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect));
    }
}
