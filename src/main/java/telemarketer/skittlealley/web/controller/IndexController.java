package telemarketer.skittlealley.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.RedirectView;

/**
 * @author hason
 * @version 17-9-7
 */
@Controller
@RequestMapping("/")
public class IndexController {


    @RequestMapping("/")
    public RedirectView index() {
        return new RedirectView("/index.html", HttpStatus.MOVED_PERMANENTLY);
    }



}
