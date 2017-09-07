package telemarketer.skittlealley.model;

/**
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
public enum TaskResponseCode {
    MSG_ERROR(-1),
    MSG_NEW_RUNNING(0),
    MSG_UPDATE_PROPERTY(1),
    MSG_DELETE_TASK(2),
    MSG_ALL_TASK(3);

    private final int code;

    TaskResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
