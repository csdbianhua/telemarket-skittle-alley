package telemarketer.skittlealley.web.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import telemarketer.skittlealley.framework.annotation.WSHandler;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.noonesurvived.NosContext;
import telemarketer.skittlealley.service.game.NoOneSurvived;

import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static telemarketer.skittlealley.util.Constant.GAME_WEB_SOCKET_PREFIX;

/**
 * 你画我猜WebSocket
 * <p>
 *
 * @author hason
 * @date 2017/2/6
 */
@WSHandler(GAME_WEB_SOCKET_PREFIX + NoOneSurvived.IDENTIFY)
public class NoOnSurvivedWebSocket implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(NoOnSurvivedWebSocket.class);
    private static final MsgModel PING = new MsgModel("ping");
    private final Map<String, WebSocketSession> clients = Collections.synchronizedMap(new LinkedHashMap<>());
    private final NoOneSurvived gameService;
    private final EmitterProcessor<MsgModel> processor = EmitterProcessor.create(false);
    private final FluxSink<MsgModel> sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final Disposable heartBeat = Flux.interval(Duration.ofSeconds(15), Schedulers.newSingle("HeartBeat")).doOnNext(l -> {
        if (clients.size() > 0) {
            sink.next(PING);
        }
    }).subscribe();

    @Autowired
    public NoOnSurvivedWebSocket(NoOneSurvived gameService) {
        this.gameService = gameService;
    }

    private Flux<MsgModel> afterConnectionEstablished(WebSocketSession session) {

        String id = session.getId();
        clients.put(id, session);
        NosContext ctx = gameService.connected(session);
        Object info = session.getAttributes().get("info");
        MsgModel broadMsg = new MsgModel(ApiResponse.code(-1,
                ImmutableMap.of("info", info))).setExcept(id);
        HashMap<String, Object> json = Maps.newHashMapWithExpectedSize(5);
        json.put("info", id);
        json.put("ctx", ctx);
        json.put("assign", true);
        json.put("timestamp", Instant.now().toEpochMilli());
        MsgModel toMsg = new MsgModel(ApiResponse.code(-1, json)).setToId(id);
        if (log.isDebugEnabled()) {
            log.debug("[NOS] {} join", session.getId());
        }
        return Flux.just(broadMsg, toMsg);

    }


    private void afterConnectionClosed(WebSocketSession session) {
        String id = session.getId();
        clients.remove(id);
        gameService.closed(session);
        broadcast(transformToMsg(ApiResponse.code(-1, session.getAttributes().get("info"))),
                null);
        if (log.isDebugEnabled()) {
            log.debug("[NOS] {} leave", session.getId());
        }
    }

    private void handleTextMessage(WebSocketSession session, WebSocketMessage message) {

        String result = gameService.handleRequest(message.getPayloadAsText(StandardCharsets.UTF_8), session);

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
    }


    /**
     * 广播给用户信息 可以除开某个id
     *
     * @param msg    信息
     * @param except 除开的id
     */
    public void broadcast(String msg, String except) {
        sink.next(new MsgModel(msg).setExcept(except));
    }


    public String transformToMsg(Object object) {
        return JSON.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> flux = session.receive()
                .doOnNext(msg -> handleTextMessage(session, msg))
                .doOnError(throwable -> log.error("[NOS]WebSocket error", throwable))
                .doOnTerminate(() -> afterConnectionClosed(session))
                .then();
        Mono<Void> send = session.send(processor.filter(model -> model.isMatch(session.getId()))
                .map(model -> {
                    if (log.isDebugEnabled()) {
                        log.debug("[Websocket] msg: {} , to: {}", model.content, session.getId());
                    }
                    return session.textMessage(model.getContent());
                }));
        return Flux.merge(flux,
                send,
                afterConnectionEstablished(session).doOnNext(sink::next)
        ).then();
    }

    @PreDestroy
    public void close() {
        heartBeat.dispose();
    }

    static class MsgModel {
        private final String content;
        private String except;
        private String toId;

        public MsgModel(String content) {
            this.content = content;
        }

        public MsgModel(ApiResponse body) {
            this.content = JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect);
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
