package com.example.cursoroverlay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = OverlayService.ACTION_START
            intent.putExtra(OverlayService.EXTRA_CURSOR_URI, it.toString())
            startService(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnPick).setOnClickListener {
            pickFile.launch("*/*")
        }

        findViewById<Button>(R.id.btnTogglePermission).setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = OverlayService.ACTION_STOP
            startService(intent)
        }
    }
}
