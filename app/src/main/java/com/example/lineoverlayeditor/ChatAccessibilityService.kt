package com.example.lineoverlayeditor

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView

class ChatAccessibilityService : AccessibilityService() {
    private var overlay: android.view.View? = null
    private var lastNote: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName?.toString() != "jp.naver.line.android") {
            hideOverlay()
            return
        }
        val prefs = getSharedPreferences("overlay", MODE_PRIVATE)
        val match = prefs.getString("match", "").orEmpty()
        val note = prefs.getString("note", "").orEmpty()
        if (match.isBlank() || note.isBlank()) {
            hideOverlay()
            return
        }
        val root = rootInActiveWindow ?: return
        if (containsText(root, match)) {
            if (lastNote != note) showOverlay(note)
            lastNote = note
        } else {
            hideOverlay()
            lastNote = null
        }
    }

    private fun containsText(node: AccessibilityNodeInfo, target: String): Boolean {
        if (node.text?.toString()?.contains(target, ignoreCase = true) == true) return true
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = containsText(child, target)
            child.recycle()
            if (found) return true
        }
        return false
    }

    private fun showOverlay(note: String) {
        if (overlay != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            setBackgroundColor(0xEE263238.toInt())
            addView(TextView(this@ChatAccessibilityService).apply {
                text = "บันทึกส่วนตัวของคุณ · ไม่ใช่ข้อความ LINE"
                textSize = 12f
                setTextColor(0xFFFFD180.toInt())
            })
            addView(TextView(this@ChatAccessibilityService).apply {
                text = note
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 6, 0, 0)
            })
        }
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.BOTTOM }
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(panel, params)
            overlay = panel
        } catch (_: Exception) {
            overlay = null
        }
    }

    private fun hideOverlay() {
        val view = overlay ?: return
        try { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view) } catch (_: Exception) {}
        overlay = null
    }

    override fun onInterrupt() { hideOverlay() }
    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }
}
