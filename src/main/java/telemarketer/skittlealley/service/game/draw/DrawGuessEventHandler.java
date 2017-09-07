package telemarketer.skittlealley.service.game.draw;

import org.springframework.stereotype.Service;
import telemarketer.skittlealley.model.game.drawguess.DrawCode;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Author: Hanson
 * Time: 17-2-6
 * Email: imyijie@outlook.com
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Service
public @interface DrawGuessEventHandler {
    DrawCode[] value();
}
