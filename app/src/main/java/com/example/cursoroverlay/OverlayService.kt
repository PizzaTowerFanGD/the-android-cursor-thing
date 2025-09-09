package com.example.cursoroverlay

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import androidx.core.graphics.applyCanvas
import java.io.InputStream

class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.example.cursoroverlay.START"
        const val ACTION_STOP = "com.example.cursoroverlay.STOP"
        const val EXTRA_CURSOR_URI = "cursor_uri"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var cursorBitmap: Bitmap? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val uriString = intent.getStringExtra(EXTRA_CURSOR_URI)
                if (uriString != null) {
                    try {
                        val uri = Uri.parse(uriString)
                        contentResolver.openInputStream(uri)?.use { stream ->
                            cursorBitmap = CursorParser.parseCUR(stream)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                startOverlay()
            }
            ACTION_STOP -> stopOverlay()
        }
        return START_STICKY
    }

    private fun startOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        val root = object : View(this) {
            private val image = ImageView(context).apply {
                visibility = View.INVISIBLE
            }

            init {
                // attach image view to window manually by drawing
            }

            private var lastX = 0f
            private var lastY = 0f

            override fun onTouchEvent(event: MotionEvent): Boolean {
                lastX = event.x
                lastY = event.y
                if (cursorBitmap != null) {
                    invalidate()
                }
                return true
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                cursorBitmap?.let {
                    val x = lastX - it.width / 2
                    val y = lastY - it.height / 2
                    canvas.drawBitmap(it, x, y, null)
                }
            }
        }

        overlayView = root
        windowManager?.addView(root, params)
        // Make sure view receives touch
        root.isClickable = true
        root.isFocusable = true
    }

    private fun stopOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
        }
        overlayView = null
        stopSelf()
    }

    override fun onDestroy() {
        stopOverlay()
        super.onDestroy()
    }
}
