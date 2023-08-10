package iss.ca.wbgt;

public class UserCurrentData {

    private String stationName;
    private String wbgtValue;
    //added by mthn
    private String stationId;

    public UserCurrentData(){

    }

    public UserCurrentData(String stationId, String stationName, String wbgtValue){
        this.stationId = stationId;
        this.stationName = stationName;
        this.wbgtValue = wbgtValue;
    }

    public String getStationName() {
        return stationName;
    }
    public String getStationId(){return stationId;}

    public String getWbgtValue() {
        return wbgtValue;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public void setWbgtValue(String wbgtValue) {
        this.wbgtValue = wbgtValue;
    }
    public void setStationId(String stationId){this.stationId = stationId;}
}
