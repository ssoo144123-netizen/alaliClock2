package com.jalaliclock

import android.content.Context
import android.content.pm.PackageManager
import android.provider.BaseColumns
import android.provider.CallLog
import android.provider.Telephony
import androidx.core.content.ContextCompat

/** Missed (unseen) calls and unread SMS. Returns -1 on failure. */
object NotifCounter {

    fun hasPermissions(ctx: Context): Boolean =
        ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

    fun missedCalls(ctx: Context): Int = count(
        ctx, CallLog.Calls.CONTENT_URI,
        "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.NEW} = 1",
        arrayOf(CallLog.Calls.MISSED_TYPE.toString())
    )

    fun unreadSms(ctx: Context): Int = count(
        ctx, Telephony.Sms.Inbox.CONTENT_URI,
        "${Telephony.Sms.READ} = 0", null
    )

    private fun count(ctx: Context, uri: android.net.Uri, sel: String, args: Array<String>?): Int = try {
        ctx.contentResolver.query(uri, arrayOf(BaseColumns._ID), sel, args, null)?.use { it.count } ?: 0
    } catch (e: Exception) {
        -1
    }
}
