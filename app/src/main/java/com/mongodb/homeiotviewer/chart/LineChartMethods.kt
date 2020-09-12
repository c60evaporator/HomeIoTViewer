package com.mongodb.homeiotviewer.chart//プロジェクト構成に合わせ変更

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mongodb.homeiotviewer.R//プロジェクト構成に合わせ変更
import java.util.*

/**
 * 折れ線グラフ用Entryのリスト作成
 * @param[x]:X軸のデータ(Float型)
 * @param[y]:Y軸のデータ(Float型)
 */
fun makeLineChartData(x: List<Float>, y: List<Float>): MutableList<Entry> {
    //出力用のMutableList<Entry>
    var entryList = mutableListOf<Entry>()
    //xとyのサイズが異なるとき、エラーを出して終了
    if(x.size != y.size)
    {
        throw IllegalArgumentException("size of x and y are not equal")
    }
    //軸のデータを全てループしてEntryに格納
    for(i in x.indices){
        entryList.add(
            Entry(x[i], y[i])
        )
    }
    return entryList
}

/**
 * 時系列折れ線グラフ用Entryのリスト作成
 * @param[x]:X軸のデータ(Date型)
 * @param[y]:Y軸のデータ(Float型)
 */
fun makeDateLineChartData(x: List<Date>, y: List<Float>, timeAccuracy: Boolean): MutableList<Entry>{
    //出力用のMutableList<Entry>
    var entryList = mutableListOf<Entry>()
    //xとyのサイズが異なるとき、エラーを出して終了
    if(x.size != y.size)
    {
        throw IllegalArgumentException("size of x and y are not equal")
    }
    //軸のデータを全てループしてEntryに格納
    //全ラベル表示のとき
    if(!timeAccuracy){
        for(i in x.indices){
            entryList.add(
                Entry(i.toFloat(), y[i], x[i])
            )
        }
    }
    //最初と最後のラベルのみ表示するとき（精度重視）
    else{
        //日付をシリアル値に変換して規格化
        val xSerial = x.map{it.time.toFloat()}//シリアル値変換
        val maxSerial = xSerial.max()!!//最大値
        val minSerial = xSerial.min()!!//最小値
        val size = x.size//データの要素数
        val xFloat = xSerial.map { (it - minSerial) / (maxSerial - minSerial) * (size - 1) }//最小値が0、最大値がsize - 1となるよう規格化
        //entryListに入力
        for(i in x.indices){
            entryList.add(
                Entry(xFloat[i], y[i], x[i])
            )
        }
    }
    return entryList
}

/**
 * 折れ線グラフ描画
 * @param[allLinesEntries]:折れ線グラフのデータ本体 ({折れ線1名称:ListOf(折れ線1のEntry), 折れ線2名称:ListOf(折れ線2のEntry), ‥}の構造)
 * @param[lineChart]:描画対象のLineChartビュー
 * @param[allLinesFormat]:グラフ全体のフォーマットを指定(独自クラスAllLinesFormatで記載)
 * @param[onelineFormat]:折れ線ごとのフォーマットを指定(placeをキーとして、独自クラスOneLineFormatのmapで記載)
 * @param[context]:呼び出し元のActivityあるいはFragmentのContext
 */
fun setupLineChart(
    allLinesEntries: MutableMap<String, MutableList<Entry>>,
    lineChart: LineChart,
    lineChartFormat: LineChartFormat,
    dataSetFormats: Map<String, LineDataSetFormat>,
    context: Context
) {
    //xAxisDateFormatとtoolTipFormat.secondの日付指定有無が一致していないとき、例外を投げる
    if((lineChartFormat.xAxisDateFormat == null && lineChartFormat.toolTipDateFormat != null)
        || (lineChartFormat.xAxisDateFormat != null && lineChartFormat.toolTipDateFormat == null))
    {
        throw IllegalArgumentException("xAxisDateFormatとtoolTipFormat.secondのどちらかのみにnullを指定することはできません")
    }

    //②LineDataSetのリストを作成
    val lineDataSets = mutableListOf<ILineDataSet>()
    for((k, v) in allLinesEntries){
        var lineDataSet: ILineDataSet = LineDataSet(v, k).apply{
            //③DataSetフォーマット適用
            formatLineDataSet(this, dataSetFormats[k]!!)
        }
        lineDataSets.add(lineDataSet)
    }

    //④LineDataにLineDataSet格納
    val lineData = LineData(lineDataSets)
    //⑤LineChartにLineDataを格納
    lineChart.data = lineData
    //⑥グラフ全体フォーマットの適用
    formatLineChart(lineChart, lineChartFormat, context)
    //日付軸の設定
    if(lineChartFormat.xAxisDateFormat != null) {
        //全ラベル表示のとき
        if(!lineChartFormat.timeAccuracy){
            //X軸ラベルのリスト取得
            val xLabel = allLinesEntries[allLinesEntries.keys.first()]?.map { lineChartFormat.xAxisDateFormat?.format(it.data) }
            //上記リストをX軸ラベルに設定
            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabel)
        }
        //精度重視のとき
        else{
            //X軸ラベルのリスト取得
            val size = allLinesEntries[allLinesEntries.keys.first()]?.size!!
            val xLabel = allLinesEntries[allLinesEntries.keys.first()]?.mapIndexed { index, entry ->
                when(index){
                    0 -> lineChartFormat.xAxisDateFormat?.format(entry.data)
                    size - 1 -> lineChartFormat.xAxisDateFormat?.format(entry.data)
                    else -> ""
                }
            }
            //上記リストをX軸ラベルに設定
            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabel)
        }
    }
    //⑦LineChart更新
    lineChart.invalidate()
}


/**
 * ⑥Chartフォーマットの適用
 * @param[lineChart]:適用対象のlineChart
 * @param[lineChartFormat]:Chartフォーマット
 * @param[context]:呼び出し元のActivityあるいはFragmentのContext
 */
fun formatLineChart(lineChart: LineChart, lineChartFormat: LineChartFormat, context: Context){
    //凡例
    if(lineChartFormat.legendFormat != null){
        //凡例形状
        lineChart.legend.form = lineChartFormat.legendFormat
        //凡例文字色
        if(lineChartFormat.legentTextColor != null) {
            lineChart.legend.textColor = lineChartFormat.legentTextColor!!
        }
    }
    else lineChart.legend.isEnabled = false //凡例非表示のとき
    //グラフ説明
    if(lineChartFormat.description != null) {
        lineChart.description.text = lineChartFormat.description
    }
    else lineChart.description.isEnabled = false//グラフ説明非表示のとき
    //全体の背景色
    if(lineChartFormat.bgColor != null) {
        lineChart.setBackgroundColor(lineChartFormat.bgColor!!)
    }
    //タッチ動作
    lineChart.setTouchEnabled(lineChartFormat.touch)

    //X軸ラベルの設定
    if(lineChartFormat.xAxisEnabled) {
        lineChart.xAxis.isEnabled = true//X軸ラベル表示
        //文字色
        if(lineChartFormat.xAxisTextColor != null) {
            lineChart.xAxis.textColor = lineChartFormat.xAxisTextColor!!
        }
        //文字サイズ
        if(lineChartFormat.xAxisTextSize != null) {
            lineChart.xAxis.textSize = lineChartFormat.xAxisTextSize!!
        }
    }
    else lineChart.xAxis.isEnabled = false//X軸ラベル非表示のとき

    // 左Y軸ラベルの設定
    if(lineChartFormat.yAxisLeftEnabled){
        lineChart.axisLeft.isEnabled = true//左Y軸ラベル表示
        //文字色
        if(lineChartFormat.yAxisLeftTextColor != null) {
            lineChart.axisLeft.textColor = lineChartFormat.yAxisLeftTextColor!!
        }
        //文字サイズ
        if(lineChartFormat.yAxisLeftTextSize != null) {
            lineChart.axisLeft.textSize = lineChartFormat.yAxisLeftTextSize!!
        }
        //表示範囲の下限
        if(lineChartFormat.yAxisLeftMin != null){
            lineChart.axisLeft.axisMinimum = lineChartFormat.yAxisLeftMin!!
        }
        //表示範囲の上限
        if(lineChartFormat.yAxisLeftMax != null){
            lineChart.axisLeft.axisMaximum = lineChartFormat.yAxisLeftMax!!
        }
    }
    else lineChart.axisLeft.isEnabled = false//左Y軸ラベル非表示のとき

    // 右Y軸ラベルの設定
    if(lineChartFormat.yAxisRightEnabled){
        lineChart.axisRight.isEnabled = true//右Y軸ラベル表示
        //文字色
        if(lineChartFormat.yAxisRightTextColor != null) {
            lineChart.axisRight.textColor = lineChartFormat.yAxisRightTextColor!!
        }
        //文字サイズ
        if(lineChartFormat.yAxisRightTextSize != null) {
            lineChart.axisRight.textSize = lineChartFormat.yAxisRightTextSize!!
        }
    }
    else lineChart.axisRight.isEnabled = false//右Y軸ラベル非表示のとき

    //ズーム設定
    when(lineChartFormat.zoomDirection){
        null -> lineChart.setScaleEnabled(false)
        "x" -> {
            lineChart.setScaleXEnabled(true)
            lineChart.setScaleYEnabled(false)
        }
        "y" -> {
            lineChart.setScaleXEnabled(false)
            lineChart.setScaleYEnabled(true)
        }
        "xy" -> {
            lineChart.setScaleEnabled(true)
            lineChart.setPinchZoom(lineChartFormat.zoomPinch)
        }
    }

    //ツールチップの表示
    if(lineChartFormat.toolTipDirection != null) {
        val mv: SimpleMarkerView = SimpleMarkerView(
            lineChartFormat.toolTipDirection,
            lineChartFormat.toolTipDateFormat,
            lineChartFormat.toolTipUnitX,
            lineChartFormat.toolTipUnitY,
            context,
            R.layout.simple_marker_view,
            20f
        )
        mv.chartView = lineChart
        lineChart.marker = mv
    }
}

/**
 * ③DataSetフォーマットの適用
 * @param[lineDataSet]:適用対象のLineDataSet
 * @param[lineDataSetFormat]:DataSetフォーマット
 */
fun formatLineDataSet(lineDataSet: LineDataSet, lineDataSetFormat: LineDataSetFormat){
    //値のフォーマット
    //値表示するとき
    if(lineDataSetFormat.drawValue){
        lineDataSet.setDrawValues(true)
        //文字色
        if(lineDataSetFormat.valueTextColor != null) lineDataSet.valueTextColor = lineDataSetFormat.valueTextColor!!
        //文字サイズ
        if(lineDataSetFormat.valueTextSize != null) lineDataSet.valueTextSize = lineDataSetFormat.valueTextSize!!
        //文字書式の適用
        if(lineDataSetFormat.valueTextFormatter != null){
            lineDataSet.valueFormatter = object: ValueFormatter(){
                override fun getFormattedValue(value: Float): String{
                    return lineDataSetFormat.valueTextFormatter!!.format(value)
                }
            }
        }
    }
    //値表示しないとき
    else lineDataSet.setDrawValues(false)
    //使用する軸
    if(lineDataSetFormat.axisDependency != null) {
        lineDataSet.axisDependency = lineDataSetFormat.axisDependency
    }

    //線の色
    lineDataSet.color = lineDataSetFormat.lineColor
    //線の幅
    if(lineDataSetFormat.lineWidth != null)
    {
        lineDataSet.lineWidth = lineDataSetFormat.lineWidth!!
    }
    //フィッティング法
    if(lineDataSetFormat.fittingMode != null)
    {
        lineDataSet.mode = lineDataSetFormat.fittingMode!!
    }

    //データ点のフォーマット
    //データ点表示するとき
    if(lineDataSetFormat.drawCircles){
        lineDataSet.setDrawCircles(true)
        //データ点の色
        if(lineDataSetFormat.circleColor != null){
            lineDataSet.setCircleColor(lineDataSetFormat.circleColor!!)
        }
        if(lineDataSetFormat.circleRadius!= null){
            lineDataSet.circleRadius = lineDataSetFormat.circleRadius!!
        }
    }
    //データ点表示しないとき
    else lineDataSet.setDrawCircles(false)
}