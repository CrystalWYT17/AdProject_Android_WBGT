package iss.ca.wbgt;

public class ForecastDay {
    private String day;
    private String highValue;
    private String lowValue;

    public ForecastDay(String day, String highValue, String lowValue){
        this.day = day;
        this.highValue = highValue;
        this.lowValue = lowValue;
    }

    public String getDay(){
        return this.day;
    }

    public String getHighValue(){
        return this.highValue;
    }

    public String getLowValue(){
        return this.lowValue;
    }
}
