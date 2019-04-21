package telemarketer.skittlealley.model.game.noonesurvived;

import telemarketer.skittlealley.model.GameCtx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NosContext implements GameCtx {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, NosItem> itemMap = new ConcurrentHashMap<>();

    public NosPlayer newPlayer() {
        NosPlayer player = new NosPlayer(idGenerator.getAndIncrement());
        player.x = 200;
        player.y = 200;
        itemMap.put(player.getId(), player);
        return player;
    }

    public NosZombie newZombie() {
        NosZombie zombie = new NosZombie(idGenerator.getAndIncrement());
        itemMap.put(zombie.getId(), zombie);
        return zombie;
    }

}
