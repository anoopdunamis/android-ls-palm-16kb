package com.dunamis.world.lspalm.recog

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dunamis.world.lspalm.R
import com.dunamis.world.lspalm.db.Attendance
import com.dunamis.world.lspalm.db.DatabaseHandler
import com.dunamis.world.lspalm.db.Palm
import com.dunamis.world.lspalm.db.Students
import com.dunamis.world.lspalm.statics.SharedPref
import com.dunamis.world.lspalm.statics.URLS
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RecognitionPresenter(c: RecognitionActivity) {

    private var c: Context = c
    private var recognitionView: RecognitionView? = c
    private var sharedPref: SharedPref = SharedPref(c)
    private var databaseHandler = DatabaseHandler(c)

    fun markAttendance() {

        class GetAttendanceTask : Thread() {
            override fun run() {
                super.run()

                val jArrayObject = JSONArray()
                val attendanceDetails: MutableList<Attendance> =
                    databaseHandler.getAllAttendanceDetails() as MutableList<Attendance>
                val params: MutableMap<String?, String?> = HashMap<String?, String?>()
                var jsonObject: JSONObject? = null

                var lastLocalId = -1
                for (attendance in attendanceDetails) {
                    lastLocalId = attendance.id
                    jsonObject = JSONObject()
                    try {
                        jsonObject.put("at_loc_longi", attendance.longitude)
                        jsonObject.put("at_loc_lati", attendance.latitude)
                        jsonObject.put("at_device_id", attendance.busId)
                        jsonObject.put("at_time_stamp_device", attendance.timeStamp)
                        jsonObject.put("at_loc_data_type", attendance.dataType)
                        jsonObject.put("at_child_id", attendance.childId)
                        jsonObject.put("at_in_or_out", attendance.pickUpDropOff)
                        jsonObject.put("at_child_status", attendance.childStatus)
                        jsonObject.put("child_nfc", attendance.childNfcId)
                        jsonObject.put("child_forgot_card", attendance.childForgotCard)
                        jsonObject.put("child_reg_no", attendance.childRegNo)
                        jsonObject.put("at_child_trip_no_pickup", attendance.tripNoPickUp)
                        jsonObject.put("at_child_trip_no_drop_off", attendance.tripNoDropOff)
                        jsonObject.put("child_name", attendance.childName)
                        jsonObject.put("child_or_employee", attendance.childOrEmp)
                        jsonObject.put("authorized_person_id", attendance.childAuthId)
                        jsonObject.put("at_child_grade", attendance.childClassAuth)
                        jsonObject.put("at_child_section", attendance.childSectionAuth)
                    } catch (e: JSONException) {
                    }
                    jArrayObject.put(jsonObject)
                }
//                params.put("data", jArrayObject.toString())
//                requestQueue.cancelAll("markattendance")
//                AppController.getInstance().getRequestQueue().getCache().invalidate(sharedPref.getBaseUrl() + URLS().MARK_ATTENDANCE_BATCH, true)
                val jsonObjReq: StringRequest = object : StringRequest(Request.Method.POST,
                    sharedPref.getBaseUrl() + URLS().MARK_ATTENDANCE_BATCH,
                    { response ->
                        try {
                            val jsbObjectRes = JSONObject(response.toString())
                            if (jsbObjectRes.has("status")) {
                                if (jsbObjectRes.getString("status")
                                        .equals("success", ignoreCase = true)) {

                                    databaseHandler.deleteAttendanceUpToId(lastLocalId)
                                    recognitionView?.attendanceSuccess()
                                }
                            }
                        } catch (e: Exception) {
                            recognitionView?.attendanceFailed()
                        }
                    },
                    { error -> recognitionView?.attendanceFailed() }) {

                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        val map: MutableMap<String, String> = HashMap()
                        map["data"] = jArrayObject.toString()
                        return map
                    }
//                    val headers: MutableMap<String?, String?>
//                        get() = createBasicAuthHeader()
//
//                    fun createBasicAuthHeader(): HashMap<String?, String?> {
//                        val headerMap = HashMap<String?, String?>()
//                        val credentials = ""
//                        val base64EncodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
//                        headerMap.put("Authorization", "Basic " + base64EncodedCredentials)
//                        return headerMap
//                    }
                }

//                jsonObjReq.setTag("markattendance")
//                val policy: RetryPolicy = DefaultRetryPolicy(AppConfig.TIMEOUT_EXTENDED, 0, 0f)
//                jsonObjReq.setRetryPolicy(policy)
//                jsonObjReq.setShouldCache(false)
                val requestQueue = Volley.newRequestQueue(c)
                requestQueue.add(jsonObjReq)
//                AppController.getInstance().getRequestQueue().getCache().remove(sharedPref.getBaseUrl() + URLS().MARK_ATTENDANCE_BATCH)
            }
        }
        GetAttendanceTask().start()
    }

    fun searchStudent(tag: String, searchWord: String) {

        class GetSearchTask : Thread() {
            override fun run() {
                super.run()

                try {
                    val paramsObj = JSONObject()
                    val searchKey: String = when (tag) {
                        "0" -> "name"
                        "1" -> "register_number"
                        "2" -> "email"
                        "3" -> "phone_number"
                        else -> "NFC"
                    }
                    paramsObj.put("search_key", searchKey)
                    paramsObj.put("search_word", searchWord)
                    val request: StringRequest = object : StringRequest(
                        Method.POST, sharedPref.getBaseUrl() + URLS().searchUrl,
                        Response.Listener { response: String? ->
                            try {
                                val jsonObject = JSONObject(response)
                                val status = jsonObject.getString("status")
                                if (status == "success") {

                                    val arrStudents = jsonObject.getJSONArray("child_details")

                                    val arrFaceList: JSONArray
                                    val alFaceList = ArrayList<String>()
                                    if (jsonObject.has("face_list")) {

                                        arrFaceList = jsonObject.getJSONArray("face_list")
                                        for (i in 0 until arrFaceList.length()) {

                                            val jsonObjectFace = arrFaceList.getJSONObject(i)
                                            val faceId = jsonObjectFace.getString("face_chilld_id")
                                                .toString().trim()
                                            alFaceList.add(faceId)
                                        }
                                    }
                                    val alStudents: ArrayList<Students> = ArrayList()

                                    for (i in 0 until arrStudents.length()) {

                                        val jsonObj = arrStudents.getJSONObject(i)
                                        var childId = jsonObj.getString("child_id").trim()
                                        var name = jsonObj.getString("child_name").trim()
                                        var regNo = jsonObj.getString("child_reg_no").trim()
                                        var className = jsonObj.getString("child_class").trim()
                                        var section = jsonObj.getString("child_section").trim()
                                        var gender = jsonObj.getString("child_gender").trim()
                                        var photo = jsonObj.getString("child_photo").trim()
                                        var nfcId = jsonObj.getString("child_nfc_id").trim()

                                        if (childId == null || childId == "" || childId == "null") {

                                            childId = ""
                                        }
                                        if (name == null || name == "" || name == "null") {

                                            name = ""
                                        }
                                        if (regNo == null || regNo == "" || regNo == "null") {

                                            regNo = ""
                                        }
                                        if (className == null || className == "" || className == "null") {

                                            className = ""
                                        }
                                        if (section == null || section == "" || section == "null") {

                                            section = ""
                                        }
                                        if (gender == null || gender == "" || gender == "null") {

                                            gender = ""
                                        }
                                        if (photo == null || photo == "" || photo == "null") {

                                            photo = ""
                                        }
                                        if (nfcId == null || nfcId == "" || nfcId == "null") {

                                            nfcId = ""
                                        }
                                        alStudents.add(Students(
                                            childId = childId,
                                            childRegNo = regNo,
                                            childName = name,
                                            childClass = className,
                                            childSection = section,
                                            childGender = gender,
                                            childPhoto = photo,
                                            childNfcId = nfcId,
                                            childFatherName = null,
                                            tripNoPickUp = null,
                                            tripNoDropOff = null,
                                            childOrEmp = null,
                                            pcrValidityEndDate = null,
                                            parentNfcId = null,
                                            parentAcNo = null,
                                            handicappedType = null,
                                            suspensionDate = null,
                                            motherNfc = null,
                                            entryRestriction = null,
                                            childAuthorizedPickup = null,
                                            childFatherEmail = null,
                                            childFatherTel = null,
                                            childMotherEmail = null,
                                            childMotherTel = null,
                                            faceEmbed = alFaceList.contains(childId)))
                                    }
                                    recognitionView?.searchSuccess(alStudents, searchWord)
                                } else {

                                    recognitionView?.searchFailed(
                                        c.getString(R.string.no_data_found)
                                    )
                                }
                            } catch (e: Exception) {

                                recognitionView?.searchFailed(c.getString(R.string.no_internet)
                                    .toString())
                            }
                        },
                        Response.ErrorListener { error: VolleyError? ->

                            recognitionView?.searchFailed(
                                c.getString(
                                    R.string.no_internet
                                ).toString()
                            )
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val map: MutableMap<String, String> = HashMap()
                            map["data"] = paramsObj.toString()
                            return map
                        }
                    }
                    val requestQueue = Volley.newRequestQueue(c)
                    requestQueue.add(request)
                } catch (e: JSONException) {

                    recognitionView?.searchFailed(c.getString(R.string.no_internet))
                }
            }
        }
        GetSearchTask().start()
    }

    fun addPalmAPI(
        childID: String,
        rgbArrayL: JSONArray,
        irArrayL: JSONArray,
        rgbArrayR: JSONArray,
        irArrayR: JSONArray) {

        class AddPalmTask : Thread() {
            override fun run() {
                super.run()

                val jsn = JSONObject()
                try {
                    jsn.put("child_id", childID)
                    jsn.put("palm_data_rgb_left", rgbArrayL.toString())
                    jsn.put("palm_data_ir_left", irArrayL.toString())
                    jsn.put("palm_data_rgb_right", rgbArrayR.toString())
                    jsn.put("palm_data_ir_right", irArrayR.toString())
                } catch (e: JSONException) {

                    recognitionView?.palmFailed()
                }
                val queue = Volley.newRequestQueue(c)
                val request: StringRequest = object :
                    StringRequest(Method.POST,
                        sharedPref.getBaseUrl() + URLS().ADD_PALM_API,
                        Response.Listener { response: String? ->
                            try {
                                val jsonObject = JSONObject(response)
                                val status = if (jsonObject.has("status")) {
                                    jsonObject.getString("status").trim { it <= ' ' }
                                } else {
                                    ""
                                }
                                if (status.equals("success", ignoreCase = true)) {

                                    recognitionView?.palmSuccess()
                                } else {

                                    recognitionView?.palmFailed()
                                }
                            } catch (e: JSONException) {

                                recognitionView?.palmFailed()
                            }
                        },
                        Response.ErrorListener { error: VolleyError? ->

                            recognitionView?.palmFailed()
                        }) {
                    override fun getParams(): MutableMap<String?, String?> {
                        val params: MutableMap<String?, String?> = HashMap()
                        params["data"] = jsn.toString()

                        params["data"] =
                            "{\"child_id\":$childID,\"palm_data_rgb_left\":$rgbArrayL,\"palm_data_ir_left\":$irArrayL,\"palm_data_rgb_right\":$rgbArrayR,\"palm_data_ir_right\":$irArrayR}"
                        return params
                    }
                }
                queue.add(request)
            }
        }
        AddPalmTask().start()
    }

    fun deletePalmAPI(childID: String) {
        class DeletePalmTask : Thread() {
            override fun run() {
                super.run()

                val jsn = JSONObject()
                try {
                    jsn.put("child_id", childID)
                } catch (e: JSONException) {

                    recognitionView?.palmDeleteFailed()
                }
                val queue = Volley.newRequestQueue(c)
                val request: StringRequest = object :
                    StringRequest(Method.POST,
                        sharedPref.getBaseUrl() + URLS().DELETE_PALM_API,
                        Response.Listener { response: String? ->
                            try {
                                val jsonObject = JSONObject(response)
                                val status = if (jsonObject.has("status")) {
                                    jsonObject.getString("status").trim { it <= ' ' }
                                } else {
                                    ""
                                }
                                if (status.equals("success", ignoreCase = true)) {

                                    databaseHandler.deletePalmByChildId(childID)
                                    recognitionView?.palmDeleteSuccess()
                                } else {

                                    recognitionView?.palmDeleteFailed()
                                }
                            } catch (e: JSONException) {

                                recognitionView?.palmDeleteFailed()
                            }
                        },
                        Response.ErrorListener { error: VolleyError? ->

                            recognitionView?.palmDeleteFailed()
                        }) {
                    override fun getParams(): MutableMap<String?, String?> {
                        val params: MutableMap<String?, String?> = HashMap()
                        params["data"] = jsn.toString()
                        return params
                    }
                }
                queue.add(request)
            }
        }
        DeletePalmTask().start()
    }

    fun getAttendanceDetails(pageNo: String) {

        class GetAttendanceTask : Thread() {
            override fun run() {
                super.run()

                try {
                    val paramsObj = JSONObject()
                    paramsObj.put("page", pageNo)
                    val url: String = sharedPref.getBaseUrl() + URLS().STUDENT_LIST_UPDATED
                    val jsonObjReq: StringRequest =
                        object : StringRequest(Request.Method.POST,
                            url,
                            Response.Listener { response: String? ->
                                try {
                                    val jsonObjectRes = JSONObject(response.toString())
                                    if (jsonObjectRes.has("status")) {
                                        if (jsonObjectRes.getString("status").equals("success")) {
                                            if (pageNo == "1") {

                                                databaseHandler.resetStudentsTable()
                                                if (jsonObjectRes.has("student_total_count")) {

                                                    sharedPref.setTotalStudent(jsonObjectRes.getString(
                                                        "student_total_count"))
                                                }
                                            }

                                            val tripList: JSONArray =
                                                jsonObjectRes.getJSONArray("student_list")
                                            val alChildDetails = mutableListOf<Students>()
                                            if (tripList.length() > 0) {
                                                for (i in 0 until tripList.length()) {

                                                    val obj = tripList.getJSONObject(i)

                                                    fun getSafe(key: String,
                                                        default: String = ""): String {
                                                        return if (obj.has(key)) {
                                                            val value = obj.getString(key).trim()
                                                            if (value.equals("null",
                                                                    true) || value.isEmpty()) default else value
                                                        } else default
                                                    }

                                                    val student = Students(
                                                        childId = getSafe("child_id"),
                                                        childName = getSafe("child_name"),
                                                        childClass = getSafe("child_class"),
                                                        childSection = getSafe("child_section"),
                                                        childNfcId = getSafe("child_nfc_id"),
                                                        childRegNo = getSafe("child_reg_no"),
                                                        childFatherName = getSafe("child_father_name"),
                                                        tripNoPickUp = getSafe("child_trip_no_pickup"),
                                                        tripNoDropOff = getSafe("child_trip_no_drop_off"),
                                                        childOrEmp = getSafe("child_or_employee"),
                                                        pcrValidityEndDate = getSafe("child_pcr_validity_end_date"),
                                                        parentNfcId = getSafe("child_parent_nfc_id"),
                                                        parentAcNo = getSafe("parent_account_no"),
                                                        handicappedType = getSafe("child_handicapped_type"),
                                                        suspensionDate = getSafe("quarantine_end_date"),
                                                        motherNfc = getSafe("child_mother_nfc"),
                                                        entryRestriction = getSafe("child_city",
                                                            "IN-OUT"),
                                                        childAuthorizedPickup = getSafe("child_authorized_pickup",
                                                            "no"),
                                                        childGender = getSafe("child_gender"),
                                                        childPhoto = getSafe("child_photo"),
                                                        childFatherEmail = getSafe("child_father_email_id"),
                                                        childFatherTel = getSafe("child_father_tel"),
                                                        childMotherEmail = getSafe("child_mother_email_id"),
                                                        childMotherTel = getSafe("child_mother_tel")
                                                    )
                                                    alChildDetails.add(student)
                                                }

                                                val data: Boolean =
                                                    databaseHandler.addBatchStudents(alChildDetails)
                                                if (data) {
                                                    recognitionView?.getAttendanceRes(data)
                                                } else {
                                                    databaseHandler.resetStudentsTable()
                                                    recognitionView?.getAttendanceRes(data)
                                                }
                                            } else {
                                                recognitionView?.attendanceDownloaded()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    recognitionView?.attendanceDownloadFailed()
                                }
                            },
                            Response.ErrorListener { error -> recognitionView?.attendanceDownloadFailed() }) {
                            @Throws(AuthFailureError::class)
                            override fun getParams(): Map<String, String> {
                                val map: MutableMap<String, String> = HashMap()
                                map["data"] = paramsObj.toString()
                                return map
                            }
                        }

                    val policy: RetryPolicy = DefaultRetryPolicy(8000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                    jsonObjReq.setRetryPolicy(policy)
                    jsonObjReq.setShouldCache(false)
                    val requestQueue = Volley.newRequestQueue(c)
                    requestQueue.add(jsonObjReq)
                } catch (e: JSONException) {

                    recognitionView?.attendanceDownloadFailed()
                }
            }
        }
        GetAttendanceTask().start()
    }

    fun getPalmDetails(palmPageNo: String) {

        class GetPalmTask : Thread() {
            override fun run() {
                super.run()

                try {
                    val paramsObj = JSONObject()
                    paramsObj.put("page", palmPageNo)
                    paramsObj.put("last_updated_time",
                        sharedPref.getPalmDBDownloadedTimeGMT() ?: "")
                    val url: String = sharedPref.getBaseUrl() + URLS().GET_PALM_BATCH
                    val jsonObjReq: StringRequest =
                        object : StringRequest(Request.Method.POST,
                            url,
                            Response.Listener { response ->
                                try {
                                    val jsonObjectRes = JSONObject(response ?: "")
                                    if (jsonObjectRes.optString("status").equals("success", ignoreCase = true)) {

                                        if (palmPageNo == "1") {
                                            if (sharedPref.getPalmDBDownloadedTimeGMT()
                                                    .isNullOrEmpty()) {
                                                databaseHandler.resetPalmTable()
                                            }
                                            if (jsonObjectRes.has("total_count")) {
                                                sharedPref.setTotalPalm(jsonObjectRes.optString("total_count")
                                                    .trim())
                                            }
                                        }

                                        val jsonArray = jsonObjectRes.optJSONArray("palm_list")
                                        if (jsonArray != null && jsonArray.length() > 0) {
                                            val alPalmDetails = mutableListOf<Palm>()
                                            val childIdsToDelete = mutableListOf<String?>()
                                            val isDbDownloaded =
                                                !sharedPref.getPalmDBDownloadedTimeGMT()
                                                    .isNullOrEmpty()

                                            val optStringOrNull = { obj: JSONObject, key: String ->
                                                if (obj.isNull(key)) null
                                                else {
                                                    val s = obj.optString(key)
                                                    if (s == "null" || s.isEmpty()) null else s
                                                }
                                            }

                                            val jsonArrayToByteArray = { jsonStr: String? ->
                                                if (jsonStr == null || jsonStr == "null" || jsonStr == "[]" || jsonStr.isEmpty()) {
                                                    null
                                                } else {
                                                    try {
                                                        val arr = JSONArray(jsonStr)
                                                        ByteArray(arr.length()) { k ->
                                                            arr.getInt(k).toByte()
                                                        }
                                                    } catch (e: Exception) {
                                                        null
                                                    }
                                                }
                                            }

                                            for (i in 0 until jsonArray.length()) {
                                                val obj = jsonArray.getJSONObject(i)
                                                val palmChildId = optStringOrNull(obj, "palm_chilld_id")

                                                val palmDataRgbLeft = jsonArrayToByteArray(obj.optString("palm_data_rgb_left"))
                                                val palmDataIrLeft = jsonArrayToByteArray(obj.optString("palm_data_ir_left"))
                                                val palmDataRgbRight = jsonArrayToByteArray(obj.optString("palm_data_rgb_right"))
                                                val palmDataIrRight = jsonArrayToByteArray(obj.optString("palm_data_ir_right"))

                                                if (palmDataRgbLeft != null || palmDataIrLeft != null || palmDataRgbRight != null || palmDataIrRight != null) {
                                                    if (isDbDownloaded && palmChildId != null) {
                                                        childIdsToDelete.add(palmChildId)
                                                    }

                                                    alPalmDetails.add(
                                                        Palm(
                                                            palmId = optStringOrNull(obj, "palm_id"),
                                                            palmDataOne = optStringOrNull(obj, "palm_data_1"),
                                                            childId = palmChildId,
                                                            childRegNo = optStringOrNull(obj, "palm_reg_no"),
                                                            palmDataRgbLeft = palmDataRgbLeft,
                                                            palmDataIrLeft = palmDataIrLeft,
                                                            palmDataRgbRight = palmDataRgbRight,
                                                            palmDataIrRight = palmDataIrRight
                                                        )
                                                    )
                                                }
                                            }

                                            if (childIdsToDelete.isNotEmpty()) {
                                                databaseHandler.deletePalmsByChildIds(
                                                    childIdsToDelete)
                                            }

                                            val data = databaseHandler.addBatchPalm(alPalmDetails)
                                            if (data) {
                                                recognitionView?.getPalmRes(data)
                                            } else {
                                                databaseHandler.resetPalmTable()
                                                recognitionView?.getPalmRes(data)
                                            }
                                        } else {
                                            recognitionView?.palmDownloaded()
                                        }
                                    } else {
                                        recognitionView?.palmDownloaded()
                                    }
                                } catch (e: Exception) {
                                    recognitionView?.palmDownloadFailed()
                                }
                            },
                            Response.ErrorListener { recognitionView?.palmDownloadFailed() }) {
                            @Throws(AuthFailureError::class)
                            override fun getParams(): Map<String, String> {
                                val map: MutableMap<String, String> = HashMap()
                                map["data"] = paramsObj.toString()
                                return map
                            }
                        }
                    val policy: RetryPolicy = DefaultRetryPolicy(8000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                    jsonObjReq.retryPolicy = policy
                    jsonObjReq.setShouldCache(false)
                    val requestQueue = Volley.newRequestQueue(c)
                    requestQueue.add(jsonObjReq)
                } catch (e: JSONException) {
                    recognitionView?.palmDownloadFailed()
                }
            }
        }
        GetPalmTask().start()
    }
}