package com.mongodb.homeiotviewer.tab

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mongodb.homeiotviewer.TempHumidData
import java.util.*

class TabPagerAdapter(
    fm: FragmentManager,
    val newestData: MutableList<TempHumidData>,
    val airconData: Pair<String?, String?>,
    val watt: Int?,
    val placeTempData: Map<String, MutableList<Pair<Date, Double>>>,
    val placeHumidData: Map<String, MutableList<Pair<Date, Double>>>,
    val tempStatsData: Map<String, List<Pair<Date, Double>>>,
    val humidStatsData: Map<String, List<Pair<Date, Double>>>,
    private val context: Context)
    : FragmentPagerAdapter(fm){

    //各タブごとのFragmentインスタンス作成
    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> { return SummaryFragment(newestData, airconData, watt) }
            1 -> { return TempFragment(newestData, placeTempData, tempStatsData) }
            else ->  { return HumidFragment(newestData, placeHumidData, humidStatsData) }
        }
    }

    //各タブごとのタイトル付与
    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0 -> { return "サマリー" }
            1 -> { return "温度" }
            else ->  { return "湿度" }
        }
    }

    //タブの最大値をset
    override fun getCount(): Int {
        return 3
    }
}