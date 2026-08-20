package com.example.SondePiscineAndroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editIp = findViewById<EditText>(R.id.editIp)
        val editEndpoint = findViewById<EditText>(R.id.editEndpoint)
        val txtResult = findViewById<TextView>(R.id.txtResult)
        val btnGo = findViewById<Button>(R.id.btnGo)

        btnGo.setOnClickListener {
            val ip = editIp.text.toString().trim()
            val endpoint = editEndpoint.text.toString().trim()
            val url = "http://$ip$endpoint"

            txtResult.text = "Requête vers $url ..."

            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        txtResult.text = "ERREUR:\n${e.message}\n\n1. Même WiFi ?\n2. IP correcte ?\n3. ESP32 allumé ?"
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: "Vide"
                    runOnUiThread {
                        txtResult.text = "HTTP ${response.code}\n\n$body"
                    }
                }
            })
        }
    }
}
