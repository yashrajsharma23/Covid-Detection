package com.matrix.aimlcapstone.utils

import android.content.Context
import android.preference.PreferenceManager

open class SharedPreferenceHelper {

    fun setFirstTimeTutorial(context: Context, tutorialKey: String) {
        val prefer = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefer.edit()
        editor.putBoolean(tutorialKey, true)
        editor.apply()
    }

    fun getFirstTimeTutorial(context: Context, tutorialKey: String): Boolean {
        val prefer = PreferenceManager.getDefaultSharedPreferences(context)
        return prefer.getBoolean(tutorialKey, false)
    }
}