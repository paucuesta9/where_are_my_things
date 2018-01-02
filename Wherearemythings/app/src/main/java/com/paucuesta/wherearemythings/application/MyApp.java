package com.paucuesta.wherearemythings.application;

import android.app.Application;

import com.paucuesta.wherearemythings.models.Item;
import com.paucuesta.wherearemythings.models.Category;

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

    //Before creating app
    @Override
    public void onCreate() {
        super.onCreate();
        //Start realm and configuration
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
        Realm realm = Realm.getInstance(config);
        CategoryID = getIdByTable(realm, Category.class);
        ItemID = getIdByTable(realm, Item.class);
        realm.close();
    }

    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass) {
        RealmResults<T> results = realm.where(anyClass).findAll();
        return (results.size() > 0) ? new AtomicInteger(results.max("id").intValue()) : new AtomicInteger();
    }

}
