package com.dunamis.world.lspalm.login

interface LoginView {

    fun success(faceAuth: String, scanType: String)
    fun failed(msg: String)
    fun logoImgRes(logoImgUrl: String)
}