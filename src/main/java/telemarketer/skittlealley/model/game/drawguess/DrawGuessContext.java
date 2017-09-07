package telemarketer.skittlealley.model.game.drawguess;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 你画我猜上下文对象
 * <p>
 * Author: Hanson
 * Time: 17-2-8
 * Email: imyijie@outlook.com
 */
public class DrawGuessContext {
    public static final int DEFAULT_WIDTH = 9;
    public static final String DEFAULT_COLOR = "blue";
    private String currentUser;
    private DrawGameStatus status;
    private DrawWord currentWord;
    private Long endTime;
    private Map<String, DrawPlayerInfo> players = Collections.synchronizedMap(new LinkedHashMap<>());
    private Integer width = DEFAULT_WIDTH;
    private String color = DEFAULT_COLOR;
    @JSONField(serialize = false, deserialize = false)
    private final Timer timer = new Timer();
    @JSONField(serialize = false, deserialize = false)
    private TimerTask timerTask;
    @JSONField(serialize = false, deserialize = false)
    private final ReentrantLock lock = new ReentrantLock(true); // 锁
    private final AtomicInteger rightNumber = new AtomicInteger(0);
    @JSONField(serialize = false, deserialize = false)
    private final AtomicInteger drawNumber = new AtomicInteger(0);
    @JSONField(serialize = false, deserialize = false)
    private final AtomicInteger roomPeopleNumber = new AtomicInteger(0);

    public int getRoomPeopleNumber() {
        return roomPeopleNumber.get();
    }

    public int incrRoomPeopleNumber() {
        return roomPeopleNumber.incrementAndGet();
    }

    public int decrRoomPeopleNumber() {
        return roomPeopleNumber.decrementAndGet();
    }

    public int getDrawNumber() {
        return drawNumber.get();
    }

    public int beforeIncrDrawNumber() {
        return drawNumber.getAndIncrement();
    }

    public int getRightNumber() {
        return rightNumber.get();
    }

    public int incrRightNumber() {
        players.get(currentUser).incrScore();
        return rightNumber.incrementAndGet();
    }


    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isCurrentUser(String id) {
        return StringUtils.equals(id, currentUser);
    }

    public boolean isCurrentWord(String word) {
        return StringUtils.equals(word, currentWord.getWord());
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Collection<DrawPlayerInfo> getPlayers() {
        return players.values();
    }

    public Map<String, DrawPlayerInfo> players() {
        return players;
    }

    public void addPlayer(DrawPlayerInfo player) {
        players.put(player.getId(), player);
    }

    public DrawPlayerInfo removePlayer(String id) {
        return players.remove(id);
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public DrawGuessContext setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
        getPlayers().forEach(u -> u.setStatus(DrawUserStatus.GUESS));
        players.get(currentUser).setStatus(DrawUserStatus.DRAW);
        return this;
    }

    public int getStatus() {
        return status.getCode();
    }

    public DrawGameStatus status() {
        return status;
    }

    public DrawGuessContext setStatus(DrawGameStatus status) {
        this.status = status;
        return this;
    }

    public DrawWord getCurrentWord() {
        return currentWord;
    }

    public DrawGuessContext setCurrentWord(DrawWord currentWord) {
        this.currentWord = currentWord;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public DrawGuessContext setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public void startTimerTask(TimerTask timerTask, long time) {
        timer.schedule(timerTask, new Date(time));
        this.timerTask = timerTask;
    }

    public void cancelTimerTask() {
        if (this.timerTask != null) {
            timerTask.cancel(); // 取消任务(正在执行不影响)
            timerTask = null; // 扔给垃圾回收
        }
    }

    public DrawGuessContext clearGameInfo() {
        rightNumber.set(0);
        width = DEFAULT_WIDTH;
        color = DEFAULT_COLOR;
        currentWord = null;
        currentUser = null;
        return this;
    }

    public DrawGuessContext clearAll() {
        cancelTimerTask();
        status = DrawGameStatus.READY;
        endTime = null;
        drawNumber.set(0);
        getPlayers().forEach(DrawPlayerInfo::clearScore);
        players = Collections.synchronizedMap(new LinkedHashMap<>());
        clearGameInfo();
        return this;
    }

}
