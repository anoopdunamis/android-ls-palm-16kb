package com.dunamis.world.lspalm.statics

import android.content.Context
import android.content.SharedPreferences

class SharedPref(c: Context) {

    private val sp: SharedPreferences =
        c.getSharedPreferences(URLS().palmAppSharedPref, Context.MODE_PRIVATE)
    private val spEditor = sp.edit()

    fun clearAll() {

        spEditor.clear()
        spEditor.apply()
    }

    fun setBaseUrls(baseUrlRaw: String?,
        baseUrl: String?,
        imgUrl: String?,
        logoImgUrl: String?,
        imgUrlFace: String?) {

        spEditor.putString("base_url_raw", baseUrlRaw)
        spEditor.putString("base_url", baseUrl)
        spEditor.putString("image_url", imgUrl)
        spEditor.putString("logo_image_base_url", logoImgUrl)
        spEditor.putString("image_url_face", imgUrlFace)
        spEditor.apply()
    }

    fun getBaseUrlRaw(): String? {
        return sp.getString("base_url_raw", "")
    }

    fun getBaseUrl(): String? {
        return sp.getString("base_url", "")
    }

    fun getImgUrl(): String? {
        return sp.getString("image_url", "")
    }

    fun getImgUrlFace(): String? {

        return sp.getString("image_url_face", "")
    }

    fun setSchoolLogo(data: String?) {

        spEditor.putString("schoolLogo", data)
        spEditor.apply()
    }

    fun getSchoolLogo(): String? {

        return sp.getString("schoolLogo", "")
    }

    fun getLogoImgBaseUrl(): String? {
        return sp.getString("logo_image_base_url", "")
    }

    fun setLoggedIn(data: Boolean) {

        spEditor.putBoolean("isLoggedIn", data)
        spEditor.apply()
    }

    fun getLoggedIn(): Boolean {

        return sp.getBoolean("isLoggedIn", false)
    }

    fun clearUpToLogin() {

        val baseUrlRaw = getBaseUrlRaw()
        val baseUrl = getBaseUrl()
        val imgUrl = getImgUrl()
        val logoImgUrl = getLogoImgBaseUrl()
        val imgUrlFace = getImgUrlFace()
        val schoolLogo = getSchoolLogo()

        clearAll()
        setBaseUrls(baseUrlRaw, baseUrl, imgUrl, logoImgUrl, imgUrlFace)
        setSchoolLogo(schoolLogo)
    }

    fun setFaceReg(faceAuth: String) {

        val faceData: Boolean = faceAuth.equals("yes", ignoreCase = true)
        spEditor.putBoolean("faceRecognition", faceData)
        spEditor.apply()
    }

    fun getFaceReg(): Boolean {

        return sp.getBoolean("faceRecognition", false);
    }

    fun setScanType(data: String) {

        spEditor.putString("scanTypeForNFC", data)
        spEditor.apply()
    }

    fun getScanType(): String? {

        return sp.getString("scanTypeForNFC", "")
    }

    fun setDBUpdatedTime(data: String) {

        spEditor.putString("dbUpdatedTime", data)
        spEditor.apply()
    }

    fun getDBUpdatedTime(): String? {

        return sp.getString("dbUpdatedTime", "")
    }

    fun setDBRefreshMandatory(data: Boolean) {

        spEditor.putBoolean("dbRefreshMandatory", data)
        spEditor.apply()
    }

    fun getDBRefreshMandatory(): Boolean? {

        return sp.getBoolean("dbRefreshMandatory", false)
    }

    fun setTotalStudent(data: String?) {

        spEditor.putString("totalStudent", data)
        spEditor.apply()
    }

    fun getTotalStudent(): String? {
        return sp.getString("totalStudent", "0")
    }

    fun setTotalPalm(data: String?) {

        spEditor.putString("totalPalm", data)
        spEditor.apply()
    }

    fun getTotalPalm(): String? {
        return sp.getString("totalPalm", "0")
    }

    fun setStudentDBDownloadedTime(data: String?) {

        spEditor.putString("DBDownloadedTime", data)
        spEditor.apply()
    }

    fun getStudentDBDownloadedTime(): String? {

        return sp.getString("DBDownloadedTime", "---")
    }

    fun setPalmDBDownloadedTimeGMT(data: String?) {

        spEditor.putString("PalmDBDownloadedTime", data)
        spEditor.apply()
    }

    fun getPalmDBDownloadedTimeGMT(): String? {

        return sp.getString("PalmDBDownloadedTime", "")
    }

    fun setRecognitionPageTime(data: String?) {

        spEditor.putString("RecognitionPageTime", data)
        spEditor.apply()
    }

    fun getRecognitionPageTime(): String? {

        return sp.getString("RecognitionPageTime", "")
    }

    fun setBusId(data: String?) {
        spEditor.putString("busId", data)
        spEditor.apply()
    }

    fun getBusId(): String? {
        return sp.getString("busId", "")
    }

    fun setEntryType(data: String?) {

        spEditor.putString("entryType", data)
        spEditor.apply()
    }

    fun getEntryType(): String? {

        return sp.getString("entryType", "")
    }
}