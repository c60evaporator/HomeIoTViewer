package com.mongodb.homeiotviewer

import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mongodb.homeiotviewer.model.Sensor
import com.mongodb.homeiotviewer.model.SensorList
import com.mongodb.homeiotviewer.tab.TabPagerAdapter
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.kotlin.where
import io.realm.mongodb.User
import io.realm.mongodb.sync.SyncConfiguration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm//Realmデータベースのインスタンス
    private var user: User? = null
    private var restartFlg: Boolean = false//再起動用のフラグ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Realmデータベースのインスタンス初期化
        realm = Realm.getDefaultInstance()
    }

    override fun onStart() {
        super.onStart()
        //ログイン中ユーザの取得
        try {
            user = app.currentUser()
        } catch (e: IllegalStateException) {
            Log.w(TAG(), e)
        }
        //ログイン中ユーザが存在しない時、ログイン画面を表示する
        if (user == null) {
            // if no user is currently logged in, start the login activity so the user can authenticate
            startActivity(Intent(this, LoginActivity::class.java))
        }
        //ログイン中ユーザが存在するとき
        else {
            //MongoDB Realmとの同期設定
            val partitionValue: String = "Project HomeIoT"//
            val config = SyncConfiguration.Builder(user!!, "Project HomeIoT")
                .waitForInitialRemoteData()
                .build()
            //上記設定をデフォルトとして保存
            Realm.setDefaultConfiguration(config)
            //非同期バックグラウンド処理でMongoDB Realmと同期実行
            Realm.getInstanceAsync(config, object: Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    //同期したRealmインスタンスを親クラスMainActivityのインスタンスに設定
                    this@MainActivity.realm = realm
                    //クエリ操作用インスタンス作成
                    val listQuery: RealmQuery<SensorList> = realm.where<SensorList>().sort("no")//sensor_listsコレクション
                    val sensorQuery: RealmQuery<Sensor>  = realm.where<Sensor>().sort("Date_Master")//sensorsコレクション

                    //データ変換クラスのインスタンス作成
                    val sensorConverter = RealmSensorDataConverter(listQuery, sensorQuery)
                    //最新センサデータ
                    val newestData = sensorConverter.GetNewestData()
                    //最新エアコンおよび電力データ
                    val airconData = sensorConverter.GetNewestAircon()
                    val watt = sensorConverter.GetNewestPower()
                    //場所ごとの時系列気温データ(折れ線グラフ用)
                    val placeTempData = sensorConverter.getPlaceTempData()
                    //場所ごとの時系列湿度データ(折れ線グラフ用)
                    val placeHumidData = sensorConverter.getPlaceHumidData()
                    //日ごと最高最低平均気温データ
                    val tempStatsData = sensorConverter.getDailyTempStatsData("outdoor_shade")
                    //日ごと最高最低平均湿度データ
                    val humidStatsData = sensorConverter.getDailyHumidStatsData("outdoor_shade")

                    //Adapterの生成
                    pager.adapter = TabPagerAdapter(supportFragmentManager,
                        newestData,
                        airconData,
                        watt,
                        placeTempData,
                        placeHumidData,
                        tempStatsData,
                        humidStatsData,
                        this@MainActivity)
                    tab_layout.setupWithViewPager(pager)
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        user.run {
            realm.close()
        }
    }

    //アクティビティ終了時の処理(realmインスタンスをClose)
    override fun onDestroy() {
        super.onDestroy()
        // if a user hasn't logged out when the activity exits, still need to explicitly close the realm
        realm.close()
        //再起動時
        if(restartFlg){
            restartFlg = false
            val intent  = Intent()
            intent.setClass(this, this.javaClass)
            this.startActivity(intent)
        }
    }

    //logoutメニューをMainActivity上に設置
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    //logoutメニューを押したときの処理(ログアウト)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                user?.logOutAsync {
                    if (it.isSuccess) {
                        // always close the realm when finished interacting to free up resources
                        realm.close()
                        user = null
                        Log.v(TAG(), "user logged out")
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        Log.e(TAG(), "log out failed! Error: ${it.error}")
                    }
                }
                true
            }
            R.id.action_refresh -> {
                restartFlg = true
                this.finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}