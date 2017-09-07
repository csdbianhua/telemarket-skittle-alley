package telemarketer.skittlealley.model.game.drawguess;

/**
 * 游戏状态
 * <p>
 * Author: Hanson
 * Time: 17-2-8
 * Email: imyijie@outlook.com
 */
public enum DrawGameStatus {
    READY(0, -1),  // 准备状态
    RUN(1, 60000),  // 进行状态
    WAIT_SHOW(2, 10000), // 记分板状态
    END(3, 10000); // 最终记分板状态

    private final int code;
    private final int spendTime; // 持续时间 -1为无限长

    DrawGameStatus(int code, int spendTime) {
        this.code = code;
        this.spendTime = spendTime;
    }

    public int getSpendTime() {
        return spendTime;
    }

    public int getCode() {
        return code;
    }
}
