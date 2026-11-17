package com.example.lineoverlayeditor

import android.app.Activity
import android.content.Intent
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
            setSingleLine(false)
            minLines = 2
            setText(prefs.getString("match", ""))
        }
        root.addView(match, ViewGroup.LayoutParams(-1, -2))

        val note = EditText(this).apply {
            hint = "ข้อความบันทึกส่วนตัวที่ต้องการแสดง"
            setSingleLine(false)
            minLines = 2
            setText(prefs.getString("note", ""))
        }
        root.addView(note, ViewGroup.LayoutParams(-1, -2))

        val saveStatus = TextView(this).apply {
            text = "สถานะ: ยังไม่ได้บันทึกการเปลี่ยนแปลง"
            textSize = 14f
            setPadding(0, 10, 0, 10)
        }
        root.addView(saveStatus, ViewGroup.LayoutParams(-1, -2))

        root.addView(Button(this).apply {
            text = "บันทึกการตั้งค่า"
            isAllCaps = false
            setOnClickListener {
                val matchText = match.text.toString().trim()
                val noteText = note.text.toString().trim()
                if (matchText.isEmpty() || noteText.isEmpty()) {
                    saveStatus.text = "กรุณากรอกข้อความทั้ง 2 ช่องก่อนบันทึก"
                    Toast.makeText(this@MainActivity, "กรุณากรอกข้อมูลให้ครบทั้ง 2 ช่อง", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val saved = prefs.edit()
                    .putString("match", matchText)
                    .putString("note", noteText)
                    .commit()

                val verified = prefs.getString("match", null) == matchText &&
                    prefs.getString("note", null) == noteText

                if (saved && verified) {
                    saveStatus.text = "สถานะ: บันทึกสำเร็จ ✓"
                    Toast.makeText(this@MainActivity, "บันทึกการตั้งค่าสำเร็จ", Toast.LENGTH_SHORT).show()
                } else {
                    saveStatus.text = "สถานะ: บันทึกไม่สำเร็จ กรุณาลองใหม่"
                    Toast.makeText(this@MainActivity, "บันทึกไม่สำเร็จ กรุณาลองใหม่", Toast.LENGTH_LONG).show()
                }
            }
        }, ViewGroup.LayoutParams(-1, -2))

        root.addView(Button(this).apply {
            text = "เปิดการตั้งค่าการช่วยเหลือพิเศษ"
            isAllCaps = false
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }, ViewGroup.LayoutParams(-1, -2))

        root.addView(TextView(this).apply {
            text = "วิธีใช้\n1. กรอกข้อความต้นฉบับและบันทึก\n2. เปิดการตั้งค่าการช่วยเหลือพิเศษ\n3. เปิดบริการ LINE Overlay Editor\n4. เปิด LINE เพื่อดูป้ายบันทึกส่วนตัวเมื่อพบข้อความที่ตรงกัน\n\nป้ายนี้เป็นคำอธิบายส่วนตัวจากแอป ไม่ใช่ข้อความจริงใน LINE"
            textSize = 14f
            setPadding(0, 22, 0, 0)
        })

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
    }
}
