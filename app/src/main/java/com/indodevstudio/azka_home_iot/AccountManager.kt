package com.indodevstudio.azka_home_iot

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AccountManager {
    private const val PREF_NAME = "account_prefs"
    private const val ACCOUNTS_KEY = "accounts"
    private const val CURRENT_EMAIL_KEY = "current_account_email"

    fun getAllAccounts(context: Context): List<AccountData> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(ACCOUNTS_KEY, null)
        return if (json != null) {
            Gson().fromJson(json, object : TypeToken<List<AccountData>>() {}.type)
        } else emptyList()
    }

    fun saveAccount(context: Context, account: AccountData) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val list = getAllAccounts(context).filter { it.email != account.email }.toMutableList()
        list.add(account)

        prefs.edit()
            .putString(ACCOUNTS_KEY, Gson().toJson(list))
            .apply()
    }

    fun setCurrentAccount(context: Context, email: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(CURRENT_EMAIL_KEY, email)
            .apply()
    }

    fun getCurrentAccount(context: Context): AccountData? {
        val email = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(CURRENT_EMAIL_KEY, null)
        return getAllAccounts(context).find { it.email == email }
    }
}
