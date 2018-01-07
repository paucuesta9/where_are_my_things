package com.appengineering.wherearemythings.models;

import com.appengineering.wherearemythings.application.MyApp;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Pau on 30/12/2017.
 */

public class Category extends RealmObject {

    //Required to realm
    @PrimaryKey
    private int id;
    @Required
    private String name;
    private String photo;
    private int photoType;

    //List items
    private RealmList<Item> items;

    public Category() {

    }

    public Category(String name, String photo, int photoType) {
        this.id = MyApp.CategoryID.incrementAndGet();
        this.name = name;
        this.photo = photo;
        this.items = new RealmList<Item>();
        this.photoType = photoType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public RealmList<Item> getItems() {
        return items;
    }

    public int getPhotoType() {
        return photoType;
    }

    public void setPhotoType(int photoType) {
        this.photoType = photoType;
    }
}
