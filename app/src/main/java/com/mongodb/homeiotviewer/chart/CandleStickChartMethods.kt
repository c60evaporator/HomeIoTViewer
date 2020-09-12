package com.mongodb.homeiotviewer.chart//プロジェクト構成に合わせ変更

import android.content.Context
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mongodb.homeiotviewer.R//プロジェクト構成に合わせ変更
import java.text.SimpleDateFormat
import java.util.*

/**
 * ローソク足グラフ用Entryのリスト作成
 * @param[x]:X軸のデータ(Float型)
 * @param[yHigh]:Y軸最大値(Float型)
 * @param[yLow]:Y軸最小値(Float型)
 * @param[yOpen]:開始時Y軸のデータ(Float型、箱ひげとして使用するときは第三四分位点)
 * @param[yClose]:終了時Y軸のデータ(Float型、箱ひげとして使用するときは第一四分位点)
 */
fun makeCandleChartData(x: List<Float>, yHigh: List<Float>, yLow: List<Float>, yOpen: List<Float>, yClose: List<Float>): MutableList<CandleEntry>{
    //出力用のMutableList<Entry>, ArrayList<String>
    var entryList = mutableListOf<CandleEntry>()
    //xとyのサイズが異なるとき、エラーを出して終了
    if(x.size != yHigh.size || x.size != yLow.size || x.size != yOpen.size || x.size != yClose.size)
    {
        throw IllegalArgumentException("size of x and y are not equal")
    }
    //軸のデータを全てループしてEntryに格納
    for(i in x.indices){
        entryList.add(
            CandleEntry(x[i],
                yHigh[i],
                yLow[i],
                yOpen[i],
                yClose[i])
        )
    }
    return entryList
}

/**
 * 時系列ローソク足グラフ用Entryのリスト作成
 * @param[x]:X軸のデータ(Date型)
 * @param[yHigh]:Y軸最大値(Float型)
 * @param[yLow]:Y軸最小値(Float型)
 * @param[yOpen]:開始時Y軸のデータ(Float型、箱ひげとして使用するときは第三四分位点)
 * @param[yClose]:終了時Y軸のデータ(Float型、箱ひげとして使用するときは第一四分位点)
 */
fun makeDateCandleChartData(x: List<Date>, yHigh: List<Float>, yLow: List<Float>, yOpen: List<Float>, yClose: List<Float>): MutableList<CandleEntry>{
    //出力用のMutableList<Entry>, ArrayList<String>
    var entryList = mutableListOf<CandleEntry>()
    //xとyのサイズが異なるとき、エラーを出して終了
    if(x.size != yHigh.size || x.size != yLow.size || x.size != yOpen.size || x.size != yClose.size)
    {
        throw IllegalArgumentException("size of x and y are not equal")
    }
    //軸のデータを全てループしてEntryに格納
    for(i in x.indices){
        entryList.add(
            CandleEntry(i.toFloat(),
                yHigh[i],
                yLow[i],
                yOpen[i],
                yClose[i],
                x[i])
        )
    }
    return entryList
}

/**
 * CandleStickグラフ描画
 * @param[candleEntries]:ローソク足グラフのデータ本体
 * @param[candleStickChart]:描画対象のCandleStickChartビュー
 * @param[label]:グラフのタイトルラベル
 * @param[candleFormat]ローソク足グラフのChartフォーマット
 * @param[candleDataSetFormat]ローソク足グラフのChartフォーマット
 * @param[context]:呼び出し元のActivityあるいはFragmentのContext
 */
fun setupCandleStickChart(
    candleEntries: MutableList<CandleEntry>,
    candleStickChart: CandleStickChart,
    candleChartFormat: CandleChartFormat,
    candleDataSetFormat: CandleDataSetFormat,
    context: Context
) {
    //xAxisDateFormatとtoolTipFormat.secondの日付指定有無が一致していないとき、例外を投げる
    if((candleChartFormat.xAxisDateFormat == null && candleChartFormat.toolTipDateFormat != null)
        || (candleChartFormat.xAxisDateFormat != null && candleChartFormat.toolTipDateFormat == null))
    {
        throw IllegalArgumentException("xAxisDateFormatとtoolTipFormat.secondのどちらかのみにnullを指定することはできません")
    }
    //xがdate型だがxAxisDateFormatがnullのとき、xAxisDateFormatおよびtoolTipDateFormatに仮フォーマット入力
    val dataType = candleEntries.firstOrNull()?.data?.javaClass
    if(dataType?.name == "java.util.Date" && candleChartFormat.xAxisDateFormat == null){
        candleChartFormat.xAxisDateFormat = SimpleDateFormat("M/d H:mm")
        candleChartFormat.toolTipDateFormat = SimpleDateFormat("M/d H:mm")
    }
    //X軸のラベルをリセット(過去のラベルが残る可能性があるため)
    candleStickChart.xAxis.valueFormatter = DefaultAxisValueFormatter(candleEntries.size)

    //②CandleDataSetを作成
    val candleDataSet = CandleDataSet(candleEntries, "SampleChandleData")
    //③CandleDataSetのフォーマット適用
    formatCandleDataSet(candleDataSet, candleDataSetFormat)
    //④CandleDataにCandDataSetを格納
    val candleData = CandleData(candleDataSet)
    //⑤CandleStickChartにCandleDataを格納
    candleStickChart.data = candleData
    //⑥Chartフォーマットの適用
    formatCandleChart(candleStickChart, candleChartFormat, context)
    //日付軸の設定
    if(candleChartFormat.xAxisDateFormat != null) {
        //X軸ラベルのリスト取得
        val xLabel = candleEntries.map { candleChartFormat.xAxisDateFormat?.format(it.data) }
        //上記リストをX軸ラベルに設定
        candleStickChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabel)
    }
    //⑦linechart更新
    candleStickChart.invalidate()
}

/**
 * ⑥Chartフォーマットの適用
 * @param[candleStickChart]:適用対象のCandleStickChart
 * @param[candleChartFormat]:Chartフォーマット
 * @param[context]:呼び出し元のActivityあるいはFragmentのContext
 */
fun formatCandleChart(candleStickChart: CandleStickChart, candleChartFormat: CandleChartFormat, context: Context){
    //凡例
    if(candleChartFormat.legendFormat != null){
        //凡例形状
        candleStickChart.legend.form = candleChartFormat.legendFormat
        //凡例文字色
        if(candleChartFormat.legentTextColor != null) {
            candleStickChart.legend.textColor = candleChartFormat.legentTextColor!!
        }
    }
    else candleStickChart.legend.isEnabled = false //凡例非表示のとき
    //グラフ説明
    if(candleChartFormat.description != null) {
        candleStickChart.description.text = candleChartFormat.description
    }
    else candleStickChart.description.isEnabled = false//グラフ説明非表示のとき
    //全体の背景色
    if(candleChartFormat.bgColor != null) {
        candleStickChart.setBackgroundColor(candleChartFormat.bgColor!!)
    }
    //タッチ動作
    candleStickChart.setTouchEnabled(candleChartFormat.touch)

    //X軸ラベルの設定
    if(candleChartFormat.xAxisEnabled) {
        candleStickChart.xAxis.isEnabled = true//X軸ラベル表示
        //文字色
        if(candleChartFormat.xAxisTextColor != null) {
            candleStickChart.xAxis.textColor = candleChartFormat.xAxisTextColor!!
        }
        //文字サイズ
        if(candleChartFormat.xAxisTextSize != null) {
            candleStickChart.xAxis.textSize = candleChartFormat.xAxisTextSize!!
        }
    }
    else candleStickChart.xAxis.isEnabled = false//X軸ラベル非表示のとき

    // 左Y軸ラベルの設定
    if(candleChartFormat.yAxisLeftEnabled){
        candleStickChart.axisLeft.isEnabled = true//左Y軸ラベル表示
        //文字色
        if(candleChartFormat.yAxisLeftTextColor != null) {
            candleStickChart.axisLeft.textColor = candleChartFormat.yAxisLeftTextColor!!
        }
        //文字サイズ
        if(candleChartFormat.yAxisLeftTextSize != null) {
            candleStickChart.axisLeft.textSize = candleChartFormat.yAxisLeftTextSize!!
        }
        //表示範囲の下限
        if(candleChartFormat.yAxisLeftMin != null){
            candleStickChart.axisLeft.axisMinimum = candleChartFormat.yAxisLeftMin!!
        }
        //表示範囲の上限
        if(candleChartFormat.yAxisLeftMax != null){
            candleStickChart.axisLeft.axisMaximum = candleChartFormat.yAxisLeftMax!!
        }
    }
    else candleStickChart.axisLeft.isEnabled = false//左Y軸ラベル非表示のとき

    // 右Y軸ラベルの設定
    if(candleChartFormat.yAxisRightEnabled){
        candleStickChart.axisRight.isEnabled = true//右Y軸ラベル表示
        //文字色
        if(candleChartFormat.yAxisRightTextColor != null) {
            candleStickChart.axisRight.textColor = candleChartFormat.yAxisRightTextColor!!
        }
        //文字サイズ
        if(candleChartFormat.yAxisRightTextSize != null) {
            candleStickChart.axisRight.textSize = candleChartFormat.yAxisRightTextSize!!
        }
    }
    else candleStickChart.axisRight.isEnabled = false//右Y軸ラベル非表示のとき

    //ズーム設定
    when(candleChartFormat.zoomDirection){
        null -> candleStickChart.setScaleEnabled(false)
        "x" -> {
            candleStickChart.setScaleXEnabled(true)
            candleStickChart.setScaleYEnabled(false)
        }
        "y" -> {
            candleStickChart.setScaleXEnabled(false)
            candleStickChart.setScaleYEnabled(true)
        }
        "xy" -> {
            candleStickChart.setScaleEnabled(true)
            candleStickChart.setPinchZoom(candleChartFormat.zoomPinch)
        }
    }

    //ツールチップの表示
    if(candleChartFormat.toolTipDirection != null) {
        val mv: SimpleMarkerView = SimpleMarkerView(
            candleChartFormat.toolTipDirection,
            candleChartFormat.toolTipDateFormat,
            candleChartFormat.toolTipUnitX,
            candleChartFormat.toolTipUnitY,
            context,
            R.layout.simple_marker_view,
            20f
        )
        mv.chartView = candleStickChart
        candleStickChart.marker = mv
    }
}

/**
 * ③DataSetフォーマットの適用
 * @param[candleDataSet]:適用対象のCandleDataSet
 * @param[candleDataSetFormat]:DataSetフォーマット
 */
fun formatCandleDataSet(candleDataSet: CandleDataSet, candleDataSetFormat: CandleDataSetFormat){
    //値のフォーマット
    //値表示するとき
    if(candleDataSetFormat.drawValue){
        candleDataSet.setDrawValues(true)
        //文字色
        if(candleDataSetFormat.valueTextColor != null) candleDataSet.valueTextColor = candleDataSetFormat.valueTextColor!!
        //文字サイズ
        if(candleDataSetFormat.valueTextSize != null) candleDataSet.valueTextSize = candleDataSetFormat.valueTextSize!!
        //文字書式の適用
        if(candleDataSetFormat.valueTextFormatter != null){
            candleDataSet.valueFormatter = object: ValueFormatter(){
                override fun getFormattedValue(value: Float): String{
                    return candleDataSetFormat.valueTextFormatter!!.format(value)
                }
            }
        }
    }
    //値表示しないとき
    else candleDataSet.setDrawValues(false)
    //使用する軸
    if(candleDataSetFormat.axisDependency != null) {
        candleDataSet.axisDependency = candleDataSetFormat.axisDependency
    }

    // 細線のフォーマット
    candleDataSet.shadowColor = candleDataSetFormat.shadowColor//色
    if(candleDataSetFormat.shadowWidth != null) {
        candleDataSet.shadowWidth = candleDataSetFormat.shadowWidth!!//太さ
    }
    //Open<Closeのときの太線フォーマット
    candleDataSet.decreasingColor = candleDataSetFormat.decreasingColor//色
    if(candleDataSetFormat.decreasingPaint != null) {
        candleDataSet.decreasingPaintStyle = candleDataSetFormat.decreasingPaint!!//塗りつぶし形式
    }
    //Open>Closeのときの太線フォーマット
    if(candleDataSetFormat.increasingColor != null) {
        candleDataSet.increasingColor = candleDataSetFormat.increasingColor!!//色
    }
    if(candleDataSetFormat.increasingPaint != null) {
        candleDataSet.increasingPaintStyle = candleDataSetFormat.increasingPaint!!//塗りつぶし形式
    }
}