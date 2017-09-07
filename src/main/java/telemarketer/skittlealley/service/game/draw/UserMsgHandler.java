package telemarketer.skittlealley.service.game.draw;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.WebSocketSession;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.*;
import telemarketer.skittlealley.service.common.RequestHandler;

import java.util.ArrayList;
import java.util.Map;

/**
 * 处理用户发送的信息
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/8
 */
@DrawGuessEventHandler(DrawCode.DRAW_MSG)
public class UserMsgHandler implements RequestHandler {

    @Override
    public void apply(ApiRequest request, ApiResponse response, WebSocketSession session) {
        String msg = StringEscapeUtils.escapeHtml4(request.getMsg());
        if (StringUtils.isBlank(msg)) {
            return;
        }
        Map<String, Object> attributes = session.getAttributes();
        String id = session.getId();
        DrawPlayerInfo info = ((DrawPlayerInfo) attributes.get("info"));
        DrawGuessContext ctx = (DrawGuessContext) attributes.get("ctx");
        DrawGameStatus status = ctx.status();
        ArrayList<String> msgs = new ArrayList<>(2);
        if (status == DrawGameStatus.RUN) {
            if (StringUtils.equals(id, ctx.getCurrentUser())) {
                protectSecret(info, ctx, msg, msgs);
            } else {
                processGussPerson(info, ctx, msg, msgs);
            }
        } else {
            msgs.add("<b>" + info.getName() + "</b>: " + msg);
        }
        response.setCode(DrawCode.DRAW_MSG.getCode()).setData(msgs);
    }

    private void processGussPerson(DrawPlayerInfo info, DrawGuessContext ctx, String msg, ArrayList<String> msgs) {
        if (info.status() == DrawUserStatus.GUESS && ctx.isCurrentWord(msg)) {
            info.incrScore();
            info.setStatus(DrawUserStatus.RIGHT);
            ctx.incrRightNumber();
            msgs.add("机智的 <b>" + info.getName() + "</b> 答对了!");
        } else {
            protectSecret(info, ctx, msg, msgs);
        }

    }

    private void protectSecret(DrawPlayerInfo info, DrawGuessContext ctx, String msg, ArrayList<String> msgs) {
        if (StringUtils.contains(msg, ctx.getCurrentWord().getWord())) {
            msg = "**";
        }
        msgs.add("<b>" + info.getName() + "</b>:" + msg);
    }

}
