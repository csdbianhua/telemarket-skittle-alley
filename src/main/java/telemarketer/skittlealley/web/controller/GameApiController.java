package telemarketer.skittlealley.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import telemarketer.skittlealley.persist.tables.pojos.DrawWord;
import telemarketer.skittlealley.service.game.DrawGuess;


@RestController
@RequestMapping("/api/games/")

public class GameApiController {
    private static final Logger log = LoggerFactory.getLogger(GameApiController.class);
    private final DrawGuess drawGuess;

    public GameApiController(DrawGuess drawGuess) {
        this.drawGuess = drawGuess;
    }

    @PostMapping("/draw_guess/word_submit")
    public Mono<String> drawGuessWordSubmitPost(DrawWord drawWord) {
        return Mono.fromCompletionStage(drawGuess.saveWord(drawWord))
                .map(i -> "新增成功")
                .onErrorResume(RuntimeException.class, e -> {
                    log.error("[你画我猜]提交词汇失败", e);
                    return Mono.just("失败");
                });

    }

}
