package com.raimundo.smartpos.core.localstorage.db

import android.content.Context
import androidx.room.Room

fun createSmartPosDatabase(context: Context): SmartPosDatabase =
    Room.databaseBuilder(context, SmartPosDatabase::class.java, SmartPosDatabase.NAME).build()
