package telemarketer.skittlealley.service.drawguess.handler;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.model.game.drawguess.*;
import telemarketer.skittlealley.service.RequestHandler;

import static telemarketer.skittlealley.model.game.drawguess.DrawCode.USER_READY;

/**
 * 处理用户的准备信息
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/9
 */
@Service
public class ReadyHandler implements RequestHandler {

    private static final String STATUS_NAME = "status";
    private static final String ID_NAME = "id";

    @Override
    public Flux<MsgModel> apply(ApiRequest request, WebSocketSession session) {
        DrawGuessContext ctx = (DrawGuessContext) session.getAttributes().get("ctx");
        JSONObject obj = JSONObject.parseObject(request.getMsg());
        boolean ready = obj.getBooleanValue(STATUS_NAME);
        String id = obj.getString(ID_NAME);
        DrawPlayerInfo info = ((DrawPlayerInfo) session.getAttributes().get("info"));
        if (ctx.status() != DrawGameStatus.READY) {
            return Flux.empty();
        }
        if (ready && info.status() == DrawUserStatus.WAIT) {
            info.setStatus(DrawUserStatus.READY);
            ctx.addPlayer(info);
        } else if (!ready && info.status() == DrawUserStatus.READY) {
            info.setStatus(DrawUserStatus.WAIT);
            ctx.removePlayer(id);
        }
        return Flux.just(MsgModel.content(ApiResponse.builder().setData(info).setCode(USER_READY.getCode())));
    }

    @Override
    public DrawCode[] supported() {
        return new DrawCode[]{USER_READY};
    }
}
