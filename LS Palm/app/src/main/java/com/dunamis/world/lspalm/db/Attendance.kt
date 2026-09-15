package com.dunamis.world.lspalm.db

data class Attendance(
    val id: Int = 0,                 // KEY_ID (AUTOINCREMENT)
    val latitude: String?,
    val longitude: String?,
    val dataType: String?,
    val busId: String?,
    val timeStamp: String?,
    val childId: String,
    val pickUpDropOff: String?,
    val childStatus: String?,
    val childNfcId: String?,
    val childForgotCard: String?,
    val childRegNo: String?,
    val tripNoPickUp: String?,
    val tripNoDropOff: String?,
    val childName: String?,
    val childOrEmp: String?,
    val childAuthId: String?,
    val childClassAuth: String?,
    val childSectionAuth: String?
)