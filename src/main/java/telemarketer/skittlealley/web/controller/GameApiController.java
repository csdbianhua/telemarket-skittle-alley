package telemarketer.skittlealley.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import telemarketer.skittlealley.model.game.drawguess.DrawWord;
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
    public String drawGuessWordSubmitPost(DrawWord drawWord) {
        if (StringUtils.isAnyBlank(drawWord.getWord(), drawWord.getWordTip())) {
            return "不能有空的";
        }
        drawGuess.saveWord(drawWord);
        return "新增成功";
    }

}
