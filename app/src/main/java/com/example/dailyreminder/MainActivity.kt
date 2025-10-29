package com.example.dailyreminder

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private val notifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) askExactAlarmThenStart() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Restore toggle state when page loads
                if (PreferenceManager.areNotificationsEnabled(this@MainActivity)) {
                    webView.evaluateJavascript("notificationsEnabled()", null)
                }
            }
        }
        
        // Interface JavaScript pour communiquer avec le HTML
        webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")
        
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun enableNotifications() {
            runOnUiThread {
                // Prevent race conditions with synchronized
                synchronized(this@MainActivity) {
                    if (!PreferenceManager.areNotificationsEnabled(this@MainActivity)) {
                        PreferenceManager.setNotificationsEnabled(this@MainActivity, true)
                        if (Build.VERSION.SDK_INT >= 33) {
                            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            askExactAlarmThenStart()
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun disableNotifications() {
            runOnUiThread {
                // Prevent race conditions with synchronized
                synchronized(this@MainActivity) {
                    if (PreferenceManager.areNotificationsEnabled(this@MainActivity)) {
                        PreferenceManager.setNotificationsEnabled(this@MainActivity, false)
                        AlarmScheduler.cancelNext(this@MainActivity)
                    }
                }
            }
        }
        
        @JavascriptInterface
        fun isNotificationsEnabled(): Boolean {
            return PreferenceManager.areNotificationsEnabled(this@MainActivity)
        }
        
        @JavascriptInterface
        fun getMessageCycleStatus(): String {
            val totalMessages = MessageRepository.getAllMessages(this@MainActivity).size
            val sentMessages = MessageRepository.getSentMessagesCount(this@MainActivity)
            val availableMessages = MessageRepository.getAvailableMessagesCount(this@MainActivity)
            return "Cycle: $sentMessages/$totalMessages sent • $availableMessages remaining"
        }
        
        @JavascriptInterface
        fun resetMessageCycle() {
            runOnUiThread {
                PreferenceManager.resetSentMessages(this@MainActivity)
            }
        }

        @JavascriptInterface
        fun testNow() {
            runOnUiThread {
                // Test the actual message selection system
                val message = MessageRepository.randomMessage(this@MainActivity)
                NotificationHelper.showNow(this@MainActivity, message)
                // Mark as sent for cycle tracking
                MessageRepository.markMessageAsSent(this@MainActivity, message)
            }
        }

        @JavascriptInterface
        fun test30s() {
            runOnUiThread {
                scheduleInSeconds(30)
            }
        }
        
    }

    private fun askExactAlarmThenStart() {
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                })
                return
            }
        }
        AlarmScheduler.scheduleNext(this)
        webView.evaluateJavascript("notificationsEnabled()", null)
    }

    private fun scheduleInSeconds(sec: Int) {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + sec * 1000L
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("test_message", "Test in 30s - Hello Bubble working!")
        }
        val pi = PendingIntent.getBroadcast(
            this, 2002, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}