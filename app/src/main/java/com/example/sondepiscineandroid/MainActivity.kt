package com.example.SondePiscineAndroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.util.Log // jcb pour log html
fun getValue(html: String, name: String): String? {
    val start = html.indexOf("%$name=")
    if (start == -1) return null

    val valueStart = start + name.length + 2
    val valueEnd = html.indexOf("_", valueStart)

    if (valueEnd == -1) return null

    return html.substring(valueStart, valueEnd).trim()
}
val jcbListOfCapteurs =listOf(
    listOf("AirTemp", "Temp: "," °C<br>"),
    listOf("Humidité","Hum: "," %<br>"),
    listOf("Eau Temp",  "Temp eau: "," °C<br>"),
    listOf("Pression","Pression: "," hPa<br>"),
    listOf("PoolHTemp", "Temp BMP: "," °C<br>")

);


fun jcbGetValue(html: String,Capteur : List<String>) {
    val position = texte.indexOf("Temp eau:")
    val debut = texte.indexOf(":") + 1
    val fin = texte.indexOf("°")

    val temperature = texte.substring(debut, fin).trim()

}
class MainActivity : AppCompatActivity() {


    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
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
            // log request
            Log.d("HTTP_REQUEST", "URL exacte : ${request.url}")
            Log.d("HTTP_REQUEST", "Méthode : ${request.method}")
            Log.d("HTTP_REQUEST", "Headers : ${request.headers}")
            // log request

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {"BMPSLPressure")
                    val dj1Temp = getValue(body, "DJ1Temp")
                    val resetSec = getValue(body, "ResetSec")
                    val forecast = getValue(body, "ForeCast")
                    Log.d("DEBUG", "Pression = $bmpPressure")
                        txtResult.text = "ERREUR:\n${e.message}\n\n1. Même WiFi ?\n2. IP correcte ?\n3. ESP32 allumé ?"
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: "Vide"
                    val espTime = getValue(body, "ESPTime")
                    val dhtTemp = getValue(body, "Temp BMP")
                    val dhtHumidity = getValue(body, "DHTHumidity")
                    val bmpTemp = getValue(body, "Temp BMP")
                    val bmpPressure = getValue(body, "BMPPressure")
                    val bmpSLPressure = getValue(body, "BMPSLPressure")
                    val dj1Temp = getValue(body, "DJ1Temp")
                    val resetSec = getValue(body, "ResetSec")
                    val forecast = getValue(body, "ForeCast")
                    Log.d("DEBUG", "Pression = $bmpPressure")

                    println("Heure       = $espTime")
                    println("DHT Temp    = $dhtTemp")
                    println("Humidité    = $dhtHumidity")
                    println("BMP Temp    = $bmpTemp")
                    println("Pression    = $bmpPressure")
                    println("Pression SL = $bmpSLPressure")
                    println("DJ1 Temp    = $dj1Temp")
                    println("Reset Sec   = $resetSec")
                    println("Prévision   = $forecast")
                    runOnUiThread {

                        var Result: String = ""
                        //Result += "Heure       = $espTime\n"
                        //Result +="DHT Temp    = $dhtTemp\n"
                        //Result +="Humidité    = $dhtHumidity\n"

                        Result +="BMP Temp    = $bmpTemp\n"
                        Result +="Pression    = $bmpPressure\n"
                        Result +="Pression SL = $bmpSLPressure\n"
                        Result +="DJ1 Temp    = $dj1Temp\n"

                        //Result +="Reset Sec   = $resetSec\n"
                        //Result +="Prévision   = $forecast\n"
                        Result += body
                        txtResult.text = Result //"HTTP ${response.code}\n\n$body"
                    }
                }
            })
        }
    }
}
