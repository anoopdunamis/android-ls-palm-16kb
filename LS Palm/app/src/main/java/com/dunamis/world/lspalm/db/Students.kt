package com.dunamis.world.lspalm.db

data class Students(
    val id: Int = 0,                     // KEY_ID (AUTOINCREMENT)
    val childId: String,                 // KEY_CHILD_ID (UNIQUE)
    val childName: String?,
    val childClass: String?,
    val childSection: String?,
    val childNfcId: String?,
    val childRegNo: String?,
    val childFatherName: String?,
    val tripNoPickUp: String?,
    val tripNoDropOff: String?,
    val childOrEmp: String?,
    val pcrValidityEndDate: String?,
    val parentNfcId: String?,
    val parentAcNo: String?,
    val handicappedType: String?,
    val suspensionDate: String?,
    val motherNfc: String?,
    val entryRestriction: String?,
    val childAuthorizedPickup: String?,
    val childGender: String?,
    val childPhoto: String?,
    val childFatherEmail: String?,
    val childFatherTel: String?,
    val childMotherEmail: String?,
    val childMotherTel: String?,
    val faceEmbed: Boolean = false
)