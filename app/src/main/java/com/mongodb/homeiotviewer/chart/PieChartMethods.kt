package com.mongodb.homeiotviewer.chart//プロジェクト構成に合わせ変更

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.max

/**
 * ダッシュボード風円グラフ用データ作成
 * @param[displayValue]:描画対象の値
 * @param[tempLowerThresh]:値の下限(円グラフの0%に相当)
 * @param[tempUpperThresh]:値の上限(円グラフの100%に相当)
 */
fun makePieDashboardData(displayValue: Float, tempLowerThresh: Float, tempUpperThresh: Float): Pair<List<String>, List<Float>> {
    val dimensions: List<String> = listOf(" ", "")//ラベルを表示したくないので、スペースをディメンジョンに指定
    val values: List<Float> = listOf(max(displayValue - tempLowerThresh,0f), max(tempUpperThresh - displayValue,0f))
    return Pair(dimensions, values)
}

/**
 * 円グラフ用Entryのリスト作成
 * @param[dimensions]:分割円の名称(String型)
 * @param[values]:分割円の大きさ(Float型)
 */
fun makePieChartEntries(dimensions: List<String>, values: List<Float>): MutableList<PieEntry> {
    //出力用のMutableList<Entry>
    var entryList = mutableListOf<PieEntry>()
    //dimensionsとvaluesのサイズが異なるとき、エラーを出して終了
    if(dimensions.size != values.size)
    {
        throw IllegalArgumentException("size of labels and values are not equal")
    }
    //データを全てループしてEntryに格納
    for (i in values.indices) {
        entryList.add( PieEntry(values[i], dimensions[i]) )
    }
    return entryList
}

/**
 * 円グラフ描画
 * @param[pieChartData]:円グラフのデータ本体
 * @param[pieChart]:描画対象のPieChart部品
 * @param[label]:グラフのタイトルラベル
 * @param[pieChartFormat]:円グラフのChartフォーマット
 * @param[pieDataSetFormat]:円グラフのDataSetフォーマット
 */
fun setupPieChart(pieEntries: MutableList<PieEntry>,
                  pieChart: PieChart,
                  label: String,
                  pieChartFormat: PieChartFormat,
                  pieDataSetFormat: PieDataSetFormat
) {
    //②PieDataSetにデータ格納
    val pieDataSet = PieDataSet(pieEntries, label)
    //③PieDataSetにフォーマット指定
    formatPieDataSet(pieDataSet, pieDataSetFormat)
    //④PieDataにPieDataSetを格納
    val pieData = PieData(pieDataSet)
    //⑤piechartにPieDataをセット
    pieChart.data = pieData
    //⑥円グラフ描画フォーマットの適用
    formatPieChart(pieChart, pieChartFormat)
    //⑦piechart更新
    pieChart.invalidate()
}

/**
 * ⑥Chartフォーマットの適用
 * @param[pieChart]:適用対象のPieChart
 * @param[pieChartFormat]:Chartフォーマット
 */
fun formatPieChart(pieChart: PieChart, pieChartFormat: PieChartFormat){
    //凡例
    if(pieChartFormat.legendFormat != null){
        //凡例形状
        pieChart.legend.form = pieChartFormat.legendFormat
        //凡例文字色
        if(pieChartFormat.legentTextColor != null) pieChart.legend.textColor = pieChartFormat.legentTextColor!!
    }
    else pieChart.legend.isEnabled = false //凡例非表示のとき
    //グラフ説明
    if(pieChartFormat.description != null) {
        pieChart.description.text = pieChartFormat.description
    }
    else pieChart.description.isEnabled = false//グラフ説明非表示のとき
    //全体の背景色
    if(pieChartFormat.bgColor != null) pieChart.setBackgroundColor(pieChartFormat.bgColor!!)
    //タッチ動作
    pieChart.setTouchEnabled(pieChartFormat.touch)

    //中央に表示する値
    if(pieChartFormat.centerText != null){
        //表示するテキスト
        pieChart.centerText = pieChartFormat.centerText
        //値のテキストサイズ
        if(pieChartFormat.centerTextSize != null) pieChart.setCenterTextSize(pieChartFormat.centerTextSize!!)
        //値のテキストカラー
        if(pieChartFormat.centerTextColor != null) pieChart.setCenterTextColor(pieChartFormat.centerTextColor!!)
    }
    //中央の塗りつぶし色
    if(pieChartFormat.centorColor != null) pieChart.setHoleColor(pieChartFormat.centorColor!!)
    //中央の穴の半径
    if(pieChartFormat.holeRadius != null) pieChart.holeRadius = pieChartFormat.holeRadius!!
}

/**
 * ③DataSetフォーマットの適用
 * @param[pieDataSet]:適用対象のPieDataSet
 * @param[pieDataSetFormat]:DataSetフォーマット
 */
fun formatPieDataSet(pieDataSet: PieDataSet, pieDataSetFormat: PieDataSetFormat){
    //値のフォーマット
    //値表示するとき
    if(pieDataSetFormat.drawValue){
        pieDataSet.setDrawValues(true)
        //文字色
        if(pieDataSetFormat.valueTextColor != null) pieDataSet.valueTextColor = pieDataSetFormat.valueTextColor!!
        //文字サイズ
        if(pieDataSetFormat.valueTextSize != null) pieDataSet.valueTextSize = pieDataSetFormat.valueTextSize!!
        //文字書式の適用
        if(pieDataSetFormat.valueTextFormatter != null){
            pieDataSet.valueFormatter = object: ValueFormatter(){
                override fun getFormattedValue(value: Float): String{
                    return pieDataSetFormat.valueTextFormatter!!.format(value)
                }
            }
        }
    }
    //値表示しないとき
    else pieDataSet.setDrawValues(false)
    //使用する軸
    if(pieDataSetFormat.axisDependency != null) {
        pieDataSet.axisDependency = pieDataSetFormat.axisDependency
    }
    //グラフの色
    pieDataSet.colors = pieDataSetFormat.colorList
}