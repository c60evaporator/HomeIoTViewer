package com.mongodb.homeiotviewer.chart

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.mongodb.homeiotviewer.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 時系列グラフ用Entryのリスト作成
 * @param[x]:X軸のデータ(Date型)
 * @param[y]:Y軸のデータ(Float型)
 */
fun makeDateLineChartData(x: List<Date>, y: List<Float>): MutableList<Entry>{
    //出力用のMutableList<Entry>, ArrayList<String>
    var entryList = mutableListOf<Entry>()
    //xとyのサイズが異なるとき、エラーを出して終了
    if(x.size != y.size)
    {
        throw IllegalArgumentException("size of x and y are not equal")
    }
    //軸のデータを全てループ
    for(i in x.indices){
        entryList.add(
            Entry(i.toFloat(), y[i], x[i])
        )
    }
    return entryList
}

/**
 * 折れ線グラフ描画
 * @param[allLinesData]:折れ線グラフのデータ本体 ({折れ線1名称:ListOf(折れ線1のEntry), 折れ線2名称:ListOf(折れ線2のEntry), ‥}の構造)
 * @param[lineChart]:描画対象のLineChartビュー
 * @param[allLinesFormat]:グラフ全体のフォーマットを指定(独自クラスAllLinesFormatで記載)
 * @param[onelineFormat]:折れ線ごとのフォーマットを指定(placeをキーとして、独自クラスOneLineFormatのmapで記載)
 * @param[context]:呼び出し元のActivityあるいはFragmentのContext
 */
fun setupLineChart(
    allLinesData: MutableMap<String, MutableList<Entry>>,
    lineChart: LineChart,
    allLinesFormat: AllLinesFormat,
    oneLineFormat: Map<String, OneLineFormat>,
    context: Context
) {
    //xAxisDateFormatとtoolTipFormat.secondの日付指定有無が一致していないとき、
    if((allLinesFormat.xAxisDateFormat == null && allLinesFormat.toolTipFormat.second != null)
        || (allLinesFormat.xAxisDateFormat != null && allLinesFormat.toolTipFormat.second == null))
    {
        throw IllegalArgumentException("xAxisDateFormatとtoolTipFormat.secondのどちらかのみにnullを指定することはできません")
    }
    // 右Y軸は表示しない
    lineChart.axisRight.isEnabled = false
    //グラフ全体フォーマットの適用
    formatAllLines(lineChart, allLinesFormat, context)

    //日付軸の設定
    if(allLinesFormat.xAxisDateFormat != null) {
        //X軸ラベルのリスト取得
        val xLabel = allLinesData[allLinesData.keys.first()]?.map { allLinesFormat.xAxisDateFormat?.format(it.data) }
        //上記リストをX軸ラベルに設定
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabel)
    }

    //LineDataSetのリストを作成
    val lineDataSets = mutableListOf<ILineDataSet>()
    for((k, v) in allLinesData){
        var lineDataSet: ILineDataSet = LineDataSet(v, k).apply{
            //線ごとフォーマット適用
            formatOneLine(this, oneLineFormat[k]!!)
        }
        lineDataSets.add(lineDataSet)
    }

    //lineDataにlineDataSetsをセット
    val lineData = LineData(lineDataSets)
    //lineChartにlineDataをセット
    lineChart.data = lineData
    //linechart更新
    lineChart.notifyDataSetChanged()
    lineChart.invalidate()
}

fun formatAllLines(lineChart: LineChart, allLinesFormat: AllLinesFormat, context: Context){
    //凡例
    if(allLinesFormat.legendFormat.first != null){
        lineChart.legend.textColor = allLinesFormat.legendFormat.first!!
        lineChart.legend.form = allLinesFormat.legendFormat.second
    }
    else lineChart.legend.isEnabled = false //凡例無効
    //グラフ説明
    when(allLinesFormat.descFormat){
        "false" -> lineChart.description.isEnabled = false//非表示
    }
    //背景色(non nullのとき)
    if(allLinesFormat.bgColor != null) {
        lineChart.setBackgroundColor(allLinesFormat.bgColor!!)
    }
    //X軸ラベル表示有無
    if(allLinesFormat.xAxis.first == null) {
        lineChart.xAxis.isEnabled = false
    }
    //X軸の文字色
    else{
        lineChart.xAxis.textColor = allLinesFormat.xAxis.first!!
        //文字サイズ
        if(allLinesFormat.xAxis.second != null) {
            lineChart.xAxis.textSize = allLinesFormat.xAxis.second!!
        }
    }
    // 左Y軸の設定
    if(allLinesFormat.yAxisLeft == null){
        lineChart.axisLeft.isEnabled = false
    }
    else{
        lineChart.axisLeft.textColor = allLinesFormat.yAxisLeft!!
    }
    //左Y軸の表示範囲
    if(allLinesFormat.yAxisLeftMinMax.first != null){
        lineChart.axisLeft.axisMinimum = allLinesFormat.yAxisLeftMinMax.first!!
    }
    if(allLinesFormat.yAxisLeftMinMax.second != null){
        lineChart.axisLeft.axisMaximum = allLinesFormat.yAxisLeftMinMax.second!!
    }
    //タッチ動作
    lineChart.setTouchEnabled(allLinesFormat.touch)
    //ズーム設定
    when(allLinesFormat.zoom.first){
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
            lineChart.setPinchZoom(allLinesFormat.zoom.second)
        }
    }

    //ツールチップの表示
    if(allLinesFormat.toolTipFormat.first != null) {
        val mv: SimpleMarkerView = SimpleMarkerView(
            allLinesFormat.toolTipFormat,
            allLinesFormat.toolTipUnit,
            context,
            R.layout.simple_marker_view,
            20f
        )
        mv.chartView = lineChart
        lineChart.marker = mv
    }
}


fun formatOneLine(lineDataSet: LineDataSet, oneLineFormat: OneLineFormat){
    //線の色
    lineDataSet.color = oneLineFormat.lineColor
    //線形状のフォーマット
    if(oneLineFormat.lineShape.first != null)//線の幅
    {
        lineDataSet.lineWidth = oneLineFormat.lineShape.first!!
    }
    if(oneLineFormat.lineShape.second != null)//フィッティング法
    {
        lineDataSet.mode = oneLineFormat.lineShape.second!!
    }
    //データ点のフォーマット
    lineDataSet.setDrawCircles(oneLineFormat.circleFormat.first)
    if(oneLineFormat.circleFormat.first)
    {
        if(oneLineFormat.circleFormat.second != null){
            lineDataSet.setCircleColor(oneLineFormat.circleFormat.second!!)
        }
        if(oneLineFormat.circleFormat.third != null){
            lineDataSet.circleRadius = oneLineFormat.circleFormat.third!!
        }
    }
}

/**
 * 折れ線グラフ全体描画フォーマット指定用クラス
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[legendFormat]:凡例(1項目:文字色(nullなら凡例表示なし), 2項目:形状)
 * @param[descFormat]:グラフ説明(default:デフォルト, false:表示なし)
 * @param[bgColor]:塗りつぶし色(nullならデフォルト)
 * @param[xAxis]:X軸の設定(1項目:文字色(nullなら表示なし), 2項目:文字サイズ(nullならデフォルト))
 * @param[xAxisDateFormat]:X軸の日付軸設定(nullなら日付軸ではない)
 * @param[yAxisLeft]:左Y軸の文字色(nullなら表示なし)
 * @param[yAxisLeftMinMax]:左Y軸の表示範囲(1項目:最小値(nullならデフォルト), 2項目:最大値(nullならデフォルト))
 * @param[touch]:タッチ操作の有効(trueなら有効、falseなら無効)
 * @param[zoom]:ズーム設定(1項目:ズームの方向(null, x, y, xy), 2項目:ピンチ動作をXY同時にするか(trueなら同時、falseなら1軸に限定))
 * @param[toolTipFormat]:ツールチップ設定(1項目:表示内容(null, x, y, xy), 2項目:日付のフォーマット(日付軸のときのみ、それ以外ならnull))
 * @param[toolTipUnit]:ツールチップの表示単位(1項目:x軸, 2項目:y軸)
 */
class AllLinesFormat(){
    //プロパティ
    var legendFormat: Pair<Int?, Legend.LegendForm>//凡例 (文字色＋形状)
    var descFormat: String//グラフ説明
    var bgColor: Int?//塗りつぶし色（背景）
    var xAxis: Pair<Int?, Float?>//X軸の設定（文字色＋文字サイズ）
    var xAxisDateFormat: SimpleDateFormat?//X軸の日付軸設定
    var yAxisLeft: Int?//左Y軸の設定（文字色）
    var yAxisLeftMinMax: Pair<Float?, Float?>//左Y軸の表示範囲(最小値＋最大値)
    var touch: Boolean//タッチ操作
    var zoom: Pair<String?, Boolean>//ズーム設定(X方向＋ピンチ動作をXY同時にするか)
    var toolTipFormat: Pair<String?, SimpleDateFormat?>//ツールチップ表示
    var toolTipUnit: Pair<String, String>//ツールチップの表示単位
    //プライマリコンストラクタ（デフォルト状態）
    init{
        this.legendFormat = Pair(Color.BLACK, Legend.LegendForm.DEFAULT) //黒＋デフォルト
        this.descFormat = "default" //デフォルト
        this.bgColor = null//デフォルト
        this.xAxis = Pair(Color.BLACK, null)//黒＋デフォルト
        this.xAxisDateFormat = null//日付軸ではない
        this.yAxisLeft = Color.BLACK//黒
        this.yAxisLeftMinMax = Pair(null, null)//最大最小ともにデフォルト
        this.touch = true//タッチ有効
        this.zoom = Pair("xy", true)//どちらも許可
        this.toolTipFormat = Pair(null, null)//表示なし
        this.toolTipUnit = Pair("", "")//空文字
    }
    //セカンダリコンストラクタ（各フォーマットを指定）
    constructor(
        legendFormat: Pair<Int?, Legend.LegendForm>,
        descFormat: String,
        bgColor: Int?,
        xAxis: Pair<Int?, Float?>,
        xAxisDateFormat: SimpleDateFormat?,
        yAxisLeft: Int?,
        yAxisLeftMinMax: Pair<Float?, Float?>,
        touch: Boolean,
        zoom: Pair<String?, Boolean>,
        toolTipFormat: Pair<String?, SimpleDateFormat?>,
        toolTipUnit: Pair<String, String>
    ): this(){
        this.legendFormat = legendFormat
        this.descFormat = descFormat
        this.bgColor = bgColor
        this.xAxis = xAxis
        this.xAxisDateFormat = xAxisDateFormat
        this.yAxisLeft = yAxisLeft
        this.yAxisLeftMinMax = yAxisLeftMinMax
        this.touch = touch
        this.zoom = zoom
        this.toolTipFormat = toolTipFormat
        this.toolTipUnit = toolTipUnit
    }
}

/**
 * 折れ線グラフ線ごと描画フォーマット指定用クラス
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[lineColor]:線の色
 * @param[lineShape]:線形状のフォーマット(1項目:太さ(nullならデフォルト), 2項目:フィッティング法(nullならデフォルト))
 * @param[circleFormat]:データ点のフォーマット(1項目:有無(falseなら表示なし), 2項目:色(nullならデフォルト), 3項目:大きさ(nullならデフォルト))
 */
class OneLineFormat(){
    //プロパティ
    var lineColor: Int//線の色
    var lineShape: Pair<Float?, LineDataSet.Mode?>//線形状のフォーマット
    var circleFormat: Triple<Boolean, Int?, Float?>//データ点のフォーマット
    //プライマリコンストラクタ（デフォルト状態）
    init{
        this.lineColor = Color.BLUE//デフォルトは青
        this.lineShape = Pair(null, null) //線の幅、フィッティング法ともにデフォルト
        this.circleFormat = Triple(true, null, null)//デフォルト
    }
    //セカンダリコンストラクタ（各フォーマットを指定）
    constructor(
        lineColor: Int,
        lineShape: Pair<Float?, LineDataSet.Mode?>,
        circleFormat: Triple<Boolean, Int?, Float?>
    )
            : this(){
        this.lineColor = lineColor
        this.lineShape = lineShape
        this.circleFormat = circleFormat
    }
}