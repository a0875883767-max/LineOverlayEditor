package com.example.lineoverlayeditor

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.TextView
import java.util.ArrayDeque

class ChatAccessibilityService : AccessibilityService() {
    private var overlay: View? = null
    private var lastNote: String? = null
    private val tag = "LineOverlayService"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(tag, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        try {
            if (event.packageName?.toString() != "jp.naver.line.android") {
                hideOverlay()
                lastNote = null
                return
            }

            val prefs = getSharedPreferences("overlay", MODE_PRIVATE)
            val match = prefs.getString("match", "").orEmpty()
            val note = prefs.getString("note", "").orEmpty()
            if (match.isBlank() || note.isBlank()) {
                hideOverlay()
                lastNote = null
                return
            }

            val root = rootInActiveWindow
            if (root == null) {
                hideOverlay()
                lastNote = null
                return
            }

            if (containsTextSafely(root, match)) {
                // Also recreate the panel if it was removed while LINE was in the background.
                if (overlay == null || lastNote != note) showOverlay(note)
                lastNote = note
            } else {
                hideOverlay()
                lastNote = null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error handling accessibility event", e)
            hideOverlay()
            lastNote = null
        }
    }

    // Iterative traversal avoids deep recursion on large or unusually nested UI trees.
    private fun containsTextSafely(root: AccessibilityNodeInfo, target: String): Boolean {
        val pending = ArrayDeque<AccessibilityNodeInfo>()
        pending.add(root)
        var visited = 0
        while (pending.isNotEmpty() && visited < 1500) {
            val node = pending.removeFirst()
            visited++
            try {
                if (node.text?.toString()?.contains(target, ignoreCase = true) == true ||
                    node.contentDescription?.toString()?.contains(target, ignoreCase = true) == true) {
                    return true
                }
                for (i in 0 until node.childCount) {
                    val child = node.getChild(i) ?: continue
                    pending.add(child)
                }
            } finally {
                if (node !== root) {
                    @Suppress("DEPRECATION")
                    node.recycle()
                }
            }
        }
        // Recycle any nodes left in the queue if the safety limit is reached.
        while (pending.isNotEmpty()) {
            @Suppress("DEPRECATION")
            pending.removeFirst().recycle()
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

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
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
        } catch (e: Exception) {
            Log.e(tag, "Unable to display overlay", e)
            overlay = null
        }
    }

    private fun hideOverlay() {
        val view = overlay ?: return
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view)
        } catch (e: Exception) {
            Log.w(tag, "Unable to remove overlay cleanly", e)
        } finally {
            overlay = null
        }
    }

    override fun onInterrupt() {
        hideOverlay()
        lastNote = null
    }

    override fun onDestroy() {
        hideOverlay()
        lastNote = null
        super.onDestroy()
    }
}
