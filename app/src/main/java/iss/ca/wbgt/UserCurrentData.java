package iss.ca.wbgt;

public class UserCurrentData {

    private String stationName;
    private String wbgtValue;

    public UserCurrentData(){

    }

    public UserCurrentData(String stationName, String wbgtValue){
        this.stationName = stationName;
        this.wbgtValue = wbgtValue;
    }

    public String getStationName() {
        return stationName;
    }

    public String getWbgtValue() {
        return wbgtValue;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public void setWbgtValue(String wbgtValue) {
        this.wbgtValue = wbgtValue;
    }
}
