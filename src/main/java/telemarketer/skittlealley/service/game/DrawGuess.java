package telemarketer.skittlealley.service.game;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.socket.WebSocketSession;
import telemarketer.skittlealley.framework.annotation.Game;
import telemarketer.skittlealley.model.ApiRequest;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.*;
import telemarketer.skittlealley.service.common.MessageHandler;
import telemarketer.skittlealley.service.common.RequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 你画我猜游戏
 * <p>
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Game(value = DrawGuess.IDENTIFY, gameName = "你画我猜",
        attrs = "{DEFAULT_WIDTH:" + DrawGuessContext.DEFAULT_WIDTH + "," +
                "DEFAULT_COLOR:\"" + DrawGuessContext.DEFAULT_COLOR + "\"}")
public class DrawGuess extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DrawGuess.class);

    public static final String IDENTIFY = "draw_guess";
    private final AtomicReference<DrawGuessContext> ctx = new AtomicReference<>(null);
    private List<DrawWord> wordList;

    public DrawGuess(List<RequestHandler> handlers) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource("/data/draw_word.txt").getInputStream()));
        String line;
        List<DrawWord> list = new LinkedList<>();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            DrawWord dw = new DrawWord();
            dw.setWord(parts[0]);
            dw.setWordTip(parts[1]);
            list.add(dw);
        }
        wordList = new CopyOnWriteArrayList<>(list);
        HashMap<Integer, RequestHandler> handlerMap = new HashMap<>();
        for (RequestHandler handler : handlers) {
            for (DrawCode drawCode : handler.supported()) {
                handlerMap.put(drawCode.getCode(), handler);
            }
        }
        setRequestHandlers(handlerMap);
    }

    /**
     * 处理用户连接事件 设置上下文和玩家信息
     *
     * @param session 会话
     * @return 上下文
     */
    public DrawGuessContext connected(WebSocketSession session) {
        if (ctx.get() == null) {
            ctx.compareAndSet(null, new DrawGuessContext().setStatus(DrawGameStatus.READY));
        }
        DrawGuessContext context = this.ctx.get();
        Map<String, Object> attributes = session.getAttributes();
        DrawPlayerInfo info = new DrawPlayerInfo();
        info.setName(generateUserName());
        info.setId(session.getId());
        attributes.put("ctx", context);
        attributes.put("info", info);
        context.incrRoomPeopleNumber();
        return context;
    }

    /**
     * 处理用户离开
     *
     * @param session 会话
     */
    public DrawGuessContext closed(WebSocketSession session) {
        DrawGuessContext context = this.ctx.get();
        context.removePlayer(session.getId());
        context.decrRoomPeopleNumber();
        return context;
    }

    private DrawWordInfo randomWord() {
        int select = RandomUtils.nextInt(0, wordList.size());
        DrawWord drawWord = wordList.get(select);
        DrawWordInfo info = new DrawWordInfo();
        info.setWord(drawWord.getWord());
        info.setWordType(drawWord.getWordTip());
        info.setWordCount(drawWord.getWord().length());
        return info;
    }


    /**
     * 处理web socket请求
     *
     * @param payload 请求内容
     * @param session 会话
     * @return 处理结果字符串
     */
    @Override
    public String handleRequest(String payload, WebSocketSession session) {
        ApiRequest request = JSONObject.parseObject(payload, ApiRequest.class);
        ApiResponse response = new ApiResponse();
        try {
            handle(request, response, session);
            return response.empty() ? StringUtils.EMPTY : JSONObject.toJSONString(response);
        } catch (RuntimeException e) {
            LOGGER.error("[{}]处理请求异常", IDENTIFY, e);
            return StringUtils.EMPTY;
        }

    }

    private String generateUserName() {
        long epochSecond = Instant.now().getEpochSecond();
        String s = String.valueOf(epochSecond);
        return RandomStringUtils.randomAlphabetic(4) + "_" + s.substring(s.length() - 4);
    }

    /**
     * 迁移至运行状态
     *
     * @param ctx 上下文
     * @return 修改后的上下文
     */
    public DrawGuessContext processToRun(DrawGuessContext ctx) {
        ctx.clearGameInfo();
        ctx.setStatus(DrawGameStatus.RUN);
        Iterator<String> iterator = ctx.players().keySet().iterator();
        int drawNumber = ctx.beforeIncrDrawNumber();
        for (int i = 0; i < drawNumber; i++) {
            iterator.next();
        }
        String currentUser = iterator.next();
        ctx.setCurrentUser(currentUser);
        long endTime = System.currentTimeMillis() + DrawGameStatus.RUN.getSpendTime();
        ctx.setEndTime(endTime);
        ctx.setCurrentWord(randomWord());
        Runnable timerTask = () -> processToWait(ctx);
        ctx.startTimerTask(timerTask, endTime);
        return ctx;
    }

    /**
     * 处理游戏等待状态
     *
     * @param ctx 上下文
     * @return 修改后的上下文
     */
    public DrawGuessContext processToWait(DrawGuessContext ctx) {
        ctx.setStatus(DrawGameStatus.WAIT_SHOW);
        int spendTime = DrawGameStatus.WAIT_SHOW.getSpendTime();
        long endTime = System.currentTimeMillis() + spendTime;
        ctx.setEndTime(endTime);
        int size = ctx.players().size();
        if (ctx.getDrawNumber() < size) {
            ctx.startTimerTask(() -> processToRun(ctx), endTime);
        } else {
            ctx.startTimerTask(() -> processToEnd(ctx), endTime);
        }
        return ctx;
    }

    /**
     * 最终计分板状态
     *
     * @param ctx 上下文
     * @return 修改后的上下文
     */
    public DrawGuessContext processToEnd(DrawGuessContext ctx) {
        ctx.setStatus(DrawGameStatus.END);
        ctx.clearGameInfo();
        int spendTime = DrawGameStatus.END.getSpendTime();
        long endTime = System.currentTimeMillis() + spendTime;
        ctx.setEndTime(endTime);
        ctx.startTimerTask(() -> processToReady(ctx), endTime);
        return ctx;
    }

    /**
     * 恢复初始状态 清楚所有分数游戏信息
     *
     * @param ctx 上下文
     * @return 上下文
     */
    public DrawGuessContext processToReady(DrawGuessContext ctx) {
        ctx.clearAll();
        return ctx;
    }

    /**
     * 判断玩家准备是否要开始游戏(游戏为准备状态，且玩家数大于1)
     *
     * @param ctx 上下文
     * @return 是否已处理开始
     */
    public boolean checkStart(DrawGuessContext ctx) {
        int readySize = ctx.players().size();
        if (readySize <= 1 || ctx.status() != DrawGameStatus.READY || ctx.getRoomPeopleNumber() != readySize) {
            return false;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[DrawGuess] check to start success");
        }
        processToRun(ctx);
        return true;
    }

    /**
     * 判断玩家离开是否要终止游戏(游戏正在进行且是游戏中玩家退出)
     *
     * @param ctx 上下文
     * @return 是否处理退出
     */
    public boolean checkStop(DrawGuessContext ctx) {
        if (ctx.status() != DrawGameStatus.READY) {
            processToReady(ctx);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断玩家答对是否停止这轮游戏
     *
     * @param ctx    上下文
     * @param number 答对人数
     * @return 是否处理了停止
     */
    public boolean checkPause(DrawGuessContext ctx, int number) {
        if (ctx.players().size() == number + 1) {
            processToWait(ctx);
            return true;
        } else {
            return false;
        }
    }

    public void saveWord(DrawWord word) {
        wordList.add(word);
    }
}
