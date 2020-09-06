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

//タイムゾーンを指定タイムゾーン → システムデフォルトに変換
fun changeTimeZoneToDefault(srcDate: Date, srcTimeZone: TimeZone): Date {
    val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")
    val strDate = sdf.format(srcDate)//指定タイムゾーンでの現在時刻文字列
    sdf.setTimeZone(srcTimeZone)//タイムゾーンを指定タイムゾーンに設定
    return sdf.parse(strDate)
}

//タイムゾーンをシステムデフォルト → 指定タイムゾーンに変換
fun changeTimeZoneFromDefault(srcDate: Date, dstTimeZone: TimeZone): Date {
    val sdf: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")
    sdf.setTimeZone(dstTimeZone)
    val strDate = sdf.format(srcDate)//システムデフォルトタイムゾーンでの現在時刻文字列
    sdf.setTimeZone(TimeZone.getDefault())//タイムゾーンを指定タイムゾーンに設定
    return sdf.parse(strDate)
}