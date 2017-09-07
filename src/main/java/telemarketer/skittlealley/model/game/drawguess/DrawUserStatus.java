package telemarketer.skittlealley.model.game.drawguess;

/**
 * Be careful.
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/9
 */
public enum DrawUserStatus {
    WAIT(0), READY(1), DRAW(2), GUESS(3), RIGHT(4);

    private final int code;

    DrawUserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
