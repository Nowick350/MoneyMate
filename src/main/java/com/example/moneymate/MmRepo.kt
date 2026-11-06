package com.example.moneymate

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CLOUD_SYNC = true  -> Firebase Authentication signs people in, and every change is
 *                       copied up to Firestore whenever the phone has internet.
 * CLOUD_SYNC = false -> nothing ever leaves the phone. The Room database is the only
 *                       store (handy for a demo copy you hand to someone else).
 */
const val CLOUD_SYNC = true

/**
 * The Google OAuth client id for the "Sign in with Google" button.
 * Create it in: Google Cloud Console -> APIs & Services -> Credentials ->
 * Create OAuth client ID (Android) with package com.example.moneymate and
 * your debug SHA-1, then paste the id below (it ends in .apps.googleusercontent.com).
 */
const val GOOGLE_CLIENT_ID = "PASTE_YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com"

/**
 * The error type MmRepo throws. It carries the friendly sentence the screens show
 * in the red note. It lives here (not inside MainActivity) so this file stands on
 * its own: if anything in MainActivity is mid-edit, MmRepo still compiles.
 */
class MmError(message: String) : Exception(message)

/**
 * The engine behind Api. It does four jobs:
 *   1. sign in / sign up / change password through Firebase Authentication
 *   2. read and write the Room database that lives on the phone
 *   3. copy changes to Firestore (and pull changes back) when there is internet
 *   4. build the dashboard JSON that every screen already knows how to draw
 */
object MmRepo {

    private const val PRESET_CATEGORIES =
        "Groceries,Transport,Bills,Eating out,Shopping,Entertainment,Health,Education,Savings,Income,Other"

    private var database: MmDatabase? = null
    private var appContext: Context? = null

    private val auth get() = FirebaseAuth.getInstance()

    private val cloud get() = FirebaseFirestore.getInstance()

    fun init(context: Context) {
        database = MmDatabase.get(context)
        appContext = context.applicationContext
    }

    private fun dao(): MmDao = database?.mm() ?: error("MmRepo.init(context) was not called")

    fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {
        }
    }

    // ================================================================= helpers
    private fun mmNewId(): String = java.util.UUID.randomUUID().toString()

    /** Waits for a Firebase task that hands back a value. */
    private suspend fun <T> Task<T>.awaitMm(): T? = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            val problem = task.exception
            if (problem != null) cont.resumeWithException(problem) else cont.resume(task.result)
        }
    }

    /** Waits for a Firebase task that hands back nothing. */
    private suspend fun <T> Task<T>.awaitDone() = suspendCancellableCoroutine<Unit> { cont ->
        addOnCompleteListener { task ->
            val problem = task.exception
            if (problem != null) cont.resumeWithException(problem) else cont.resume(Unit)
        }
    }

    private fun friendly(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidUserException -> "That email or password is not right."
        is FirebaseAuthInvalidCredentialsException -> "That email or password is not right."
        is FirebaseAuthUserCollisionException -> "An account with that email already exists."
        is FirebaseAuthWeakPasswordException -> "That password is too weak. Use at least 10 characters."
        is FirebaseAuthException ->
            if (error.errorCode == "ERROR_EMAIL_EXISTS" ||
                error.errorCode == "account-exists-with-different-credential"
            ) {
                L.t("An account with that email already exists. Sign in with your password instead.")
            } else error.message ?: "Something went wrong talking to Firebase."
        else -> error.message ?: "Something went wrong talking to Firebase."
    }

    private fun userRoot(uid: String) = cloud.collection("users").document(uid)

    private fun tombstoneKey(uid: String, collection: String, docId: String) =
        uid + "|" + collection + "|" + docId

    // ================================================================= auth
    private suspend fun register(body: JSONObject): JSONObject {
        val name = body.optString("name").trim()
        val email = body.optString("email").trim().lowercase()
        val password = body.optString("password")

        if (name.length < 2) throw MmError(L.t("Please enter your name."))
        if (!email.contains("@") || !email.contains(".")) {
            throw MmError(L.t("Please enter a valid email address."))
        }
        if (password.length < 10) {
            throw MmError(L.t("Your password needs at least 10 characters."))
        }

        val result = try {
            auth.createUserWithEmailAndPassword(email, password).awaitMm()
        } catch (e: Exception) {
            throw MmError(friendly(e))
        }
        val user = result?.user ?: throw MmError(L.t("Could not create that account."))
        val uid = user.uid

        val shown = name.ifEmpty { email.substringBefore("@") }
        try {
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(shown).build())
                .awaitDone()
        } catch (_: Exception) {
        }

        dao().upsertProfile(ProfileEntity(uid, shown, email))
        ensureSettings(uid)

        // A brand-new account starts with the same sample workspace as before.
        seedDemo(uid)
        pushDirty(uid)

        return JSONObject().put("token", uid).put("user", publicUser(uid))
    }

    private suspend fun login(body: JSONObject): JSONObject {
        val email = body.optString("email").trim().lowercase()
        val password = body.optString("password")

        if (!email.contains("@")) throw MmError(L.t("Please enter a valid email address."))
        if (password.isEmpty()) throw MmError(L.t("Please enter your password."))

        val result = try {
            auth.signInWithEmailAndPassword(email, password).awaitMm()
        } catch (e: Exception) {
            throw MmError(friendly(e))
        }
        val user = result?.user ?: throw MmError(L.t("That email or password is not right."))
        val uid = user.uid

        val known = dao().profile(uid)
        if (known == null) {
            val shown = user.displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            dao().upsertProfile(ProfileEntity(uid, shown, email))
        }
        ensureSettings(uid)

        return JSONObject().put("token", uid).put("user", publicUser(uid))
    }

    private suspend fun changePassword(body: JSONObject): JSONObject {
        val user = auth.currentUser ?: throw MmError(L.t("Please sign in again."))
        val email = user.email ?: throw MmError(L.t("Please sign in again."))
        val current = body.optString("currentPassword")
        val next = body.optString("newPassword")

        if (next.length < 10) {
            throw MmError(L.t("Your new password needs at least 10 characters."))
        }
        if (next == current) {
            throw MmError(L.t("Please choose a password you have not used here before."))
        }

        try {
            user.reauthenticate(EmailAuthProvider.getCredential(email, current)).awaitDone()
        } catch (e: Exception) {
            throw MmError(L.t("That current password is not right."))
        }
        try {
            user.updatePassword(next).awaitDone()
        } catch (e: Exception) {
            throw MmError(friendly(e))
        }
        return JSONObject().put("ok", true).put("token", user.uid)
    }

    /**
     * "Sign in with Google": the id token arrives from the Credential Manager
     * dialog, Firebase verifies it, and the user lands in MoneyMate.
     */
    suspend fun googleSignIn(idToken: String): JSONObject {
        if (GOOGLE_CLIENT_ID.isEmpty() || GOOGLE_CLIENT_ID.startsWith("PASTE_")) {
            throw MmError(L.t("Google sign-in is not set up yet. Use email and password."))
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = try {
            auth.signInWithCredential(credential).awaitMm()
        } catch (e: Exception) {
            throw MmError(friendly(e))
        }
        val user = result?.user ?: throw MmError(L.t("That Google sign-in did not work. Please try again."))
        val uid = user.uid
        val email = user.email ?: ""
        val shown = user.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@").ifEmpty { "User" }

        try {
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(shown).build())
                .awaitDone()
        } catch (_: Exception) {
        }

        dao().upsertProfile(ProfileEntity(uid, shown, email))
        ensureSettings(uid)
        seedDemo(uid)
        pushDirty(uid)

        return JSONObject().put("token", uid).put("user", publicUser(uid))
    }

    private suspend fun ensureSettings(uid: String) {
        if (dao().settings(uid) == null) {
            dao().upsertSettings(SettingsEntity(uid, "ZAR", "light", false, true, true))
        }
    }

    // ================================================================= seed data
    /** The sample workspace a fresh account starts with. */
    private suspend fun seedDemo(uid: String) {
        if (dao().transactionCount(uid) > 0) return

        val everydayId = mmNewId()
        val pocketId = mmNewId()

        dao().upsertAccounts(
            listOf(
                AccountEntity(everydayId, uid, "Everyday account", "4092", 2_615_000, true),
                AccountEntity(pocketId, uid, "Pocket savings", "8016", 2_250_000, true)
            )
        )
        dao().upsertTransactions(
            listOf(
                TxEntity(mmNewId(), uid, "Monthly salary", "Income", 3_250_000, "2026-09-01", everydayId, true),
                TxEntity(mmNewId(), uid, "Woolworths", "Groceries", -84_230, "2026-09-04", everydayId, true),
                TxEntity(mmNewId(), uid, "Vodacom", "Bills", -39_900, "2026-09-06", everydayId, true),
                TxEntity(mmNewId(), uid, "Uber", "Transport", -11_600, "2026-09-09", everydayId, true),
                TxEntity(mmNewId(), uid, "Checkers", "Groceries", -51_875, "2026-09-12", everydayId, true)
            )
        )
        dao().upsertBudgets(
            listOf(
                BudgetEntity(mmNewId(), uid, "Groceries", 400_000, true),
                BudgetEntity(mmNewId(), uid, "Transport", 150_000, true),
                BudgetEntity(mmNewId(), uid, "Bills", 250_000, true)
            )
        )
        dao().upsertGoals(
            listOf(
                GoalEntity(mmNewId(), uid, "Emergency fund", 3_000_000, 850_000, true),
                GoalEntity(mmNewId(), uid, "December holiday", 1_200_000, 300_000, true)
            )
        )
    }

    // ================================================================= cloud sync
    /** Copies anything that changed on the phone up to Firestore. Never throws. */
    private suspend fun pushDirty(uid: String) {
        if (!CLOUD_SYNC) return
        try {
            val root = userRoot(uid)

            dao().dirtyAccounts(uid).forEach { row ->
                root.collection("accounts").document(row.id).set(
                    mapOf(
                        "name" to row.name,
                        "lastFour" to row.lastFour,
                        "balanceCents" to row.balanceCents
                    )
                ).awaitDone()
                dao().upsertAccounts(listOf(row.copy(dirty = false)))
            }

            dao().dirtyTransactions(uid).forEach { row ->
                root.collection("transactions").document(row.id).set(
                    mapOf(
                        "merchant" to row.merchant,
                        "category" to row.category,
                        "amountCents" to row.amountCents,
                        "date" to row.date,
                        "accountId" to row.accountId,
                        "receiptUrl" to row.receiptUrl
                    )
                ).awaitDone()
                dao().upsertTransactions(listOf(row.copy(dirty = false)))
            }

            dao().dirtyBudgets(uid).forEach { row ->
                root.collection("budgets").document(row.id).set(
                    mapOf("category" to row.category, "limitCents" to row.limitCents)
                ).awaitDone()
                dao().upsertBudgets(listOf(row.copy(dirty = false)))
            }

            dao().dirtyGoals(uid).forEach { row ->
                root.collection("goals").document(row.id).set(
                    mapOf(
                        "name" to row.name,
                        "targetCents" to row.targetCents,
                        "savedCents" to row.savedCents
                    )
                ).awaitDone()
                dao().upsertGoals(listOf(row.copy(dirty = false)))
            }

            dao().dirtySettings(uid).forEach { row ->
                root.collection("meta").document("settings").set(
                    mapOf(
                        "currency" to row.currency,
                        "theme" to row.theme,
                        "hideBalances" to row.hideBalances,
                        "budgetAlerts" to row.budgetAlerts
                    )
                ).awaitDone()
                dao().upsertSettings(row.copy(dirty = false))
            }

            val profile = dao().profile(uid)
            if (profile != null) {
                root.collection("meta").document("profile").set(
                    mapOf("name" to profile.name, "email" to profile.email)
                ).awaitDone()
            }

            // Finish off any deletions that happened while offline.
            dao().tombstones(uid).forEach { stone ->
                root.collection(stone.collection).document(stone.docId).delete().awaitDone()
                dao().removeTombstone(stone.key)
            }
        } catch (_: Exception) {
            // No internet: the changes stay marked as dirty and go up next time.
        }
    }

    /** Copies the cloud copy back down into Room. Never throws. */
    private suspend fun pull(uid: String) {
        if (!CLOUD_SYNC) return
        try {
            val root = userRoot(uid)
            val gone = dao().tombstones(uid).map { it.key }.toSet()

            val remoteAccounts = root.collection("accounts").get().awaitMm()
            if (remoteAccounts != null && !remoteAccounts.isEmpty) {
                val keepLocal = dao().dirtyAccounts(uid).map { it.id }.toSet()
                val rows = remoteAccounts.documents
                    .filter { it.id !in keepLocal }
                    .filter { tombstoneKey(uid, "accounts", it.id) !in gone }
                    .map { doc ->
                        AccountEntity(
                            doc.id,
                            uid,
                            doc.getString("name") ?: "Account",
                            doc.getString("lastFour") ?: "",
                            (doc.getLong("balanceCents") ?: 0L).toInt()
                        )
                    }
                if (rows.isNotEmpty()) dao().upsertAccounts(rows)
            }

            val remoteTransactions = root.collection("transactions").get().awaitMm()
            if (remoteTransactions != null && !remoteTransactions.isEmpty) {
                val keepLocal = dao().dirtyTransactions(uid).map { it.id }.toSet()
                val rows = remoteTransactions.documents
                    .filter { it.id !in keepLocal }
                    .filter { tombstoneKey(uid, "transactions", it.id) !in gone }
                    .map { doc ->
                        TxEntity(
                            doc.id,
                            uid,
                            doc.getString("merchant") ?: "Transaction",
                            doc.getString("category") ?: "Other",
                            (doc.getLong("amountCents") ?: 0L).toInt(),
                            doc.getString("date") ?: today(),
                            doc.getString("accountId") ?: "",
                            receiptUrl = doc.getString("receiptUrl") ?: ""
                        )
                    }
                if (rows.isNotEmpty()) dao().upsertTransactions(rows)
            }

            val remoteBudgets = root.collection("budgets").get().awaitMm()
            if (remoteBudgets != null && !remoteBudgets.isEmpty) {
                val keepLocal = dao().dirtyBudgets(uid).map { it.id }.toSet()
                val rows = remoteBudgets.documents
                    .filter { it.id !in keepLocal }
                    .filter { tombstoneKey(uid, "budgets", it.id) !in gone }
                    .map { doc ->
                        BudgetEntity(
                            doc.id,
                            uid,
                            doc.getString("category") ?: "Other",
                            (doc.getLong("limitCents") ?: 0L).toInt()
                        )
                    }
                if (rows.isNotEmpty()) dao().upsertBudgets(rows)
            }

            val remoteGoals = root.collection("goals").get().awaitMm()
            if (remoteGoals != null && !remoteGoals.isEmpty) {
                val keepLocal = dao().dirtyGoals(uid).map { it.id }.toSet()
                val rows = remoteGoals.documents
                    .filter { it.id !in keepLocal }
                    .filter { tombstoneKey(uid, "goals", it.id) !in gone }
                    .map { doc ->
                        GoalEntity(
                            doc.id,
                            uid,
                            doc.getString("name") ?: "Goal",
                            (doc.getLong("targetCents") ?: 0L).toInt(),
                            (doc.getLong("savedCents") ?: 0L).toInt()
                        )
                    }
                if (rows.isNotEmpty()) dao().upsertGoals(rows)
            }

            val remoteSettings = root.collection("meta").document("settings").get().awaitMm()
            if (remoteSettings != null && remoteSettings.exists()) {
                val local = dao().settings(uid)
                if (local == null || !local.dirty) {
                    dao().upsertSettings(
                        SettingsEntity(
                            uid,
                            remoteSettings.getString("currency") ?: "ZAR",
                            remoteSettings.getString("theme") ?: "light",
                            remoteSettings.getBoolean("hideBalances") ?: false,
                            remoteSettings.getBoolean("budgetAlerts") ?: true
                        )
                    )
                }
            }

            val remoteProfile = root.collection("meta").document("profile").get().awaitMm()
            if (remoteProfile != null && remoteProfile.exists()) {
                val name = remoteProfile.getString("name")
                val email = remoteProfile.getString("email")
                if (!name.isNullOrEmpty() && !email.isNullOrEmpty()) {
                    dao().upsertProfile(ProfileEntity(uid, name, email))
                }
            }
        } catch (_: Exception) {
            // Offline: the phone's own copy is already up to date, so just carry on.
        }
    }

    /** Forgets a doc in the cloud as well as on the phone. Never throws. */
    private suspend fun deleteRemote(uid: String, collection: String, docId: String) {
        val key = tombstoneKey(uid, collection, docId)
        if (!CLOUD_SYNC) return
        try {
            userRoot(uid).collection(collection).document(docId).delete().awaitDone()
        } catch (_: Exception) {
            dao().addTombstone(DeletedEntity(key, uid, collection, docId))
        }
    }

    /** Wipes a whole collection in the cloud (used by Reset demo data). */
    private suspend fun wipeRemote(uid: String, collection: String) {
        if (!CLOUD_SYNC) return
        try {
            val docs = userRoot(uid).collection(collection).get().awaitMm() ?: return
            docs.documents.forEach { it.reference.delete().awaitDone() }
        } catch (_: Exception) {
        }
    }

    // ================================================================= receipts
    /**
     * Uploads a receipt photo to Firebase Storage under the user's own folder
     * and returns its public download link. Returns "" if the upload fails -
     * the transaction is still saved, just without a receipt.
     */
    private suspend fun uploadReceipt(uid: String, txId: String, uriString: String): String {
        try {
            val storage = FirebaseStorage.getInstance()
            val ref = storage.reference.child("users/$uid/receipts/$txId.jpg")
            ref.putFile(android.net.Uri.parse(uriString)).awaitDone()
            return ref.downloadUrl.awaitMm()?.toString() ?: ""
        } catch (_: Exception) {
            return ""
        }
    }

    // ================================================================= budget alerts
    /** Total spent in one category so far this month (money out only). */
    private suspend fun spentFor(uid: String, category: String): Int {
        var spent = 0
        dao().transactions(uid).forEach { row ->
            if (row.category == category && row.amountCents < 0) spent -= row.amountCents
        }
        return spent
    }

    /**
     * Posts a notification if this transaction just pushed a budget past 80%
     * of its limit, or over the limit entirely. Only money-out transactions
     * count, and only while "Budget alerts" is switched on in Settings.
     */
    private suspend fun budgetAlert(uid: String, tx: TxEntity) {
        val ctx = appContext ?: return
        val settings = dao().settings(uid) ?: return
        if (!settings.budgetAlerts || tx.amountCents >= 0) return
        val budget = dao().budgetFor(uid, tx.category) ?: return
        val limit = budget.limitCents
        if (limit <= 0) return
        val after = spentFor(uid, tx.category)
        val before = after + tx.amountCents
        if (before < limit * 0.8 && after >= limit * 0.8) {
            val percent = (after * 100 / limit)
            MmPush.notifyNow(
                ctx,
                L.t("Budget almost used up"),
                L.budgetAlmostUsed(tx.category, percent, after, limit)
            )
        } else if (before < limit && after >= limit) {
            MmPush.notifyNow(
                ctx,
                L.t("Budget over"),
                L.budgetOver(tx.category, after - limit)
            )
        }
    }

    // ================================================================= reading
    private suspend fun publicUser(uid: String): JSONObject {
        val profile = dao().profile(uid)
        val settings = dao().settings(uid)
        return JSONObject()
            .put("id", uid)
            .put("name", profile?.name ?: "MoneyMate user")
            .put("email", profile?.email ?: (auth.currentUser?.email ?: ""))
            .put("settings", settingsJson(settings))
    }

    private fun settingsJson(settings: SettingsEntity?): JSONObject = JSONObject()
        .put("currency", settings?.currency ?: "ZAR")
        .put("theme", settings?.theme ?: "light")
        .put("hideBalances", settings?.hideBalances ?: false)
        .put("budgetAlerts", settings?.budgetAlerts ?: true)

    private fun accountJson(row: AccountEntity): JSONObject = JSONObject()
        .put("id", row.id)
        .put("name", row.name)
        .put("lastFour", row.lastFour)
        .put("balanceCents", row.balanceCents)

    private fun txJson(row: TxEntity): JSONObject = JSONObject()
        .put("id", row.id)
        .put("merchant", row.merchant)
        .put("category", row.category)
        .put("amountCents", row.amountCents)
        .put("date", row.date)
        .put("accountId", row.accountId)
        .put("receiptUrl", row.receiptUrl)

    private fun budgetJson(row: BudgetEntity, spentCents: Int): JSONObject = JSONObject()
        .put("id", row.id)
        .put("category", row.category)
        .put("limitCents", row.limitCents)
        .put("spentCents", spentCents)

    private fun goalJson(row: GoalEntity): JSONObject {
        val percent = if (row.targetCents > 0) {
            minOf(100, Math.round(row.savedCents.toFloat() / row.targetCents * 100))
        } else 0
        return JSONObject()
            .put("id", row.id)
            .put("name", row.name)
            .put("savedCents", row.savedCents)
            .put("targetCents", row.targetCents)
            .put("percent", percent)
    }

    private suspend fun dashboard(uid: String): JSONObject {
        val accounts = dao().accounts(uid)
        val transactions = dao().transactions(uid)
        val budgets = dao().budgets(uid)
        val goals = dao().goals(uid)

        var income = 0
        var expenses = 0
        var totalBalance = 0

        accounts.forEach { totalBalance += it.balanceCents }
        transactions.forEach { if (it.amountCents > 0) income += it.amountCents else expenses -= it.amountCents }

        val transactionArray = JSONArray()
        transactions.forEach { transactionArray.put(txJson(it)) }

        val budgetArray = JSONArray()
        budgets.forEach { budget ->
            var spent = 0
            transactions.forEach { row ->
                if (row.category == budget.category && row.amountCents < 0) spent -= row.amountCents
            }
            budgetArray.put(budgetJson(budget, spent))
        }

        val goalArray = JSONArray()
        goals.forEach { goalArray.put(goalJson(it)) }

        val categories = sortedSetOf<String>()
        PRESET_CATEGORIES.split(",").forEach { categories.add(it) }
        budgets.forEach { if (it.category.isNotEmpty()) categories.add(it.category) }
        transactions.forEach { if (it.category.isNotEmpty()) categories.add(it.category) }
        val categoryArray = JSONArray()
        categories.forEach { categoryArray.put(it) }

        val accountArray = JSONArray()
        accounts.forEach { accountArray.put(accountJson(it)) }

        return JSONObject()
            .put("user", publicUser(uid))
            .put("accounts", accountArray)
            .put("transactions", transactionArray)
            .put("budgets", budgetArray)
            .put("goals", goalArray)
            .put("categories", categoryArray)
            .put(
                "summary",
                JSONObject()
                    .put("totalBalance", totalBalance)
                    .put("income", income)
                    .put("expenses", expenses)
                    .put("currency", "ZAR")
            )
    }

    // ================================================================= writing
    private suspend fun patchSettings(uid: String, body: JSONObject): JSONObject {
        val current = dao().settings(uid) ?: SettingsEntity(uid, "ZAR", "light", false, true)
        val changed = current.copy(
            theme = if (body.has("theme")) body.optString("theme", current.theme) else current.theme,
            hideBalances = if (body.has("hideBalances")) {
                body.optBoolean("hideBalances", current.hideBalances)
            } else current.hideBalances,
            budgetAlerts = if (body.has("budgetAlerts")) {
                body.optBoolean("budgetAlerts", current.budgetAlerts)
            } else current.budgetAlerts,
            currency = "ZAR",
            dirty = true
        )
        dao().upsertSettings(changed)
        pushDirty(uid)
        return JSONObject().put("settings", settingsJson(changed))
    }

    private suspend fun addTransaction(uid: String, body: JSONObject): JSONObject {
        val merchant = body.optString("merchant").trim()
        val category = body.optString("category").trim().ifEmpty { "Other" }
        val amountCents = Math.round(body.optDouble("amount", 0.0) * 100).toInt()

        if (merchant.length < 2) throw MmError(L.t("Give this transaction a description."))
        if (amountCents == 0) throw MmError(L.t("Enter an amount above R0.00."))

        val accounts = dao().accounts(uid)
        if (accounts.isEmpty()) throw MmError(L.t("There is no account to put this in."))
        val account = accounts.firstOrNull { it.id == body.optString("accountId") } ?: accounts.first()

        val given = body.optString("date")
        val date = if (Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(given)) given else today()

        // A receipt photo (if one was picked) is uploaded to Firebase Storage first,
        // and named after the transaction it belongs to.
        val rowId = mmNewId()
        val receiptUri = body.optString("receiptUri", "")
        val receiptUrl = if (receiptUri.isNotEmpty()) uploadReceipt(uid, rowId, receiptUri) else ""

        val row = TxEntity(rowId, uid, merchant, category, amountCents, date, account.id, true, receiptUrl)
        dao().upsertTransactions(listOf(row))
        dao().upsertAccounts(
            listOf(account.copy(balanceCents = account.balanceCents + amountCents, dirty = true))
        )
        pushDirty(uid)
        budgetAlert(uid, row)
        return JSONObject().put("transaction", txJson(row)).put("dashboard", dashboard(uid))
    }

    private suspend fun patchTransaction(uid: String, id: String, body: JSONObject): JSONObject {
        val row = dao().transactions(uid).firstOrNull { it.id == id }
            ?: throw MmError(L.t("That transaction no longer exists."))
        var amountCents = row.amountCents

        if (body.has("amount")) {
            amountCents = Math.round(body.optDouble("amount", 0.0) * 100).toInt()
            if (amountCents == 0) throw MmError(L.t("Enter an amount above R0.00."))
        }

        val merchant = if (body.has("merchant")) body.optString("merchant").trim() else row.merchant
        if (merchant.length < 2) throw MmError(L.t("Give this transaction a description."))

        val date = if (body.has("date")) {
            body.optString("date").takeIf { Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(it) } ?: row.date
        } else row.date

        val category = if (body.has("category")) {
            body.optString("category").trim().ifEmpty { "Other" }
        } else row.category

        val updated = row.copy(
            merchant = merchant,
            category = category,
            amountCents = amountCents,
            date = date,
            dirty = true
        )
        dao().upsertTransactions(listOf(updated))

        if (amountCents != row.amountCents) {
            dao().accounts(uid).firstOrNull { it.id == row.accountId }?.let { account ->
                dao().upsertAccounts(
                    listOf(
                        account.copy(
                            balanceCents = account.balanceCents + (amountCents - row.amountCents),
                            dirty = true
                        )
                    )
                )
            }
        }

        pushDirty(uid)
        return JSONObject().put("transaction", txJson(updated)).put("dashboard", dashboard(uid))
    }

    private suspend fun deleteTransaction(uid: String, id: String): JSONObject {
        val row = dao().transactions(uid).firstOrNull { it.id == id }
            ?: throw MmError(L.t("That transaction no longer exists."))
        dao().deleteTransaction(id)
        dao().accounts(uid).firstOrNull { it.id == row.accountId }?.let { account ->
            dao().upsertAccounts(
                listOf(account.copy(balanceCents = account.balanceCents - row.amountCents, dirty = true))
            )
        }
        deleteRemote(uid, "transactions", id)
        pushDirty(uid)
        return JSONObject().put("ok", true).put("dashboard", dashboard(uid))
    }

    private suspend fun addBudget(uid: String, body: JSONObject): JSONObject {
        val category = body.optString("category").trim()
        val limitCents = Math.round(body.optDouble("limit", 0.0) * 100).toInt()

        if (category.length < 2) throw MmError(L.t("Please name the category."))
        if (limitCents <= 0) throw MmError(L.t("Enter a monthly limit above R0.00."))
        if (dao().budgetFor(uid, category) != null) {
            throw MmError(L.budgetExists(category))
        }

        val row = BudgetEntity(mmNewId(), uid, category, limitCents, true)
        dao().upsertBudgets(listOf(row))
        pushDirty(uid)
        return JSONObject().put("budget", budgetJson(row, 0)).put("dashboard", dashboard(uid))
    }

    private suspend fun patchBudget(uid: String, id: String, body: JSONObject): JSONObject {
        val row = dao().budgets(uid).firstOrNull { it.id == id }
            ?: throw MmError(L.t("That budget no longer exists."))

        val category = if (body.has("category")) {
            body.optString("category").trim().also {
                if (it.length < 2) throw MmError(L.t("Please name the category."))
            }
        } else row.category

        val limitCents = if (body.has("limit")) {
            Math.round(body.optDouble("limit", 0.0) * 100).toInt().also {
                if (it <= 0) throw MmError(L.t("Enter a monthly limit above R0.00."))
            }
        } else row.limitCents

        val updated = row.copy(category = category, limitCents = limitCents, dirty = true)
        dao().upsertBudgets(listOf(updated))
        pushDirty(uid)
        return JSONObject().put("budget", budgetJson(updated, 0)).put("dashboard", dashboard(uid))
    }

    private suspend fun deleteBudget(uid: String, id: String): JSONObject {
        dao().budgets(uid).firstOrNull { it.id == id }
            ?: throw MmError(L.t("That budget no longer exists."))
        dao().deleteBudget(id)
        deleteRemote(uid, "budgets", id)
        pushDirty(uid)
        return JSONObject().put("ok", true).put("dashboard", dashboard(uid))
    }

    private suspend fun addGoal(uid: String, body: JSONObject): JSONObject {
        val name = body.optString("name").trim()
        val targetCents = Math.round(body.optDouble("target", 0.0) * 100).toInt()
        val savedCents = if (body.has("saved")) {
            maxOf(0, Math.round(body.optDouble("saved", 0.0) * 100).toInt())
        } else 0

        if (name.length < 2) throw MmError(L.t("Please name your goal."))
        if (targetCents <= 0) throw MmError(L.t("Enter a target amount above R0.00."))

        val row = GoalEntity(mmNewId(), uid, name, targetCents, savedCents, true)
        dao().upsertGoals(listOf(row))
        pushDirty(uid)
        return JSONObject().put("goal", goalJson(row)).put("dashboard", dashboard(uid))
    }

    private suspend fun patchGoal(uid: String, id: String, body: JSONObject): JSONObject {
        val row = dao().goals(uid).firstOrNull { it.id == id }
            ?: throw MmError(L.t("That goal no longer exists."))

        val name = if (body.has("name")) {
            body.optString("name").trim().also {
                if (it.length < 2) throw MmError(L.t("Please name your goal."))
            }
        } else row.name

        val targetCents = if (body.has("target")) {
            Math.round(body.optDouble("target", 0.0) * 100).toInt().also {
                if (it <= 0) throw MmError(L.t("Enter a target amount above R0.00."))
            }
        } else row.targetCents

        val savedCents = when {
            body.has("add") -> maxOf(
                0,
                row.savedCents + Math.round(body.optDouble("add", 0.0) * 100).toInt()
            )
            body.has("saved") -> maxOf(0, Math.round(body.optDouble("saved", 0.0) * 100).toInt())
            else -> row.savedCents
        }

        val updated = row.copy(
            name = name,
            targetCents = targetCents,
            savedCents = savedCents,
            dirty = true
        )
        dao().upsertGoals(listOf(updated))
        pushDirty(uid)
        return JSONObject().put("goal", goalJson(updated)).put("dashboard", dashboard(uid))
    }

    private suspend fun deleteGoal(uid: String, id: String): JSONObject {
        dao().goals(uid).firstOrNull { it.id == id }
            ?: throw MmError(L.t("That goal no longer exists."))
        dao().deleteGoal(id)
        deleteRemote(uid, "goals", id)
        pushDirty(uid)
        return JSONObject().put("ok", true).put("dashboard", dashboard(uid))
    }

    private suspend fun resetDemo(uid: String): JSONObject {
        listOf("accounts", "transactions", "budgets", "goals").forEach { wipeRemote(uid, it) }
        dao().wipeAccounts(uid)
        dao().wipeTransactions(uid)
        dao().wipeBudgets(uid)
        dao().wipeGoals(uid)
        dao().wipeTombstones(uid)
        seedDemo(uid)
        pushDirty(uid)
        return JSONObject().put("ok", true).put("dashboard", dashboard(uid))
    }

    // ================================================================= routing
    /**
     * Answers one request. The paths are exactly the same as the old server's, so
     * no screen in MainActivity had to change.
     */
    suspend fun dispatch(method: String, path: String, body: JSONObject, token: String?): JSONObject {
        if (method == "POST" && path == "/api/auth/register") return register(body)
        if (method == "POST" && path == "/api/auth/login") return login(body)
        if (method == "POST" && path == "/api/auth/logout") {
            signOut()
            return JSONObject().put("ok", true)
        }

        val uid = auth.currentUser?.uid ?: token
        if (uid.isNullOrEmpty()) throw MmError(L.t("Please sign in again."))

        if (method == "GET" && path == "/api/dashboard") {
            // First: anything waiting to go up. Then: everything new from the cloud.
            // Then: a first-time account gets the sample workspace.
            ensureSettings(uid)
            pushDirty(uid)
            pull(uid)
            seedDemo(uid)
        }

        return when {
            method == "GET" && path == "/api/dashboard" -> dashboard(uid)
            method == "GET" && path == "/api/auth/me" -> JSONObject().put("user", publicUser(uid))
            method == "PATCH" && path == "/api/settings" -> patchSettings(uid, body)
            method == "POST" && path == "/api/auth/change-password" -> changePassword(body)
            method == "POST" && path == "/api/demo/reset" -> resetDemo(uid)

            method == "POST" && path == "/api/transactions" -> addTransaction(uid, body)
            method == "PATCH" && path.startsWith("/api/transactions/") ->
                patchTransaction(uid, path.substringAfterLast('/'), body)
            method == "DELETE" && path.startsWith("/api/transactions/") ->
                deleteTransaction(uid, path.substringAfterLast('/'))

            method == "POST" && path == "/api/budgets" -> addBudget(uid, body)
            method == "PATCH" && path.startsWith("/api/budgets/") ->
                patchBudget(uid, path.substringAfterLast('/'), body)
            method == "DELETE" && path.startsWith("/api/budgets/") ->
                deleteBudget(uid, path.substringAfterLast('/'))

            method == "POST" && path == "/api/goals" -> addGoal(uid, body)
            method == "PATCH" && path.startsWith("/api/goals/") ->
                patchGoal(uid, path.substringAfterLast('/'), body)
            method == "DELETE" && path.startsWith("/api/goals/") ->
                deleteGoal(uid, path.substringAfterLast('/'))

            else -> throw MmError(L.t("That action is not available."))
        }
    }
}