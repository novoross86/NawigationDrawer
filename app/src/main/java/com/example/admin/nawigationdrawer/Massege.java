package com.example.admin.nawigationdrawer;

public class Massege {

    private String name;
    private String msg;
    private String image;



    public Massege(String name, String msg, String image){
        this.name = name;
        this.msg = msg;
        this.image = image;

    }

    public Massege(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
