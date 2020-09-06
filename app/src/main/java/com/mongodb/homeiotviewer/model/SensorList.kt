package com.mongodb.homeiotviewer.model

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId;

open class SensorList(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var _partition: String? = null,
    var aircon: Boolean? = null,
    var humidity: Boolean? = null,
    var no: Long? = null,
    var place: String? = null,
    var power: Boolean? = null,
    var sensorname: String? = null,
    var temperature: Boolean? = null
): RealmObject() {}