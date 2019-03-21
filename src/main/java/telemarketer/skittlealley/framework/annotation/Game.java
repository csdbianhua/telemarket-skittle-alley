package telemarketer.skittlealley.framework.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于标注游戏服务
 * <p>
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Service
public @interface Game {

    /**
     * url及模板文件名
     */
    String value();

    /**
     * 游戏中文名称
     */
    String gameName();

    /**
     * 需要带入模版的属性 JSON格式
     */
    String attrs() default "{}";
}
