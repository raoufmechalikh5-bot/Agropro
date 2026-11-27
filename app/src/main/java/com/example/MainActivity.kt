package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.AgriProApp
import com.example.LocaleHelper
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {
    private val crashReportName = "agripro-crash-report.txt"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashRecorder()

        val previousCrash = File(filesDir, crashReportName)
        if (BuildConfig.DEBUG && previousCrash.exists()) {
            showCrashReport(previousCrash)
        } else {
            launchAgroPro()
        }
    }

    private fun installCrashRecorder() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val writer = StringWriter()
                error.printStackTrace(PrintWriter(writer))
                File(filesDir, crashReportName).writeText(
                    "AgroPro crash report\\nThread: ${thread.name}\\n" +
                        "Time: ${System.currentTimeMillis()}\\n\\n" + writer.toString()
                )
            } catch (_: Throwable) {
                // Preserve the original crash even if writing the diagnostic report fails.
            }
            previousHandler?.uncaughtException(thread, error)
        }
    }

    private fun showCrashReport(report: File) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val title = TextView(this).apply {
            text = "تم تسجيل سبب إغلاق AgroPro"
            textSize = 20f
            setPadding(0, 0, 0, 16)
        }
        val hint = TextView(this).apply {
            text = "اضغط على نسخ سجل الخطأ، ثم الصق النص كاملًا في محادثتنا."
            textSize = 15f
            setPadding(0, 0, 0, 12)
        }
        val details = TextView(this).apply {
            text = report.readText()
            textSize = 12f
            setTextIsSelectable(true)
            setPadding(8, 8, 8, 8)
        }
        val scroll = ScrollView(this).apply {
            addView(details)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val copy = Button(this).apply {
            text = "نسخ سجل الخطأ"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("AgroPro crash report", report.readText()))
                text = "تم النسخ — الصق النص هنا"
            }
        }
        val continueButton = Button(this).apply {
            text = "متابعة فتح التطبيق"
            setOnClickListener {
                report.delete()
                launchAgroPro()
            }
        }
        root.addView(title)
        root.addView(hint)
        root.addView(scroll)
        root.addView(copy)
        root.addView(continueButton)
        setContentView(root)
    }

    private fun launchAgroPro() {
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            AgriProApp()
        }
    }
}
