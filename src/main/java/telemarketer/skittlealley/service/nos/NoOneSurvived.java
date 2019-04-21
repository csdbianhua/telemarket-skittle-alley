package telemarketer.skittlealley.service.nos;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import telemarketer.skittlealley.model.MsgModel;
import telemarketer.skittlealley.model.game.noonesurvived.NosCode;
import telemarketer.skittlealley.model.game.noonesurvived.NosContext;
import telemarketer.skittlealley.model.game.noonesurvived.NosUserInfo;
import telemarketer.skittlealley.service.CodeMessageHandler;
import telemarketer.skittlealley.service.IWebSocketGameService;
import telemarketer.skittlealley.service.RequestHandler;
import telemarketer.skittlealley.service.nos.handler.NosRequestHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 无人生还
 *
 * @author hason
 */
@Service
public class NoOneSurvived extends CodeMessageHandler implements IWebSocketGameService<NosContext> {

    private static final NosContext context = new NosContext();
    public static final String IDENTIFY = "nos";

    public NoOneSurvived(List<NosRequestHandler> handlers) {
        Map<Integer, RequestHandler<?>> handlerMap = new HashMap<>();
        for (NosRequestHandler handler : handlers) {
            for (NosCode drawCode : handler.supported()) {
                handlerMap.put(drawCode.getCode(), handler);
            }
        }
        setRequestHandlers(handlerMap);
    }

    @Override
    public Mono<NosContext> connected(WebSocketSession session) {
        NosUserInfo userInfo = new NosUserInfo();
        userInfo.setId(session.getId());
        userInfo.setName(RandomStringUtils.random(5, true, true));
        session.getAttributes().put("info", userInfo);
        session.getAttributes().put("ctx", context);
        return Mono.just(context);
    }

    @Override
    public Flux<MsgModel> closed(WebSocketSession session) {
        return Flux.empty();
    }

}
