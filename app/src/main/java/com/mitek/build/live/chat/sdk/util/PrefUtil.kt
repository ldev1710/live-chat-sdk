package com.mitek.build.live.chat.sdk.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

@SuppressLint("StaticFieldLeak")
object PrefUtil {
    private val NAME_PREF = "LiveChat.Pref"
    var instance: SharedPreferences? = null
    var context: Context? = null

    fun init(context: Context){
        this.context = context
        instance = context.getSharedPreferences(NAME_PREF,MODE_PRIVATE)
    }

    private fun instance(): SharedPreferences{
        if(instance == null) instance = context!!.getSharedPreferences(NAME_PREF,MODE_PRIVATE)
        return instance!!
    }

    fun getString(key: String): String? {
        return instance().getString(key,null)
    }

    fun setString(key: String,value: String) {
        var editor: Editor = instance().edit()
        editor.putString(key,value)
        editor.apply()
    }
}