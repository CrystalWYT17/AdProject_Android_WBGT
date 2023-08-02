package iss.ca.wbgt;

public class Notification {
    private String title;
    private String message;
    private String time;
    private int drawable;

    public Notification(String title, String message, String time){
        this.title = title;
        this.message = message;
        this.time = time;
        this.drawable = R.drawable.right;
    }

    public String getTitle(){
        return this.title;
    }
    public String getMessage(){
        return this.message;
    }
    public String getTime(){
        return this.time;
    }
    public int getDrawable(){
        return this.drawable;
    }
}
