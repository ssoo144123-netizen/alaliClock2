package com.jalaliclock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = this

        fun toggle(label: String, key: String): Switch = Switch(ctx).apply {
            text = label
            textSize = 18f
            isChecked = AppPrefs.isOn(ctx, key)
            setOnCheckedChangeListener { _, v -> AppPrefs.set(ctx, key, v) }
        }

        fun button(label: String, action: () -> Unit): Button = Button(ctx).apply {
            text = label
            setOnClickListener { action() }
        }

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            addView(toggle("صدای tick هر ثانیه", AppPrefs.TICK))
            addView(toggle("چایم هر ساعت", AppPrefs.CHIME))
            addView(toggle("نمایش همیشه روشن (کم‌نور)", AppPrefs.ALWAYS_ON))
            addView(toggle("نمایش درصد باتری", AppPrefs.SHOW_BATTERY))
            addView(toggle("نمایش تماس و پیام خوانده‌نشده", AppPrefs.SHOW_COUNTS))
            addView(button("فعال‌سازی قفل صفحه برای ویجت") {
                startActivity(
                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(ctx, AdminReceiver::class.java))
                        .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "برای خاموش کردن صفحه با ویجت لازم است")
                )
            })
            addView(button("انتخاب اسکرین‌سیور") {
                startActivity(Intent(AndroidSettings.ACTION_DREAM_SETTINGS))
            })
        }

        val scroll = ScrollView(ctx).apply {
            addView(root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        setContentView(scroll)
    }
}
