package com.aryvir58.jarvis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private lateinit var input: EditText
    private val requestCode = 41
    private val voiceCode = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        buildUi()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), requestCode)
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 50, 32, 28); setBackgroundColor(android.graphics.Color.rgb(8,11,18))
        }
        val title = TextView(this).apply {
            text = "JARVIS"; textSize = 32f; gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.WHITE)
        }
        val sub = TextView(this).apply {
            text = "Your Android voice assistant"; textSize = 15f; gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.LTGRAY)
        }
        status = TextView(this).apply {
            text = "Ready"; textSize = 20f; gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.CYAN); setPadding(0,50,0,40)
        }
        input = EditText(this).apply {
            hint = "Type a command…"; setSingleLine(false); minLines = 2
            setTextColor(android.graphics.Color.WHITE); setHintTextColor(android.graphics.Color.GRAY)
        }
        val send = Button(this).apply { text = "SEND" }
        val mic = Button(this).apply { text = "🎙  TALK TO JARVIS" }
        val help = TextView(this).apply {
            text = "Try: “what time is it”, “hello”, “open YouTube”"; textSize = 13f
            setTextColor(android.graphics.Color.GRAY); setPadding(0,30,0,0)
        }
        send.setOnClickListener { handle(input.text.toString()) }
        mic.setOnClickListener { startVoice() }
        root.addView(title, LinearLayout.LayoutParams(-1, -2)); root.addView(sub)
        root.addView(status, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(input, LinearLayout.LayoutParams(-1, -2))
        root.addView(send, LinearLayout.LayoutParams(-1, -2)); root.addView(mic, LinearLayout.LayoutParams(-1, -2))
        root.addView(help)
        setContentView(root)
    }

    private fun startVoice() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Jarvis")
        }
        try { startActivityForResult(i, voiceCode) } catch (_: Exception) { speak("Voice recognition is not available on this device.") }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == voiceCode && resultCode == RESULT_OK) {
            val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            input.setText(text); handle(text)
        }
    }

    private fun handle(raw: String) {
        val q = raw.trim(); if (q.isEmpty()) return
        val l = q.lowercase(Locale.getDefault())
        val answer = when {
            l.contains("hello") || l.contains("hi") || l.contains("namaste") -> "Hello. Jarvis is ready."
            l.contains("time") -> "The time is " + java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(java.util.Date())
            l.contains("date") -> "Today is " + java.text.SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(java.util.Date())
            l.contains("who are you") -> "I am Jarvis, your Android voice assistant."
            else -> "I heard: $q. Connect an AI endpoint in the next version for full conversational answers."
        }
        status.text = answer
        speak(answer)
    }

    private fun speak(text: String) { if (::tts.isInitialized) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis") }
    override fun onInit(statusCode: Int) { if (statusCode == TextToSpeech.SUCCESS) tts.language = Locale.getDefault() }
    override fun onDestroy() { tts.shutdown(); super.onDestroy() }
}
