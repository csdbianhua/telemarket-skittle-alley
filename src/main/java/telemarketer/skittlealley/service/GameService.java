package telemarketer.skittlealley.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import telemarketer.skittlealley.annotation.Game;
import telemarketer.skittlealley.model.game.GameInfo;

import java.util.*;

/**
 * 游戏管理
 * <p>
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Service
public class GameService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    private final Map<String, GameInfo> info;

    public GameService(ApplicationContext context) {
        String[] beans = context.getBeanNamesForAnnotation(Game.class);
        Map<String, GameInfo> info = new LinkedHashMap<>();
        for (String bean : beans) {
            Class<?> aClass = context.getBean(bean).getClass();
            Game annotation = aClass.getAnnotation(Game.class);
            String name = annotation.gameName();
            String value = annotation.value();
            JSONObject attrs = JSONObject.parseObject(annotation.attrs());
            GameInfo gameInfo = new GameInfo().setName(name).setIdentify(value);
            for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                gameInfo.putAttr(entry.getKey(), entry.getValue());
            }
            info.put(value, gameInfo);
            LOGGER.info("[GameService]注册游戏 [{}]", name);
        }
        this.info = Collections.unmodifiableMap(info);
    }

    public Optional<GameInfo> findInfo(String identify) {
        if (StringUtils.isBlank(identify)) {
            return Optional.empty();
        }
        return Optional.ofNullable(info.get(identify));
    }

    public Collection<GameInfo> getGames() {
        return info.values();
    }
}
