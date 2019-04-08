package telemarketer.skittlealley.service.game.draw;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.model.game.drawguess.DrawGuessContext;
import telemarketer.skittlealley.service.common.RequestHandler;

import static telemarketer.skittlealley.model.game.drawguess.DrawCode.DRAW_CHANGE_BRUSH;

/**
 * 处理改变笔刷的请求
 * <p>
 * Author: Hanson
 * Time: 17-2-8
 * Email: imyijie@outlook.com
 */
@Service
public class ChangeBrushHandler implements RequestHandler {

    private static final String TYPE_COLOR = "color";
    private static final String TYPE_WIDTH = "width";
    private static final String VALUE_NAME = "value";
    private static final String TYPE_NAME = "type";

    @Override
    public void apply(ApiRequest request, ApiResponse response, WebSocketSession session) {
        DrawGuessContext ctx = (DrawGuessContext) session.getAttributes().get("ctx");
        if (!ctx.isCurrentUser(session.getId())) {
            return;
        }
        JSONObject obj = JSONObject.parseObject(request.getMsg());
        String type = obj.getString(TYPE_NAME);
        if (TYPE_COLOR.equals(type)) {
            ctx.setColor(obj.getString(VALUE_NAME));
        } else if (TYPE_WIDTH.equals(type)) {
            ctx.setWidth(obj.getInteger(VALUE_NAME));
        }
        response.setCode(DRAW_CHANGE_BRUSH.getCode()).setData(obj);
    }

    @Override
    public DrawCode[] supported() {
        return new DrawCode[]{DRAW_CHANGE_BRUSH};
    }
}
