package com.mongodb.homeiotviewer.model

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey
import java.util.Date;
import org.bson.types.ObjectId;

open class Sensor(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var Date_Master: Date = Date(),
    var Date_ScanStart: Date? = null,
    var _partition: String? = null,
    var no01_AirDirection: String? = null,
    var no01_AirVolume: String? = null,
    var no01_AirconMode: String? = null,
    var no01_AirconPower: String? = null,
    var no01_CumulativeEnergy: Double? = null,
    var no01_Date: Date? = null,
    var no01_DeviceName: String? = null,
    var no01_HumanMotion: Long? = null,
    var no01_Human_last: String? = null,
    var no01_Humidity: Double? = null,
    var no01_Light: Long? = null,
    var no01_TempSetting: Long? = null,
    var no01_Temperature: Double? = null,
    var no01_Watt: Long? = null,
    var no02_Date: Date? = null,
    var no02_DeviceName: String? = null,
    var no02_Humidity: Double? = null,
    var no02_Light: Long? = null,
    var no02_Noise: Double? = null,
    var no02_Pressure: Double? = null,
    var no02_Temperature: Double? = null,
    var no02_eCO2: Long? = null,
    var no02_eTVOC: Long? = null,
    var no03_Date: Date? = null,
    var no03_DeviceName: String? = null,
    var no03_Humidity: Double? = null,
    var no03_Temperature: Double? = null,
    var no04_Date: Date? = null,
    var no04_DeviceName: String? = null,
    var no04_Humidity: Double? = null,
    var no04_Temperature: Double? = null,
    var no05_BatteryVoltage: Double? = null,
    var no05_Date: Date? = null,
    var no05_DeviceName: String? = null,
    var no05_Humidity: Double? = null,
    var no05_Light: Long? = null,
    var no05_Noise: Double? = null,
    var no05_Pressure: Double? = null,
    var no05_Temperature: Double? = null,
    var no05_UV: Double? = null,
    var no06_Date: Date? = null,
    var no06_DeviceName: String? = null,
    var no06_Humidity: Double? = null,
    var no06_Temperature: Double? = null,
    var no07_BatteryVoltage: Long? = null,
    var no07_Date: Date? = null,
    var no07_DeviceName: String? = null,
    var no07_Humidity: Double? = null,
    var no07_Temperature: Double? = null,
    var no08_Date: Date? = null,
    var no08_DeviceName: String? = null,
    var no08_HumanLast: String? = null,
    var no08_HumanMotion: Long? = null
): RealmObject() {
    fun getTemprature(number: Int): Double?{
        when(number){
            1 -> return no01_Temperature
            2 -> return no02_Temperature
            3 -> return no03_Temperature
            4 -> return no04_Temperature
            5 -> return no05_Temperature
            6 -> return no06_Temperature
            7 -> return no07_Temperature
            else -> return null
        }
    }
    fun getHumidity(number: Int): Double?{
        when(number){
            1 -> return no01_Humidity
            2 -> return no02_Humidity
            3 -> return no03_Humidity
            4 -> return no04_Humidity
            5 -> return no05_Humidity
            6 -> return no06_Humidity
            7 -> return no07_Humidity
            else -> return null
        }
    }
    fun getDate(number: Int): Date? {
        when (number) {
            1 -> return no01_Date
            2 -> return no02_Date
            3 -> return no03_Date
            4 -> return no04_Date
            5 -> return no05_Date
            6 -> return no06_Date
            7 -> return no07_Date
            8 -> return no08_Date
            else -> return null
        }
    }
    fun getAirconPower(number: Int): String? {
        when (number) {
            1 -> return no01_AirconPower
            else -> return null
        }
    }
    fun getAirconMode(number: Int): String? {
        when (number) {
            1 -> return no01_AirconMode
            else -> return null
        }
    }
    fun getWatt(number: Int): Long? {
        when (number) {
            1 -> return no01_Watt
            else -> return null
        }
    }
}