package telemarketer.skittlealley.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.MsgModel;

import java.util.Collections;
import java.util.Map;

/**
 * 带code的信息处理
 *
 * @author Hanson
 * @date 2017/2/6
 */
public abstract class CodeMessageHandler {

    private final RequestHandler defaultHandler =
            (rq, s) -> Flux.just(MsgModel.content(new ApiResponse().setCode(-1).setData("服务器不知道该如何处理此消息，请确认信息是否正确")));
    protected Map<Integer, RequestHandler<?>> requestHandlers = Collections.emptyMap();

    protected void setRequestHandlers(Map<Integer, RequestHandler<?>> requestHandlers) {
        this.requestHandlers = Collections.unmodifiableMap(requestHandlers);
    }

    /**
     * 处理web socket请求
     *
     * @param payload 请求内容
     * @param session 会话
     * @return 处理结果字符串
     */
    public Flux<MsgModel> handleRequest(String payload, WebSocketSession session) {
        ApiRequest request = JSONObject.parseObject(payload, ApiRequest.class);
        return requestHandlers.getOrDefault(request.getCode(), defaultHandler).apply(request, session);
    }

}
