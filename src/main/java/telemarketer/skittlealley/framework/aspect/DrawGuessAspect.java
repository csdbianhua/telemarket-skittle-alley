package telemarketer.skittlealley.framework.aspect;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import telemarketer.skittlealley.model.ApiResponse;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;
import telemarketer.skittlealley.model.game.drawguess.DrawGameStatus;
import telemarketer.skittlealley.model.game.drawguess.DrawGuessContext;
import telemarketer.skittlealley.model.game.drawguess.DrawPlayerInfo;
import telemarketer.skittlealley.service.game.DrawGuess;
import telemarketer.skittlealley.web.websocket.DrawGuessWebSocket;

/**
 * 你画我猜游戏过程控制
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/9
 */
@Aspect
public class DrawGuessAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(DrawGuessAspect.class);
    private DrawGuess service;
    private DrawGuessWebSocket webSocket;

    public void setService(DrawGuess service) {
        this.service = service;
    }

    public void setWebSocket(DrawGuessWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    /**
     * 判断玩家准备是否要开始游戏
     *
     * @param point 切点
     */
    @After(value = "execution(* telemarketer.skittlealley.model.game.drawguess.DrawGuessContext.addPlayer(..))",
            argNames = "point")
    public void checkStart(JoinPoint point) {
        DrawGuessContext ctx = (DrawGuessContext) point.getTarget();
        service.checkStart(ctx);
    }

    /**
     * 判断玩家退出是否终止游戏
     *
     * @param point 切点
     * @param info  退出玩家信息
     */
    @AfterReturning(value = "execution(* telemarketer.skittlealley.model.game.drawguess.DrawGuessContext.removePlayer(..)) ",
            returning = "info", argNames = "point,info")
    public void checkStop(JoinPoint point, DrawPlayerInfo info) {
        DrawGuessContext ctx = (DrawGuessContext) point.getTarget();
        if (info == null) { // 并不在准备玩家中
            // 如果需要所有未准备玩家退出时开始游戏 可进行checkStart
            return;
        }
        service.checkStop(ctx);
    }

    /**
     * 判断玩家答对是否要停止这轮游戏
     *
     * @param point  切点
     * @param number 答对人数
     */
    @AfterReturning(value = "execution(* telemarketer.skittlealley.model.game.drawguess.DrawGuessContext.incrRightNumber(..)) ",
            returning = "number", argNames = "point,number")
    public void checkPause(JoinPoint point, Integer number) {
        DrawGuessContext ctx = (DrawGuessContext) point.getTarget();
        service.checkPause(ctx, number);
    }

    /**
     * 迁移状态过程锁定
     *
     * @param pjp 切点
     * @param ctx 上下文
     * @return 原返回值
     */
    @Around(value = "execution(* telemarketer.skittlealley.service.game.DrawGuess.process*(..))  && args(ctx)",
            argNames = "pjp,ctx")
    public Object lockContext(ProceedingJoinPoint pjp, DrawGuessContext ctx) {
        ctx.lock();
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            LOGGER.error("你画我猜状态迁移异常,即将恢复初始状态,ctx:{}", JSONObject.toJSONString(ctx), e);
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            if (StringUtils.equals(signature.getName(), "processToReady")) {
                LOGGER.error("严重错误，游戏无法恢复初始状态。即将退出");
                throw new IllegalStateException(e);
            }
            return service.processToReady(ctx);
        } finally {
            ctx.unlock();
        }
    }

    /**
     * 迁移状态变更通知玩家
     *
     * @param ctx 上下文
     */
    @AfterReturning(value = "execution(* telemarketer.skittlealley.service.game.DrawGuess.process*(..))", returning = "ctx",
            argNames = "ctx")
    public void notifyWebSocket(DrawGuessContext ctx) {
        if (ctx.status() == DrawGameStatus.RUN) {
            notifyRunStatus(ctx);
        } else {
            notifyOtherStatus(ctx);
        }

    }

    private void notifyOtherStatus(DrawGuessContext ctx) {
        ApiResponse response = service.borrowObject();
        try {
            response.setCode(DrawCode.GAME_UPDATE.getCode()).setData(ctx);
            TextMessage textMessage = webSocket.transformToMsg(response);
            webSocket.broadcast(textMessage, null);
        } finally {
            service.returnObject(response);
        }
    }

    private void notifyRunStatus(DrawGuessContext ctx) {
        ApiResponse response = service.borrowObject();
        try {
            String currentUser = ctx.getCurrentUser();
            JSONObject content = (JSONObject) JSONObject.toJSON(ctx);
            response.setCode(DrawCode.GAME_UPDATE.getCode()).setData(content);
            webSocket.sendTo(webSocket.transformToMsg(response.setData(content)), currentUser);
            content.getJSONObject("currentWord").remove("word"); // 广播消息去除敏感信息
            TextMessage textMessage = webSocket.transformToMsg(response);
            webSocket.broadcast(textMessage, currentUser);
        } finally {
            service.returnObject(response);
        }
    }

}
