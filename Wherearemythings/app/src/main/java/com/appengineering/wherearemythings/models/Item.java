package com.appengineering.wherearemythings.models;

import com.appengineering.wherearemythings.application.MyApp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Pau on 30/12/2017.
 */

public class Item extends RealmObject {

    @PrimaryKey
    private int id;
    @Required
    private String name;
    private String description;
    @Required
    private String place;
    private int quantity;
    private String photo;
    private int photoType;

    public Item() {

    }

    public Item(String name, String description, String place, int quantity, String photo, int photoType) {
        this.id = MyApp.ItemID.incrementAndGet();
        this.name = name;
        this.description = description;
        this.place = place;
        this.quantity = quantity;
        this.photo = photo;
        this.photoType = photoType;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getPhotoType() {
        return photoType;
    }

    public void setPhotoType(int photoType) {
        this.photoType = photoType;
    }
}
