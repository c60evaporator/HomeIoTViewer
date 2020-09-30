package com.mongodb.homeiotviewer.tab

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.YAxis
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

        //Chartフォーマット
        val indoorChartFormat = PieChartFormat(
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
        val kitchenChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "ｷｯﾁﾝ\n${"%.1f".format(kitchenHumid)}%",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        val shadeChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "日陰\n${"%.1f".format(shadeHumid)}%",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        val sunnyChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "日なた\n${"%.1f".format(sunnyHumid)}%",//中央に表示するテキスト
            centerTextSize = 16f,//中央に表示するテキストサイズ
            centerTextColor = Color.WHITE,//中央に表示するテキストカラー
            holeRadius = 75f,//中央の穴の半径
            holeColor = Color.TRANSPARENT//中央の塗りつぶし色
        )
        //DataSetフォーマット
        val indoorDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.rgb(243, 201, 14), Color.GRAY)//円の色
        )
        val kitchenDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.rgb(128, 255, 0), Color.GRAY)//円の色
        )
        val shadeDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.rgb(185, 122, 87), Color.GRAY)//円の色
        )
        val sunnyDSFormat = PieDataSetFormat(
            drawValue = false,
            valueTextColor = null,
            valueTextSize = null,
            valueTextFormatter = null,
            axisDependency = null,
            colors = listOf(Color.RED, Color.GRAY)//円の色
        )

        //屋内湿度を円グラフ用データに整形
        val (dimensionsIndoorHumid, valuesIndoorHumid) = makePieDashboardData(
            indoorHumid.toFloat(),
            0f,
            100f
        )
        //キッチン湿度を円グラフ用に整形
        val (dimensionsKitchenHumid, valuesKitchenHumid) = makePieDashboardData(
            kitchenHumid.toFloat(),
            0f,
            100f
        )
        //日陰湿度を円グラフ用に整形
        val (dimensionsShadeHumid, valuesShadeHumid) = makePieDashboardData(
            shadeHumid.toFloat(),
            0f,
            100f
        )
        //日なた湿度を円グラフ用に整形
        val (dimensionsSunnyHumid, valuesSunnyHumid) = makePieDashboardData(
            sunnyHumid.toFloat(),
            0f,
            100f
        )

        //①場所ごとにEntryのリストを作成
        val indoorEntries = makePieChartEntries(dimensionsIndoorHumid, valuesIndoorHumid)
        val kitchenEntries = makePieChartEntries(dimensionsKitchenHumid, valuesKitchenHumid)
        val shadeEntries = makePieChartEntries(dimensionsShadeHumid, valuesShadeHumid)
        val sunnyEntries = makePieChartEntries(dimensionsSunnyHumid, valuesSunnyHumid)
        //②～⑦円グラフ描画
        setupPieChart(
            indoorEntries, view.piechartHumidIndoorHumid, "室内温度",
            indoorChartFormat, indoorDSFormat
        )
        setupPieChart(
            kitchenEntries, view.piechartHumidKitchen, "室外温度",
            kitchenChartFormat, kitchenDSFormat
        )
        setupPieChart(
            shadeEntries, view.piechartHumidOutdoorShade, "室内湿度",
            shadeChartFormat, shadeDSFormat
        )
        setupPieChart(
            sunnyEntries, view.piechartHumidOutdoorSunny, "室外湿度",
            sunnyChartFormat, sunnyDSFormat
        )
    }

    //湿度推移データの描画
    private fun refreshHumidTimeSeriesData(
        context: Context,
        humidSeriesData: Map<String, MutableList<Pair<Date, Double>>>,
        view: View
    ) {
        //Chartフォーマット
        val humidLineChartFormat = LineChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor = null,//凡例文字色
            description = null,//グラフ説明
            bgColor = Color.BLACK,//背景塗りつぶし色
            touch = true,//タッチ有効
            xAxisEnabled = true,//X軸有効
            xAxisTextColor = Color.WHITE,//X軸文字色
            xAxisDateFormat = SimpleDateFormat("M/d H:mm"),//X軸の日付軸フォーマット(日付軸でないときnull指定)
            yAxisLeftEnabled = true,//左Y軸有効
            yAxisLeftTextColor = Color.WHITE,//左Y軸文字色
            yAxisRightEnabled = false,//右Y軸有効
            zoomDirection = "xy",//ズームの方向(x, y, xy, null=ズーム無効)
            zoomPinch = false,//ズームのピンチ動作をXY同時にするか(trueなら同時、falseなら1軸に限定)
            toolTipDirection = "xy",//ツールチップに表示する軸内容(x, y, xy, null=表示なし)
            toolTipDateFormat = SimpleDateFormat("M/d H:mm"),//ツールチップX軸表示の日付フォーマット(日付軸以外ならnull)
            toolTipUnitX = "",//ツールチップのX軸内容表示に付加する単位
            toolTipUnitY = "%",//ツールチップのY軸内容表示に付加する単位
            timeAccuracy = false//時系列グラフのX軸を正確にプロットするか
        )
        //DataSetフォーマット(カテゴリ名のMapで作成)
        val humidLineDataSetFormat: Map<String, LineDataSetFormat> = mapOf(
            "indoor" to LineDataSetFormat(
                false,
                null,
                null,
                null,
                null,
                Color.rgb(243, 201, 14),
                2f,
                null,
                false,
                null,
                null
            ),
            "kitchen" to LineDataSetFormat(
                false,
                null,
                null,
                null,
                null,
                Color.rgb(128, 255, 0),
                2f,
                null,
                false,
                null,
                null
            ),
            "outdoor_shade" to LineDataSetFormat(
                false,
                null,
                null,
                null,
                null,
                Color.rgb(185, 122, 87),
                2f,
                null,
                false,
                null,
                null
            ),
            "outdoor_sunny" to LineDataSetFormat(
                false,
                null,
                null,
                null,
                null,
                Color.RED,
                2f,
                null,
                false,
                null,
                null
            )
        )
        //①場所ごとに必要期間のデータ抜き出してEntryのリストに入力
        val places = humidSeriesData.keys//場所のリスト
        val allLinesEntries: MutableMap<String, MutableList<Entry>> = mutableMapOf()
        for(pl in places){
            //要素数が0なら処理を終了
            if(humidSeriesData[pl]?.size == 0) return
            //Entryにデータ入力
            val x = humidSeriesData[pl]?.map { it.first }!!//X軸（日時データ)
            val y = humidSeriesData[pl]?.map { it.second.toFloat() }!!//Y軸(温度データ)
            allLinesEntries[pl] = makeDateLineChartData(x, y, humidLineChartFormat.timeAccuracy)//日時と温度をEntryのリストに変換
        }

        //②～⑦グラフの作成
        setupLineChart(allLinesEntries, view.lineChartHumidTimeSeries, humidLineChartFormat, humidLineDataSetFormat, context)
    }

    //温度推移データの描画
    private fun refreshHumidStatsData(
        context: Context,
        humidSeriesData: Map<String, List<Pair<Date, Double>>>,
        view: View
    ){
        //要素数が0なら終了
        if(humidSeriesData["max"]?.size == 0) return

        //Chartフォーマット
        val candleChartFormat = CandleChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor = null,//凡例文字色
            description = null,//グラフ説明
            bgColor = Color.BLACK,//背景塗りつぶし色
            touch = true,//タッチ有効
            xAxisEnabled = true,//X軸有効
            xAxisTextColor = Color.WHITE,//X軸文字色
            xAxisDateFormat = SimpleDateFormat("M/d"),//X軸の日付軸フォーマット(日付軸でないときnull指定)
            yAxisLeftEnabled = true,//左Y軸有効
            yAxisLeftTextColor = Color.WHITE,//左Y軸文字色
            yAxisRightEnabled = false,//右Y軸有効
            zoomDirection = "xy",//ズームの方向(x, y, xy, null=ズーム無効)
            zoomPinch = false,//ズームのピンチ動作をXY同時にするか(trueなら同時、falseなら1軸に限定)
            toolTipDirection = null,//ツールチップに表示する軸内容(x, y, xy, null=表示なし)
            toolTipDateFormat = SimpleDateFormat("M/d"),//ツールチップX軸表示の日付フォーマット(日付軸以外ならnull)
            toolTipUnitX = "",//ツールチップのX軸内容表示に付加する単位
            toolTipUnitY = ""//ツールチップのY軸内容表示に付加する単位
        )
        //DataSetフォーマット
        val candleDSFormat = CandleDataSetFormat(
            true,//値の表示有無
            Color.rgb(220, 120, 80),//値表示の文字色
            7f,//値表示の文字サイズ
            "%.0f",//値表示の文字書式
            YAxis.AxisDependency.LEFT,//使用する軸
            Color.rgb(220, 120, 80),//細線部分の色
            2.5f,//細線部分の太さ
            Color.rgb(255, 100, 50),//Open>Close時の太線部分の色
            Paint.Style.FILL,//Open>Close時の太線部分の塗りつぶし形式
            null,//Open<Close時の太線部分の色
            null//Open<Close時の太線部分の塗りつぶし形式
        )

        //①必要データをEntryのリストに入力
        val x = humidSeriesData["max"]?.map{it.first}!!
        val yHigh = humidSeriesData["max"]?.map{it.second.toFloat()}!!
        val yLow = humidSeriesData["min"]?.map{it.second.toFloat()}!!
        val yMidHigh = humidSeriesData["avg"]?.map{it.second.toFloat()+0.2f}!!
        val yMidLow = humidSeriesData["avg"]?.map{it.second.toFloat()-0.2f}!!
        val candleEntries = makeDateCandleChartData(x, yHigh, yLow, yMidHigh, yMidLow)

        //②～⑦グラフの作成
        setupCandleStickChart(
            candleEntries,
            view.candleStickChartHumidStats,
            candleChartFormat,
            candleDSFormat,
            context
        )
    }
}