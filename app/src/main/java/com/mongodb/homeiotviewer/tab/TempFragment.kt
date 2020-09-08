package com.mongodb.homeiotviewer.tab

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.mongodb.homeiotviewer.R
import com.mongodb.homeiotviewer.TempHumidData
import com.mongodb.homeiotviewer.chart.*
import kotlinx.android.synthetic.main.fragment_tab_temp.view.*
import java.text.SimpleDateFormat
import java.util.*

//リスナの継承削除する
class TempFragment(
    val newestSensorData: MutableList<TempHumidData>,
    val placeTempData: Map<String, MutableList<Pair<Date, Double>>>,
    val tempStatsData: Map<String, List<Pair<Date, Double>>>)
    : Fragment(), OnChartValueSelectedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //レイアウトからViewを生成
        val tempView: View = inflater.inflate(R.layout.fragment_tab_temp, container, false)
        //現在の温湿度データの描画
        refreshCurrentTempData(newestSensorData, tempView)

        //タイムゾーンを表示
        tempView.textViewTempTimeZone.text = "[TimeZone=${TimeZone.getDefault().id}]  "

        //温度推移グラフの描画
        //FragmentのContextはrequireContextを使用(https://developer.android.com/kotlin/common-patterns?hl=ja)
        refreshTempTimeSeriesData(this.requireContext(), placeTempData, tempView)

        //日ごとの最高最低平均気温グラフ描画
        refreshTempStatsData(this.requireContext(), tempStatsData, tempView)

        return tempView
    }
    //リスナ用
    override fun onValueSelected(e: Entry, h: Highlight) {
        Log.i("Entry selected", e.toString())
    }
    //リスナ用
    override fun onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.")
    }

    //現在の気温データの描画
    private fun refreshCurrentTempData(sensorData: MutableList<TempHumidData>, view: View) {
        //屋内、キッチン、日陰、日なたにデータを分ける
        val indoorData = sensorData.filter { it.place == "indoor" }
        val kitchenData = sensorData.filter { it.place == "kitchen" }
        val shadeData = sensorData.filter { it.place == "outdoor_shade" }
        val sunnyData = sensorData.filter { it.place == "outdoor_sunny" }
        //屋内外の平均温湿度を計算
        val indoorTemp = indoorData.mapNotNull { it.temperature }.average()
        val kitchenTemp = kitchenData.mapNotNull { it.temperature }.average()
        val shadeTemp = shadeData.mapNotNull { it.temperature }.average()
        val sunnyTemp = sunnyData.mapNotNull { it.temperature }.average()

        //屋内温度を円グラフ用データに整形
        val (dimensionsIndoorTemp, valuesIndoorTemp) = makePieDashboardData(
            indoorTemp.toFloat(),
            -10f,
            40f
        )
        //屋外温度を円グラフ用に整形
        val (dimensionsKitchenTemp, valuesKitchenTemp) = makePieDashboardData(
            kitchenTemp.toFloat(),
            -10f,
            40f
        )
        //屋内湿度を円グラフ用に整形
        val (dimensionsShadeTemp, valuesShadeTemp) = makePieDashboardData(
            shadeTemp.toFloat(),
            -10f,
            40f
        )
        //屋外湿度を円グラフ用に整形
        val (dimensionsSunnyTemp, valuesSunnyTemp) = makePieDashboardData(
            sunnyTemp.toFloat(),
            -10f,
            40f
        )
        //描画フォーマット
        val indoorTempFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "屋内\n${"%.1f".format(indoorTemp)}°C",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.rgb(243, 201, 14), Color.GRAY),
            "false"
        )
        val kitchenTempFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "ｷｯﾁﾝ\n${"%.1f".format(kitchenTemp)}°C",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.rgb(128, 255, 0), Color.GRAY),
            "false"
        )
        val shadeTempFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "日陰\n${"%.1f".format(shadeTemp)}°C",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.rgb(185, 122, 87), Color.GRAY),
            "false"
        )
        val sunnyTempFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "日なた\n${"%.1f".format(sunnyTemp)}°C",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.RED, Color.GRAY),
            "false"
        )
        //円グラフ描画
        setupPieChart(
            dimensionsIndoorTemp,
            valuesIndoorTemp,
            view.piechartTempIndoorTemp,
            "室内温度",
            indoorTempFormat
        )
        setupPieChart(
            dimensionsKitchenTemp,
            valuesKitchenTemp,
            view.piechartTempKitchen,
            "室外温度",
            kitchenTempFormat
        )
        setupPieChart(
            dimensionsShadeTemp,
            valuesShadeTemp,
            view.piechartTempOutdoorShade,
            "室内湿度",
            shadeTempFormat
        )
        setupPieChart(
            dimensionsSunnyTemp,
            valuesSunnyTemp,
            view.piechartTempOutdoorSunny,
            "室外湿度",
            sunnyTempFormat
        )
    }

    //温度推移データの描画
    private fun refreshTempTimeSeriesData(
        context: Context,
        tempSeriesData: Map<String, MutableList<Pair<Date, Double>>>,
        view: View
    ) {
        //グラフ全体のフォーマット
        val tempSeriesFormat = AllLinesFormat(
            Pair(Color.WHITE, Legend.LegendForm.DEFAULT),//凡例 (文字色＋形状)
            "false",//グラフ説明
            Color.BLACK,//塗りつぶし色（背景）
            Pair(Color.WHITE, null),//X軸の設定（文字色＋ラベル表示間隔）
            SimpleDateFormat("M/d H:mm"),//X軸の日付軸フォーマット(日付軸でないときnull指定)
            Color.WHITE,//左Y軸の設定（文字色）
            Pair(null, null),//左Y軸の表示範囲(最小値＋最大値)
            true,//タッチ有効
            Pair("xy", false),//ズーム設定(有無＋ピンチ動作をXY方向に限定するか)
            Pair("xy", SimpleDateFormat("M/d H:mm")),//ツールチップ設定(表示軸方向＋日付軸フォーマット)
            Pair("","°C"),//ツールチップの表示単位(X軸＋Y軸)
            true//時系列グラフのX軸を正確にプロットするか
        )
        //線ごとのフォーマット
        val tempLineFormat: Map<String, OneLineFormat> = mapOf(
            "indoor" to OneLineFormat(
                Color.rgb(243, 201, 14),
                Pair(2f, null),
                Triple(false, null, null)
            ),
            "kitchen" to OneLineFormat(
                Color.rgb(128, 255, 0),
                Pair(2f, null),
                Triple(false, null, null)
            ),
            "outdoor_shade" to OneLineFormat(
                Color.rgb(185, 122, 87),
                Pair(2f, null),
                Triple(false, null, null)
            ),
            "outdoor_sunny" to OneLineFormat(
                Color.RED,
                Pair(2f, null),
                Triple(false, null, null)
            )
        )
        //場所ごとに必要期間のデータ抜き出してEntryのリストに入力
        val places = tempSeriesData.keys//場所のリスト
        val allLinesData: MutableMap<String, MutableList<Entry>> = mutableMapOf()
        for(pl in places){
            //要素数が0なら処理を終了
            if(tempSeriesData[pl]?.size == 0) return
            //Entryにデータ入力
            val x = tempSeriesData[pl]?.map { it.first }!!//X軸（日時データ)
            val y = tempSeriesData[pl]?.map { it.second.toFloat() }!!//Y軸(温度データ)
            allLinesData[pl] = makeDateLineChartData(x, y, tempSeriesFormat.timeAccuracy)//日時と温度をEntryのリストに変換
        }
        setupLineChart(allLinesData, view.lineChartTempTimeSeries, tempSeriesFormat, tempLineFormat, context)
    }

    //温度推移データの描画
    private fun refreshTempStatsData(
        context: Context,
        tempSeriesData: Map<String, List<Pair<Date, Double>>>,
        view: View
    ){
        //要素数が0なら終了
        if(tempSeriesData["max"]?.size == 0) return
        //必要データをEntryのリストに入力
        val x = tempSeriesData["max"]?.map{it.first}!!
        val yHigh = tempSeriesData["max"]?.map{it.second.toFloat()}!!
        val yLow = tempSeriesData["min"]?.map{it.second.toFloat()}!!
        val yMidHigh = tempSeriesData["avg"]?.map{it.second.toFloat()+0.2f}!!
        val yMidLow = tempSeriesData["avg"]?.map{it.second.toFloat()-0.2f}!!
        val candleData = makeDateLineChartData(x, yHigh, yLow, yMidHigh, yMidLow)

        //グラフ全体のフォーマット
        val candleFormat = CandleFormat(
            Pair(null, Legend.LegendForm.DEFAULT),//凡例 (文字色＋形状)
            "false",//グラフ説明
            Color.BLACK,//塗りつぶし色（背景）
            Pair(Color.WHITE, null),//X軸の設定（文字色＋ラベル表示間隔）
            SimpleDateFormat("M/d"),//X軸の日付軸フォーマット(日付軸でないときnull指定)
            Color.WHITE,//左Y軸の設定（文字色）
            Pair(null, null),//左Y軸の表示範囲(最小値＋最大値)
            true,//タッチ有効
            Pair("xy", false),//ズーム設定(有無＋ピンチ動作をXY方向に限定するか)
            Pair(null, SimpleDateFormat("M/d")),//ツールチップ設定(表示軸方向＋日付軸フォーマット)
            Pair("",""),//ツールチップの表示単位(X軸＋Y軸)
            Pair(Color.rgb(220, 120, 80), 2.5f),//細線部分のフォーマット(色＋太さ)
            Pair(Color.rgb(255, 100, 50), Paint.Style.FILL),//Open>Closeのときの太線部分フォーマット(色＋塗りつぶし形式)
            Pair(null, null),//Open<Closeのときの太線部分フォーマット(色＋塗りつぶし形式)
            Pair(Color.rgb(220, 120, 80), 7f),//値表示のフォーマット(文字色＋文字サイズ)
            "%.0f"//値表示の文字書式
        )

        setupCandleStickChart(candleData, view.candleStickChartTempStats, candleFormat, context)
    }
}
