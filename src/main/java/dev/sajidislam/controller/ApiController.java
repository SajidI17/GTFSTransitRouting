package dev.sajidislam.controller;

import dev.sajidislam.util.*;
import java.util.*;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class ApiController {
    private final List<Integer> availableDates = Main.getAvailableDates();

    //localhost:8080/getRoute?busStopOrigin=7851&busStopDest=3052&time=09:45:00&date=20250715&weekDayType=WEEKDAY
    @GetMapping("/getRoute")
    public List<BusStopWeb> getRoute(@RequestParam String busStopOrigin, @RequestParam String busStopDest, @RequestParam String time, @RequestParam int date){
        return Main.runProgram(busStopOrigin, busStopDest, time, date);
    }

    @GetMapping("/getDates")
    public List<Integer> getDates(){
        return availableDates;
    }

    @GetMapping("/apitest")
    public List<BusStopWeb> apitest(@RequestParam String busStopOrigin, @RequestParam String busStopDest, @RequestParam String time, @RequestParam int date){
        List<BusStopWeb> busStopWebList = Arrays.asList(
                new BusStopWeb("10737","Walking","Walking","10:54:00","10738",45.384998,-75.696465),
                new BusStopWeb("10738","23078090","10","10:52:00","10146",45.385485,-75.694867),
                new BusStopWeb("10146","23078090","10","10:51:37","4313",45.384071,-75.693941),
                new BusStopWeb("4313","23078090","10","10:49:12","10752",45.379857,-75.685367),
                new BusStopWeb("10752","23078090","10","10:47:54",null,45.377668,-75.682039),
                new BusStopWeb("10752","Walking","Walking","10:44:21","10750",45.377668,-75.682039),
                new BusStopWeb("10750","14462090","90","10:40:21","795",45.377396,-75.685391),
                new BusStopWeb("795","14462090","90","10:39:20","8418",45.378958,-75.680653),
                new BusStopWeb("8418","14462090","90","10:38:39","9490",45.382702,-75.67927),
                new BusStopWeb("9490","14462090","90","10:38:00","104",45.38485,-75.676439),
                new BusStopWeb("104","14462090","90","10:36:42","103",45.392675,-75.669518),
                new BusStopWeb("103","14462090","90","10:35:53","102",45.39673,-75.669454),
                new BusStopWeb("102","14462090","90","10:35:14","725",45.401402,-75.66669),
                new BusStopWeb("725","14462090","90","10:34:21","9954",45.406607,-75.664385),
                new BusStopWeb("9954","14462090","90","10:33:00",null,45.412035,-75.666702),
                new BusStopWeb("9954","Walking","Walking","10:32:00","AF990",45.412035,-75.666702),
                new BusStopWeb("AF990","99815558","1-350","10:30:00","AE990",45.412316,-75.6648),
                new BusStopWeb("AE990","99815558","1-350","10:27:00","EB990",45.416554,-75.654048),
                new BusStopWeb("EB990","99815558","1-350","10:24:00","EC990",45.42062,-75.637847),
                new BusStopWeb("EC990","99815558","1-350","10:22:00","EE990",45.422603,-75.626354),
                new BusStopWeb("EE990","99815558","1-350","10:20:00",null,45.430798,-75.608693),
                new BusStopWeb("EE990","Walking","Walking","10:19:00","9872",45.430798,-75.608693),
                new BusStopWeb("9872","31815090","25","10:18:00","1334",45.430942,-75.608977),
                new BusStopWeb("1334","31815090","25","10:16:23","1738",45.427675,-75.600855),
                new BusStopWeb("1738","31815090","25","10:15:48","1737",45.425513,-75.599035),
                new BusStopWeb("1737","31815090","25","10:15:23","1736",45.422955,-75.597723),
                new BusStopWeb("1736","31815090","25","10:15:02","1735",45.420928,-75.596452),
                new BusStopWeb("1735","31815090","25","10:14:45","317",45.419878,-75.595903),
                new BusStopWeb("317","31815090","25","10:13:22","316",45.42286,-75.58697),
                new BusStopWeb("316","31815090","25","10:12:38","314",45.425053,-75.581383),
                new BusStopWeb("314","31815090","25","10:11:11","313",45.428152,-75.573468),
                new BusStopWeb("313","31815090","25","10:10:32","7497",45.430433,-75.56777),
                new BusStopWeb("7497","31815090","25","10:10:15","311",45.431467,-75.565243),
                new BusStopWeb("311","31815090","25","10:09:59","310",45.432412,-75.562898),
                new BusStopWeb("310","31815090","25","10:09:38","309",45.433485,-75.560212),
                new BusStopWeb("309","31815090","25","10:09:24","308",45.43443,-75.55782),
                new BusStopWeb("308","31815090","25","10:09:00","307",45.435702,-75.554467),
                new BusStopWeb("307","31815090","25","10:08:22","305",45.437108,-75.551097),
                new BusStopWeb("305","31815090","25","10:07:38","304",45.438682,-75.547043),
                new BusStopWeb("304","31815090","25","10:07:04","1729",45.439898,-75.543897),
                new BusStopWeb("1729","31815090","25","10:05:39","4230",45.442187,-75.538595),
                new BusStopWeb("4230","31815090","25","10:04:23","4229",45.445017,-75.531443),
                new BusStopWeb("4229","31815090","25","10:03:47","4228",45.446542,-75.5277),
                new BusStopWeb("4228","31815090","25","10:02:41","4226",45.448625,-75.522622),
                new BusStopWeb("4226","31815090","25","10:01:22","1688",45.451263,-75.515938),
                new BusStopWeb("1688","31815090","25","10:00:40","1687",45.452742,-75.512138),
                new BusStopWeb("1687","31815090","25","10:00:00","1686",45.45433,-75.508395),
                new BusStopWeb("1686","31815090","25","09:59:40","1767",45.455515,-75.505733),
                new BusStopWeb("1767","31815090","25","09:59:03","1766",45.456747,-75.501347),
                new BusStopWeb("1766","31815090","25","09:58:43","4627",45.457327,-75.49909),
                new BusStopWeb("4627","31815090","25","09:58:22","4626",45.457947,-75.497142),
                new BusStopWeb("4626","31815090","25","09:57:49","4625",45.459195,-75.494067),
                new BusStopWeb("4625","31815090","25","09:57:07","5746",45.460657,-75.488908),
                new BusStopWeb("5746","31815090","25","09:56:27","5745",45.46188,-75.484832),
                new BusStopWeb("5745","31815090","25","09:55:59","5769",45.462692,-75.482353),
                new BusStopWeb("5769","31815090","25","09:55:13",null,45.464257,-75.477115)
        );
        return busStopWebList;
    }
}
