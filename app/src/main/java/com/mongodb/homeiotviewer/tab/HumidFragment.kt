package com.mongodb.homeiotviewer.tab

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.mongodb.homeiotviewer.R
import com.mongodb.homeiotviewer.TempHumidData
import com.mongodb.homeiotviewer.chart.*
import kotlinx.android.synthetic.main.fragment_tab_humid.view.*
import java.text.SimpleDateFormat
import java.util.*

class HumidFragment(
    val newestSensorData: MutableList<TempHumidData>,
    val placeHumidData: Map<String, MutableList<Pair<Date, Double>>>,
    val humidStatsData: Map<String, List<Pair<Date, Double>>>)
    : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //レイアウトからViewを生成
        val humidView: View = inflater.inflate(R.layout.fragment_tab_humid, container, false)
        //現在の温湿度データの描画
        refreshCurrentHumidData(newestSensorData, humidView)

        //タイムゾーンを表示
        humidView.textViewHumidTimeZone.text = "[TimeZone=${TimeZone.getDefault().id}]  "

        //温度推移グラフの描画
        //FragmentのContextはrequireContextを使用(https://developer.android.com/kotlin/common-patterns?hl=ja)
        refreshHumidTimeSeriesData(this.requireContext(), placeHumidData, humidView)

        //日ごとの最高最低平均気温グラフ描画
        refreshHumidStatsData(this.requireContext(), humidStatsData, humidView)

        return humidView
    }
    //現在の湿度データの描画
    private fun refreshCurrentHumidData(sensorData: MutableList<TempHumidData>, view: View) {
        //屋内、キッチン、日陰、日なたにデータを分ける
        val indoorData = sensorData.filter { it.place == "indoor" }
        val kitchenData = sensorData.filter { it.place == "kitchen" }
        val shadeData = sensorData.filter { it.place == "outdoor_shade" }
        val sunnyData = sensorData.filter { it.place == "outdoor_sunny" }
        //屋内外の平均温湿度を計算
        val indoorHumid = indoorData.mapNotNull { it.humidity }.average()
        val kitchenHumid = kitchenData.mapNotNull { it.humidity }.average()
        val shadeHumid = shadeData.mapNotNull { it.humidity }.average()
        val sunnyHumid = sunnyData.mapNotNull { it.humidity }.average()

        //屋内温度を円グラフ用データに整形
        val (dimensionsIndoorHumid, valuesIndoorHumid) = makePieDashboardData(
            indoorHumid.toFloat(),
            0f,
            100f
        )
        //屋外温度を円グラフ用に整形
        val (dimensionsKitchenHumid, valuesKitchenHumid) = makePieDashboardData(
            kitchenHumid.toFloat(),
            0f,
            100f
        )
        //屋内湿度を円グラフ用に整形
        val (dimensionsShadeHumid, valuesShadeHumid) = makePieDashboardData(
            shadeHumid.toFloat(),
            0f,
            100f
        )
        //屋外湿度を円グラフ用に整形
        val (dimensionsSunnyHumid, valuesSunnyHumid) = makePieDashboardData(
            sunnyHumid.toFloat(),
            0f,
            100f
        )
        //描画フォーマット
        val indoorHumidFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "屋内\n${"%.1f".format(indoorHumid)}%",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.rgb(243, 201, 14), Color.GRAY),
            "false"
        )
        val kitchenHumidFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "ｷｯﾁﾝ\n${"%.1f".format(kitchenHumid)}%",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.rgb(128, 255, 0), Color.GRAY),
            "false"
        )
        val shadeHumidFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "日陰\n${"%.1f".format(shadeHumid)}%",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.rgb(185, 122, 87), Color.GRAY),
            "false"
        )
        val sunnyHumidFormat = PieFormat(
            Pair(null, Color.WHITE),//凡例 (形状＋文字色)
            "false",
            "日なた\n${"%.1f".format(sunnyHumid)}%",
            Pair(16f, Color.WHITE),
            Pair(null, Color.TRANSPARENT),
            listOf(Color.RED, Color.GRAY),
            "false"
        )
        //円グラフ描画
        setupPieChart(
            dimensionsIndoorHumid,
            valuesIndoorHumid,
            view.piechartHumidIndoorHumid,
            "室内温度",
            indoorHumidFormat
        )
        setupPieChart(
            dimensionsKitchenHumid,
            valuesKitchenHumid,
            view.piechartHumidKitchen,
            "室外温度",
            kitchenHumidFormat
        )
        setupPieChart(
            dimensionsShadeHumid,
            valuesShadeHumid,
            view.piechartHumidOutdoorShade,
            "室内湿度",
            shadeHumidFormat
        )
        setupPieChart(
            dimensionsSunnyHumid,
            valuesSunnyHumid,
            view.piechartHumidOutdoorSunny,
            "室外湿度",
            sunnyHumidFormat
        )
    }

    //温度推移データの描画
    private fun refreshHumidTimeSeriesData(
        context: Context,
        humidSeriesData: Map<String, MutableList<Pair<Date, Double>>>,
        view: View
    ) {
        //場所ごとに必要期間のデータ抜き出してEntryのリストに入力
        val places = humidSeriesData.keys//場所のリスト
        val allLinesData: MutableMap<String, MutableList<Entry>> = mutableMapOf()
        for(pl in places){
            //要素数が0なら処理を終了
            if(humidSeriesData[pl]?.size == 0) return
            //Entryにデータ入力
            val x = humidSeriesData[pl]?.map { it.first }!!//X軸（日時データ)
            val y = humidSeriesData[pl]?.map { it.second.toFloat() }!!//Y軸(温度データ)
            allLinesData[pl] = makeDateLineChartData(x, y)//日時と温度をEntryのリストに変換
        }
        //グラフ全体のフォーマット
        val humidSeriesFormat = AllLinesFormat(
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
            Pair("","%")//ツールチップの表示単位(X軸＋Y軸)
        )
        //線ごとのフォーマット
        val humidLineFormat: Map<String, OneLineFormat> = mapOf(
            "indoor" to OneLineFormat(
                Color.rgb(243, 201, 14), Pair(2f, null), Triple(
                    false,
                    null,
                    null
                )
            ),
            "kitchen" to OneLineFormat(
                Color.rgb(128, 255, 0), Pair(2f, null), Triple(
                    false,
                    null,
                    null
                )
            ),
            "outdoor_shade" to OneLineFormat(
                Color.rgb(185, 122, 87), Pair(2f, null), Triple(
                    false,
                    null,
                    null
                )
            ),
            "outdoor_sunny" to OneLineFormat(Color.RED, Pair(2f, null), Triple(false, null, null))
        )
        setupLineChart(allLinesData, view.lineChartHumidTimeSeries, humidSeriesFormat, humidLineFormat, context)
    }

    //温度推移データの描画
    private fun refreshHumidStatsData(
        context: Context,
        humidSeriesData: Map<String, List<Pair<Date, Double>>>,
        view: View
    ){
        //要素数が0なら終了
        if(humidSeriesData["max"]?.size == 0) return
        //必要データをEntryのリストに入力
        val x = humidSeriesData["max"]?.map{it.first}!!
        val yHigh = humidSeriesData["max"]?.map{it.second.toFloat()}!!
        val yLow = humidSeriesData["min"]?.map{it.second.toFloat()}!!
        val yMidHigh = humidSeriesData["avg"]?.map{it.second.toFloat()+0.2f}!!
        val yMidLow = humidSeriesData["avg"]?.map{it.second.toFloat()-0.2f}!!
        val candleData = makeDateLineChartData(x, yHigh, yLow, yMidHigh, yMidLow)

        //グラフ全体のフォーマット
        val candleFormat = CandleFormat(
            Pair(null, Legend.LegendForm.DEFAULT),//凡例 (文字色＋形状)
            "false",//グラフ説明
            Color.BLACK,//塗りつぶし色（背景）
            Pair(Color.WHITE, null),//X軸の設定（文字色＋文字サイズ）
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

        setupCandleStickChart(candleData, view.candleStickChartHumidStats, candleFormat, context)
    }
}