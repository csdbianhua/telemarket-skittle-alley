package telemarketer.skittlealley.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class MsgModel {
    private final String content;
    private String except;
    private String toId;

    private MsgModel(String content) {
        this.content = content;
    }

    public static MsgModel content(String body) {
        return new MsgModel(body);
    }

    public static MsgModel content(Object body) {
        return new MsgModel(JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect));
    }

    public boolean isMatch(String id) {
        if (toId != null) {
            return toId.equals(id);
        }
        return except == null || !except.equals(id);
    }

    public MsgModel setToId(String toId) {
        this.toId = toId;
        return this;
    }

    public String getContent() {
        return content;
    }


    public MsgModel setExcept(String except) {
        this.except = except;
        return this;
    }
}
