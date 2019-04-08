package telemarketer.skittlealley.service.common;

import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;

/**
 * 请求处理器。
 * <p>
 */
@FunctionalInterface
public interface RequestHandler {

    /**
     * 处理请求
     *
     * @param request  请求
     * @param response 响应
     * @param session  session
     */
    void apply(ApiRequest request, ApiResponse response, WebSocketSession session);

    /**
     * 支持的命令
     *
     * @return 支持命令的数组
     */
    default DrawCode[] supported() {
        return new DrawCode[0];
    }
}
