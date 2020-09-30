package com.mongodb.homeiotviewer.tab

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mongodb.homeiotviewer.R
import com.mongodb.homeiotviewer.TempHumidData
import com.mongodb.homeiotviewer.chart.*
import kotlinx.android.synthetic.main.fragment_tab_summary.view.*
import kotlinx.android.synthetic.main.table_row_sensorinfo.view.*
import java.text.SimpleDateFormat
import java.util.*

class SummaryFragment(val newestSensorData: MutableList<TempHumidData>,
                      val airconData: Pair<String?, String?>,
                      val watt: Int?)
    : Fragment() {
    //固定値変数
    val SENSOR_FAIL_THRESHOLD: Double = 30.0//「現在時刻 - 最新取得時間」がここで指定した分以上なら、異常発生とみなしてセンサ情報テーブルを強調表示

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //サンプルデータ作成
        val sumpleData = makeSummarySumple()
        //使用データをサンプルにするか本番用にするか選択
        val tempHumidData = newestSensorData

        //レイアウトからViewを生成
        val summaryView: View = inflater.inflate(R.layout.fragment_tab_summary, container, false)

        //現在の温湿度データの描画
        refreshSummaryTempData(tempHumidData, summaryView)

        //タイムゾーンを表示
        summaryView.textViewSummaryTimeZone.text = "[TimeZone=${TimeZone.getDefault().id}]  "

        //センサテーブル情報表示
        refreshSensorTable(tempHumidData, summaryView.sensorTableLayout)

        //エアコンおよび消費電力情報表示
        refreshAirconAndPower(summaryView.imageViewAircon, summaryView.textViewPower, airconData, watt)
        return summaryView
    }

    //現在の温湿度データの描画
    private fun refreshSummaryTempData(sensorData: MutableList<TempHumidData>, view: View) {
        //屋内と屋外にデータを分ける
        val indoorData = sensorData.filter { it.place == "indoor" }
        val outdoorData = sensorData.filter { it.place.split("_").first() == "outdoor" }
        //屋内外の平均温湿度を計算
        val indoorTemp = indoorData.mapNotNull { it.temperature }.average()
        val outdoorTemp = outdoorData.mapNotNull { it.temperature }.average()
        val indoorHumid = indoorData.mapNotNull { it.humidity }.average()
        val outdoorHumid = outdoorData.mapNotNull { it.humidity }.average()
        //屋内温度を円グラフ用データに整形
        val (dimensionsIndoorTemp, valuesIndoorTemp) = makePieDashboardData(indoorTemp.toFloat(), -10f, 40f)
        //屋外温度を円グラフ用に整形
        val (dimensionsOutdoorTemp, valuesOutdoorTemp) = makePieDashboardData(outdoorTemp.toFloat(), -10f, 40f)
        //屋内湿度を円グラフ用に整形
        val (dimensionsIndoorHumid, valuesIndoorHumid) = makePieDashboardData(indoorHumid.toFloat(), 0f, 100f)
        //屋外湿度を円グラフ用に整形
        val (dimensionsOutdoorHumid, valuesOutdoorHumid) = makePieDashboardData(outdoorHumid.toFloat(), 0f, 100f)
        //Chartフォーマット
        val indoorTempChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "屋内\n${"%.1f".format(indoorTemp)}°C",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        val outdoorTempChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "屋外\n${"%.1f".format(outdoorTemp)}°C",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        val indoorHumidChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "屋内\n${"%.1f".format(indoorHumid)}%",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        val outdoorHumidChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "屋外\n${"%.1f".format(outdoorHumid)}%",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        //DataSetフォーマット
        val indoorTempDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.rgb(243, 201, 14), Color.GRAY)//円の色
        )
        val outdoorTempDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.RED, Color.GRAY)//円の色
        )
        val indoorHumidDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.CYAN, Color.GRAY)//円の色
        )
        val outdoorHumidDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.BLUE, Color.GRAY)//円の色
        )

        //①場所ごとにEntryのリストを作成
        val indoorTempEntries = makePieChartEntries(dimensionsIndoorTemp, valuesIndoorTemp)
        val outdoorTempEntries = makePieChartEntries(dimensionsOutdoorTemp, valuesOutdoorTemp)
        val indoorHumidEntries = makePieChartEntries(dimensionsIndoorHumid, valuesIndoorHumid)
        val outdoorHumidEntries = makePieChartEntries(dimensionsOutdoorHumid, valuesOutdoorHumid)
        //②～⑦グラフ描画
        setupPieChart(indoorTempEntries, view.piechartTempIndoor, "室内温度",
            indoorTempChartFormat, indoorTempDSFormat)
        setupPieChart(outdoorTempEntries, view.piechartTempOutdoor, "室外温度",
            outdoorTempChartFormat, outdoorTempDSFormat)
        setupPieChart(indoorHumidEntries, view.piechartHumidIndoor, "室内湿度",
            indoorHumidChartFormat, indoorHumidDSFormat)
        setupPieChart(outdoorHumidEntries, view.piechartHumidOutdoor, "室外湿度",
            outdoorHumidChartFormat, outdoorHumidDSFormat)
    }

    //センサ情報テーブル表示
    private fun refreshSensorTable(newestSensorData: MutableList<TempHumidData>, tableLayout: TableLayout) {
        for ((i, sensor) in newestSensorData.withIndex()) {
            val tableRow: TableRow = getLayoutInflater().inflate(R.layout.table_row_sensorinfo, null) as TableRow
            //各列の情報を入力
            tableRow.rowtextSensorName.text = sensor.sensorName
            tableRow.rowtextPlace.text = sensor.place.split("_").first()//場所はアンダーバーで分けた最初のみ考慮(日なたと日陰は分けない)
            tableRow.rowtextLastDate.text = SimpleDateFormat("MM/dd HH:mm").format(sensor.date)
            tableRow.rowtextTemperature.text = "${sensor.temperature}°C"
            tableRow.rowtextHumidity.text = "${sensor.humidity}%"
            //取得時間が現在時刻より〇分以上前なら、背景色を変える
            val durationFromSuccess: Double = (Date().time - sensor.date.time) / (1000.0 * 60.0)
            emphasizeTextByThreshold(durationFromSuccess, SENSOR_FAIL_THRESHOLD, 0.0, Color.YELLOW,
                mutableListOf(tableRow.rowtextSensorName,tableRow.rowtextPlace, tableRow.rowtextLastDate, tableRow.rowtextTemperature, tableRow.rowtextHumidity))
            //行を追加
            tableLayout.addView(tableRow)
        }
    }

    //閾値を超えたらTextViewの色を変える
    private fun <T: Comparable<T>> emphasizeTextByThreshold(value: T, upper: T, lower: T, color: Int, rowTextViews: MutableList<TextView>){
        //上側閾値
        if((value > upper) or (value < lower)){
            for(textView in rowTextViews){
                textView.setBackgroundColor(color)
            }
        }
    }

    //エアコンおよび消費電力情報表示
    private fun refreshAirconAndPower(imageViewAircon: ImageView, textViewPower: TextView, airconData: Pair<String?, String?>, watt: Int?){
        //エアコンOn-Off情報を画像表示
        //エアコンOnのとき
        if((airconData.first == "power-on") or (airconData.first == "power-on_maybe"))
        {
            //モードによって表示画像を変える
            when(airconData.second){
                "cool" -> { imageViewAircon.setImageResource(R.drawable.aircon_cold) }
                "warm" -> { imageViewAircon.setImageResource(R.drawable.aircon_hot) }
                "dry" -> { imageViewAircon.setImageResource(R.drawable.aircon_dry) }
            }
        }
        //エアコンOffのとき
        else{
            imageViewAircon.setImageResource(R.drawable.aircon_off)
        }

        //消費電力を数値で表示
        if(watt != null){
            textViewPower.text = watt.toString() + " W"
        }
    }
}

//サンプルデータ作成
private fun makeSummarySumple(): MutableList<TempHumidData>{
    val sumpleSensorData: MutableList<TempHumidData> = mutableListOf()
    //日時読込用のフォーマット
    val dform = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    //サンプルデータ作成
    sumpleSensorData.add(TempHumidData("Remo_1", dform.parse("2020/07/28 01:35:00")!!,"indoor",27.4, 64.0))
    sumpleSensorData.add(TempHumidData("Omron_USB_1", dform.parse("2020/07/28 01:35:00")!!,"indoor",27.1, 64.94))
    sumpleSensorData.add(TempHumidData("Inkbird_IBSTH1_1", dform.parse("2020/07/28 01:35:00")!!,"indoor",25.46, null))
    sumpleSensorData.add(TempHumidData("Inkbird_IBSTH1_2", dform.parse("2020/07/28 01:35:00")!!,"kitchen",29.75, 65.58))
    sumpleSensorData.add(TempHumidData("Omron_BAG_1", dform.parse("2020/07/28 01:35:00")!!,"outdoor_sunny",30.11, 73.26))
    sumpleSensorData.add(TempHumidData("Inkbird_IBSTH1mini_1", dform.parse("2020/07/28 01:35:00")!!,"indoor",null, 73.49))
    sumpleSensorData.add(TempHumidData("SwitchBot_Thermo_1", dform.parse("2020/07/28 01:35:00")!!,"outdoor_shade",29.7, 73.0))
    return sumpleSensorData
}