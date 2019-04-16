package telemarketer.skittlealley.model.game.noonesurvived;

public enum NosCode {
    /**
     * 玩家发送信息
     */
    MSG(4),
    /**
     * 用户进入
     */
    USER_JOIN(10),
    ;


    private final int code;

    NosCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
