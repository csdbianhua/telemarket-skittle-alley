package telemarketer.skittlealley.service.game.draw;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.service.common.RequestHandler;

/**
 * 原样直接传达信息
 * <p>
 * Author: Hanson
 * Time: 17-2-6
 * Email: imyijie@outlook.com
 */
@Service
public class EchoHandler implements RequestHandler {

    @Override
    public void apply(ApiRequest request, ApiResponse response, WebSocketSession session) {
        response.setCode(request.getCode()).setData(JSONObject.parseObject(request.getMsg()));
    }

    @Override
    public DrawCode[] supported() {
        return new DrawCode[]{DrawCode.DRAW_MOVE, DrawCode.DRAW_CLEAR};
    }
}
