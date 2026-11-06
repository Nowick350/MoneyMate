package com.example.moneymate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Live exchange rates from the Frankfurter REST API (https://api.frankfurter.dev).
 *
 * Why this exists: the project brief asks for a REST API integration. Frankfurter
 * is a free, real public REST service that answers with JSON over HTTPS, so this
 * card shows genuine data coming from the internet - not simulated values.
 *
 * - Base currency is ZAR: each answer is "1 rand buys X of that currency".
 * - The download runs on a background thread; the UI just shows the result.
 * - If there is no internet (or the answer is broken), the card shows a friendly
 *   note instead of crashing.
 */
object ExchangeRates {
    private const val API_URL =
        "https://api.frankfurter.dev/v2/rates?base=ZAR&quotes=USD,GBP,EUR"

    data class Snapshot(val date: String, val usd: Double, val eur: Double, val gbp: Double)

    /** Fetches the latest rates. Throws on any problem (the card handles it). */
    suspend fun fetch(): Snapshot = withContext(Dispatchers.IO) {
        val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) throw Exception("HTTP " + connection.responseCode)
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            var date = ""
            var usd = 0.0
            var eur = 0.0
            var gbp = 0.0
            for (i in 0 until array.length()) {
                val entry = array.getJSONObject(i)
                if (date.isEmpty()) date = entry.optString("date")
                when (entry.optString("quote")) {
                    "USD" -> usd = entry.optDouble("rate", 0.0)
                    "EUR" -> eur = entry.optDouble("rate", 0.0)
                    "GBP" -> gbp = entry.optDouble("rate", 0.0)
                }
            }
            if (date.isEmpty() || usd + eur + gbp <= 0.0) throw Exception("Unexpected reply")
            Snapshot(date, usd, eur, gbp)
        } finally {
            connection.disconnect()
        }
    }
}

/** One currency line inside the card: "USD · $" ..... "0.0608" */
@Composable
private fun RateRow(code: String, symbol: String, value: Double) {
    val c = LocalMmx.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$code · $symbol", fontSize = 14.sp, color = c.muted)
        Text(
            String.format(Locale.US, "%.4f", value),
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text
        )
    }
}

/**
 * The card shown on the Home screen. Loads once when the Home tab opens;
 * tap it to re-fetch the latest rates.
 */
@Composable
fun ExchangeRateCard() {
    val c = LocalMmx.current
    val scope = rememberCoroutineScope()
    var snapshot by remember { mutableStateOf<ExchangeRates.Snapshot?>(null) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    fun load() {
        loading = true
        failed = false
        scope.launch {
            try {
                snapshot = ExchangeRates.fetch()
            } catch (_: Exception) {
                if (snapshot == null) failed = true
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Card(
        Modifier
            .fillMaxWidth()
            .clickable { if (!loading) load() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    L.t("Live exchange rates"),
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text
                )
                if (loading) {
                    Spacer(Modifier.width(10.dp))
                    CircularProgressIndicator(
                        color = c.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(L.t("1 South African rand"), fontSize = 12.sp, color = c.muted)

            when {
                snapshot != null -> {
                    val s = snapshot!!
                    Spacer(Modifier.height(10.dp))
                    RateRow("USD", "$", s.usd)
                    RateRow("EUR", "€", s.eur)
                    RateRow("GBP", "£", s.gbp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        L.t("Updated") + " " + s.date + " · " + L.t("Tap to refresh"),
                        fontSize = 12.sp, color = c.muted
                    )
                }
                loading -> {
                    Spacer(Modifier.height(10.dp))
                    Text(L.t("Checking today's rates…"), fontSize = 13.sp, color = c.muted)
                }
                else -> {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        L.t("Rates are not available right now. Check your internet and tap this card to try again."),
                        fontSize = 13.sp, color = c.muted
                    )
                }
            }
        }
    }
}