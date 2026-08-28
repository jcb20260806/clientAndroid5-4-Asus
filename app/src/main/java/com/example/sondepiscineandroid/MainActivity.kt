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
    val startTag = "%$name="
    val start = html.indexOf(startTag)
    if (start == -1) return null

    val valueStart = start + startTag.length
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
                        txtResult.text = Result //"HTTP ${response.code}\n\n$body"
                    }
                }
            })
        }
    }
}
