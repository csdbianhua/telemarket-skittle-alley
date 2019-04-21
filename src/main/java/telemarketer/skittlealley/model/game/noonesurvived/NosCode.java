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
    /**
     * 用户准备
     */
    USER_READY(11),
    /**
     * 玩家移动
     */
    PLAYER_MOVE(20),
    /**
     * 玩家转向
     */
    PLAYER_TURN_TO(21),
    /**
     * 玩家攻击
     */
    PLAYER_ATTACK(22),
    ;


    private final int code;

    NosCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
