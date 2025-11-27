package com.example.myapplication.ui.Log_in

import android.content.Context

object TokenStore {
    private const val SP = "auth"
    private const val KEY_TOKEN = "accessToken"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_ROLE = "role"

    fun saveAll(context: Context, token: String?, nickname: String?, role: String?) {
        if (token.isNullOrBlank()) return
        val sp = context.getSharedPreferences(SP, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getToken(context: Context): String? =
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    fun getNickname(context: Context): String? =
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .getString(KEY_NICKNAME, null)

    fun getRole(context: Context): String? =
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)

    fun clear(context: Context) {
        context.getSharedPreferences(SP, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
