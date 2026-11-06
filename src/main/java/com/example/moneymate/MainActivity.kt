package com.example.moneymate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.core.app.NotificationManagerCompat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------- config
// 10.0.2.2 is how the Android emulator reaches your own computer.
// On a real phone, replace this with your computer's network IP, e.g. "http://192.168.1.15:4000"
const val API_BASE = "http://10.0.2.2:4000"
private const val PREFS = "moneymate"
private const val TOKEN_KEY = "token"

// How long the opening screen stays up, in milliseconds.
private const val OPENING_MILLIS = 1800L

// true  = every request is answered by MmRepo: Room on the phone, Firestore in the cloud.
// false = every request goes to your own node server over HTTP (see API_BASE above).
const val USE_LOCAL_DB = true

// ---------------------------------------------------------------- brand
val Forest = Color(0xFF14432A)
val Lime = Color(0xFFB8E986)
val Sand = Color(0xFFF7F4EE)
val Ink = Color(0xFF1B1B18)
val Muted = Color(0xFF6B6B60)
val Danger = Color(0xFFB3261E)

/**
 * Two full palettes. The app picks one from the theme saved in your account
 * settings, so every screen follows it.
 */
data class MmxColors(
    val background: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val primary: Color,
    val onPrimary: Color,
    val danger: Color,
    val divider: Color,
    val errorBackground: Color,
    val isDark: Boolean
)

val LightMmx = MmxColors(
    background = Sand,
    surface = Color.White,
    text = Ink,
    muted = Muted,
    accent = Forest,
    primary = Forest,
    onPrimary = Color.White,
    danger = Danger,
    divider = Color(0xFFE6E1D6),
    errorBackground = Color(0xFFFDECEA),
    isDark = false
)

val DarkMmx = MmxColors(
    background = Color(0xFF0E1410),
    surface = Color(0xFF17201A),
    text = Color(0xFFEDF1EA),
    muted = Color(0xFF9BA79C),
    accent = Color(0xFF8FD79B),
    primary = Color(0xFF1F5C3A),
    onPrimary = Color.White,
    danger = Color(0xFFFF8A80),
    divider = Color(0xFF2B352D),
    errorBackground = Color(0xFF3A211F),
    isDark = true
)

/** Which palette the composables below are currently drawing with. */
val LocalMmx = compositionLocalOf { LightMmx }

enum class Screen { LOADING, WELCOME, REGISTER, LOGIN, MAIN, ADD_TRANSACTION }
enum class Tab { HOME, BUDGETS, GOALS, ACTIVITY, SETTINGS }

// ---------------------------------------------------------------- local + cloud
// The data layer now lives in two files next to this one:
//   MmDatabase.kt : the Room tables, the DAO and the database (the phone's own copy)
//   MmRepo.kt     : Firebase sign-in, Room reads/writes, Firestore sync, dashboard JSON
// Every screen below still calls Api.get / Api.post exactly as before - only the
// engine behind Api has changed.
// ---------------------------------------------------------------- tiny API client
object Api {
    var token: String? = null

    class ApiError(message: String) : Exception(message)

    /** Answers one request and returns the parsed JSON body. */
    private suspend fun call(method: String, path: String, payload: JSONObject?): JSONObject {
        // Room on the phone answers everything while USE_LOCAL_DB is true.
        if (USE_LOCAL_DB) return MmRepo.dispatch(method, path, payload ?: JSONObject(), token)

        val connection = (URL(API_BASE + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Content-Type", "application/json")
            token?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (payload != null) {
                doOutput = true
                outputStream.use { it.write(payload.toString().toByteArray()) }
            }
        }

        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val text = stream?.bufferedReader()?.use(BufferedReader::readText).orEmpty()
        connection.disconnect()

        val json = if (text.isBlank()) JSONObject() else JSONObject(text)
        if (status !in 200..299) {
            throw ApiError(json.optString("error", L.t("Something went wrong. Please try again.")))
        }
        return json
    }

    suspend fun post(path: String, payload: JSONObject): JSONObject =
        withContext(Dispatchers.IO) { call("POST", path, payload) }

    suspend fun get(path: String): JSONObject =
        withContext(Dispatchers.IO) { call("GET", path, null) }

    suspend fun patch(path: String, payload: JSONObject): JSONObject =
        withContext(Dispatchers.IO) { call("PATCH", path, payload) }

    suspend fun delete(path: String): JSONObject =
        withContext(Dispatchers.IO) { call("DELETE", path, null) }
}

// ---------------------------------------------------------------- formatting helpers
/** Turns 4865000 cents into "R 48 650.00" */
fun rands(cents: Int): String {
    val symbols = DecimalFormatSymbols().apply { groupingSeparator = ' '; decimalSeparator = '.' }
    return "R " + DecimalFormat("#,##0.00", symbols).format(cents / 100.0)
}

/** Turns 400000 cents into "4000.00" — for filling in an edit form. */
fun plainRands(cents: Int): String = String.format(Locale.US, "%.2f", cents / 100.0)

/** Reads what someone typed into a money box, e.g. "2500", "2 500", "2499,50". */
fun parseRand(text: String): Double? {
    val cleaned = text.trim().replace(" ", "").replace(",", ".")
    if (cleaned.isEmpty()) return null
    val value = cleaned.toDoubleOrNull() ?: return null
    return if (value > 0 && value < 10_000_000) value else null
}

fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MmRepo.init(applicationContext)
        L.init(applicationContext)
        setContent { MoneyMateApp() }
    }
}

@Composable
fun MoneyMateApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var screen by remember { mutableStateOf(Screen.LOADING) }
    var tab by remember { mutableStateOf(Tab.HOME) }
    var dashboard by remember { mutableStateOf<JSONObject?>(null) }
    var theme by remember { mutableStateOf(prefs.getString("theme", "light") ?: "light") }

    // The receipt picker and the "Sign in with Google" dialog must be
    // registered while the app is starting, so they live here at the top
    // level - never inside the individual screens.
    val activity = context as? ComponentActivity
    val appScope = rememberCoroutineScope()
    var receiptUri by remember { mutableStateOf<String?>(null) }

    val pickReceipt = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) receiptUri = uri.toString()
    }

    fun signedIn(data: JSONObject, token: String) {
        Api.token = token
        prefs.edit().putString(TOKEN_KEY, token).apply()
        dashboard = data
        tab = Tab.HOME
        screen = Screen.MAIN
    }

    fun triggerGoogleSignIn() {
        appScope.launch {
            try {
                if (GOOGLE_CLIENT_ID.startsWith("PASTE_")) {
                    Toast.makeText(
                        context,
                        L.t("Google sign-in is not set up yet. Use email and password."),
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(GOOGLE_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val res = MmRepo.googleSignIn(googleIdTokenCredential.idToken)
                    signedIn(Api.get("/api/dashboard"), res.getString("token"))
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.message ?: L.t("Something went wrong. Please try again."),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Stay signed in: reuse the saved token on launch.
    LaunchedEffect(Unit) {
        val startedAt = System.currentTimeMillis()

        // Work out where to go while the opening screen is showing.
        val saved = prefs.getString(TOKEN_KEY, null)
        var next = Screen.WELCOME
        if (!saved.isNullOrEmpty()) {
            Api.token = saved
            try {
                dashboard = Api.get("/api/dashboard")
                next = Screen.MAIN
            } catch (_: Exception) {
                Api.token = null
                prefs.edit().remove(TOKEN_KEY).apply()
            }
        }

        // Hold the opening on screen for a moment so it does not just flash past.
        val elapsed = System.currentTimeMillis() - startedAt
        if (elapsed < OPENING_MILLIS) delay(OPENING_MILLIS - elapsed)
        screen = next
    }

    // Follow the theme saved in the account, so it travels with the user.
    val savedTheme = dashboard?.optJSONObject("user")
        ?.optJSONObject("settings")?.optString("theme")
    LaunchedEffect(savedTheme) {
        if (!savedTheme.isNullOrEmpty() && savedTheme != theme) {
            theme = savedTheme
            prefs.edit().putString("theme", savedTheme).apply()
        }
    }

    val palette = if (theme == "dark") DarkMmx else LightMmx

    CompositionLocalProvider(LocalMmx provides palette) {
        MaterialTheme(
            colorScheme = if (palette.isDark) {
                darkColorScheme(
                    primary = palette.accent,
                    onPrimary = palette.onPrimary,
                    background = palette.background,
                    surface = palette.surface,
                    onBackground = palette.text,
                    onSurface = palette.text,
                    error = palette.danger
                )
            } else {
                lightColorScheme(
                    primary = palette.primary,
                    onPrimary = palette.onPrimary,
                    background = palette.background,
                    surface = palette.surface,
                    onBackground = palette.text,
                    onSurface = palette.text,
                    error = palette.danger
                )
            }
        ) {
            when (screen) {
                Screen.LOADING -> SplashScreen()

                Screen.WELCOME -> WelcomeScreen(
                    onRegister = { screen = Screen.REGISTER },
                    onLogin = { screen = Screen.LOGIN }
                )

                Screen.REGISTER -> AuthScreen(
                    isRegister = true,
                    onBack = { screen = Screen.WELCOME },
                    onSwap = { screen = Screen.LOGIN },
                    onGoogle = { triggerGoogleSignIn() },
                    onDone = { data, token -> signedIn(data, token) }
                )

                Screen.LOGIN -> AuthScreen(
                    isRegister = false,
                    onBack = { screen = Screen.WELCOME },
                    onSwap = { screen = Screen.REGISTER },
                    onGoogle = { triggerGoogleSignIn() },
                    onDone = { data, token -> signedIn(data, token) }
                )

                Screen.MAIN -> MainScreen(
                    data = dashboard,
                    tab = tab,
                    onTab = { tab = it },
                    onRefresh = { dashboard = it },
                    onAddTransaction = { screen = Screen.ADD_TRANSACTION },
                    onSignOut = {
                        MmRepo.signOut()
                        Api.token = null
                        prefs.edit().remove(TOKEN_KEY).apply()
                        dashboard = null
                        tab = Tab.HOME
                        screen = Screen.WELCOME
                    },
                    onToken = { newToken ->
                        if (newToken.isNotEmpty()) {
                            Api.token = newToken
                            prefs.edit().putString(TOKEN_KEY, newToken).apply()
                        }
                    }
                )

                Screen.ADD_TRANSACTION -> AddTransactionScreen(
                    data = dashboard,
                    onCancel = { screen = Screen.MAIN },
                    onPickReceipt = { pickReceipt.launch("image/*") },
                    receiptUri = receiptUri,
                    onClearReceipt = { receiptUri = null },
                    onSaved = { updated ->
                        receiptUri = null
                        dashboard = updated
                        tab = Tab.ACTIVITY
                        screen = Screen.MAIN
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------- welcome
@Composable
fun WelcomeScreen(onRegister: () -> Unit, onLogin: () -> Unit) {
    val c = LocalMmx.current
    Column(
        Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
        LogoMark()
        Spacer(Modifier.height(20.dp))
        Text("MoneyMate", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = c.text)
        Spacer(Modifier.height(8.dp))
        Text(L.t("A little clarity for your money."), fontSize = 16.sp, color = c.muted)
        Spacer(Modifier.height(48.dp))
        PrimaryButton(L.t("Create my account"), onClick = onRegister)
        Spacer(Modifier.height(12.dp))
        SecondaryButton(L.t("I already have an account"), onClick = onLogin)
        Spacer(Modifier.height(28.dp))
        Text(
            L.t("Demo prototype. Balances and transactions are simulated, not real money."),
            fontSize = 12.sp, color = c.muted
        )
    }
}

// ---------------------------------------------------------------- register / login
@Composable
fun AuthScreen(
    isRegister: Boolean,
    onBack: () -> Unit,
    onSwap: () -> Unit,
    onGoogle: () -> Unit,
    onDone: (JSONObject, String) -> Unit
) {
    val c = LocalMmx.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        error = when {
            isRegister && name.trim().length < 2 -> L.t("Please enter your name.")
            !email.contains("@") -> L.t("Please enter a valid email address.")
            password.isEmpty() -> L.t("Please enter your password.")
            isRegister && password.length < 10 -> L.t("Your password needs at least 10 characters.")
            isRegister && password != confirm -> L.t("Your passwords don't match.")
            else -> ""
        }
        if (error.isNotEmpty()) return

        busy = true
        scope.launch {
            try {
                val payload = JSONObject().put("email", email.trim()).put("password", password)
                if (isRegister) payload.put("name", name.trim())
                val path = if (isRegister) "/api/auth/register" else "/api/auth/login"

                val result = Api.post(path, payload)
                val token = result.getString("token")
                Api.token = token
                onDone(Api.get("/api/dashboard"), token)
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not reach the MoneyMate server.")
            } finally {
                busy = false
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Text(L.t("← Back"), color = c.accent)
        }
        Spacer(Modifier.height(12.dp))
        LogoMark()
        Spacer(Modifier.height(20.dp))
        Text(
            if (isRegister) L.t("Meet your money's new mate.") else L.t("Good to see you again."),
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (isRegister) L.t("A fresh start for you and your finances.")
            else L.t("Sign in and pick up where you left off."),
            fontSize = 15.sp, color = c.muted
        )
        Spacer(Modifier.height(28.dp))

        if (isRegister) {
            LabelledField(L.t("Full name"), name, { name = it }, L.t("e.g. Thando Mokoena"))
            Spacer(Modifier.height(14.dp))
        }
        LabelledField(L.t("Email address"), email, { email = it }, "you@example.com", KeyboardType.Email)
        Spacer(Modifier.height(14.dp))
        LabelledField(
            L.t("Password"), password, { password = it },
            if (isRegister) L.t("At least 10 characters") else L.t("Your password"), isPassword = true
        )
        if (isRegister) {
            Spacer(Modifier.height(14.dp))
            LabelledField(L.t("Confirm password"), confirm, { confirm = it }, L.t("One more time"), isPassword = true)
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            ErrorNote(error)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            if (busy) L.t("Please wait…")
            else if (isRegister) L.t("Create my account") else L.t("Sign in to MoneyMate"),
            enabled = !busy,
            onClick = ::submit
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSwap, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (isRegister) L.t("Already a mate? Sign in") else L.t("New around here? Create an account"),
                color = c.accent
            )
        }
        Spacer(Modifier.height(10.dp))
        SecondaryButton(L.t("Sign in with Google"), enabled = !busy) { onGoogle() }
        Spacer(Modifier.height(8.dp))
        Text(
            L.t("Your password is checked by Firebase Authentication and never stored by MoneyMate. Everything you add is kept in the phone's Room database and copied to Firestore too."),
            fontSize = 12.sp, color = c.muted
        )
        Spacer(Modifier.height(40.dp))
    }
}

// ---------------------------------------------------------------- main shell
@Composable
fun MainScreen(
    data: JSONObject?,
    tab: Tab,
    onTab: (Tab) -> Unit,
    onRefresh: (JSONObject) -> Unit,
    onAddTransaction: () -> Unit,
    onSignOut: () -> Unit,
    onToken: (String) -> Unit
) {
    val c = LocalMmx.current
    if (data == null) {
        Box(Modifier.fillMaxSize().background(c.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = c.accent)
        }
        return
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                Tab.HOME -> HomeTab(data, onRefresh, onAddTransaction)
                Tab.BUDGETS -> BudgetsTab(data, onRefresh)
                Tab.GOALS -> GoalsTab(data, onRefresh)
                Tab.ACTIVITY -> ActivityTab(data, onRefresh, onAddTransaction)
                Tab.SETTINGS -> SettingsTab(data, onRefresh, onSignOut, onToken)
            }
        }
        BottomBar(tab, onTab)
    }
}

@Composable
fun BottomBar(tab: Tab, onTab: (Tab) -> Unit) {
    val c = LocalMmx.current
    val items = listOf(
        Tab.HOME to L.t("Home"),
        Tab.BUDGETS to L.t("Budgets"),
        Tab.GOALS to L.t("Goals"),
        Tab.ACTIVITY to L.t("Activity"),
        Tab.SETTINGS to L.t("Settings")
    )
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
        Row(
            Modifier.fillMaxWidth().background(c.surface).padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (target, label) ->
                val selected = tab == target
                Column(
                    Modifier
                        .weight(1f)
                        .clickable { onTab(target) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .width(22.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selected) c.accent else Color.Transparent)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) c.accent else c.muted
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- home tab
@Composable
fun HomeTab(data: JSONObject, onRefresh: (JSONObject) -> Unit, onAddTransaction: () -> Unit) {
    val c = LocalMmx.current
    val scope = rememberCoroutineScope()
    val user = data.getJSONObject("user")
    val summary = data.getJSONObject("summary")
    val accounts = data.getJSONArray("accounts")
    val transactions = data.getJSONArray("transactions")
    val budgets = data.getJSONArray("budgets")
    val hide = user.getJSONObject("settings").optBoolean("hideBalances", false)

    fun show(cents: Int) = if (hide) "R ••••" else rands(cents)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text(L.t("Welcome back,"), fontSize = 14.sp, color = c.muted)
        Text(user.getString("name"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)

        Spacer(Modifier.height(20.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = c.primary)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(L.t("TOTAL BALANCE"), color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(
                    show(summary.getInt("totalBalance")),
                    color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Lime))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${accounts.length()} ${L.t("demo accounts")} · ${summary.getString("currency")}",
                        color = Color.White, fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        ExchangeRateCard()

        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("Your accounts"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(10.dp))
                for (i in 0 until accounts.length()) {
                    val a = accounts.getJSONObject(i)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            a.getString("name") + " •" + a.getString("lastFour"),
                            fontSize = 14.sp, color = c.muted
                        )
                        Text(show(a.getInt("balanceCents")), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniCard(L.t("Income"), show(summary.getInt("income")), Modifier.weight(1f))
            MiniCard(L.t("Spent"), show(summary.getInt("expenses")), Modifier.weight(1f))
        }

        Spacer(Modifier.height(18.dp))
        PrimaryButton(L.t("+ Add a transaction"), onClick = onAddTransaction)

        Spacer(Modifier.height(26.dp))
        Text(L.t("Budgets at a glance"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text)
        Spacer(Modifier.height(10.dp))
        if (budgets.length() == 0) {
            EmptyNote(L.t("No budgets yet — open the Budgets tab to add one."))
        }
        for (i in 0 until minOf(3, budgets.length())) {
            val b = budgets.getJSONObject(i)
            val spent = b.getInt("spentCents")
            val limit = b.getInt("limitCents")
            val over = spent > limit
            Card(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(L.t(b.getString("category")), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                        Text("${show(spent)} ${L.t("of")} ${show(limit)}", fontSize = 13.sp, color = c.muted)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (spent.toFloat() / limit).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (over) c.danger else c.accent
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(L.t("Recent activity"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.text)
        Spacer(Modifier.height(10.dp))
        for (i in 0 until minOf(3, transactions.length())) {
            TransactionRow(transactions.getJSONObject(i), hide)
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(6.dp))
        TextButton(onClick = {
            scope.launch { try { onRefresh(Api.get("/api/dashboard")) } catch (_: Exception) {} }
        }) { Text(L.t("Refresh everything"), color = c.accent) }

        Spacer(Modifier.height(8.dp))
        Text(
            L.t("Simulated demo data, kept in this phone's Room database and copied to Firestore. No real bank account is connected."),
            fontSize = 12.sp, color = c.muted
        )
        Spacer(Modifier.height(30.dp))
    }
}

// ---------------------------------------------------------------- budgets tab
@Composable
fun BudgetsTab(data: JSONObject, onRefresh: (JSONObject) -> Unit) {
    val c = LocalMmx.current
    val user = data.getJSONObject("user")
    val hide = user.getJSONObject("settings").optBoolean("hideBalances", false)
    val budgets = data.getJSONArray("budgets")
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }

    fun show(cents: Int) = if (hide) "R ••••" else rands(cents)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(L.t("Your budgets"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.text)
            TextButton(onClick = { adding = true }) { Text(L.t("+ Add"), color = c.accent) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            L.t("A budget tracks what you spend in one category this month. Spending counts when a transaction is added with that category."),
            fontSize = 13.sp, color = c.muted
        )
        Spacer(Modifier.height(18.dp))

        if (budgets.length() == 0) {
            EmptyNote(L.t("No budgets yet. Tap + Add to create your first one."))
            Spacer(Modifier.height(10.dp))
        }

        for (i in 0 until budgets.length()) {
            val b = budgets.getJSONObject(i)
            val spent = b.getInt("spentCents")
            val limit = b.getInt("limitCents")
            val over = spent > limit
            val percent = if (limit > 0) (spent * 100 / limit) else 0

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { editing = b },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(L.t(b.getString("category")), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                        Text(
                            "$percent%",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = if (over) c.danger else c.accent
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (spent.toFloat() / limit).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (over) c.danger else c.accent
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${show(spent)} ${L.t("spent")} ${L.t("of")} ${show(limit)}", fontSize = 12.sp, color = c.muted)
                        Text(
                            if (over) "${L.t("Over by")} ${show(spent - limit)}" else "${show(limit - spent)} ${L.t("left")}",
                            fontSize = 12.sp, color = if (over) c.danger else c.accent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(L.t("Tap to edit or delete"), fontSize = 12.sp, color = c.muted)
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }

    if (adding) {
        BudgetDialog(
            existing = null,
            onDismiss = { adding = false },
            onSaved = { adding = false; onRefresh(it) }
        )
    }
    editing?.let { budget ->
        BudgetDialog(
            existing = budget,
            onDismiss = { editing = null },
            onSaved = { editing = null; onRefresh(it) }
        )
    }
}

// ---------------------------------------------------------------- goals tab
@Composable
fun GoalsTab(data: JSONObject, onRefresh: (JSONObject) -> Unit) {
    val c = LocalMmx.current
    val user = data.getJSONObject("user")
    val hide = user.getJSONObject("settings").optBoolean("hideBalances", false)
    val goals = data.optJSONArray("goals") ?: JSONArray()
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<JSONObject?>(null) }

    fun show(cents: Int) = if (hide) "R ••••" else rands(cents)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(L.t("Savings goals"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.text)
            TextButton(onClick = { adding = true }) { Text(L.t("+ Add"), color = c.accent) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            L.t("Give your saving a target. Tap a goal to add money, rename it, or change the target."),
            fontSize = 13.sp, color = c.muted
        )
        Spacer(Modifier.height(18.dp))

        if (goals.length() == 0) {
            EmptyNote(L.t("No goals yet. Tap + Add and give your saving something to aim at."))
            Spacer(Modifier.height(10.dp))
        }

        for (i in 0 until goals.length()) {
            val g = goals.getJSONObject(i)
            val saved = g.getInt("savedCents")
            val target = g.getInt("targetCents")
            val percent = g.optInt("percent", 0)
            val reached = saved >= target

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { editing = g },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = c.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(g.getString("name"), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                        Text(
                            if (reached) L.t("Reached!") else "$percent%",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = c.accent
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (percent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = c.accent
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${show(saved)} ${L.t("of")} ${show(target)}", fontSize = 12.sp, color = c.muted)
                        Text(
                            if (reached) L.t("Goal met 🎉") else "${show(target - saved)} ${L.t("to go")}",
                            fontSize = 12.sp, color = c.accent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(L.t("Tap to add money or edit"), fontSize = 12.sp, color = c.muted)
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }

    if (adding) {
        GoalDialog(
            existing = null,
            onDismiss = { adding = false },
            onSaved = { adding = false; onRefresh(it) }
        )
    }
    editing?.let { goal ->
        GoalDialog(
            existing = goal,
            onDismiss = { editing = null },
            onSaved = { editing = null; onRefresh(it) }
        )
    }
}

// ---------------------------------------------------------------- activity tab
@Composable
fun ActivityTab(data: JSONObject, onRefresh: (JSONObject) -> Unit, onAddTransaction: () -> Unit) {
    val c = LocalMmx.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val user = data.getJSONObject("user")
    val hide = user.getJSONObject("settings").optBoolean("hideBalances", false)
    val transactions = data.getJSONArray("transactions")
    var asking by remember { mutableStateOf<JSONObject?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var showReceipt by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(L.t("Activity"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.text)
            TextButton(
                enabled = !refreshing,
                onClick = {
                    refreshing = true
                    scope.launch {
                        try { onRefresh(Api.get("/api/dashboard")) } catch (_: Exception) {}
                        refreshing = false
                    }
                }
            ) { Text(if (refreshing) "…" else L.t("Refresh"), color = c.accent) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${transactions.length()} ${L.t("transactions")} · ${L.t("tap one to delete it")}",
            fontSize = 13.sp, color = c.muted
        )
        Spacer(Modifier.height(18.dp))

        if (transactions.length() == 0) {
            EmptyNote(L.t("Nothing here yet. Add your first transaction."))
            Spacer(Modifier.height(10.dp))
        }

        for (i in 0 until transactions.length()) {
            val t = transactions.getJSONObject(i)
            Box(Modifier.clickable { asking = t }) {
                TransactionRow(t, hide, onReceipt = { showReceipt = it })
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(10.dp))
        PrimaryButton(L.t("+ Add a transaction"), onClick = onAddTransaction)
        Spacer(Modifier.height(30.dp))
    }

    // The receipt viewer: a simple full-width picture of the uploaded photo.
    showReceipt?.let { url ->
        AlertDialog(
            onDismissRequest = { showReceipt = null },
            title = { Text(L.t("Receipt"), fontWeight = FontWeight.Bold) },
            text = {
                AsyncImage(
                    model = Uri.parse(url),
                    contentDescription = L.t("Receipt"),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showReceipt = null }) {
                    Text(L.t("Close"), color = c.accent)
                }
            }
        )
    }

    asking?.let { t ->
        ConfirmDialog(
            title = L.t("Delete this transaction?"),
            message = t.optString("merchant") + " (" + rands(t.optInt("amountCents")) +
                    L.t("). Deleting it also adjusts your account balance, so the numbers stay truthful."),
            confirmText = L.t("Delete"),
            onCancel = { asking = null },
            onConfirm = {
                asking = null
                scope.launch {
                    try {
                        val result = Api.delete("/api/transactions/" + t.getString("id"))
                        onRefresh(result.getJSONObject("dashboard"))
                        Toast.makeText(context, L.t("Transaction deleted"), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: L.t("Could not delete"), Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
fun TransactionRow(t: JSONObject, hide: Boolean, onReceipt: ((String) -> Unit)? = null) {
    val c = LocalMmx.current
    val cents = t.optInt("amountCents")
    val shown = if (hide) "R ••••" else rands(kotlin.math.abs(cents))
    val hasReceipt = t.optString("receiptUrl").isNotEmpty()
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(t.optString("merchant"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Text(
                    L.t(t.optString("category")) + " · " + t.optString("date"),
                    fontSize = 13.sp, color = c.muted
                )
                if (hasReceipt && onReceipt != null) {
                    TextButton(
                        onClick = { onReceipt(t.optString("receiptUrl")) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "📷 " + L.t("View receipt"),
                            color = c.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Text(
                (if (cents > 0) "+ " else "− ") + shown,
                fontSize = 15.sp, fontWeight = FontWeight.Bold,
                color = if (cents > 0) c.accent else c.text
            )
        }
    }
}

// ---------------------------------------------------------------- settings tab
@Composable
fun SettingsTab(
    data: JSONObject,
    onRefresh: (JSONObject) -> Unit,
    onSignOut: () -> Unit,
    onToken: (String) -> Unit
) {
    val c = LocalMmx.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    var askReset by remember { mutableStateOf(false) }
    var askPassword by remember { mutableStateOf(false) }

    val user = data.getJSONObject("user")
    val settings = user.getJSONObject("settings")
    var hideBalances by remember(settings.toString()) {
        mutableStateOf(settings.optBoolean("hideBalances", false))
    }
    var budgetAlerts by remember(settings.toString()) {
        mutableStateOf(settings.optBoolean("budgetAlerts", true))
    }

    val isDark = settings.optString("theme", "light") == "dark"

    fun saveSetting(key: String, value: Boolean, after: () -> Unit) {
        busy = true
        scope.launch {
            try {
                Api.patch("/api/settings", JSONObject().put(key, value))
                onRefresh(Api.get("/api/dashboard"))
                Toast.makeText(context, L.t("Setting saved"), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: L.t("Could not save"), Toast.LENGTH_LONG).show()
            } finally {
                busy = false
                after()
            }
        }
    }

    fun saveTheme(wantDark: Boolean) {
        busy = true
        scope.launch {
            try {
                Api.patch("/api/settings", JSONObject().put("theme", if (wantDark) "dark" else "light"))
                onRefresh(Api.get("/api/dashboard"))
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: L.t("Could not save"), Toast.LENGTH_LONG).show()
            } finally {
                busy = false
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(32.dp))
        Text(L.t("Settings"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.text)
        Spacer(Modifier.height(18.dp))

        // --- profile ---
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("Your profile"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(10.dp))
                InfoRow(L.t("Name"), user.getString("name"))
                InfoRow(L.t("Email"), user.getString("email"))
                InfoRow(L.t("Currency"), settings.optString("currency", "ZAR") + L.t(" (locked to rand)"))
            }
        }

        // --- privacy ---
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("Privacy"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    title = L.t("Hide my balances"),
                    subtitle = L.t("Masks every amount with dots."),
                    checked = hideBalances,
                    enabled = !busy
                ) { wanted ->
                    hideBalances = wanted
                    saveSetting("hideBalances", wanted) { }
                }
                ToggleRow(
                    title = L.t("Budget alerts"),
                    subtitle = L.t("Warn me when a budget is nearly used up."),
                    checked = budgetAlerts,
                    enabled = !busy
                ) { wanted ->
                    budgetAlerts = wanted
                    saveSetting("budgetAlerts", wanted) { }
                }
            }
        }

        // --- appearance ---
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("Appearance"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    title = L.t("Dark mode"),
                    subtitle = L.t("Saved to your account, so it follows you to any device."),
                    checked = isDark,
                    enabled = !busy
                ) { wanted -> saveTheme(wanted) }
                Spacer(Modifier.height(10.dp))
                Text(L.t("Language"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectableChip("English", L.code == "en") { L.set(context, "en") }
                    SelectableChip("Zulu", L.code == "zu") { L.set(context, "zu") }
                    SelectableChip("Sesotho", L.code == "st") { L.set(context, "st") }
                }
            }
        }

        // --- push notifications ---
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(
                    L.t("Push notifications"),
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text
                )
                Spacer(Modifier.height(10.dp))
                PushNotificationsRow()
            }
        }

        // --- security ---
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("Security"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(6.dp))
                Text(
                    L.t("Changing your password signs out every other device and keeps you signed in on this one."),
                    fontSize = 13.sp, color = c.muted
                )
                Spacer(Modifier.height(12.dp))
                SecondaryButton(L.t("Change password"), enabled = !busy) { askPassword = true }
            }
        }

        // --- data ---
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("Demo data"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(6.dp))
                Text(
                    L.t("Everything in MoneyMate is simulated. Reset puts the sample accounts, transactions, budgets and goals back the way they started."),
                    fontSize = 13.sp, color = c.muted
                )
                Spacer(Modifier.height(12.dp))
                SecondaryButton(L.t("Reset demo data"), enabled = !busy) { askReset = true }
            }
        }

        // --- how this copy runs ---
        Spacer(Modifier.height(14.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = c.surface)
        ) {
            Column(Modifier.padding(18.dp)) {
                Text(L.t("How this copy runs"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Spacer(Modifier.height(10.dp))
                InfoRow(L.t("Sign-in"), "Firebase Authentication")
                InfoRow(L.t("Database"), L.t("Room · kept on this phone"))
                InfoRow(
                    L.t("Cloud copy"),
                    if (CLOUD_SYNC) L.t("Firestore · syncs when you are online")
                    else L.t("Off · this build stays on the phone")
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        SecondaryButton(L.t("Sign out"), onClick = onSignOut)
        Spacer(Modifier.height(18.dp))
        Text(
            L.t("MoneyMate is a learning prototype. Real bank linking needs a provider account (for example Stitch) before it can be switched on."),
            fontSize = 12.sp, color = c.muted
        )
        Spacer(Modifier.height(30.dp))
    }

    if (askPassword) {
        ChangePasswordDialog(
            onDismiss = { askPassword = false },
            onChanged = { newToken ->
                askPassword = false
                onToken(newToken)
                Toast.makeText(context, L.t("Password changed"), Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (askReset) {
        ConfirmDialog(
            title = L.t("Reset your demo data?"),
            message = L.t("Your balances, transactions, budgets and goals go back to the sample values. Your login and settings stay the same."),
            confirmText = L.t("Reset"),
            onCancel = { askReset = false },
            onConfirm = {
                askReset = false
                busy = true
                scope.launch {
                    try {
                        val result = Api.post("/api/demo/reset", JSONObject())
                        onRefresh(result.getJSONObject("dashboard"))
                        Toast.makeText(context, L.t("Demo data reset"), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: L.t("Could not reset"), Toast.LENGTH_LONG).show()
                    } finally {
                        busy = false
                    }
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    val c = LocalMmx.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = c.muted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit
) {
    val c = LocalMmx.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            Text(subtitle, fontSize = 13.sp, color = c.muted)
        }
        Switch(checked = checked, enabled = enabled, onCheckedChange = onChange)
    }
}

@Composable
fun PushNotificationsRow() {
    val c = LocalMmx.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionGranted by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var token by remember { mutableStateOf("") }

    // While this card is on screen, keep an eye out for the user switching
    // notifications on (in the phone's settings) and pick up the FCM token.
    LaunchedEffect(Unit) {
        while (true) {
            val nowGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (nowGranted != permissionGranted) permissionGranted = nowGranted
            if (permissionGranted && token.isEmpty()) token = MmPush.fcmToken()
            delay(1000)
        }
    }

    fun copyToken() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("fcm", token))
        Toast.makeText(context, L.t("Token copied"), Toast.LENGTH_SHORT).show()
    }

    fun openNotificationSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
        ).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)
    }

    if (permissionGranted) {
        InfoRow(L.t("Notifications"), L.t("Allowed"))
        Spacer(Modifier.height(6.dp))
        Text(L.t("FCM token"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        Spacer(Modifier.height(4.dp))
        if (token.isEmpty()) {
            Text(L.t("Getting token…"), fontSize = 13.sp, color = c.muted)
        } else {
            Text(token, fontSize = 11.sp, color = c.muted)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = ::copyToken) {
                Text(L.t("Copy token"), color = c.accent, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                L.t("Use this token in the Firebase console to send a push to this phone."),
                fontSize = 12.sp, color = c.muted
            )
        }
    } else {
        Text(
            L.t("Turn notifications on in your phone's settings to receive alerts."),
            fontSize = 13.sp, color = c.muted
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(L.t("Allow notifications")) {
            openNotificationSettings()
        }
    }
}

// ---------------------------------------------------------------- add transaction
@Composable
fun AddTransactionScreen(
    data: JSONObject?,
    onCancel: () -> Unit,
    onPickReceipt: () -> Unit,
    receiptUri: String?,
    onClearReceipt: () -> Unit,
    onSaved: (JSONObject) -> Unit
) {
    val c = LocalMmx.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val accounts = data?.optJSONArray("accounts") ?: JSONArray()
    val categories = (data?.optJSONArray("categories") ?: JSONArray())
        .let { arr -> (0 until arr.length()).map { arr.getString(it) } }

    var moneyIn by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Groceries") }
    var date by remember { mutableStateOf(today()) }
    var accountId by remember {
        mutableStateOf(if (accounts.length() > 0) accounts.getJSONObject(0).optString("id") else "")
    }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun save() {
        val value = parseRand(amount)
        val signed = if (moneyIn) value else value?.let { -it }
        error = when {
            merchant.trim().length < 2 -> L.t("Give this transaction a description, like Woolworths.")
            value == null -> L.t("Enter an amount above R0.00.")
            accountId.isEmpty() -> L.t("There is no account to put this in.")
            else -> ""
        }
        if (error.isNotEmpty()) return

        busy = true
        scope.launch {
            try {
                val payload = JSONObject()
                    .put("merchant", merchant.trim())
                    .put("category", category.trim().ifEmpty { "Other" })
                    .put("amount", signed ?: 0.0)
                    .put("accountId", accountId)
                    .put("date", date.trim())
                    .put("receiptUri", receiptUri ?: "")
                val result = Api.post("/api/transactions", payload)
                Toast.makeText(context, L.t("Transaction saved"), Toast.LENGTH_SHORT).show()
                onSaved(result.getJSONObject("dashboard"))
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not save. Is the server running?")
            } finally {
                busy = false
            }
        }
    }

    Column(
        Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onCancel, contentPadding = PaddingValues(0.dp)) {
            Text(L.t("← Back"), color = c.accent)
        }
        Spacer(Modifier.height(12.dp))
        Text(L.t("Add a transaction"), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = c.text)
        Spacer(Modifier.height(6.dp))
        Text(
            L.t("Money out lowers your balance. Money in raises it."),
            fontSize = 14.sp, color = c.muted
        )
        Spacer(Modifier.height(22.dp))

        // money out / in
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectableChip(L.t("Money out"), !moneyIn, Modifier.weight(1f)) { moneyIn = false }
            SelectableChip(L.t("Money in"), moneyIn, Modifier.weight(1f)) { moneyIn = true }
        }

        Spacer(Modifier.height(18.dp))
        LabelledField(
            L.t("Amount in rand"), amount, { amount = it },
            if (moneyIn) "e.g. 1200" else "e.g. 250.50", KeyboardType.Decimal
        )

        Spacer(Modifier.height(16.dp))
        LabelledField(L.t("Description"), merchant, { merchant = it }, L.t("e.g. Pick n Pay"))

        Spacer(Modifier.height(16.dp))
        Text(L.t("Category"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (name in categories) {
                SelectableChip(L.t(name), category.equals(name, ignoreCase = true)) {
                    category = name
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LabelledField(L.t("Or type your own"), category, { category = it }, L.t("e.g. Pet care"))

        Spacer(Modifier.height(16.dp))
        Text(L.t("Account"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until accounts.length()) {
                val a = accounts.getJSONObject(i)
                val id = a.optString("id")
                SelectableChip(
                    a.optString("name") + " •" + a.optString("lastFour"),
                    accountId == id
                ) { accountId = id }
            }
        }

        Spacer(Modifier.height(16.dp))
        LabelledField(L.t("Date"), date, { date = it }, "YYYY-MM-DD")

        Spacer(Modifier.height(16.dp))
        Text(L.t("Receipt"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        Spacer(Modifier.height(8.dp))
        if (receiptUri == null) {
            SecondaryButton(L.t("Attach a receipt photo")) { onPickReceipt() }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(L.t("Receipt attached"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.accent)
                TextButton(onClick = onClearReceipt) {
                    Text(L.t("Remove receipt photo"), color = c.danger)
                }
            }
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            ErrorNote(error)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(if (busy) L.t("Saving…") else L.t("Save transaction"), enabled = !busy, onClick = ::save)
        Spacer(Modifier.height(12.dp))
        Text(
            L.t("This writes to your MoneyMate server, updates your account balance, and counts towards any budget with the same category."),
            fontSize = 12.sp, color = c.muted
        )
        Spacer(Modifier.height(40.dp))
    }
}

// ---------------------------------------------------------------- budget sheet
@Composable
fun BudgetDialog(existing: JSONObject?, onDismiss: () -> Unit, onSaved: (JSONObject) -> Unit) {
    val c = LocalMmx.current
    val isEdit = existing != null
    val context = LocalContext.current
    var category by remember { mutableStateOf(existing?.optString("category").orEmpty()) }
    var limit by remember {
        mutableStateOf(existing?.let { plainRands(it.optInt("limitCents", 0)) }.orEmpty())
    }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun save() {
        val amount = parseRand(limit)
        error = when {
            category.trim().length < 2 -> L.t("Give this budget a name, like Groceries.")
            amount == null -> L.t("Enter a monthly limit, like 2500.")
            else -> ""
        }
        if (error.isNotEmpty()) return

        busy = true
        scope.launch {
            try {
                val payload = JSONObject()
                    .put("category", category.trim())
                    .put("limit", amount ?: 0.0)
                val result = if (isEdit) {
                    Api.patch("/api/budgets/" + existing!!.getString("id"), payload)
                } else {
                    Api.post("/api/budgets", payload)
                }
                Toast.makeText(context, L.t("Budget saved"), Toast.LENGTH_SHORT).show()
                onSaved(result.getJSONObject("dashboard"))
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not save. Is the server running?")
            } finally {
                busy = false
            }
        }
    }

    fun remove() {
        busy = true
        scope.launch {
            try {
                val result = Api.delete("/api/budgets/" + existing!!.getString("id"))
                Toast.makeText(context, L.t("Budget deleted"), Toast.LENGTH_SHORT).show()
                onSaved(result.getJSONObject("dashboard"))
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not delete. Is the server running?")
            } finally {
                busy = false
            }
        }
    }

    if (askDelete) {
        ConfirmDialog(
            title = L.t("Delete this budget?"),
            message = L.t("MoneyMate will stop tracking ") + L.t(existing?.optString("category").orEmpty()) +
                    L.t(". Your transactions stay exactly as they are."),
            confirmText = L.t("Delete"),
            onCancel = { askDelete = false },
            onConfirm = { askDelete = false; remove() }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (isEdit) L.t("Edit budget") else L.t("New budget"), fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    LabelledField(L.t("Category"), category, { category = it }, L.t("e.g. Groceries"))
                    Spacer(Modifier.height(14.dp))
                    LabelledField(L.t("Monthly limit"), limit, { limit = it }, L.t("e.g. 2500"), KeyboardType.Decimal)
                    Spacer(Modifier.height(8.dp))
                    Text(L.t("Rand, with or without cents."), fontSize = 12.sp, color = c.muted)
                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        ErrorNote(error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = ::save, enabled = !busy) {
                    Text(if (busy) L.t("Saving…") else L.t("Save"), color = c.accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Row {
                    if (isEdit) {
                        TextButton(onClick = { askDelete = true }, enabled = !busy) {
                            Text(L.t("Delete"), color = c.danger)
                        }
                    }
                    TextButton(onClick = onDismiss, enabled = !busy) { Text(L.t("Cancel"), color = c.muted) }
                }
            }
        )
    }
}

// ---------------------------------------------------------------- goal sheet
@Composable
fun GoalDialog(existing: JSONObject?, onDismiss: () -> Unit, onSaved: (JSONObject) -> Unit) {
    val c = LocalMmx.current
    val isEdit = existing != null
    val context = LocalContext.current
    var name by remember { mutableStateOf(existing?.optString("name").orEmpty()) }
    var target by remember {
        mutableStateOf(existing?.let { plainRands(it.optInt("targetCents", 0)) }.orEmpty())
    }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun save() {
        val targetValue = parseRand(target)
        val extra = amount.trim()
        val extraValue = if (extra.isEmpty()) null else parseRand(extra)

        error = when {
            name.trim().length < 2 -> L.t("Give your goal a name, like New laptop.")
            targetValue == null -> L.t("Enter a target amount, like 18000.")
            extra.isNotEmpty() && extraValue == null -> L.t("Enter an amount like 500, or leave it empty.")
            else -> ""
        }
        if (error.isNotEmpty()) return

        busy = true
        scope.launch {
            try {
                val payload = JSONObject()
                    .put("name", name.trim())
                    .put("target", targetValue ?: 0.0)
                if (isEdit) {
                    if (extraValue != null) payload.put("add", extraValue)
                } else if (extraValue != null) {
                    payload.put("saved", extraValue)
                }
                val result = if (isEdit) {
                    Api.patch("/api/goals/" + existing!!.getString("id"), payload)
                } else {
                    Api.post("/api/goals", payload)
                }
                Toast.makeText(context, L.t("Goal saved"), Toast.LENGTH_SHORT).show()
                onSaved(result.getJSONObject("dashboard"))
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not save. Is the server running?")
            } finally {
                busy = false
            }
        }
    }

    fun remove() {
        busy = true
        scope.launch {
            try {
                val result = Api.delete("/api/goals/" + existing!!.getString("id"))
                Toast.makeText(context, L.t("Goal deleted"), Toast.LENGTH_SHORT).show()
                onSaved(result.getJSONObject("dashboard"))
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not delete. Is the server running?")
            } finally {
                busy = false
            }
        }
    }

    if (askDelete) {
        ConfirmDialog(
            title = L.t("Delete this goal?"),
            message = existing?.optString("name").orEmpty() + " and its progress will be removed.",
            confirmText = L.t("Delete"),
            onCancel = { askDelete = false },
            onConfirm = { askDelete = false; remove() }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (isEdit) L.t("Update goal") else L.t("New savings goal"), fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    LabelledField(L.t("What are you saving for?"), name, { name = it }, L.t("e.g. New laptop"))
                    Spacer(Modifier.height(14.dp))
                    LabelledField(L.t("Target amount"), target, { target = it }, L.t("e.g. 18000"), KeyboardType.Decimal)
                    Spacer(Modifier.height(14.dp))
                    LabelledField(
                        if (isEdit) L.t("Add to savings (optional)") else L.t("Already saved (optional)"),
                        amount, { amount = it }, L.t("e.g. 500"), KeyboardType.Decimal
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isEdit) L.t("Leave empty to change only the name or target.")
                        else L.t("Leave empty to start from R 0.00."),
                        fontSize = 12.sp, color = c.muted
                    )
                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        ErrorNote(error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = ::save, enabled = !busy) {
                    Text(if (busy) L.t("Saving…") else L.t("Save"), color = c.accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Row {
                    if (isEdit) {
                        TextButton(onClick = { askDelete = true }, enabled = !busy) {
                            Text(L.t("Delete"), color = c.danger)
                        }
                    }
                    TextButton(onClick = onDismiss, enabled = !busy) { Text(L.t("Cancel"), color = c.muted) }
                }
            }
        )
    }
}

// ---------------------------------------------------------------- change password
@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onChanged: (String) -> Unit) {
    val c = LocalMmx.current
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        error = when {
            current.isEmpty() -> L.t("Enter your current password.")
            next.length < 10 -> L.t("Your new password needs at least 10 characters.")
            next == current -> L.t("Please choose a password you have not used here before.")
            next != confirm -> L.t("Your new passwords don't match.")
            else -> ""
        }
        if (error.isNotEmpty()) return

        busy = true
        scope.launch {
            try {
                val payload = JSONObject()
                    .put("currentPassword", current)
                    .put("newPassword", next)
                val result = Api.post("/api/auth/change-password", payload)
                onChanged(result.optString("token"))
            } catch (e: Exception) {
                error = e.message ?: L.t("Could not change your password.")
            } finally {
                busy = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L.t("Change password"), fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                LabelledField(L.t("Current password"), current, { current = it }, L.t("Your password now"), isPassword = true)
                Spacer(Modifier.height(14.dp))
                LabelledField(L.t("New password"), next, { next = it }, L.t("At least 10 characters"), isPassword = true)
                Spacer(Modifier.height(14.dp))
                LabelledField(L.t("Confirm new password"), confirm, { confirm = it }, L.t("One more time"), isPassword = true)
                Spacer(Modifier.height(8.dp))
                Text(
                    L.t("Your new password is hashed on the server, and every other device is signed out."),
                    fontSize = 12.sp, color = c.muted
                )
                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ErrorNote(error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = ::submit, enabled = !busy) {
                Text(if (busy) L.t("Saving…") else L.t("Save"), color = c.accent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text(L.t("Cancel"), color = c.muted) }
        }
    )
}

// ---------------------------------------------------------------- shared pieces
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val c = LocalMmx.current
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = c.danger, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(L.t("Cancel"), color = c.accent) }
        }
    )
}

@Composable
fun SelectableChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalMmx.current
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) c.primary else c.surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) c.onPrimary else c.text
        )
    }
}

@Composable
fun ErrorNote(message: String) {
    val c = LocalMmx.current
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = c.errorBackground)
    ) { Text(message, color = c.danger, fontSize = 13.sp, modifier = Modifier.padding(14.dp)) }
}

@Composable
fun EmptyNote(message: String) {
    val c = LocalMmx.current
    Card(
        Modifier.fillMaxWidth().padding(bottom = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface)
    ) { Text(message, color = c.muted, fontSize = 13.sp, modifier = Modifier.padding(16.dp)) }
}

@Composable
fun LabelledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    val c = LocalMmx.current
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = c.muted, fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation =
                if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
        )
    }
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = LocalMmx.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = c.primary)
    ) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun SecondaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = LocalMmx.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp)
    ) { Text(text, fontSize = 16.sp, color = c.accent) }
}

/** The MoneyMate mark. One shape, so it looks the same everywhere. */
@Composable
fun BrandMark(size: Dp = 64.dp, bordered: Boolean = false) {
    val shape = RoundedCornerShape(size / 3.5f)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(Forest)
            .then(if (bordered) Modifier.border(2.dp, Lime, shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "M",
            color = Lime,
            fontSize = (size.value * 0.5f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LogoMark() {
    BrandMark()
}

/** The opening screen shown while MoneyMate checks if you are still signed in. */
@Composable
fun SplashScreen() {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    val fade by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(700)
    )
    val lift by animateFloatAsState(
        targetValue = if (appeared) 0f else 20f,
        animationSpec = tween(700)
    )

    Box(
        Modifier.fillMaxSize().background(Forest),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = lift.dp).alpha(fade)
        ) {
            BrandMark(size = 104.dp, bordered = true)
            Spacer(Modifier.height(24.dp))
            Text("MoneyMate", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(L.t("A little clarity for your money."), color = Lime, fontSize = 15.sp)
            Spacer(Modifier.height(44.dp))
            CircularProgressIndicator(
                color = Lime,
                strokeWidth = 3.dp,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.height(18.dp))
            Text(L.t("Simulated demo banking · no real bank connected"), color = Lime, fontSize = 11.sp)
        }

        Text(
            "ZAR",
            color = Lime,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp)
        )
    }
}

@Composable
fun MiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    val c = LocalMmx.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = c.surface)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(label, color = c.muted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(value, color = c.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}