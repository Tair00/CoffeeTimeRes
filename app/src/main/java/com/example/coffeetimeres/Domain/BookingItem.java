package com.example.coffeetimeres.Domain;

public  class BookingItem {


    private String time;
    private String name;
    private String status;

    private Integer id;







    public void setTime(String time) {
        this.time = time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BookingItem(String status, String time, String name) {
        this.time = time;
        this.name = name;
        this.status = status;

    }

    public BookingItem( String status, String time, String name, Integer id) {
        this.id = id;
        this.time = time;
        this.name = name;
        this.status=status;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }
}