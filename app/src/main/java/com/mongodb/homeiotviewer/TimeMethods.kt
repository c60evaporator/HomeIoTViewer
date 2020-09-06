package com.mongodb.homeiotviewer

import java.text.SimpleDateFormat
import java.util.*

//タイムゾーンを時刻を変えないまま変換(例: GMT 0:00 → Asia/Tokyo 0:00)
fun changeTimeZone(srcDate: Date, srcTimeZone: TimeZone, dstTimeZone: TimeZone): Date{
    val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")
    sdf.setTimeZone(srcTimeZone)
    val strDate = sdf.format(srcDate)//指定タイムゾーンでの現在時刻文字列
    sdf.setTimeZone(dstTimeZone)//タイムゾーンを指定タイムゾーンに設定
    return sdf.parse(strDate)
}