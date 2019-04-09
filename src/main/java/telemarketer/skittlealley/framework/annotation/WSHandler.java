package telemarketer.skittlealley.framework.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * WebSocketHandler
 * <p>
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/6
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Service
public @interface WSHandler {
    String value();
}
