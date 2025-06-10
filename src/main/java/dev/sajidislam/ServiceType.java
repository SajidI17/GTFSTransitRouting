package dev.sajidislam;

public class ServiceType {
    boolean isMon = false;
    boolean isTue = false;
    boolean isWed = false;
    boolean isThur = false;
    boolean isFri = false;
    boolean isSat = false;
    boolean isSun = false;
    int date = 0;

    public ServiceType(boolean isMon, boolean isTue, boolean isWed, boolean isThur, boolean isFri, boolean isSat, boolean isSun, int date) {
        this.isMon = isMon;
        this.isTue = isTue;
        this.isWed = isWed;
        this.isThur = isThur;
        this.isFri = isFri;
        this.isSat = isSat;
        this.isSun = isSun;
        this.date = date;
    }

    public boolean isWeekday(int weekday){
        return switch (weekday) {
            case 1 -> isTue;
            case 2 -> isWed;
            case 3 -> isThur;
            case 4 -> isFri;
            case 5 -> isSat;
            case 6 -> isSun;
            default -> isMon;
        };
    }
}
