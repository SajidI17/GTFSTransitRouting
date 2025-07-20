package dev.sajidislam;

import java.util.ArrayList;
import java.util.List;

public class ReturnDoubleIds {
    List<String> stopIdList;
    List<String> tripIdList;

    public ReturnDoubleIds(List<String> stopIdList, List<String> tripIdList) {
        this.stopIdList = stopIdList;
        this.tripIdList = tripIdList;
    }

    public ReturnDoubleIds(){
        this.stopIdList = new ArrayList<>();
        this.tripIdList = new ArrayList<>();
    }
}
