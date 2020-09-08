package com.mongodb.homeiotviewer.chart//プロジェクト構成に合わせ変更

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mongodb.homeiotviewer.R//プロジェクト構成に合わせ変更
import java.text.SimpleDateFormat
import java.util.*

/**
 * Candleグラフ用Entryのリスト作成
 * @param[x]:X軸のデータ(String型 → Date型でないので注意)
 * @param[yHigh]:Y軸最大値(Float型)
 * @param[yLow]:Y軸最小値(Float型)
 * @param[yOpen]:開始時Y軸のデータ(Float型、箱ひげとして使用するときは第三四分位点)
 * @param[yClose]:終了時Y軸のデータ(Float型、箱ひげとして使用するときは第一四分位点)
 */
fun makeDateLineChartData(x: List<Date>, yHigh: List<Float>, yLow: List<Float>, yOpen: List<Float>, yClose: List<Float>): MutableList<CandleEntry>{
    //出力用のMutableList<Entry>, ArrayList<String>
    var entryList = mutableListOf<CandleEntry>()
    //xとyのサイズが異なるとき、エラーを出して終了
    if(x.size != yHigh.size || x.size != yLow.size || x.size != yOpen.size || x.size != yClose.size)
    {
        throw IllegalArgumentException("size of x and y are not equal")
    }
    //軸のデータを全てループ
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
 * @param[candleData]:CandleStickグラフのデータ本体
 * @param[candleStickChart]:描画対象のCandleStickChartビュー
 * @param[candleFormat]:グラフのフォーマットを指定
 * @param[context]:呼び出し元のActivityあるいはFragmentのContext
 */
fun setupCandleStickChart(
    candleData: MutableList<CandleEntry>,
    candleStickChart: CandleStickChart,
    candleFormat: CandleFormat,
    context: Context
) {
    //xAxisDateFormatとtoolTipFormat.secondの日付指定有無が一致していないとき、
    if((candleFormat.xAxisDateFormat == null && candleFormat.toolTipFormat.second != null)
        || (candleFormat.xAxisDateFormat != null && candleFormat.toolTipFormat.second == null))
    {
        throw IllegalArgumentException("xAxisDateFormatとtoolTipFormat.secondのどちらかのみにnullを指定することはできません")
    }

    //日付軸の設定
    if(candleFormat.xAxisDateFormat != null) {
        //X軸ラベルのリスト取得
        val xLabel = candleData.map { candleFormat.xAxisDateFormat?.format(it.data) }
        //上記リストをX軸ラベルに設定
        candleStickChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabel)
    }

    //candleDataSetを作成
    val candleDataSet = CandleDataSet(candleData, "SampleChandleData")

    //グラフ全体フォーマットの適用
    formatCandle(candleStickChart, candleDataSet, candleFormat, context)

    //candleStickDataにcandleDataSetをセット
    val candleStickData = CandleData(candleDataSet)
    //candleStickChartにcandleStickDataをセット
    candleStickChart.data = candleStickData
    //linechart更新
    candleStickChart.notifyDataSetChanged()
    candleStickChart.invalidate()
}

fun formatCandle(candleStickChart: CandleStickChart, candleDataSet: CandleDataSet, candleFormat: CandleFormat, context: Context){
    //凡例
    if(candleFormat.legendFormat.first != null){
        candleStickChart.legend.textColor = candleFormat.legendFormat.first!!
        candleStickChart.legend.form = candleFormat.legendFormat.second
    }
    else candleStickChart.legend.isEnabled = false //凡例無効
    //グラフ説明
    when(candleFormat.descFormat){
        "false" -> candleStickChart.description.isEnabled = false//非表示
    }
    //背景色(non nullのとき)
    if(candleFormat.bgColor != null) {
        candleStickChart.setBackgroundColor(candleFormat.bgColor!!)
    }
    //X軸ラベル表示有無
    if(candleFormat.xAxis.first == null) {
        candleStickChart.xAxis.isEnabled = false
    }
    //X軸の文字色
    else{
        candleStickChart.xAxis.textColor = candleFormat.xAxis.first!!
        //文字サイズ
        if(candleFormat.xAxis.second != null) {
            candleStickChart.xAxis.textSize = candleFormat.xAxis.second!!
        }
    }
    // 左Y軸の設定
    if(candleFormat.yAxisLeft == null){
        candleStickChart.axisLeft.isEnabled = false
    }
    else{
        candleStickChart.axisLeft.textColor = candleFormat.yAxisLeft!!
    }
    //左Y軸の表示範囲
    if(candleFormat.yAxisLeftMinMax.first != null){
        candleStickChart.axisLeft.axisMinimum = candleFormat.yAxisLeftMinMax.first!!
    }
    if(candleFormat.yAxisLeftMinMax.second != null){
        candleStickChart.axisLeft.axisMaximum = candleFormat.yAxisLeftMinMax.second!!
    }
    // 右Y軸は表示しない
    candleStickChart.axisRight.isEnabled = false
    //タッチ動作
    candleStickChart.setTouchEnabled(candleFormat.touch)
    //ズーム設定
    when(candleFormat.zoom.first){
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
            candleStickChart.setPinchZoom(candleFormat.zoom.second)
        }
    }

    //ツールチップの表示
    if(candleFormat.toolTipFormat.first != null) {
        val mv: SimpleMarkerView = SimpleMarkerView(
            candleFormat.toolTipFormat,
            candleFormat.toolTipUnit,
            context,
            R.layout.simple_marker_view,
            20f
        )
        mv.chartView = candleStickChart
        candleStickChart.marker = mv
    }

    //candleDataSetにフォーマット適用
    candleDataSet.apply {
        axisDependency = YAxis.AxisDependency.LEFT//左側軸を使用
        setDrawIcons(false)//アイコン描画しない
        //細線のフォーマット
        shadowColor = candleFormat.shadowFormat.first//色
        if(candleFormat.shadowFormat.second != null) shadowWidth = candleFormat.shadowFormat.second!!//太さ
        //shadowColor = Color.rgb(220, 120, 80)
        //shadowWidth = 2.5f
        //Open<Closeのときの太線フォーマット
        decreasingColor = candleFormat.decreasingFormat.first//色
        if(candleFormat.decreasingFormat.second != null) decreasingPaintStyle = candleFormat.decreasingFormat.second!!//太さ
        //decreasingColor = Color.rgb(255, 100, 50)
        //decreasingPaintStyle = Paint.Style.FILL
        //Open>Closeのときの太線フォーマット
        if(candleFormat.increasingFormat.first != null){
            increasingColor = candleFormat.increasingFormat.first!!//色
            if(candleFormat.increasingFormat.second != null) increasingPaintStyle = candleFormat.increasingFormat.second!!//太さ
        }
        //値表示のフォーマット
        if(candleFormat.valueTextFormat.first != null){
            setDrawValues(true)
            valueTextColor = candleFormat.valueTextFormat.first!!//色
            if(candleFormat.valueTextFormat.second != null) valueTextSize = candleFormat.valueTextFormat.second!!//文字サイズ
            //文字書式
            if(candleFormat.valueTextFormatter != null){
                valueFormatter = object: ValueFormatter(){
                    override fun getFormattedValue(value: Float): String{
                        return candleFormat.valueTextFormatter!!.format(value)
                    }
                }
            }
        }
        //setDrawValues(true)
        //valueTextColor = Color.rgb(220, 120, 80)
    }
}

/**
 * 折れ線グラフ線ごと描画フォーマット指定用クラス
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
 * @param[shadowFormat]:細線部分のフォーマット(1項目:色, 2項目:太さ(nullならデフォルト))
 * @param[decreasingFormat]:太線部分でOpen>Closeのときのフォーマット、箱ひげとして使用するときはこちらを使用(1項目:色, 2項目:太さ(nullならデフォルト))
 * @param[increasingFormat]:太線部分でOpen<Closeのときのフォーマット、箱ひげとして使用するときは不使用(1項目:色(nullなら不使用), 2項目:太さ(nullならデフォルト))
 * @param[valueTextFormat]:値表示のフォーマット(1項目:文字色(nullなら非表示), 2項目:文字サイズ(nullならデフォルト))
 * @param[valueTextFormatter]:値表示の文字書式(nullならデフォルト)
 */
class CandleFormat(){
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
    var shadowFormat: Pair<Int, Float?>//細線部分のフォーマット(色＋太さ)
    var decreasingFormat: Pair<Int, Paint.Style?>//Open>Closeのときの太線部分フォーマット(色＋塗りつぶし形式)
    var increasingFormat: Pair<Int?, Paint.Style?>//Open<Closeのときの太線部分フォーマット(色＋塗りつぶし形式)
    var valueTextFormat: Pair<Int?, Float?>//値表示のフォーマット(文字色＋文字サイズ)
    var valueTextFormatter: String?//値表示の文字書式
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
        this.shadowFormat = Pair(Color.BLACK, null)//黒＋デフォルト
        this.decreasingFormat = Pair(Color.BLACK, null)//黒＋デフォルト
        this.increasingFormat = Pair(Color.BLACK, null)//黒＋デフォルト
        this.valueTextFormat = Pair(Color.BLACK, null)//黒＋デフォルト
        this.valueTextFormatter = null//デフォルト
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
        toolTipUnit: Pair<String, String>,
        shadowFormat: Pair<Int, Float?>,
        decreasingFormat: Pair<Int, Paint.Style?>,
        increasingFormat: Pair<Int?, Paint.Style?>,
        valueTextFormat: Pair<Int?, Float?>,
        valueTextFormatter: String?
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
        this.shadowFormat = shadowFormat
        this.decreasingFormat = decreasingFormat
        this.increasingFormat = increasingFormat
        this.valueTextFormat = valueTextFormat
        this.valueTextFormatter = valueTextFormatter
    }
}