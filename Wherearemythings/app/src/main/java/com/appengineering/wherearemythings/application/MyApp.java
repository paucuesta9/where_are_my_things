package com.appengineering.wherearemythings.application;

import android.app.Application;

import com.appengineering.wherearemythings.models.Item;
import com.appengineering.wherearemythings.models.Category;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Pau on 30/12/2017.
 */

public class MyApp extends Application {

    //ID Realm
    public static AtomicInteger CategoryID = new AtomicInteger();
    public static AtomicInteger ItemID = new AtomicInteger();

    //It will run before opening the application
    @Override
    public void onCreate() {
        super.onCreate();
        //Realm start
        Realm.init(this);
        //Defining the configuration
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        //Setting this configuration to Realm
        Realm.setDefaultConfiguration(config);
        Realm realm = Realm.getInstance(config);
        //Taking the maximums ID of categories and items
        CategoryID = getIdByTable(realm, Category.class);
        ItemID = getIdByTable(realm, Item.class);
        //Closing the database
        realm.close();
    }

    //Taking the maximiums IDs
    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass) {
        RealmResults<T> results = realm.where(anyClass).findAll();
        return (results.size() > 0) ? new AtomicInteger(results.max("id").intValue()) : new AtomicInteger();
    }

}
