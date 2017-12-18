package fs.playground;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Ctrl {

    @GetMapping("/login")
    public String login(@RequestParam(name = "error", required = false) Boolean error, Model model) {
        model.addAttribute("error", error);
        return "login";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
