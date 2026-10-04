package com.morex.father.auth

import android.content.Context
import com.morex.father.data.Prefs
import com.morex.father.network.ApiClient
import com.morex.father.network.SocketManager

object SessionManager {

    fun saveLogin(
        context: Context,
        token: String,
        parentId: String,
        name: String,
        phone: String,
        email: String?,
        avatar: String?
    ) {
        Prefs.saveToken(context, token)
        Prefs.saveParent(context, parentId, name, phone, email, avatar)
        ApiClient.setAuthToken(token)
        SocketManager.connect(context)
    }

    fun logout(context: Context) {
        try {
            SocketManager.disconnect()
        } catch (_: Exception) {}
        Prefs.clearAuth(context)
        ApiClient.setAuthToken(null)
    }

    fun isLoggedIn(context: Context): Boolean = Prefs.isLoggedIn(context)
    fun getToken(context: Context): String? = Prefs.getToken(context)
    fun getParentId(context: Context): String? = Prefs.getParentId(context)
    fun getParentName(context: Context): String? = Prefs.getParentName(context)
    fun getParentPhone(context: Context): String? = Prefs.getParentPhone(context)
}
