package com.example.lineoverlayeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("overlay", MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 24)
        }
        root.addView(TextView(this).apply {
            text = "LINE Overlay Editor"
            textSize = 24f
        })
        root.addView(TextView(this).apply {
            text = "ตั้งค่าบันทึกส่วนตัว เมื่อพบข้อความที่กำหนด แอปจะแสดงป้ายคำอธิบายของคุณ โดยไม่แก้ไขข้อความต้นฉบับใน LINE"
            textSize = 15f
            setPadding(0, 16, 0, 20)
        })
        val match = EditText(this).apply {
            hint = "ข้อความต้นฉบับที่ต้องการตรวจจับ"
            minLines = 2
            setText(prefs.getString("match", ""))
        }
        root.addView(match, ViewGroup.LayoutParams(-1, -2))
        val note = EditText(this).apply {
            hint = "ข้อความบันทึกส่วนตัวที่ต้องการแสดง"
            minLines = 2
            setText(prefs.getString("note", ""))
        }
        root.addView(note, ViewGroup.LayoutParams(-1, -2))
        val status = TextView(this).apply {
            text = "สถานะ: ยังไม่ได้บันทึก"
            setPadding(0, 12, 0, 12)
        }
        root.addView(status)
        root.addView(Button(this).apply {
            text = "บันทึกการตั้งค่า"
            isAllCaps = false
            setOnClickListener {
                val m = match.text.toString().trim()
                val n = note.text.toString().trim()
                if (m.isEmpty() || n.isEmpty()) {
                    status.text = "กรุณากรอกข้อมูลให้ครบทั้ง 2 ช่อง"
                    Toast.makeText(this@MainActivity, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_LONG).show()
                } else {
                    val ok = prefs.edit().putString("match", m).putString("note", n).commit()
                    status.text = if (ok) "สถานะ: บันทึกสำเร็จ ✓" else "สถานะ: บันทึกไม่สำเร็จ"
                    Toast.makeText(this@MainActivity, status.text, Toast.LENGTH_SHORT).show()
                }
            }
        }, ViewGroup.LayoutParams(-1, -2))
        root.addView(Button(this).apply {
            text = "อนุญาตแสดงทับแอปอื่น"
            isAllCaps = false
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                    try {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    } catch (_: Exception) {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                    }
                } else {
                    Toast.makeText(this@MainActivity, "อนุญาตแสดงทับแอปอื่นแล้ว", Toast.LENGTH_SHORT).show()
                }
            }
        }, ViewGroup.LayoutParams(-1, -2))
        root.addView(Button(this).apply {
            text = "เปิดการตั้งค่าการช่วยเหลือพิเศษ"
            isAllCaps = false
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, ViewGroup.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "วิธีใช้\n1. กรอกข้อความต้นฉบับและข้อความบันทึกส่วนตัว\n2. กดบันทึกการตั้งค่าและตรวจสอบสถานะ\n3. อนุญาตแสดงทับแอปอื่น\n4. เปิดบริการช่วยเหลือพิเศษของแอป\n5. เปิด LINE เพื่อทดสอบ\n\nป้ายที่แสดงเป็นบันทึกส่วนตัว ไม่ใช่ข้อความจริงใน LINE"
            textSize = 14f
            setPadding(0, 22, 0, 0)
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
