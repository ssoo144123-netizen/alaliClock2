package com.jalaliclock

import android.app.Activity
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import java.util.Calendar

class AlarmsActivity : Activity() {
    private lateinit var adapter: ArrayAdapter<String>
    private var alarms = mutableListOf<Alarm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
        list.adapter = adapter
        list.setOnItemClickListener { _, _, pos, _ ->
            alarms[pos].enabled = !alarms[pos].enabled
            save()
        }
        list.setOnItemLongClickListener { _, _, pos, _ ->
            alarms.removeAt(pos)
            save()
            true
        }
        val add = Button(this).apply {
            text = "افزودن آلارم"
            setOnClickListener { pickTime() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            addView(add)
            addView(list, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        })
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        alarms = AlarmStore.load(this)
        adapter.clear()
        adapter.addAll(alarms.map { (if (it.enabled) "✓ " else "✗ ") + Jalali.toPersianDigits(it.label()) })
    }

    private fun save() {
        AlarmStore.save(this, alarms)
        AlarmScheduler.scheduleAll(this)
        reload()
    }

    private fun pickTime() {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            alarms.add(Alarm(h, m, true))
            save()
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }
}
