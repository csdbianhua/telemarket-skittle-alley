package telemarketer.skittlealley.service.nos.handler;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.model.game.noonesurvived.NosCode;
import telemarketer.skittlealley.model.game.noonesurvived.NosUserInfo;

import java.util.Map;

import static telemarketer.skittlealley.model.game.drawguess.DrawCode.MSG;

/**
 * @author hason
 */
@Service
public class NosUserMsgHandler implements NosRequestHandler {
    @Override
    public Flux<MsgModel> apply(ApiRequest request, WebSocketSession session) {
        String msg = StringEscapeUtils.escapeHtml4(request.getMsg());
        if (StringUtils.isBlank(msg)) {
            return Flux.empty();
        }
        Map<String, Object> attributes = session.getAttributes();
        NosUserInfo info = ((NosUserInfo) attributes.get("info"));

        return Flux.just(MsgModel.content(ApiResponse.builder()
                .setCode(MSG.getCode())
                .setData(ImmutableMap.of("name", info.getName(), "content", msg))));
    }

    @Override
    public NosCode[] supported() {
        return new NosCode[]{NosCode.MSG};
    }
}
