package telemarketer.skittlealley.web.websocket;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import telemarketer.skittlealley.model.MsgModel;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public abstract class BaseGameWebSocketHandler implements WebSocketHandler {
    private static final Logger BASE_LOGGER = LoggerFactory.getLogger(BaseGameWebSocketHandler.class);
    private static final MsgModel PING = MsgModel.content("ping");
    protected final Map<String, WebSocketSession> clients = Collections.synchronizedMap(new LinkedHashMap<>());
    private final EmitterProcessor<MsgModel> processor = EmitterProcessor.create(false);
    protected final FluxSink<MsgModel> sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final Disposable heartBeat = Flux.interval(Duration.ofSeconds(15), Schedulers.newSingle("HeartBeat")).doOnNext(l -> {
        if (clients.size() > 0) {
            sink.next(PING);
        }
    }).subscribe();

    private Flux<MsgModel> afterConnectionEstablished(WebSocketSession session) {
        clients.put(session.getId(), session);
        return doAfterConnectionEstablished(session);
    }

    private Flux<MsgModel> afterConnectionClosed(WebSocketSession session) {
        clients.remove(session.getId());
        return doAfterConnectionClosed(session);
    }

    protected abstract Flux<MsgModel> doAfterConnectionEstablished(WebSocketSession session);

    protected abstract Flux<MsgModel> doAfterConnectionClosed(WebSocketSession session);

    protected abstract Flux<MsgModel> handleTextMessage(WebSocketSession session, WebSocketMessage message);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Mono<Void> flux = session.receive()
                .doOnNext(msg -> handleTextMessage(session, msg).subscribe(sink::next, throwable -> BASE_LOGGER.error("[WebSocket]handleRequest error", throwable)))
                .doOnError(throwable -> BASE_LOGGER.error("[Websocket]WebSocket error", throwable))
                .doOnTerminate(() -> afterConnectionClosed(session).subscribe(sink::next, throwable -> BASE_LOGGER.error("[WebSocket]handle connection closed error", throwable)))
                .then();
        Mono<Void> send = session.send(processor.filter(model -> model.isMatch(session.getId()))
                .map(model -> {
                    if (BASE_LOGGER.isDebugEnabled()) {
                        BASE_LOGGER.debug("[Websocket] msg: {} , to: {}", model.getContent(), session.getId());
                    }
                    return session.textMessage(model.getContent());
                }));
        return Flux.merge(flux,
                send,
                afterConnectionEstablished(session).doOnNext(sink::next)
        ).then();
    }

    @PreDestroy
    public final void close() {
        heartBeat.dispose();
    }

}
