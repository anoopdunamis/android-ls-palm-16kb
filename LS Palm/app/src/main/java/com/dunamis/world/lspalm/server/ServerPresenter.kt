package com.dunamis.world.lspalm.server

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.dunamis.world.lspalm.statics.URLS
import org.json.JSONObject

class ServerPresenter(c: ServerActivity) {

    private var c: Context? = c
    private var serverView: ServerView? = c

    fun getSchools() {

        class GetServerTask : Thread() {
            override fun run() {
                super.run()

                // on below line we are creating a variable for our request queue and initializing it.
                val queue: RequestQueue = Volley.newRequestQueue(c)
                // on below line we are creating a variable for request and initializing it with json object request
                val request =
                    JsonObjectRequest(Request.Method.POST, URLS().serverUrl, null, { response ->
                        try {
                            val jsonObject = JSONObject(response.toString())
                            if (jsonObject.getString("status")
                                    .equals("success", ignoreCase = true)) {

                                val alKeysList = ArrayList<String>()
                                val alValueList = ArrayList<String>()
                                val alImageList = ArrayList<String>()

                                val serverNameArray =
                                    jsonObject.getJSONArray("server_list_short_name")
                                for (i in 0 until serverNameArray.length()) {

                                    alKeysList.add(serverNameArray.getString(i))
                                }
                                val serverLinkArray = jsonObject.getJSONArray("server_list_links")
                                for (i in 0 until serverLinkArray.length()) {

                                    alValueList.add(serverLinkArray.getString(i))
                                }
                                val serverImagesArray =
                                    jsonObject.getJSONArray("server_list_image_url")
                                for (i in 0 until serverImagesArray.length()) {

                                    alImageList.add(serverImagesArray.getString(i))
                                }
                                serverView?.success(alKeysList, alValueList, alImageList)
                            } else {

                                serverView?.failed()
                            }
                        } catch (e: Exception) {

                            serverView?.failed()
                        }
                    }, { error ->

                        serverView?.failed()
                    })
                // at last we are adding our request to our queue.
                queue.add(request)
            }
        }
        GetServerTask().start()
    }
}