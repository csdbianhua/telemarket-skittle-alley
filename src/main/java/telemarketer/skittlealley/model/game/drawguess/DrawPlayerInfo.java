package telemarketer.skittlealley.model.game.drawguess;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 你画我猜用户信息
 * <p>
 * Author: Hanson
 * Time: 17-2-8
 * Email: imyijie@outlook.com
 */
public class DrawPlayerInfo {

    private String id;
    private String name;
    private DrawUserStatus status = DrawUserStatus.WAIT;
    private AtomicInteger score = new AtomicInteger(0);

    public int getScore() {
        return score.get();
    }

    public int incrScore() {
        return score.incrementAndGet();
    }

    public void clearScore() {
        this.status = DrawUserStatus.WAIT;
        this.score.set(0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status.getCode();
    }

    public DrawUserStatus status() {
        return status;
    }

    public void setStatus(DrawUserStatus status) {
        this.status = status;
    }
}
