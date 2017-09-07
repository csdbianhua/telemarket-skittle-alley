package telemarketer.skittlealley.model.game.drawguess;

/**
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
public enum DrawCode {
    USER_JOIN(10), // 用户进入 用户信息
    USER_READY(11), // 用户准备 用户信息
    USER_LEFT(12), // 用户离开 用户信息
    USER_CHANGE_NAME(13), // 用户离开 用户信息
    GAME_UPDATE(20), // 游戏内容更新 context
    DRAW_MOVE(1), // 移动
    DRAW_CLEAR(2), // 清除画板
    DRAW_CHANGE_BRUSH(3), // 改变画笔
    DRAW_MSG(4); // 信息

    private final int code;

    DrawCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
