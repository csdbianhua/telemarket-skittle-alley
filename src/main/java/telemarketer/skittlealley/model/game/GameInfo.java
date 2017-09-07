package telemarketer.skittlealley.model.game;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏信息
 * <p>
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
public class GameInfo {
    private String name;
    private String identify;
    private Map<String, Object> attrs = new HashMap<>();

    public GameInfo() {
    }

    public String getName() {
        return name;
    }

    public GameInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getIdentify() {
        return identify;
    }

    public GameInfo setIdentify(String identify) {
        this.identify = identify;
        return this;
    }

    public GameInfo putAttr(String key, Object val) {
        attrs.put(key, val);
        return this;
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameInfo gameInfo = (GameInfo) o;
        return identify.equals(gameInfo.identify);
    }

    @Override
    public int hashCode() {
        return identify.hashCode();
    }
}
