package telemarketer.skittlealley.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.socket.TextMessage;
import telemarketer.skittlealley.model.ApiResponse;

/**
 * Chen Yijie on 2016/11/16 14:10.
 */
public class Constant {

    public static final short TRUE = 1;
    public static final short FALSE = 0;

    public static final String GAME_WEB_SOCKET_PREFIX = "/ws/games/";
    public static final TextMessage WEB_SOCKET_ERROR_MSG = new TextMessage(JSONObject.toJSONString(ApiResponse.error("系统异常")));

    private Constant() {
    }
}
