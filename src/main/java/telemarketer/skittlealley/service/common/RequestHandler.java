package telemarketer.skittlealley.service.common;

import org.springframework.web.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;

/**
 * 请求处理器。
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/4
 */
@FunctionalInterface
public interface RequestHandler {

    void apply(ApiRequest request, ApiResponse response, WebSocketSession session);
}
