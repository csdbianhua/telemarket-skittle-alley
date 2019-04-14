package telemarketer.skittlealley.service;

import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.MsgModel;

import java.util.Collections;
import java.util.Map;

/**
 * @author Hanson
 * @date 2017/2/6
 */
public abstract class MessageHandler {

    private final RequestHandler defaultHandler =
            (rq, s) -> Flux.just(MsgModel.content(new ApiResponse().setCode(-1).setData("服务器不知道该如何处理此消息，请确认信息是否正确")));
    protected Map<Integer, RequestHandler> requestHandlers = Collections.emptyMap();

    protected void setRequestHandlers(Map<Integer, RequestHandler> requestHandlers) {
        this.requestHandlers = Collections.unmodifiableMap(requestHandlers);
    }

    protected Flux<MsgModel> handle(ApiRequest request, WebSocketSession session) {
        return requestHandlers.getOrDefault(request.getCode(), defaultHandler).apply(request, session);
    }


}
