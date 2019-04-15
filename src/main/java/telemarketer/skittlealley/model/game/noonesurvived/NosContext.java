package telemarketer.skittlealley.model.game.noonesurvived;

import telemarketer.skittlealley.model.GameCtx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NosContext implements GameCtx {

    private final AtomicLong idGenerator = new AtomicLong(0);

    private Map<Long, NosItem> itemMap = new ConcurrentHashMap<>();

    public NosPlayer newPlayer() {
        NosPlayer player = new NosPlayer(idGenerator.getAndIncrement());
        itemMap.put(player.getId(), player);
        return player;
    }

}
