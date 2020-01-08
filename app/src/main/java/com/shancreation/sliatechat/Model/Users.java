package com.shancreation.sliatechat.Model;

public class Users {
    public String name;
    public String image;
    public String status;
    public String thumb_Image;

    public Users(){}

    public Users( String image,String name, String status,String thumb_Image) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_Image =thumb_Image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_Image() {
        return thumb_Image;
    }

    public void setThumb_Image(String thumb_Image) {
        this.thumb_Image = thumb_Image;
    }
}
