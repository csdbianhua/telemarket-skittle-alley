package telemarketer.skittlealley.service.game;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.model.game.noonesurvived.NosContext;

/**
 * 无人生还
 *
 * @author hason
 */
@Service
public class NoOneSurvived {

    public static final String IDENTIFY = "nos";

    public NosContext connected(WebSocketSession session) {
        return null;
    }

    public void closed(WebSocketSession session) {

    }

    public String handleRequest(String payloadAsText, WebSocketSession session) {
        return null;
    }
}
