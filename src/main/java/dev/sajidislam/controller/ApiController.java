package dev.sajidislam.controller;

import dev.sajidislam.util.*;
import java.util.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    //localhost:8080/getRoute?busStopOrigin=7851&busStopDest=3052&time=09:45:00&date=20250715&weekDayType=WEEKDAY
    @GetMapping("/getRoute")
    public List<BusStopWeb> getRoute(@RequestParam String busStopOrigin, @RequestParam String busStopDest, @RequestParam String time, @RequestParam int date, @RequestParam String weekDayType){
        return Main.runProgram(busStopOrigin, busStopDest, time, date, weekDayType);
    }
}
