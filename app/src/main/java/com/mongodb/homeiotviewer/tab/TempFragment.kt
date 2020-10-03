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
import kotlinx.android.synthetic.main.fragment_tab_temp.view.*
import java.text.SimpleDateFormat
import java.util.*

class TempFragment(
    val newestSensorData: MutableList<TempHumidData>,
    val placeTempData: Map<String, MutableList<Pair<Date, Double>>>,
    val tempStatsData: Map<String, List<Pair<Date, Double>>>)
    : Fragment() {

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

    //現在の気温データの描画
    private fun refreshCurrentTempData(sensorData: MutableList<TempHumidData>, view: View) {
        //屋内、キッチン、日陰、日なたにデータを分ける
        val indoorData = sensorData.filter { it.place == "indoor" }
        val kitchenData = sensorData.filter { it.place == "kitchen" }
        val shadeData = sensorData.filter { it.place == "outdoor_shade" }
        val sunnyData = sensorData.filter { it.place == "outdoor_sunny" }
        //場所ごとの平均気温を計算
        val indoorTemp = indoorData.mapNotNull { it.temperature }.average()
        val kitchenTemp = kitchenData.mapNotNull { it.temperature }.average()
        val shadeTemp = shadeData.mapNotNull { it.temperature }.average()
        val sunnyTemp = sunnyData.mapNotNull { it.temperature }.average()

        //Chartフォーマット
        val indoorChartFormat = PieChartFormat(
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
        val kitchenChartFormat = PieChartFormat(
            legendFormat = null,//凡例形状
            legentTextColor =  Color.WHITE,//凡例文字色
            description =  null,//グラフ説明
            bgColor = null,//背景色
            touch = false,//タッチ操作の有効
            centerText = "ｷｯﾁﾝ\n${"%.1f".format(kitchenTemp)}°C",//中央に表示するテキスト
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
            centerText = "日陰\n${"%.1f".format(shadeTemp)}°C",//中央に表示するテキスト
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
            centerText = "日なた\n${"%.1f".format(sunnyTemp)}°C",//中央に表示するテキスト
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

        //屋内気温を円グラフ用データに整形
        val (dimensionsIndoorTemp, valuesIndoorTemp) = makePieDashboardData(
            indoorTemp.toFloat(),
            -10f,
            40f
        )
        //キッチン気温を円グラフ用に整形
        val (dimensionsKitchenTemp, valuesKitchenTemp) = makePieDashboardData(
            kitchenTemp.toFloat(),
            -10f,
            40f
        )
        //日陰気温を円グラフ用に整形
        val (dimensionsShadeTemp, valuesShadeTemp) = makePieDashboardData(
            shadeTemp.toFloat(),
            -10f,
            40f
        )
        //日なた気温を円グラフ用に整形
        val (dimensionsSunnyTemp, valuesSunnyTemp) = makePieDashboardData(
            sunnyTemp.toFloat(),
            -10f,
            40f
        )
        //①場所ごとにEntryのリストを作成
        val indoorEntries = makePieChartEntries(dimensionsIndoorTemp, valuesIndoorTemp)
        val kitchenEntries = makePieChartEntries(dimensionsKitchenTemp, valuesKitchenTemp)
        val shadeEntries = makePieChartEntries(dimensionsShadeTemp, valuesShadeTemp)
        val sunnyEntries = makePieChartEntries(dimensionsSunnyTemp, valuesSunnyTemp)
        //②～⑦円グラフ描画
        setupPieChart(
            indoorEntries, view.piechartTempIndoorTemp, "室内気温",
            indoorChartFormat, indoorDSFormat
        )
        setupPieChart(
            kitchenEntries, view.piechartTempKitchen, "室外気温",
            kitchenChartFormat, kitchenDSFormat
        )
        setupPieChart(
            shadeEntries, view.piechartTempOutdoorShade, "室内湿度",
            shadeChartFormat,shadeDSFormat
        )
        setupPieChart(
            sunnyEntries, view.piechartTempOutdoorSunny, "室外湿度",
            sunnyChartFormat, sunnyDSFormat
        )
    }

    //温度推移折れ線グラフの描画
    private fun refreshTempTimeSeriesData(
        context: Context,
        tempSeriesData: Map<String, MutableList<Pair<Date, Double>>>,
        view: View
    ) {
        //Chartフォーマット
        val tempLineChartFormat = LineChartFormat(
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
            toolTipUnitY = "°C",//ツールチップのY軸内容表示に付加する単位
            timeAccuracy = false//時系列グラフのX軸を正確にプロットするか
        )
        //DataSetフォーマット(カテゴリ名のMapで作成)
        val tempLineDataSetFormat: Map<String, LineDataSetFormat> = mapOf(
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
        val places = tempSeriesData.keys//場所のリスト
        val allLinesEntries: MutableMap<String, MutableList<Entry>> = mutableMapOf()
        for(pl in places){
            //要素数が0なら処理を終了
            if(tempSeriesData[pl]?.size == 0) return
            //Entryにデータ入力
            val x = tempSeriesData[pl]?.map { it.first }!!//X軸（日時データ)
            val y = tempSeriesData[pl]?.map { it.second.toFloat() }!!//Y軸(温度データ)
            allLinesEntries[pl] = makeDateLineChartData(x, y, tempLineChartFormat.timeAccuracy)//日時と温度をEntryのリストに変換
        }

        //②～⑦グラフの作成
        setupLineChart(allLinesEntries, view.lineChartTempTimeSeries, tempLineChartFormat, tempLineDataSetFormat, context)
    }

    //屋外日陰の最高最低気温推移ローソク足グラフの描画
    private fun refreshTempStatsData(
        context: Context,
        tempSeriesData: Map<String, List<Pair<Date, Double>>>,
        view: View
    ){
        //要素数が0なら終了
        if(tempSeriesData["max"]?.size == 0) return

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
        val x = tempSeriesData["max"]?.map{it.first}!!
        val yHigh = tempSeriesData["max"]?.map{it.second.toFloat()}!!
        val yLow = tempSeriesData["min"]?.map{it.second.toFloat()}!!
        val yMidHigh = tempSeriesData["avg"]?.map{it.second.toFloat()+0.2f}!!
        val yMidLow = tempSeriesData["avg"]?.map{it.second.toFloat()-0.2f}!!
        val candleEntries = makeDateCandleChartData(x, yHigh, yLow, yMidHigh, yMidLow)

        //②～⑦グラフの作成
        setupCandleStickChart(
            candleEntries,
            view.candleStickChartTempStats,
            candleChartFormat,
            candleDSFormat,
            context
        )
    }
}
