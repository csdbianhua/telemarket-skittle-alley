package telemarketer.skittlealley.service;

import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.MsgModel;

/**
 * 请求处理器。
 * <p>
 */
@FunctionalInterface
public interface RequestHandler<T> {

    /**
     * 处理请求
     *
     * @param request  请求
     * @param session  session
     */
    Flux<MsgModel> apply(ApiRequest request, WebSocketSession session);

    /**
     * 支持的命令
     *
     * @return 支持命令的数组
     */
    default T[] supported() {
        return (T[]) new Object[0];
    }
}
