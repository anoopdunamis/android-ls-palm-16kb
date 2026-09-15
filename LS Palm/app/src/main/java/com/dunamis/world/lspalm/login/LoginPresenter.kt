package com.dunamis.world.lspalm.login

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dunamis.world.lspalm.R
import com.dunamis.world.lspalm.statics.SharedPref
import com.dunamis.world.lspalm.statics.URLS
import org.json.JSONArray
import org.json.JSONObject

class LoginPresenter(c: LoginActivity) {

    private var c: Context = c
    private var loginView: LoginView? = c
    private var sharedPref: SharedPref = SharedPref(c)

    fun login(u: String, p: String, baseUrl: String) {

        class GetLoginTask : Thread() {
            override fun run() {
                super.run()

                val paramsObj = JSONObject()
                paramsObj.put("user_name", u)
                paramsObj.put("passwod", p)

                val request: StringRequest = object : StringRequest(
                    Method.POST, baseUrl + URLS().loginUrl,
                    Response.Listener { response: String? ->
                        try {
                            val jsonObject = JSONObject(response.toString())

                            if (jsonObject.getString("status")
                                    .equals("success", ignoreCase = true)) {

                                val faceAuth = if (jsonObject.has("set_face_auth")) {

                                    jsonObject.getString("set_face_auth").toString().trim();
                                } else {

                                    ""
                                }

                                val scanType = if (jsonObject.has("card_scan_type")) {

                                    jsonObject.getString("card_scan_type").toString().trim();
                                } else {

                                    ""
                                }

                                if (jsonObject.has("student_db_updated_time")) {
                                    val dbUpdatedTime =
                                        jsonObject.getString("student_db_updated_time").trim()
                                    if (sharedPref.getDBUpdatedTime() == dbUpdatedTime) {

                                        sharedPref.setDBRefreshMandatory(false)
                                    } else {

                                        sharedPref.setDBUpdatedTime(dbUpdatedTime)
                                        sharedPref.setDBRefreshMandatory(true)
                                    }
                                }

                                loginView?.success(faceAuth, scanType)
                            } else {

                                loginView?.failed(jsonObject.getString("msg").toString().trim())
                            }
                        } catch (e: Exception) {

                            loginView?.failed(c.resources.getString(R.string.no_internet))
                        }
                    },
                    Response.ErrorListener { error: VolleyError? -> loginView?.failed(c.getString(R.string.no_internet)) }) {
                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        val map: MutableMap<String, String> = HashMap()
                        map["data"] = paramsObj.toString()
                        return map
                    }
                }
                val requestQueue = Volley.newRequestQueue(c)
                requestQueue.add(request)
            }
        }
        GetLoginTask().start()
    }

    fun logoReq(baseUrl: String) {

        class GetLogoTask : Thread() {
            override fun run() {
                super.run()

                val paramsObj = JSONObject()
                val request: StringRequest = object : StringRequest(
                    Method.POST, baseUrl + "json/" + URLS().logoUrl,
                    Response.Listener { response: String? ->
                        try {
                            val jsonObject = JSONObject(response.toString())
                            val tripList: JSONArray = jsonObject.getJSONArray("logo_images")
                            for (i in 0 until tripList.length()) {

                                val objectJson: JSONObject = tripList.getJSONObject(i)
                                loginView?.logoImgRes(objectJson.getString("B_logo_image_andriod"))
                            }
                        } catch (e: Exception) {

                            loginView?.logoImgRes("")
                        }
                    },
                    Response.ErrorListener { error: VolleyError? -> loginView?.logoImgRes("") }) {
                    @Throws(AuthFailureError::class)
                    override fun getParams(): Map<String, String> {
                        val map: MutableMap<String, String> = HashMap()
                        map["data"] = paramsObj.toString()
                        return map
                    }
                }
                val requestQueue = Volley.newRequestQueue(c)
                requestQueue.add(request)
            }
        }
        GetLogoTask().start()
    }
}