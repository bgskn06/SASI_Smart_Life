//package com.example.sasi_smart_life
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.util.Log
//
//class BootReceiver : BroadcastReceiver() {
//
//    override fun onReceive(context: Context, intent: Intent) {
//
//        Log.d("BOOT_DEBUG_SASI", "BootReceiver triggered: ${intent.action}")
//
//        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
//            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
//
//            Log.d("BOOT_DEBUG_SASI", "Boot completed detected")
//
//            val serviceIntent = Intent(context, SmartHomeService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(serviceIntent)
//            } else {
//                context.startService(serviceIntent)
//            }
//        }
//    }
//}