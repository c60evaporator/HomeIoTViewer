package com.mongodb.homeiotviewer

import android.app.Application
import android.util.Log

import io.realm.Realm
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

//Realmアプリケーションのインスタンス（グローバルインスタンスとして、アプリケーション全体で共有する）
lateinit var app: App

// global Kotlin extension that resolves to the short version
// of the name of the current class. Used for labelling logs.
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/*
* InitRealm: Sets up the Realm App and enables Realm-specific logging in debug mode.
*/
class InitRealm : Application() {

    override fun onCreate() {
        super.onCreate()
        //Realmライブラリの初期化
        Realm.init(this)
        //Realmアプリケーションにアクセスしインスタンス化
        app = App(
            AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID)
                .build())

        // デバッグモード時に追加ロギングを有効に
        if (BuildConfig.DEBUG) {
            RealmLog.setLevel(LogLevel.ALL)
        }

        Log.v(TAG(), "Initialized the Realm App configuration for: ${app.configuration.appId}")
    }
}