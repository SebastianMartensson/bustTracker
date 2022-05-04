package backend.bustracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class RouteController {

    @GetMapping("/busLines")
    public String topTenBuses(){
        DataController dc = new DataController();
        return dc.getBusLines();
    }
}
