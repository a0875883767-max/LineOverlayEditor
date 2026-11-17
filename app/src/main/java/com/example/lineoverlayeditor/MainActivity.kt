package com.example.lineoverlayeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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
            setText(prefs.getString("match", ""))
        }
        root.addView(match, ViewGroup.LayoutParams(-1, -2))
        val note = EditText(this).apply {
            hint = "ข้อความบันทึกส่วนตัวที่ต้องการแสดง"
            setText(prefs.getString("note", ""))
        }
        root.addView(note, ViewGroup.LayoutParams(-1, -2))
        root.addView(Button(this).apply {
            text = "บันทึกการตั้งค่า"
            setOnClickListener {
                prefs.edit().putString("match", match.text.toString().trim())
                    .putString("note", note.text.toString().trim()).apply()
                Toast.makeText(this@MainActivity, "บันทึกแล้ว", Toast.LENGTH_SHORT).show()
            }
        }, ViewGroup.LayoutParams(-1, -2))
        root.addView(Button(this).apply {
            text = "เปิดการตั้งค่าการช่วยเหลือพิเศษ"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, ViewGroup.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "วิธีใช้\n1. กรอกข้อความต้นฉบับและบันทึก\n2. เปิดการตั้งค่าการช่วยเหลือพิเศษ\n3. เปิดบริการ LINE Overlay Editor\n4. เปิด LINE เพื่อดูป้ายบันทึกส่วนตัวเมื่อพบข้อความที่ตรงกัน\n\nป้ายนี้เป็นคำอธิบายส่วนตัวจากแอป ไม่ใช่ข้อความจริงใน LINE"
            textSize = 14f
            setPadding(0, 22, 0, 0)
        })
        setContentView(root)
    }
}
