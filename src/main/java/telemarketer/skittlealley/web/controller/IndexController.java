package telemarketer.skittlealley.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hason
 * @version 17-9-7
 */
@Controller
@RequestMapping
public class IndexController {

    @RequestMapping
    public String index() {
        return "redirect:/games";
    }

}
