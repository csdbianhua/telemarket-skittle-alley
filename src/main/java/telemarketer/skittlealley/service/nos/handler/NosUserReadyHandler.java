package telemarketer.skittlealley.service.nos.handler;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.model.game.noonesurvived.NosCode;
import telemarketer.skittlealley.model.game.noonesurvived.NosContext;
import telemarketer.skittlealley.model.game.noonesurvived.NosPlayer;

import java.util.Map;

/**
 * 用户准备进入游戏
 *
 * @author hason
 */
@Service
public class NosUserReadyHandler implements NosRequestHandler {
    @Override
    public Flux<MsgModel> apply(ApiRequest request, WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        NosContext ctx = ((NosContext) attributes.get("ctx"));
        NosPlayer nosPlayer = ctx.newPlayer();
        attributes.put("player", nosPlayer);
        return Flux.just(MsgModel.content(ApiResponse.builder()
                .setCode(NosCode.USER_READY.getCode())
                .setData(ImmutableMap.of("x", nosPlayer.getX(), "y", nosPlayer.getY()))));
    }

    @Override
    public NosCode[] supported() {
        return new NosCode[]{NosCode.USER_READY};
    }
}
