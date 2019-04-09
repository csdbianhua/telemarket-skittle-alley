package telemarketer.skittlealley.service.common;

import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;

import java.util.Collections;
import java.util.Map;

/**
 * Be careful.
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/6
 */
public abstract class MessageHandler {

    private final RequestHandler defaultHandler =
            (rq, re, s) -> re.setCode(-1).setData("服务器不知道该如何处理此消息，请确认信息是否正确");
    protected Map<Integer, RequestHandler> requestHandlers = Collections.emptyMap();

    protected void setRequestHandlers(Map<Integer, RequestHandler> requestHandlers) {
        this.requestHandlers = Collections.unmodifiableMap(requestHandlers);
    }

    public abstract String handleRequest(String payload, WebSocketSession session);

    protected void handle(ApiRequest request, ApiResponse response, WebSocketSession session) {
        requestHandlers.getOrDefault(request.getCode(), defaultHandler).apply(request, response, session);
    }


}
