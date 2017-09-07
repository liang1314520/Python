package com.evilbeast.meizi;

import android.app.Application;
import android.content.Context;

import com.evilbeast.meizi.utils.LogUtil;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Author: sumary
 */
public class MeiZiApp extends Application {
    public static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppContext = this;
        initRealm();


    }

    public void initRealm() {
        //Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(this)
                .name("meiziappv9")
                .deleteRealmIfMigrationNeeded()
                .schemaVersion(9)
                .build();
        Realm.setDefaultConfiguration(realmConfig);
    }

    public static Context getContext() {
        return mAppContext;
    }
}
