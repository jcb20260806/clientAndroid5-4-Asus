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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// pour affichage permanent
import android.view.WindowManager
import android.net.wifi.WifiManager
val jcbListOfCapteurs =listOf(
    listOf("AirTemp", "Temp: "," °C<br>"),
    listOf("Humidité","Hum: "," %<br>"),
    listOf("Eau Temp",  "Temp eau: "," °C<br>"),
    listOf("Pression","Pression: "," hPa<br>"),
    listOf("PoolHTemp", "Temp BMP: "," °C<br>")

)


fun jcbGetValue(html: String, Capteur: List<String>): String {
    val startTag = Capteur[1]
    val endTag = Capteur[2]

    val startPos = html.indexOf(startTag)
    if (startPos == -1) return "${Capteur[0]} = non trouvé"

    val debut = startPos + startTag.length
    // On cherche la fin APRES le début de la valeur pour éviter de trouver une balise précédente
    val fin = html.indexOf(endTag, debut)

    if (fin == -1) return "${Capteur[0]} = format inconnu"

    val CapteurValue = html.substring(debut, fin).trim()

    return "${Capteur[0]} = $CapteurValue"
}
class MainActivity : AppCompatActivity() {


    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        val editIp = findViewById<EditText>(R.id.editIp)
        val editEndpoint = findViewById<EditText>(R.id.editEndpoint)
        val txtResult = findViewById<TextView>(R.id.txtResult)
        val txtResult2 = findViewById<TextView>(R.id.txtResult2)
        val btnGo = findViewById<Button>(R.id.btnGo)
        //
        //pour ssid et bssid
        val wifiManager =
            applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        val wifiInfo = wifiManager.connectionInfo

        val ssid = wifiInfo.ssid
        val bssid = wifiInfo.bssid

        println("SSID  = $ssid")
        println("BSSID = $bssid")

        btnGo.setOnClickListener {
            val ip = editIp.text.toString().trim()
            val endpoint = editEndpoint.text.toString().trim()
            val url = "http://$ip$endpoint"

            txtResult.text = "Requête vers $url ..."

            val request = Request.Builder().url(url).build()
                        Log.d("HTTP_REQUEST", "URL exacte : ${request.url}")
            Log.d("HTTP_REQUEST", "Méthode : ${request.method}")
            Log.d("HTTP_REQUEST", "Headers : ${request.headers}")
            // log request

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        txtResult.text = "ERREUR:\n${e.message}\n\n1. Même WiFi ?\n2. IP correcte ?\n3. ESP32 allumé ?"
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: "Vide"
                    runOnUiThread {

                        var Result: String = ""
                        for (c in jcbListOfCapteurs) {
                            val v: String = jcbGetValue(body, c)
                            Result += "$v \n"
                        }

                        // Result += body // inutile d' ajouter la source
                        txtResult2.text = Result
                        val heure = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date())//"HTTP ${response.code}\n\n$body"
                        txtResult.text = "Last Update at $heure From BSSID = $bssid"

                        // Wait 1 second and refresh by triggering the button click again
                        btnGo.postDelayed({
                            btnGo.performClick()
                        }, 20000)
                    }
                }
            })
        }
    }
}
