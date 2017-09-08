package telemarketer.skittlealley.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import telemarketer.skittlealley.model.game.GameInfo;
import telemarketer.skittlealley.service.GameService;

import java.util.Optional;

/**
 * Author: Hanson
 * Time: 17-2-5
 * Email: imyijie@outlook.com
 */
@Controller
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @RequestMapping("")
    public String games(Model model) {
        model.addAttribute("games", gameService.getGames());
        return "games";
    }

    @RequestMapping("/{identify}")
    public String game(@PathVariable("identify") String identify, Model model) {
        Optional<GameInfo> info = gameService.findInfo(identify);
        if (!info.isPresent()) {
            return "redirect:/games";
        }
        model.addAllAttributes(info.get().getAttrs());
        return "games/" + identify;
    }
}
