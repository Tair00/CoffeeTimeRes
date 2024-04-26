package com.example.coffeetimeres.Domain;

public class BookingItem {
    private String status;
    private String userName;
    private String cafeName;
    private String pickUpTime;
    private String coffeeName;
    private String coffeeDescription;
    private String coffeeImage;
    private int bookingId;
    private int cafeId;
    private int coffeeId;

    public String getYour_smartphone_key_here() {
        return your_smartphone_key_here;
    }

    public void setYour_smartphone_key_here(String your_smartphone_key_here) {
        this.your_smartphone_key_here = your_smartphone_key_here;
    }

    private String your_smartphone_key_here;
    public BookingItem(String status, String userName, String cafeName, String pickUpTime, String coffeeName, String coffeeDescription,
                       String coffeeImage, int bookingId, int cafeId, int coffeeId,String your_smartphone_key_here) {
        this.status = status;
        this.userName = userName;
        this.cafeName = cafeName;
        this.pickUpTime = pickUpTime;
        this.coffeeName = coffeeName;
        this.coffeeDescription = coffeeDescription;
        this.coffeeImage = coffeeImage;
        this.bookingId = bookingId;
        this.cafeId = cafeId;
        this.coffeeId = coffeeId;
        this.your_smartphone_key_here = your_smartphone_key_here;
    }

    // Геттеры и сеттеры для всех полей
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCafeName() {
        return cafeName;
    }

    public void setCafeName(String cafeName) {
        this.cafeName = cafeName;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public String getCoffeeName() {
        return coffeeName;
    }

    public void setCoffeeName(String coffeeName) {
        this.coffeeName = coffeeName;
    }

    public String getCoffeeDescription() {
        return coffeeDescription;
    }

    public void setCoffeeDescription(String coffeeDescription) {
        this.coffeeDescription = coffeeDescription;
    }

    public String getCoffeeImage() {
        return coffeeImage;
    }

    public void setCoffeeImage(String coffeeImage) {
        this.coffeeImage = coffeeImage;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCafeId() {
        return cafeId;
    }

    public void setCafeId(int cafeId) {
        this.cafeId = cafeId;
    }

    public int getCoffeeId() {
        return coffeeId;
    }

    public void setCoffeeId(int coffeeId) {
        this.coffeeId = coffeeId;
    }
}
