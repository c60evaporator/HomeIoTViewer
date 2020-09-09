package com.mongodb.homeiotviewer.chart//プロジェクト構成に合わせ変更

import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
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
 * @param[pieFormat]:円グラフのフォーマットを指定(独自クラスPieFormatで記載)
 */
fun setupPieChart(pieEntries: MutableList<PieEntry>, pieChart: PieChart, label: String, pieFormat: PieFormat) {
    //②PieDataSetにデータ格納
    val pieDataSet = PieDataSet(pieEntries, label)
    //④PieDataにPieDataSetを格納
    val pieData = PieData(pieDataSet)
    //⑤piechartにPieDataをセット
    pieChart.data = pieData
    //③⑥円グラフ描画フォーマットの適用
    formatPie(pieChart, pieDataSet, pieFormat)
    //⑦piechart更新
    pieChart.invalidate()
}

/**
 * 円グラフ描画フォーマットの適用
 * @param[pieChart]:描画対象のPieChart部品
 * @param[pieDataSet]:描画対象のPieDataSet
 * @param[pieFormat]:円グラフのフォーマットを指定(独自クラスPieFormatで記載)
 */
fun formatPie(pieChart: PieChart, pieDataSet: PieDataSet, pieFormat: PieFormat){
    //凡例
    if(pieFormat.legendFormat.first != null){
        pieChart.legend.form = pieFormat.legendFormat.first
        pieChart.legend.textColor = pieFormat.legendFormat.second
    }
    else pieChart.legend.isEnabled = false //凡例無効
    //グラフ説明
    val desc = pieChart.description
    when(pieFormat.descFormat){
        "false" -> desc.isEnabled = false//非表示
    }
    //中央に表示する値
    if(pieFormat.centerText != "default"){
        pieChart.centerText = pieFormat.centerText
        //値のテキストのフォーマット（サイズ＋色）
        if(pieFormat.centerTextFormat.first != 0f) {
            pieChart.setCenterTextSize(pieFormat.centerTextFormat.first)
        }
        if(pieFormat.centerTextFormat.second != null) {
            pieChart.setCenterTextColor(pieFormat.centerTextFormat.second!!)
        }
    }
    //中央の塗りつぶし色(non nullのとき)
    if(pieFormat.bgColor.second != null) {
        pieChart.setHoleColor(pieFormat.bgColor.second!!)
    }
    //全体の背景色(non nullのとき)
    if(pieFormat.bgColor.first != null) {
        pieChart.setBackgroundColor(pieFormat.bgColor.first!!)
    }
    //太さ
    pieChart.holeRadius = 75f
    //グラフの色
    //val colorList: List<Int> = listOf(Color.RED, Color.TRANSPARENT)
    pieDataSet.colors = pieFormat.colorList
    //値を非表示に
    when(pieFormat.descFormat){
        "false" -> pieDataSet.setDrawValues(false)
    }
}

/**
 * 円グラフ描画フォーマット指定用クラス
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[legendFormat]:凡例(1項目:形状(nullなら凡例表示なし), 2項目:文字色)
 * @param[descFormat]:グラフ説明(default:デフォルト, false:表示なし)
 * @param[centerText]:中央に表示するテキスト(default:表示なし, それ以外:記載したテキストを表示)
 * @param[centerTextFormat]:中央に表示するテキストのフォーマット(1項目:テキストサイズ(0fならデフォルト), 2項目:色(nullならデフォルト))
 * @param[bgColor]:塗りつぶし色(1項目:背景色(nullならデフォルト), 2項目:中央の色(nullならデフォルト))
 * @param[colorList]:グラフの色(dimensionsの数だけ指定が必要)
 * @param[valueFormat]:値の表示(default:デフォルト, false:表示なし)
 */
class PieFormat(){
    //プロパティ
    var legendFormat: Pair<Legend.LegendForm?, Int>//凡例 (形状＋文字色)
    var descFormat: String//グラフ説明
    var centerText: String//中央のテキスト
    var centerTextFormat: Pair<Float, Int?>//中央のテキストのフォーマット（サイズ＋色）
    var bgColor: Pair<Int?, Int?>//塗りつぶし色（背景＋中央）
    var colorList: List<Int>//グラフの色
    var valueFormat: String//値の表示
    //プライマリコンストラクタ（デフォルト状態）
    init{
        this.legendFormat = Pair(Legend.LegendForm.DEFAULT, Color.BLACK) //デフォルト＋黒
        this.descFormat = "default" //デフォルト
        this.centerText = "default" //デフォルト
        this.centerTextFormat = Pair(0f, null)//デフォルト
        this.bgColor = Pair(null, null)//デフォルト
        this.colorList = ColorTemplate.COLORFUL_COLORS.toList()//デフォルトはCOLORFUL_COLORS
        this.valueFormat = "default" //デフォルト
    }
    //セカンダリコンストラクタ（各フォーマットを指定）
    constructor(legendFormat: Pair<Legend.LegendForm?, Int>,
                descFormat: String,
                centerText: String,
                centerTextFormat: Pair<Float, Int?>,
                bgColor: Pair<Int?, Int?>,
                colorList: List<Int>,
                valueFormat: String
    ): this(){
        this.legendFormat = legendFormat
        this.descFormat = descFormat
        this.centerText = centerText
        this.centerTextFormat = centerTextFormat
        this.bgColor = bgColor
        this.colorList = colorList
        this.valueFormat = valueFormat
    }
}
