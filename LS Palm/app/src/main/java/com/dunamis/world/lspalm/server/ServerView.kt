package com.dunamis.world.lspalm.server

interface ServerView {

    fun success(
        alKeysList: ArrayList<String>,
        alValueList: ArrayList<String>,
        alImageList: ArrayList<String>
    )

    fun failed()
}