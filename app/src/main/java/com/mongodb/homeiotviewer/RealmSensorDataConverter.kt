package com.mongodb.homeiotviewer

import com.mongodb.homeiotviewer.model.Sensor
import com.mongodb.homeiotviewer.model.SensorList
import io.realm.RealmQuery
import java.text.SimpleDateFormat
import java.util.*

/**
 * センサごと温湿度データ保持用データクラス (直近データ取得に使用)
 * @param[listQuery]:センサの一覧
 * @param[sensorQuery]: 日時
 */
class RealmSensorDataConverter(
    val listQuery: RealmQuery<SensorList>,
    val sensorQuery: RealmQuery<Sensor>) {
    //クラス内変数
    private var sensorList: List<SensorList>//センサ一覧
    private var sensorData: List<Sensor>//センサデータ(List化してクエリに影響を与えずに保持)
    private var lineData: List<Sensor>//折れ線グラフ用センサデータ(sensorDataから期間抜き出し)
    private var statsData: List<Sensor>//最高最低表示用CandleStickグラフ用センサデータ(sensorDataから期間抜き出し)
    //定数
    val SENSOR_TIME_ZONE = TimeZone.getTimeZone("Asia/Tokyo")//センサ取得時刻のタイムゾーン
    val TIME_SERIES_PERIOD: Int = 2//折れ線グラフに表示する期間(日)
    val STATS_PERIOD: Int = 30//最高最低表示用CandleStickグラフに表示する期間(日)

    //初期化 (基本データの計算)
    init{
        sensorList = listQuery.findAll().toList()//センサ一覧
        sensorData = sensorQuery.findAll().toList()//センサデータ(List化してクエリに影響を与えずに保持)

        //折れ線グラフ用センサデータ(「現在日時-TIME_SERIES_PERIOD」以降を取得)
        val currentDate = Date()//現在日時
        val lineStartDate = Calendar.getInstance().run{
            this.time = currentDate
            this.add(Calendar.DATE, -TIME_SERIES_PERIOD)
            this.time
        }
        lineData = sensorData.filter { it.Date_Master > lineStartDate }!!

        //折れ線グラフ用センサデータ(「現在日時-STATS_PERIOD」以降を取得)
        val statsStartDate = Calendar.getInstance().run{
            this.time = currentDate
            this.add(Calendar.DATE, -STATS_PERIOD)
            this.time
        }
        statsData = sensorData.filter { it.Date_Master > statsStartDate }!!
    }

    //センサごとの最新値取得
    fun GetNewestData(): MutableList<TempHumidData> {
        //格納用
        val newestData: MutableList<TempHumidData> = mutableListOf()
        //センサごとに走査
        for(sensorinfo in sensorList){
            //センサ番号
            val no: Int = sensorinfo.no?.toInt()!!
            //温湿度格納用
            var temp: Double? = null
            var humid: Double? = null
            //そのセンサでnullでない最後の取得時刻
            val dateField = "no" + "%02d".format(no) + "_Date"
            val lastDate = sensorQuery.maximumDate(dateField)
            //上記最新時刻のデータを取得
            //クエリに影響を与えないよう、フィルタ処理はListに対して実行
            val newestDoc: Sensor? = sensorData.filter { it.getDate(no) == lastDate }.firstOrNull()
            //温度測定しているセンサのとき
            if(sensorinfo.temperature!!){
                temp = newestDoc?.getTemprature(no)//温度
            }
            //湿度測定しているセンサのとき
            if(sensorinfo.humidity!!){
                humid = newestDoc?.getHumidity(no)//湿度
            }
            //日時
            var date = newestDoc?.getDate(no)!!
            //タイムゾーン変換(デフォルトだとGMTとして読み込まれているものを、センサのタイムゾーンに変換)
            date = changeTimeZone(date, TimeZone.getTimeZone("GMT"), SENSOR_TIME_ZONE)
            newestData.add(TempHumidData(sensorinfo.sensorname!!, date, sensorinfo.place!!,temp, humid))
        }
        return  newestData
    }

    //最新のエアコンOnOff情報を取得(複数ある場合はNoが最も若いもののみ使用)
    fun GetNewestAircon(): Pair<String?, String?> {
        //格納用
        var airconPower: String? = null
        var airconMode: String? = null
        //センサごとに走査
        for (sensorinfo in sensorList) {
            //センサ番号
            val no: Int = sensorinfo.no?.toInt()!!
            //エアコンデータを取得
            if(sensorinfo.aircon!!){
                //そのセンサがnullでない最後の取得時刻
                val dateField = "no" + "%02d".format(no) + "_Date"
                val lastDate = sensorQuery.maximumDate(dateField)
                //上記最新時刻のデータを取得
                //クエリに影響を与えないよう、フィルタ処理はListに対して実行
                val newestDoc: Sensor? = sensorData.filter { it.getDate(no) == lastDate }.firstOrNull()
                //エアコンOnOff情報
                airconPower = newestDoc?.getAirconPower(no)
                //エアコン冷暖房情報
                airconMode = newestDoc?.getAirconMode(no)
                //ループを抜ける
                break
            }
        }
        return Pair(airconPower, airconMode)
    }

    //最新の消費電力データを取得(複数ある場合はNoが最も若いもののみ使用)
    fun GetNewestPower(): Int? {
        //格納用
        var watt: Int? = null
        //センサごとに走査
        for (sensorinfo in sensorList) {
            //センサ番号
            val no: Int = sensorinfo.no?.toInt()!!
            //電力情報を取得
            if(sensorinfo.power!!){
                //そのセンサがnullでない最後の取得時刻
                val dateField = "no" + "%02d".format(no) + "_Date"
                val lastDate = sensorQuery.maximumDate(dateField)
                //上記最新時刻のデータを取得
                //クエリに影響を与えないよう、フィルタ処理はListに対して実行
                val newestDoc: Sensor? = sensorData.filter { it.getDate(no) == lastDate }.firstOrNull()
                //エアコンOnOff情報
                watt = newestDoc?.getWatt(no)?.toInt()
                //ループを抜ける
                break
            }
        }
        return watt
    }

    //場所ごとの時系列気温データ(折れ線グラフ用)
    fun getPlaceTempData(): Map<String, MutableList<Pair<Date, Double>>> {
        //場所とセンサ番号の辞書作成
        val places: List<String> =
            sensorList.filter { it.temperature == true }.mapNotNull { it.place }.distinct()
        val nPlace = places.count()
        val placeDict =
            sensorList.filter { it.temperature == true }.groupBy({ it.place }, { it.no?.toInt()!! })
        //場所ごと平均気温保持用リスト
        val placeTempData: MutableMap<String, MutableList<Pair<Date, Double>>> = mutableMapOf()
        //リストを場所ごとに初期化
        for (pl in places){
            placeTempData[pl] = mutableListOf()
        }

        //全データを走査
        for (sensorDoc in lineData) {
            var aveTemps: MutableMap<String, Double> = mutableMapOf()
            //場所ごとに平均気温を計算
            for (pl in places) {
                var sumTemp = 0.0
                var cnt = 0
                //場所内の全気温の和を計算
                for (no in placeDict[pl]!!) {
                    val temp = sensorDoc.getTemprature(no)
                    if (temp != null) {
                        sumTemp += temp
                        cnt++
                    }
                }
                //場所内にnullでない気温が存在するとき、平均気温を計算
                if (cnt > 0) {
                    aveTemps[pl] = sumTemp / cnt
                }
            }
            //全ての場所の平均気温が計算できているとき、平均気温保持用リストに追加
            if (aveTemps.count() == nPlace) {
                val masterDate = changeTimeZone(sensorDoc.Date_Master, TimeZone.getTimeZone("GMT"), SENSOR_TIME_ZONE)//タイムゾーン変換
                for (pl in places) {
                    placeTempData[pl]?.add(Pair(masterDate, aveTemps[pl]!!))
                }
            }
        }
        return placeTempData
    }

    //場所ごとの時系列湿度データ(折れ線グラフ用)
    fun getPlaceHumidData(): Map<String, MutableList<Pair<Date, Double>>> {
        //場所とセンサ番号の辞書作成
        val places: List<String> =
            sensorList.filter { it.humidity == true }.mapNotNull { it.place }.distinct()
        val nPlace = places.count()
        val placeDict =
            sensorList.filter { it.humidity == true }.groupBy({ it.place }, { it.no?.toInt()!! })
        //場所ごと平均気温保持用リスト
        val placeHumidData: MutableMap<String, MutableList<Pair<Date, Double>>> = mutableMapOf()
        //リストを場所ごとに初期化
        for (pl in places){
            placeHumidData[pl] = mutableListOf()
        }

        //全データを走査
        for (sensorDoc in lineData) {
            var aveHumids: MutableMap<String, Double> = mutableMapOf()
            //場所ごとに平均気温を計算
            for (pl in places) {
                var sumHumid = 0.0
                var cnt = 0
                //場所内の全気温の和を計算
                for (no in placeDict[pl]!!) {
                    val humid = sensorDoc.getHumidity(no)
                    if (humid != null) {
                        sumHumid += humid
                        cnt++
                    }
                }
                //場所内にnullでない気温が存在するとき、平均気温を計算
                if (cnt > 0) {
                    aveHumids[pl] = sumHumid / cnt
                }
            }
            //全ての場所の平均気温が計算できているとき、平均気温保持用リストに追加
            if (aveHumids.count() == nPlace) {
                val masterDate = changeTimeZone(sensorDoc.Date_Master, TimeZone.getTimeZone("GMT"), SENSOR_TIME_ZONE)//タイムゾーン変換
                for (pl in places) {
                    placeHumidData[pl]?.add(Pair(masterDate, aveHumids[pl]!!))
                }
            }
        }
        return placeHumidData
    }

    //日ごとの最高最低平均気温データ(ローソク足グラフ用)
    fun getDailyTempStatsData(place: String): Map<String, List<Pair<Date, Double>>>{
        //////まず対象場所の温度データを取得//////
        //対象場所の平均気温保持用リスト
        val tempData: MutableList<Pair<Date, Double>> = mutableListOf()
        //対象のセンサ番号のリスト
        val sensorIds = sensorList.filter { it.temperature == true && it.place == place}.map{it.no?.toInt()!!}
        //全データを走査
        for (sensorDoc in statsData) {
            //平均気温を計算
            var sumTemp = 0.0
            var cnt = 0
            //場所内の全気温の和を計算
            for (no in sensorIds) {
                val temp = sensorDoc.getTemprature(no)
                if (temp != null) {
                    sumTemp += temp
                    cnt++
                }
            }
            //場所内にnullでない気温が存在するとき、平均気温を計算
            if (cnt > 0) {
                val masterDate = changeTimeZone(sensorDoc.Date_Master, TimeZone.getTimeZone("GMT"), SENSOR_TIME_ZONE)//タイムゾーン変換
                tempData.add(Pair(masterDate ,sumTemp / cnt))
            }
        }

        //////最低最高気温を計算//////
        //日付でグルーピング（システムの日付なので注意）
        val sdf = SimpleDateFormat("yyyy/M/d")
        val grby = tempData.groupBy{ sdf.parse(sdf.format(it.first)) }
        //統計値算出（最低、平均、最高）
        val min = grby.map { Pair(it.key, it.value.map { it.second }.min()!!) }
        val avg = grby.map { Pair(it.key, it.value.map { it.second }.average()!!) }
        val max = grby.map { Pair(it.key, it.value.map { it.second }.max()!!) }
        //最高最低平均気温保持用リスト
        val tempStatsData: Map<String, List<Pair<Date, Double>>> = mapOf("min" to min, "avg" to avg, "max" to max)

        return tempStatsData
    }

    //日ごとの最高最低平均気温データ(ローソク足グラフ用)
    fun getDailyHumidStatsData(place: String): Map<String, List<Pair<Date, Double>>>{
        //////まず対象場所の温度データを取得//////
        //対象場所の平均気温保持用リスト
        val humidData: MutableList<Pair<Date, Double>> = mutableListOf()
        //対象のセンサ番号のリスト
        val sensorIds = sensorList.filter { it.humidity == true && it.place == place}.map{it.no?.toInt()!!}
        //全データを走査
        for (sensorDoc in statsData) {
            //平均気温を計算
            var sumHumid = 0.0
            var cnt = 0
            //場所内の全気温の和を計算
            for (no in sensorIds) {
                val humid = sensorDoc.getHumidity(no)
                if (humid != null) {
                    sumHumid += humid
                    cnt++
                }
            }
            //場所内にnullでない気温が存在するとき、平均気温を計算
            if (cnt > 0) {
                val masterDate = changeTimeZone(sensorDoc.Date_Master, TimeZone.getTimeZone("GMT"), SENSOR_TIME_ZONE)//タイムゾーン変換
                humidData.add(Pair(masterDate ,sumHumid / cnt))
            }
        }

        //////最低最高気温を計算//////
        //日付でグルーピング（システムの日付なので注意）
        val sdf = SimpleDateFormat("yyyy/M/d")
        val grby = humidData.groupBy{ sdf.parse(sdf.format(it.first)) }
        //統計値算出（最低、平均、最高）
        val min = grby.map { Pair(it.key, it.value.map { it.second }.min()!!) }
        val avg = grby.map { Pair(it.key, it.value.map { it.second }.average()!!) }
        val max = grby.map { Pair(it.key, it.value.map { it.second }.max()!!) }
        //最高最低平均気温保持用リスト
        val humidStatsData: Map<String, List<Pair<Date, Double>>> = mapOf("min" to min, "avg" to avg, "max" to max)

        return humidStatsData
    }
}
/**
 * センサごと温湿度データ保持用データクラス (直近データ取得に使用)
 * @param[sensorName]:センサ名
 * @param[date]: 日時
 * @param[place]: センサ設置場所
 * @param[temperature]: 温度
 * @param[humidity]: 湿度
 */
data class TempHumidData(val sensorName: String, val date: Date, val place: String, val temperature: Double?, val humidity: Double?)
