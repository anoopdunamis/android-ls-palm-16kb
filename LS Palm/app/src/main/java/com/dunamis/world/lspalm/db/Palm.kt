package com.dunamis.world.lspalm.db

data class Palm(
    val id: Int = 0,                     // KEY_ID (AUTOINCREMENT)
    val palmId: String?,
    val palmDataOne: String?,
    val childId: String?,
    val childRegNo: String?,
    val palmDataRgbLeft: ByteArray?,
    val palmDataIrLeft: ByteArray?,
    val palmDataRgbRight: ByteArray?,
    val palmDataIrRight: ByteArray?
)