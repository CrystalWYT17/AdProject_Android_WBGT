package iss.ca.wbgt;

public class Station {

    private String id;
    private double longitude;
    private double latitude;

    public Station(String id,double longitude, double latitude){
        this.latitude=latitude;
        this.longitude=longitude;
        this.id=id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }
}
