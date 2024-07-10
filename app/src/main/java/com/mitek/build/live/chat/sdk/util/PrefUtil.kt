package com.mitek.build.live.chat.sdk.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("StaticFieldLeak")
object PrefUtil {
    private var prefInstance: SharedPreferences? = null
    private var NAME: String = "LiveChatSDK.Pref"
    private var context: Context? = null

    fun init(context: Context){
        this.context = context
    }

    private fun instance(): SharedPreferences{
        if(prefInstance == null) prefInstance = context!!.getSharedPreferences(NAME,Context.MODE_PRIVATE)
        return prefInstance!!
    }

    @SuppressLint("CommitPrefEdits")
    fun setString(key:String, value:String){
        var editor: SharedPreferences.Editor = instance().edit()
        editor.putString(key,value)
        editor.apply()
    }

    fun getString(key:String) : String?{
        return instance().getString(key,"")
    }
}