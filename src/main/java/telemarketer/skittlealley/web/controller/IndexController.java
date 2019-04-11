package telemarketer.skittlealley.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import telemarketer.skittlealley.persist.tables.pojos.DrawWord;
import telemarketer.skittlealley.service.game.DrawGuess;

/**
 * @author hason
 * @version 17-9-7
 */
@Controller
@RequestMapping("/")
public class IndexController {
    private final DrawGuess drawGuess;

    public IndexController(DrawGuess drawGuess) {
        this.drawGuess = drawGuess;
    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @PostMapping("/games/draw_guess/word_submit")
    public Mono<String> drawGuessWordSubmitPost(DrawWord drawWord, Model model) {
        return Mono.fromCompletionStage(drawGuess.saveWord(drawWord)).map(i -> {
            model.addAttribute("tip", "新增成功");
            return "others/draw_guess_word_submit";
        }).onErrorResume(RuntimeException.class, e -> {
            model.addAttribute("tip", "失败");
            return Mono.just("others/draw_guess_word_submit");
        });

    }

}
