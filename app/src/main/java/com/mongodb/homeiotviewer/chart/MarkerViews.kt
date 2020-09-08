package com.mongodb.homeiotviewer.chart//プロジェクト構成に合わせ変更

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.mongodb.homeiotviewer.R//プロジェクト構成に合わせ変更
import java.text.SimpleDateFormat

/**
 * X,Y軸の値をツールチップ表示
 * @param[toolTipFormat]: ツールチップのフォーマット(1項目:表示する軸, 2項目:日付のフォーマット(日付軸でないときnull))
 * @param[unit]: 表示するツールチップに付加する単位(1項目:X軸, 2項目:Y軸)
 * @param[context]: ツールチップを設置するActivityあるいはFragmentのContext
 * @param[layoutResource]: 表示するツールチップのレイアウト
 * @param[offsetY]: ツールチップ表示位置のY方向オフセット
 */
class SimpleMarkerView(val toolTipFormat: Pair<String?, SimpleDateFormat?>,
                       val unit: Pair<String, String>,
                       context: Context,
                       layoutResource: Int,
                       val offsetY: Float)
    : MarkerView(context, layoutResource){
    private lateinit var textView: TextView
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        textView = findViewById(R.id.tvSimple)
        //日付軸でないとき
        if(toolTipFormat.second == null) {
            when(toolTipFormat.first){
                "x" -> textView.text = "${e?.x}${unit.first}"//X軸のみ表示
                "y" -> textView.text = "${e?.y}${unit.second}"//Y軸のみ表示
                "xy" -> textView.text = "${e?.x}${unit.first}\n${e?.y}${unit.second}"//XY軸両方表示
            }
        }
        //日付軸のとき
        else {
            when(toolTipFormat.first){
                "x" -> textView.text = "${toolTipFormat.second?.format(e?.data)}${unit.first}"//X軸のみ表示
                "y" -> textView.text = "${e?.y}${unit.second}"//Y軸のみ表示
                "xy" -> textView.text = "${toolTipFormat.second?.format(e?.data)}${unit.first}\n${e?.y}${unit.second}"//XY軸両方表示
            }
        }
        super.refreshContent(e, highlight)
    }
    override fun getOffset(): MPPointF{
        return MPPointF(-(width / 2f), -height.toFloat() - offsetY)
    }
}
