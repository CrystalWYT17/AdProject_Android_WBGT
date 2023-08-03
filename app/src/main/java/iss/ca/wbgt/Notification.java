package iss.ca.wbgt;

public class Notification {
    private String title;
    private String message;
    private String time;
    private final int drawable;

    public Notification(){
        this.drawable = R.drawable.right;
    }

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

    public void setTitle(String title){
        this.title = title;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public void setTime(String time){
        this.time = time;
    }
}
