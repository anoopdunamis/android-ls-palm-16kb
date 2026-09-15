package com.dunamis.world.lspalm.recog

import com.dunamis.world.lspalm.db.Students

interface RecognitionView {

    fun attendanceSuccess()
    fun attendanceFailed()
    fun palmDetected()

    fun searchSuccess(alStudents: ArrayList<Students>, searchKeyword: String)
    fun searchFailed(msg: String)

    fun palmSuccess()
    fun palmFailed()

    fun getAttendanceRes(data: Boolean)
    fun attendanceDownloaded()
    fun attendanceDownloadFailed()

    fun getPalmRes(data: Boolean)
    fun palmDownloaded()
    fun palmDownloadFailed()

    fun palmDeleteSuccess()
    fun palmDeleteFailed()
}