package telemarketer.skittlealley.service.game.draw;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.model.game.drawguess.DrawGameStatus;
import telemarketer.skittlealley.model.game.drawguess.DrawGuessContext;
import telemarketer.skittlealley.model.game.drawguess.DrawPlayerInfo;
import telemarketer.skittlealley.service.common.RequestHandler;

import static telemarketer.skittlealley.model.game.drawguess.DrawCode.USER_CHANGE_NAME;

/**
 * 处理更名请求
 * <p>
 * Author: Hanson
 * Time: 17-2-9
 * Email: imyijie@outlook.com
 */
@Service
public class ChangeNameHandler implements RequestHandler {
    @Override
    public void apply(ApiRequest request, ApiResponse response, WebSocketSession session) {
        DrawGuessContext ctx = (DrawGuessContext) session.getAttributes().get("ctx");
        if (ctx.status() != DrawGameStatus.READY) {
            return;
        }
        String name = StringEscapeUtils.escapeHtml4(request.getMsg());
        DrawPlayerInfo oldInfo = (DrawPlayerInfo) session.getAttributes().get("info");
        oldInfo.setName(name);
        response.setCode(USER_CHANGE_NAME.getCode()).setData(oldInfo);
    }

    @Override
    public DrawCode[] supported() {
        return new DrawCode[]{USER_CHANGE_NAME};
    }
}
