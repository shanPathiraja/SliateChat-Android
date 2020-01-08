package com.shancreation.sliatechat.Model;

public class Messages {
    private String Message ,Type,From;
    private Long Time;
    private boolean Seen;




    public Messages(String message, boolean seen, Long time, String type, String from) {
       this.Message = message;
        this.Seen = seen;
        this.Time = time;
        this.Type = type;
        this.From =from;

    }
    public  Messages(){}

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public boolean getSeen() {
        return Seen;
    }

    public void setSeen(boolean seen) {
        Seen = seen;
    }

    public Long getTime() {
        return Time;
    }

    public void setTime(Long time) {
        Time = time;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }



}
