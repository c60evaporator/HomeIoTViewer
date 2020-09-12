package com.mongodb.homeiotviewer.chart

import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat

/**
 * Chartフォーマット指定の基底クラス
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[legendFormat]:凡例形状(nullなら凡例表示なし)
 * @param[legendTextColor]:凡例文字色(nullならデフォルト)
 * @param[description]:グラフ説明(nullなら表示なし)
 * @param[bgColor]:背景色(nullならデフォルト)
 * @param[touch]:タッチ操作の有効(trueなら有効、falseなら無効)
 */
open class ChartFormat (
    //プロパティ
    var legendFormat: Legend.LegendForm? = Legend.LegendForm.DEFAULT,//凡例形状(nullなら非表示)
    var legentTextColor: Int? = null,//凡例文字色(nullならデフォルト)
    var description: String? = null,//グラフ説明(nullなら表示なし)
    var bgColor: Int? = null,//背景色(nullならデフォルト)
    var touch: Boolean = true//タッチ操作の有効(trueなら有効、falseなら無効)
)

/**
 * XY軸系Chartフォーマット指定用クラス(ChartFormatを継承)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[xAxisEnabled]:X軸の有効無効
 * @param[xAxisTextColor]:X軸文字色(nullならデフォルト)
 * @param[xAxisTextSize]:X軸文字サイズ(nullならデフォルト)
 * @param[xAxisDateFormat]:X軸の日付軸フォーマット(nullなら日付軸ではない)
 * @param[yAxisLeftEnabled]:左Y軸の有効無効
 * @param[yAxisLeftTextColor]:左Y軸の文字色(nullならデフォルト)
 * @param[yAxisLeftTextSize]:左Y軸の文字サイズ(nullならデフォルト)
 * @param[yAxisLeftMin]:左Y軸の表示範囲下限(nullならデフォルト)
 * @param[yAxisLeftMax]:左Y軸の表示範囲上限(nullならデフォルト)
 * @param[yAxisRightEnabled]:右Y軸の有効無効
 * @param[yAxisRightTextColor]:右Y軸の文字色(nullならデフォルト)
 * @param[yAxisRightTextSize]:右Y軸の文字サイズ(nullならデフォルト)
 * @param[zoomDirection]:ズームの方向(null, x, y, xy)
 * @param[zoomPinch]:ズームのピンチ動作をXY同時にするか(trueなら同時、falseなら1軸に限定)
 * @param[toolTipDirection]:ツールチップに表示する軸内容(null, x, y, xy)
 * @param[toolTipDateFormat]:ツールチップX軸表示の日付フォーマット(日付軸のときのみ、それ以外ならnull)
 * @param[toolTipUnitX]:ツールチップのX軸表示に付加する単位
 * @param[toolTipUnitY]:ツールチップのY軸表示に付加する単位
 */
open class XYChartFormat(
    legendFormat: Legend.LegendForm? = Legend.LegendForm.DEFAULT,
    legentTextColor: Int? = null,
    description: String? = null,
    bgColor: Int? = null,
    touch: Boolean = true,
    var xAxisEnabled: Boolean = true,//X軸の有効無効
    var xAxisTextColor: Int? = Color.BLACK,//X軸文字色(nullなら表示なし)
    var xAxisTextSize: Float? = null,//X軸文字サイズ(nullならデフォルト)
    var xAxisDateFormat: SimpleDateFormat? = null,//X軸の日付軸フォーマット(nullなら日付軸ではない)
    var yAxisLeftEnabled: Boolean = true,//左Y軸の有効無効
    var yAxisLeftTextColor: Int? = Color.BLACK,//左Y軸の文字色(nullなら表示なし)
    var yAxisLeftTextSize: Float? = null,//左Y軸の文字サイズ(nullならデフォルト)
    var yAxisLeftMin: Float? = null,//左Y軸の表示範囲下限(nullならデフォルト)
    var yAxisLeftMax: Float? = null,//左Y軸の表示範囲上限(nullならデフォルト)
    var yAxisRightEnabled: Boolean = true,//右Y軸の有効無効
    var yAxisRightTextColor: Int? = null,//右Y軸の文字色(nullなら表示なし)
    var yAxisRightTextSize: Float? = null,//右Y軸の文字サイズ(nullならデフォルト)
    var zoomDirection: String? = "xy",//ズームの方向(null, x, y, xy)
    var zoomPinch: Boolean = false,//ズームのピンチ動作をXY同時にするか(trueなら同時、falseなら1軸に限定)
    var toolTipDirection: String? = null,//ツールチップに表示する軸内容(null, x, y, xy)
    var toolTipDateFormat: SimpleDateFormat? = null,//ツールチップX軸表示の日付フォーマット(日付軸以外ならnull)
    var toolTipUnitX: String = "",//ツールチップのX軸内容表示に付加する単位
    var toolTipUnitY: String = ""//ツールチップのY軸内容表示に付加する単位
): ChartFormat(legendFormat, legentTextColor, description, bgColor, touch)

/**
 * 円グラフChartフォーマット指定用クラス(ChartFormatを継承)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[centerText]:中央に表示するテキスト(nullなら表示なし)
 * @param[centerTextSize]:中央に表示するテキストサイズ(nullならデフォルト)
 * @param[centerTextColor]:中央に表示するテキストカラー(nullならデフォルト)
 * @param[holeRadius]:中央の穴の半径(nullならデフォルト)
 * @param[centerColor]:中央の塗りつぶし色(nullならデフォルト)
 */
class PieChartFormat(
    legendFormat: Legend.LegendForm? = Legend.LegendForm.DEFAULT,
    legentTextColor: Int? = null,
    description: String? = null,
    bgColor: Int? = null,
    touch: Boolean = true,
    var centerText: String? = null,//中央に表示するテキスト(nullなら表示なし)
    var centerTextSize: Float? = null,//中央に表示するテキストサイズ(nullならデフォルト)
    var centerTextColor: Int? = null,//中央に表示するテキストカラー(nullならデフォルト)
    var holeRadius: Float? = null,//中央の穴の半径
    var centorColor: Int? = null//中央の塗りつぶし色
): ChartFormat(legendFormat, legentTextColor, description, bgColor, touch)

/**
 * ローソク足グラフChartフォーマット指定用クラス(XYChartFormatと同じ)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 */
class CandleChartFormat(
    legendFormat: Legend.LegendForm? = Legend.LegendForm.DEFAULT,
    legentTextColor: Int? = null,
    description: String? = null,
    bgColor: Int? = null,
    touch: Boolean = true,
    xAxisEnabled: Boolean = true,
    xAxisTextColor: Int? = Color.BLACK,
    xAxisTextSize: Float? = null,
    xAxisDateFormat: SimpleDateFormat? = null,
    yAxisLeftEnabled: Boolean = true,
    yAxisLeftTextColor: Int? = Color.BLACK,
    yAxisLeftTextSize: Float? = null,
    yAxisLeftMin: Float? = null,
    yAxisLeftMax: Float? = null,
    yAxisRightEnabled: Boolean = true,
    yAxisRightTextColor: Int? = null,
    yAxisRightTextSize: Float? = null,
    zoomDirection: String? = "xy",
    zoomPinch: Boolean = false,
    toolTipDirection: String? = null,
    toolTipDateFormat: SimpleDateFormat? = null,
    toolTipUnitX: String = "",
    toolTipUnitY: String = ""
): XYChartFormat(legendFormat, legentTextColor, description, bgColor, touch,
    xAxisEnabled, xAxisTextColor, xAxisTextSize, xAxisDateFormat,
    yAxisLeftEnabled, yAxisLeftTextColor, yAxisLeftTextSize, yAxisLeftMin, yAxisLeftMax,
    yAxisRightEnabled, yAxisRightTextColor, yAxisRightTextSize,
    zoomDirection, zoomPinch,
    toolTipDirection, toolTipDateFormat, toolTipUnitX, toolTipUnitY)

/**
 * 折れ線グラフChartフォーマット指定用クラス(XYChartFormatを継承)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[timeAccuracy]:時系列グラフのX軸を正確にプロットするか(trueなら正確に表示するがラベルは最大最小値のみ、falseなら正確性落ちるが全ラベル表示)
 */
class LineChartFormat(
    legendFormat: Legend.LegendForm? = Legend.LegendForm.DEFAULT,
    legentTextColor: Int? = null,
    description: String? = null,
    bgColor: Int? = null,
    touch: Boolean = true,
    xAxisEnabled: Boolean = true,
    xAxisTextColor: Int? = Color.BLACK,
    xAxisTextSize: Float? = null,
    xAxisDateFormat: SimpleDateFormat? = null,
    yAxisLeftEnabled: Boolean = true,
    yAxisLeftTextColor: Int? = Color.BLACK,
    yAxisLeftTextSize: Float? = null,
    yAxisLeftMin: Float? = null,
    yAxisLeftMax: Float? = null,
    yAxisRightEnabled: Boolean = true,
    yAxisRightTextColor: Int? = null,
    yAxisRightTextSize: Float? = null,
    zoomDirection: String? = "xy",
    zoomPinch: Boolean = false,
    toolTipDirection: String? = null,
    toolTipDateFormat: SimpleDateFormat? = null,
    toolTipUnitX: String = "",
    toolTipUnitY: String = "",
    val timeAccuracy: Boolean = false//時系列グラフのX軸を正確にプロットするか
): XYChartFormat(legendFormat, legentTextColor, description, bgColor, touch,
    xAxisEnabled, xAxisTextColor, xAxisTextSize, xAxisDateFormat,
    yAxisLeftEnabled, yAxisLeftTextColor, yAxisLeftTextSize, yAxisLeftMin, yAxisLeftMax,
    yAxisRightEnabled, yAxisRightTextColor, yAxisRightTextSize,
    zoomDirection, zoomPinch,
    toolTipDirection, toolTipDateFormat, toolTipUnitX, toolTipUnitY)

/**
 * DataSetフォーマット指定の規定クラス
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[valueTextColor]:値表示の文字色(nullなら非表示)
 * @param[valueTextColor]:値表示の文字色(nullなら非表示)
 * @param[valueTextSize]:値表示の文字サイズ(nullならデフォルト)
 * @param[valueTextFormatter]:値表示の文字書式(nullならデフォルト)
 * @param[axisDependency]:使用する軸(LEFT or RIGHT)
 */
open class DataSetFormat(
    var drawValue: Boolean = true,//値の表示有無
    var valueTextColor: Int? = null,//値表示の文字色(nullならデフォルト)
    var valueTextSize: Float? = null,//値表示の文字サイズ(nullならデフォルト)
    var valueTextFormatter: String? = null,//値表示の文字書式(nullならデフォルト)
    var axisDependency: YAxis.AxisDependency? = YAxis.AxisDependency.LEFT//使用する軸(nullはデフォルト)
)

/**
 * 円グラフDataSetフォーマット指定用クラス(DataSetFormatを継承)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[colorList]:グラフの色(dimensionsの数だけ指定が必要)
 */
class PieDataSetFormat(
    drawValue: Boolean = true,
    valueTextColor: Int? = null,
    valueTextSize: Float? = null,
    valueTextFormatter: String? = null,
    axisDependency: YAxis.AxisDependency? = null,
    var colorList: List<Int> = ColorTemplate.COLORFUL_COLORS.toList()//グラフの色
): DataSetFormat(drawValue, valueTextColor, valueTextSize, valueTextFormatter, axisDependency)

/**
 * ローソク足グラフDataSetフォーマット指定用クラス(DataSetFormatを継承)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[shadowColor]:細線部分の色
 * @param[shadowWidth]:細線部分の太さ(nullならデフォルト)
 * @param[decreasingColor]:Open>Close時の太線部分の色、箱ひげとして使用するときはこちらを使用
 * @param[decreasingPaint]:Open>Close時の太線部分の塗りつぶし形式、箱ひげとして使用するときはこちらを使用(nullならデフォルト)
 * @param[increasingColor]:Open<Close時の太線部分の色、箱ひげとして使用するときは不使用(nullなら非表示)
 * @param[increasingPaint]:Open<Close時の太線部分の塗りつぶし形式、箱ひげとして使用するときは不使用(nullならデフォルト)
 */
class CandleDataSetFormat(
    drawValue: Boolean = true,
    valueTextColor: Int? = null,
    valueTextSize: Float? = null,
    valueTextFormatter: String? = null,
    axisDependency: YAxis.AxisDependency? = YAxis.AxisDependency.LEFT,
    var shadowColor: Int = Color.YELLOW,//細線部分の色
    var shadowWidth: Float? = null,//細線部分の太さ(nullならデフォルト)
    var decreasingColor: Int = Color.GREEN,//Open>Close時の太線部分の色、箱ひげとして使用するときはこちらを使用
    var decreasingPaint: Paint.Style? = null,//Open>Close時の太線部分の塗りつぶし形式(nullならデフォルト)
    var increasingColor: Int? = Color.RED,//Open<Close時の太線部分の色(nullなら非表示)
    var increasingPaint: Paint.Style? = null//Open<Close時の太線部分の色(nullならデフォルト)
): DataSetFormat(drawValue, valueTextColor, valueTextSize, valueTextFormatter, axisDependency)

/**
 * 折れ線グラフDataSetフォーマット指定用クラス(DataSetFormatを継承)
 * @constructor 指定なしなら、全てデフォルト設定を使用
 * @param[lineColor]:線の色(nullならデフォルト)
 * @param[lineWidth]:線の太さ(nullならデフォルト)
 * @param[fittingMode]:線のフィッティング法(nullならデフォルト)
 * @param[drawCircles]:データ点のプロット有無(falseなら表示なし)
 * @param[circleColor]:データ点の色(nullならデフォルト)
 * @param[circleRadius]:データ点の半径(nullならデフォルト)
 */
class LineDataSetFormat(
    drawValue: Boolean = false,
    valueTextColor: Int? = null,
    valueTextSize: Float? = null,
    valueTextFormatter: String? = null,
    axisDependency: YAxis.AxisDependency? = YAxis.AxisDependency.LEFT,
    var lineColor: Int = Color.BLUE,//線の色
    var lineWidth: Float? = null,//線の太さ(nullならデフォルト)
    var fittingMode: LineDataSet.Mode? = null,//線のフィッティング法(nullならデフォルト)
    var drawCircles: Boolean = false,//データ点のプロット有無(falseなら表示なし)
    var circleColor: Int? = null, //データ点の色(nullならデフォルト)
    var circleRadius: Float? = null//データ点の半径(nullならデフォルト)
): DataSetFormat(drawValue, valueTextColor, valueTextSize, valueTextFormatter, axisDependency)