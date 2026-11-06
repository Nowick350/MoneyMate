package com.example.moneymate

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * MoneyMate multi-language support: English (main, default) + Zulu + Sesotho.
 *
 * How it works
 * - Every user-facing string in the UI goes through L.t("English text").
 * - When Zulu or Sesotho is selected, L.t() returns the translation for that
 *   language. Anything without a translation falls back to English, so the
 *   app can never show a missing/blank string.
 * - The choice is saved on this phone (SharedPreferences) and applies the
 *   moment you tap it - no restart. `code` is a Compose state, so changing
 *   it re-renders every screen immediately.
 *
 * Data is NOT translated: category names are stored in English (so budgets
 * keep matching transactions) and only the text on screen is translated.
 */
object L {
    private const val PREFS = "moneymate_lang"
    private const val KEY = "code"

    /** "en" = English (main), "zu" = Zulu, "st" = Sesotho. */
    var code: String by mutableStateOf("en")

    fun init(context: Context) {
        code = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "en") ?: "en"
    }

    fun set(context: Context, newCode: String) {
        this.code = newCode
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, newCode).apply()
    }

    /** Translate an English UI string. Falls back to the English original. */
    fun t(en: String): String = when (code) {
        "zu" -> ZU[en] ?: en
        "st" -> ST[en] ?: en
        else -> en
    }

    /** One message with a dynamic part (the category name). */
    fun budgetExists(category: String): String = when (code) {
        "zu" -> "Usenayo ibhajethi ye-" + category + "."
        "st" -> "U se u na le tekanyo bakeng sa " + category + "."
        else -> "You already have a budget for " + category + "."
    }

    /** Notification: a budget has passed 80% of its monthly limit. */
    fun budgetAlmostUsed(category: String, percent: Int, spentCents: Int, limitCents: Int): String = when (code) {
        "zu" -> "Ibhajethi ye-" + category + " isebenziswe u-" + percent + "% (" +
                rands(spentCents) + " kwe-" + rands(limitCents) + ")."
        "st" -> "Tekanyo ya " + category + " e sebedisitswe ka " + percent + "% (" +
                rands(spentCents) + " ea " + rands(limitCents) + ")."
        else -> "Your " + category + " budget is " + percent + "% used (" +
                rands(spentCents) + " of " + rands(limitCents) + ")."
    }

    /** Notification: a budget has gone over its monthly limit. */
    fun budgetOver(category: String, overCents: Int): String = when (code) {
        "zu" -> "Ibhajethi ye-" + category + " igcwele nge-" + rands(overCents) + "."
        "st" -> "Tekanyo ya " + category + " e fetile ka " + rands(overCents) + "."
        else -> "Your " + category + " budget is over by " + rands(overCents) + "."
    }

    // ------------------------------------------------------------- Zulu
    private val ZU = mapOf(
        // tabs and app chrome
        "Home" to "Ikhaya",
        "Budgets" to "Izibhajethi",
        "Goals" to "Imigomo",
        "Activity" to "Imisebenzi",
        "Settings" to "Izilungiselelo",
        "← Back" to "← Emva",
        "TOTAL BALANCE" to "IBHALANSI EPHELELE",
        "A little clarity for your money." to "Ukuqondana okuncane nge mali yakho.",
        "Simulated demo banking · no real bank connected" to
                "Ubanki lwedemo · ayikho ibhanki yangempela ehlanganisiwe",
        "Demo prototype. Balances and transactions are simulated, not real money." to
                "I-prototype yedemo. Izibhalansi nezinkokelo zibonisiwe kuphela, hhayi imali yangempela.",
        "MoneyMate is a learning prototype. Real bank linking needs a provider account (for example Stitch) before it can be switched on." to
                "I-MoneyMate i-prototype yokufunda. Ukuhlanganisa ibhanki yangempela kudinga i-akhawunt yomshini (isibonelo i-Stitch) ngaphambi kokuthi ivulwe.",
        // welcome / sign in
        "Sign in to MoneyMate" to "Ngena ku-MoneyMate",
        "Sign in and pick up where you left off." to "Ngena bese uqhubeka lapho uvale khona.",
        "Create my account" to "Dala i-akhawunt yami",
        "I already have an account" to "Senginayo i-akhawunt",
        "New around here? Create an account" to "Uyisha lapha? Dala i-akhawunt",
        "Already a mate? Sign in" to "Usuwumngane? Ngena",
        "Welcome back," to "Uyamukelwa futhi,",
        "Full name" to "Igama eliphelele",
        "Email address" to "Ikheli leimeyili",
        "Password" to "Iphasiwedi",
        "Confirm password" to "Qinisekisa iphasiwedi",
        "One more time" to "Kanye futhi",
        "Please enter your name." to "Faka igama lakho.",
        "Sign out" to "Phuma",
        // home
        "Your accounts" to "Izakhawunt zakho",
        "Recent activity" to "Imisebenzi yaqeda",
        "Budgets at a glance" to "Izibhajethi ngokushesha",
        "Savings goals" to "Imigomo yokusonga",
        "Refresh everything" to "Vuselela konke",
        "+ Add" to "+ Engeza",
        "+ Add a transaction" to "+ Engeza inkokelo",
        "Add a transaction" to "Engeza inkokelo",
        "demo accounts" to "izakhawunt zedemo",
        "transactions" to "izinkokelo",
        "tap one to delete it" to "chofoza eyodwa uze uyisuse",
        "of" to "ye",
        "spent" to "esebenziswe",
        "left" to "esasele",
        "Over by" to "Gcwele nge",
        // exchange rates (Frankfurter REST API)
        "Live exchange rates" to "Izinga lokushintshana kwemali namuhla",
        "1 South African rand" to "IRand eyodwa yaseNingizimu Afrika",
        "Checking today's rates…" to "Kuhlola izinga namuhla…",
        "Rates are not available right now. Check your internet and tap this card to try again." to
                "Izinga alitholakali ngomzuzu. Hlola i-intanethi yakho bese uchofoza le ikhadi uze uzame futhi.",
        "Updated" to "Kuhleliswe",
        "Tap to refresh" to "Chofoza uze uvuselele",
        // add / edit transaction
        "Money out" to "Imali etholayo",
        "Money in" to "Imali efikayo",
        "Amount in rand" to "Inani nge-Rand",
        "Rand, with or without cents." to "I-Rand (amacenti awadingekile).",
        "Description" to "Incazelo",
        "Category" to "Isigaba",
        "Account" to "I-akhawunt",
        "Date" to "Usuku",
        "Save transaction" to "Gcina inkokelo",
        "Saving…" to "Kuyagcinwa…",
        "Cancel" to "Khansela",
        "Save" to "Gcina",
        "Delete" to "Susa",
        "There is no account to put this in." to "Ayikho i-akhawunt lapho ungabeka lokhu.",
        "Money out lowers your balance. Money in raises it." to
                "Imali etholayo yehlisa ibhalansi yakho. Imali efikayo iyayiphakamisa.",
        "This writes to your MoneyMate server, updates your account balance, and counts towards any budget with the same category." to
                "Lokhu kubhala eseva sakho se-MoneyMate, kuhlaziya ibhalansi ye-akhawunti yakho, futhi kubala ngebhajethi nganoma iyiphi enesigaba esifanayo.",
        "Or type your own" to "Noma bhala eyakho",
        "e.g. Pick n Pay" to "isibonelo. Pick n Pay",
        "e.g. Pet care" to "isibonelo. Ukunakekela izilwane",
        "e.g. Groceries" to "isibonelo. Izinkambo",
        "e.g. Thando Mokoena" to "isibonelo. Thando Mokoena",
        "e.g. New laptop" to "isibonelo. Ikhompyutha entsha",
        "e.g. 2500" to "isibonelo. 2500",
        "e.g. 18000" to "isibonelo. 18000",
        // budgets
        "Your budgets" to "Izibhajethi zakho",
        "A budget tracks what you spend in one category this month. Spending counts when a transaction is added with that category." to
                "Ibhajethi ilandela imali oyisebenzisile esigabeni esisodwa kule nyanga. Ukusebenzisa kubalwa uma inkokelo yengezwa naleso sigaba.",
        "New budget" to "Ibhajethi entsha",
        "Edit budget" to "Hlela ibhajethi",
        "Give this budget a name, like Groceries." to "Nika le bhajethi igama, njengezinkambo.",
        "Monthly limit" to "Umkhawulo wenyanga",
        "No budgets yet — open the Budgets tab to add one." to
                "Akukho zibhajethi — vula ikhala lezibhajethi uzengeze.",
        "Budget saved" to "Ibhajethi igciniwe",
        "Budget deleted" to "Ibhajethi isusiwe",
        "Delete this budget?" to "Uyifuna le bhajethi ususe?",
        "MoneyMate will stop tracking " to "I-MoneyMate izomisa ukulandela ",
        ". Your transactions stay exactly as they are." to ". Izinkokelo zakho zihlala njengoba zikuyo.",
        // goals
        "Tap to add money or edit" to "Chofoza ukengeze imali noma uhlele",
        "Give your goal a name, like New laptop." to
                "Nika isigomo sakho igama, njengekhompyutha entsha.",
        "What are you saving for?" to "Ubusa imali yini?",
        "Target amount" to "Inani lesigamo",
        "Leave empty to start from R 0.00." to "Uyishiye ile ukuze uqale ku-R 0.00.",
        "Leave empty to change only the name or target." to
                "Uyishiye ile ukuze uguqule igama noma isigamo kuphela.",
        "Update goal" to "Hlaziya isigomo",
        "Goal saved" to "Isigomo sigciniwe",
        "Goal deleted" to "Isigomo sinesiswe",
        "Goal met 🎉" to "Isigomo sifinyelelwe 🎉",
        "Delete this goal?" to "Ususa lesi sigomo?",
        "No goals yet. Tap + Add and give your saving something to aim at." to
                "Akukho migomo. Chofoza + Engeza, unike lokusonga okungenzela ukubheka.",
        // activity
        "Tap to edit or delete" to "Chofoza uhlele noma ususe",
        "Delete this transaction?" to "Ususa leyo nkokelo?",
        // settings
        "Your profile" to "Iphrofayili yakho",
        "Name" to "Igama",
        "Email" to "Imeyili",
        "Currency" to "Uhlobo lwemali",
        " (locked to rand)" to " (ivinjelwe ku-Rand)",
        "Privacy" to "Ubumfihlo",
        "Hide my balances" to "Fihla izibhalansi zami",
        "Masks every amount with dots." to "Ifihla inani ngalinye ngezindandatho.",
        "Appearance" to "Ukubonakala",
        "Dark mode" to "Imodi emnyama",
        "Saved to your account, so it follows you to any device." to
                "Igciniwe ekhawunt yakho, ngakho-ke izokulandelakho kunoma iyiphi idivayisi.",
        "Language" to "Ulimi",
        "Budget alerts" to "Izikhumbuzo zezibhajethi",
        "Warn me when a budget is nearly used up." to
                "Ngiyethule uma ibhajethi iseduze yokuphelela.",
        "Security" to "Ukuphepha",
        "Change password" to "Shintsha iphasiwedi",
        "Current password" to "Iphasiwedi yanamuhla",
        "New password" to "Iphasiwedi entsha",
        "Confirm new password" to "Qinisekisa iphasiwedi entsha",
        "Your password now" to "Iphasiwedi yakho yanamuhla",
        "At least 10 characters" to "Izimpawu eziyisi-10 noma ngaphezulu",
        "Changing your password signs out every other device and keeps you signed in on this one." to
                "Ukushintsha iphasiwedi yakho kuphumisa zonke izinye izidivayisi, futhi kukugcine ungenile kule.",
        "Your new password is hashed on the server, and every other device is signed out." to
                "Iphasiwedi yakho entsha 'hash'we eseva, futhi zonke izinye izidivayisi ziphumiwe.",
        "Password changed" to "Iphasiwedi ishintshwe",
        "Demo data" to "Idatha yedemo",
        "Reset demo data" to "Setha kabusha idatha yedemo",
        "Reset your demo data?" to "Usetha kabusha idatha yakho yedemo?",
        "Everything in MoneyMate is simulated. Reset puts the sample accounts, transactions, budgets and goals back the way they started." to
                "Konke kwi-MoneyMate kuboniswe. Ukubuyisela kabusha kubuyisela izakhawunt, izinkokelo, izibhajethi ne migomo yesibonela lapho ziqala khona.",
        "How this copy runs" to "Le kophi iyasebenza kanjani",
        "Sign-in" to "Ukungena",
        "Database" to "I-database",
        "Room · kept on this phone" to "Room · igciniwe kule foni",
        "Cloud copy" to "Khopi yamezu",
        // push notifications + budget alerts
        "Notifications" to "Izaziso",
        "Push notifications" to "Izaziso",
        "Allowed" to "Ivumelekile",
        "Allow notifications" to "Vumela izaziso",
        "Turn notifications on in your phone's settings to receive alerts." to
                "Vula izaziso ezilungiselelelo zefoni yakho ukuze uthole izikhumbuzo.",
        "FCM token" to "Tokeni ye-FCM",
        "Getting token…" to "Kuthola toke…",
        "Copy token" to "Kopa toke",
        "Token copied" to "Toke ikopitswe",
        "Use this token in the Firebase console to send a push to this phone." to
                "Sebenzisa le toke ku-Firebase console ukuze uthumele i-push kule foni.",
        "Budget almost used up" to "Ibhajethi iseduze yokuphelela",
        "Budget over" to "Ibhajethi igcwele",
        "You have a new message." to "Unezaziso entsha.",
        // categories (display only; stored values stay English)
        "Groceries" to "Izinkambo",
        "Transport" to "Ukuhamba",
        "Bills" to "Izibhili",
        "Eating out" to "Ukudla ngaphandle",
        "Shopping" to "Ukuthenga",
        "Entertainment" to "Ukuzijabulisa",
        "Health" to "Ezempilo",
        "Education" to "Imfundo",
        "Savings" to "Ukusonga",
        "Income" to "Ithebelelo",
        "Other" to "Ezinye",
        // error messages (thrown by MmRepo)
        "Could not create that account." to "Akukwazekile ukudala leyo akhawunt.",
        "Enter a monthly limit above R0.00." to "Faka umkhawulo wenyanga ongaphesheya ku-R0.00.",
        "Enter a target amount above R0.00." to "Faka inani lesigamo elingaphezu kwe-R0.00.",
        "Enter an amount above R0.00." to "Faka inani elingaphezu kwe-R0.00.",
        "Give this transaction a description." to "Nika leyo nkokelo incazelo.",
        "Please choose a password you have not used here before." to
                "Khetha iphasiwedi ongayisetyenzile lapha ngaphambili.",
        "Please enter a valid email address." to "Faka ikheli leimeyili elisebenzayo.",
        "Please enter your password." to "Faka iphasiwedi yakho.",
        "Please name the category." to "Nika igama isigaba.",
        "Please name your goal." to "Nika isigama isigomo sakho.",
        "Please sign in again." to "Ngena futhi.",
        "That action is not available." to "Lolo msebenzi alitholakali.",
        "That budget no longer exists." to "Leyo bhajethi isaphelile.",
        "That current password is not right." to "Leyo phasiwedi yanamuhla ayilona elilungile.",
        "That email or password is not right." to "Leyo imeyili noma iphasiwedi alilona elilungile.",
        "That goal no longer exists." to "Leso sigomo isephelile.",
        "That transaction no longer exists." to "Leyo nkokelo isephelile.",
        "Your new password needs at least 10 characters." to
                "Iphasiwedi yakho entsha idinga izimpawu eziyisi-10 noma ngaphezulu.",
        "Your password needs at least 10 characters." to
                "Iphasiwedi yakho idinga izimpawu eziyisi-10 noma ngaphezulu.",
        "Meet your money's new mate." to "Thola umngane omusha we mali yakho.",
        "Good to see you again." to "Kuhle ukubona wena futhi.",
        "A fresh start for you and your finances." to "Ukuqala okusha kuwe namali akho.",
        "Your password" to "Iphasiwedi yakho",
        "Please wait…" to "Ngicela ulinde…",
        "Enter your current password." to "Faka iphasiwedi yakho yanamuhla.",
        "Enter an amount like 500, or leave it empty." to "Faka inani njengo-500, noma uyishiye ile.",
        "Enter a monthly limit, like 2500." to "Faka umkhawulo wenyanga, njengo-2500.",
        "Enter a target amount, like 18000." to "Faka inani lesigamo, njengo-18000.",
        "Your new passwords don't match." to "Izaphasiwedi zakho ezintsha azihambisani.",
        "Your passwords don't match." to "Izaphasiwedi zakho azihambisani.",
        "Something went wrong. Please try again." to "Kwenzeke into engalungile. Zama futhi.",
        "Could not reach the MoneyMate server." to "Akukwazekile ukufika eseva se-MoneyMate.",
        "Could not save" to "Akukwazekile ukugcina",
        "Could not save. Is the server running?" to "Akukwazekile ukugcina. Ingabe iseva siyasebenza?",
        "Could not delete" to "Akukwazekile ukususa",
        "Could not delete. Is the server running?" to "Akukwazekile ukususa. Ingabe iseva siyasebenza?",
        "Could not reset" to "Akukwazekile ukubuyisela",
        "Could not change your password." to "Akukwazekile ukushintsha iphasiwedi yakho.",
        "Transaction saved" to "Inkokelo igciniwe",
        "Transaction deleted" to "Inkokelo isusiwe",
        "Demo data reset" to "Idatha yedemo ibuyiselwe",
        "Setting saved" to "Ilungiselelo ligciniwe",
        "Reset" to "Buyisela",
        "Reached!" to "Kufinyelelwe!",
        "Spent" to "Okusebenziswe",
        "Nothing here yet. Add your first transaction." to "Akukho lutho lapha. Engeza inkokelo yakho yokuqala.",
        "No budgets yet. Tap + Add to create your first one." to "Akukho zibhajethi. Chofoza + Engeza ukudala eyokuqala.",
        "Give your saving a target. Tap a goal to add money, rename it, or change the target." to "Nika lokusonga isigamo. Chofoza isigomo ukengeze imali, uhlaziye igama, noma ushintshe isigamo.",
        "New savings goal" to "Isigomo sokusonga esisha",
        "Add to savings (optional)" to "Engeza ekusongeni (okukhethwa)",
        "Already saved (optional)" to "Okusongelwe (okukhethwa)",
        "e.g. 500" to "isibonelo. 500",
        "Your password is checked by Firebase Authentication and never stored by MoneyMate. Everything you add is kept in the phone's Room database and copied to Firestore too." to "Iphasiwedi yakho ihlolwa yi-Firebase Authentication futhi ihlale ingagcinwa yi-MoneyMate. Okuqala konke okungenza kugcinwa kwi-database ye-Room yefoni futhi kukopiswe ku-Firestore.",
        "Simulated demo data, kept in this phone's Room database and copied to Firestore. No real bank account is connected." to "Idatha yedemo, igciniwe kwi-database ye-Room kule foni futhi ikopiswe ku-Firestore. Ayikho i-akhawunt ye-ibhanki yangempela exhunywe.",
        "Your balances, transactions, budgets and goals go back to the sample values. Your login and settings stay the same." to "Izibhalansi, izinkokelo, izibhajethi ne migomo yakho zibuya emavinini wesibonelo. Ukungena nezilungiselelo zakho zihlala ezifanayo.",
        "). Deleting it also adjusts your account balance, so the numbers stay truthful." to "). Ukususa kuyalungisa ibhalansi ye-akhawunti yakho, ngakho-ke imibala ihlala iqiniso.",
        "Give this transaction a description, like Woolworths." to "Nika leyo nkokelo incazelo, njengo-Woolworths.",
        "Firestore · syncs when you are online" to "Firestore · isihambisana lapho u online khona",
        "Off · this build stays on the phone" to "Off · le nkamiso lena lihlela kule foni",
        "Refresh" to "Vuselela",
        "to go" to "okusele",
        // step 4 + 5: receipts and Google sign-in
        "Sign in with Google" to "Ngena nge-Google",
        "Google sign-in is not set up yet. Use email and password." to "Ukungena nge-Google awekwa kahle. Sebenzisa imeyili nephasiwedi.",
        "That Google sign-in response could not be read." to "Impendulo ye-Google ayifundi.",
        "That Google sign-in did not work. Please try again." to "Ukungena nge-Google akuwe kusebenza. Zama futhi.",
        "An account with that email already exists. Sign in with your password instead." to "I-akhawunti nale imeyili seyikhona. Ngena ngephasiwedi yakho.",
        "Receipt" to "I-receipt",
        "Attach a receipt photo" to "Faka isithombe se-receipt",
        "Receipt attached" to "Isithombe se-receipt sikhona",
        "Remove receipt photo" to "Susa isithombe se-receipt",
        "View receipt" to "Buka i-receipt",
        "Could not save the receipt photo" to "Angikwazi ukulondloza isithombe se-receipt.",
        "Close" to "Vala",
    )

    // --------------------------------------------------------- Sesotho
    private val ST = mapOf(
        // tabs and app chrome
        "Home" to "Lehae",
        "Budgets" to "Litekanyo",
        "Goals" to "Maikemelo",
        "Activity" to "Ts'ebetso",
        "Settings" to "Ditlhophiso",
        "← Back" to "← Moraase",
        "TOTAL BALANCE" to "KAKARETSO EA MADI",
        "A little clarity for your money." to "Tlhaloso e nyane ea madi a hau.",
        "Simulated demo banking · no real bank connected" to
                "Li-banka tsa demo feela · ha ho na banka ea 'nete e hokahantsoeng",
        "Demo prototype. Balances and transactions are simulated, not real money." to
                "Demo feela. Likakaretso le lipapanyapalo ha li 'nete — eseng madi a 'nete.",
        "MoneyMate is a learning prototype. Real bank linking needs a provider account (for example Stitch) before it can be switched on." to
                "MoneyMate ke prototype ea thuto. Ho hokahanya banka ea 'nete ho hloka akhawunte ea moetsi (mohlala Stitch) pele e ka buuwa.",
        // welcome / sign in
        "Sign in to MoneyMate" to "Kenya ho MoneyMate",
        "Sign in and pick up where you left off." to "Kenya 'me u tsoelele pele moo u emetseng.",
        "Create my account" to "Theha akhawunte ea ka",
        "I already have an account" to "Ke se ke na le akhawunte",
        "New around here? Create an account" to "U mocha mona? Theha akhawunte",
        "Already a mate? Sign in" to "U se u mo MoneyMate? Kenya",
        "Welcome back," to "O amohetsoe hape,",
        "Full name" to "Lebitso le feletseng",
        "Email address" to "Aterese ea imeile",
        "Password" to "Phasewete",
        "Confirm password" to "Netefatsa phasewete",
        "One more time" to "Ngola gape",
        "Please enter your name." to "Kena lebitso la hau.",
        "Sign out" to "Tsoa",
        // home
        "Your accounts" to "Likhawunte tsa hau",
        "Recent activity" to "Ts'ebetso ea nakoana",
        "Budgets at a glance" to "Pono ea litekanyo",
        "Savings goals" to "Maikemelo a polokeho",
        "Refresh everything" to "Fusisa kaofela",
        "+ Add" to "+ Kenya",
        "+ Add a transaction" to "+ Kenya papanyapalo",
        "Add a transaction" to "Kenya papanyapalo",
        "demo accounts" to "likhawunte tsa demo",
        "transactions" to "lipapanyapalo",
        "tap one to delete it" to "tobetsa e le 'ngoe ho e tlosa",
        "of" to "ea",
        "spent" to "e sehetsoe",
        "left" to "e setseng",
        "Over by" to "Feta ka",
        // exchange rates (Frankfurter REST API)
        "Live exchange rates" to "Litekanyo tsa ho fetola lichelete",
        "1 South African rand" to "IRande e 'ngoe ya Afrika Borwa",
        "Checking today's rates…" to "E hlahloba litekanyo tsa kajeno…",
        "Rates are not available right now. Check your internet and tap this card to try again." to
                "Litekanyo ha li fumanehe hona joale. Hlola inthanete ea hao 'me u tobetsa kharoto ena ho leka hape.",
        "Updated" to "E ntlafalitsoeng",
        "Tap to refresh" to "Tobetsa ho e fusisa",
        // add / edit transaction
        "Money out" to "Madi a tswang",
        "Money in" to "Madi a eang",
        "Amount in rand" to "Kakaretso ka lirane",
        "Rand, with or without cents." to "Lirane, le liseente kapa ntle le liseente.",
        "Description" to "Tlhaloso",
        "Category" to "Mofuta",
        "Account" to "Akhawunte",
        "Date" to "Letsatsi",
        "Save transaction" to "Boloka papanyapalo",
        "Saving…" to "E boloka…",
        "Cancel" to "Hlakola",
        "Save" to "Boloka",
        "Delete" to "Tlosa",
        "There is no account to put this in." to "Ha ho akhawunte eo u ka e behang mona.",
        "Money out lowers your balance. Money in raises it." to
                "Madi a tswang a theola kakaretso ea hau. Madi a eang a e phahamisa.",
        "This writes to your MoneyMate server, updates your account balance, and counts towards any budget with the same category." to
                "Sena se ngola ho seva sa hau sa MoneyMate, se ntlafatsa kakaretso ea akhawunte ea hau, 'me se bala ho tekanyo e nang le mofuta o tšoanang.",
        "Or type your own" to "Kapa ngola wa hao",
        "e.g. Pick n Pay" to "mohlala. Pick n Pay",
        "e.g. Pet care" to "mohlala. Ho hlokomela liphoofolo",
        "e.g. Groceries" to "mohlala. Lijo",
        "e.g. Thando Mokoena" to "mohlala. Thando Mokoena",
        "e.g. New laptop" to "mohlala. Kompyuthere e ncha",
        "e.g. 2500" to "mohlala. 2500",
        "e.g. 18000" to "mohlala. 18000",
        // budgets
        "Your budgets" to "Litekanyo tsa hao",
        "A budget tracks what you spend in one category this month. Spending counts when a transaction is added with that category." to
                "Tekanyo e latela se u se sehileng ka mofuta o le mong khoeng ena. Tšebeliso e balwa ha papanyapalo e eketswa ka mofuta oo.",
        "New budget" to "Tekanyo e ncha",
        "Edit budget" to "Fetola tekanyo",
        "Give this budget a name, like Groceries." to "Fana ka lebitso tekanyong ena, joalo ka Lijo.",
        "Monthly limit" to "Moeli oa khoeli",
        "No budgets yet — open the Budgets tab to add one." to
                "Ha ho litekanyo ho fihlela joale — bula leqephe la Litekanyo ho eketsa e le 'ngoe.",
        "Budget saved" to "Tekanyo e bolokiloe",
        "Budget deleted" to "Tekanyo e tlositsoe",
        "Delete this budget?" to "U batla ho tlosa tekanyo ena?",
        "MoneyMate will stop tracking " to "MoneyMate e tla empa ho latela ",
        ". Your transactions stay exactly as they are." to ". Lipapanyapalo tsa hao ha li fetohi.",
        // goals
        "Tap to add money or edit" to "Tobetsa ho eketsa madi kapa ho fetola",
        "Give your goal a name, like New laptop." to
                "Fana ka lebitso ikemelong la hao, joalo ka Kompyuthere e ncha.",
        "What are you saving for?" to "U boloka madi bakeng sa eng?",
        "Target amount" to "Kakaretso ea lebelo",
        "Leave empty to start from R 0.00." to "E siee e le nyane ho qala ho tloha R 0.00.",
        "Leave empty to change only the name or target." to
                "E siee e le nyane ho fetola lebitso kapa lebelo feela.",
        "Update goal" to "Fetola ikemo",
        "Goal saved" to "Ikemo e bolokiloe",
        "Goal deleted" to "Ikemo e tlositsoe",
        "Goal met 🎉" to "Ikemo e fihletsoe 🎉",
        "Delete this goal?" to "U batla ho tlosa ikemo ena?",
        "No goals yet. Tap + Add and give your saving something to aim at." to
                "Ha ho maikemelo ho fihlela joale. Tobetsa + Kenya 'me u fe polokeho ea hao lebelo.",
        // activity
        "Tap to edit or delete" to "Tobetsa ho fetola kapa ho tlosa",
        "Delete this transaction?" to "U batla ho tlosa papanyapalo ena?",
        // settings
        "Your profile" to "Profaele ea hau",
        "Name" to "Lebitso",
        "Email" to "Imeile",
        "Currency" to "Lichelete",
        " (locked to rand)" to " (e tshwailwe ka lira)",
        "Privacy" to "Bokrometso",
        "Hide my balances" to "Pata likakaretso tsa ka",
        "Masks every amount with dots." to "E pata madi kaofela ka li-dot.",
        "Appearance" to "Mpono",
        "Dark mode" to "Mokhoa o lefifi",
        "Saved to your account, so it follows you to any device." to
                "E bolokiloe ho akhawunte ea hau, kahoo e u latela ho sesebelisoa sefe kapa sefe.",
        "Language" to "Puo",
        "Budget alerts" to "Litoliseho tsa litekanyo",
        "Warn me when a budget is nearly used up." to "Ntwaletse ha tekanyo e fella haholo.",
        "Security" to "Tshireletso",
        "Change password" to "Fetola phasewete",
        "Current password" to "Phasewete ea hajoale",
        "New password" to "Phasewete e ncha",
        "Confirm new password" to "Netefatsa phasewete e ncha",
        "Your password now" to "Phasewete ya hao ya hajoale",
        "At least 10 characters" to "Litlhaka tse 10 kapa ho feta",
        "Changing your password signs out every other device and keeps you signed in on this one." to
                "Ho fetola phasewete ea hau ho tswisa lisebelisoa tse ling tsohle, 'me ho u siea u ntse u kenile ho ena.",
        "Your new password is hashed on the server, and every other device is signed out." to
                "Phasewete e ncha ea hau e 'hash' holim'a seva, 'me lisebelisoa tse ling tsohle li tswisiwa.",
        "Password changed" to "Phasewete e fetotsoe",
        "Demo data" to "Data ea demo",
        "Reset demo data" to "Khutlisa data ea demo",
        "Reset your demo data?" to "U batla ho khutlisa data ea hau ea demo?",
        "Everything in MoneyMate is simulated. Reset puts the sample accounts, transactions, budgets and goals back the way they started." to
                "Lintho tsohle ho MoneyMate ha li 'nete. Ho khutlisa ho beha likhawunte, lipapanyapalo, litekanyo le maikemelo a mohlala moo li qalileng.",
        "How this copy runs" to "Camoo hlophiso ena e sebetsang",
        "Sign-in" to "Ho kena",
        "Database" to "Database",
        "Room · kept on this phone" to "Room · e bolokiloe hona foonong ena",
        "Cloud copy" to "Khopi ea maru",
        // push notifications + budget alerts
        "Notifications" to "Litokiso",
        "Push notifications" to "Litokiso",
        "Allowed" to "E lumelletsoe",
        "Allow notifications" to "Lumella litokiso",
        "Turn notifications on in your phone's settings to receive alerts." to
                "Bula litokiso ditlhophising tsa fono ea hao ho amohela litokiso.",
        "FCM token" to "Toke ya FCM",
        "Getting token…" to "E fumana toke…",
        "Copy token" to "Kopisa toke",
        "Token copied" to "Toke e kopisitswe",
        "Use this token in the Firebase console to send a push to this phone." to
                "Sebedisa toke ena ka Firebase console ho romela push ho fono ena.",
        "Budget almost used up" to "Tekanyo e fella haholo",
        "Budget over" to "Tekanyo e fetile",
        "You have a new message." to "O na le molaetsa o mocha.",
        // categories (display only; stored values stay English)
        "Groceries" to "Lijo",
        "Transport" to "Tsamaeo",
        "Bills" to "Litereke",
        "Eating out" to "Ho ja kantle",
        "Shopping" to "Ho reka",
        "Entertainment" to "Pono-pono",
        "Health" to "Boiketlo",
        "Education" to "Thuto",
        "Savings" to "Polokeho",
        "Income" to "Madi a fihlang",
        "Other" to "Tse ling",
        // error messages (thrown by MmRepo)
        "Could not create that account." to "Ha ke khone ho theha akhawunte eo.",
        "Enter a monthly limit above R0.00." to "Kena moeli oa khoeli o feta R0.00.",
        "Enter a target amount above R0.00." to "Kena kakaretso ea lebelo e feta R0.00.",
        "Enter an amount above R0.00." to "Kena kakaretso e feta R0.00.",
        "Give this transaction a description." to "Fana ka tlhaloso papanyapalong ena.",
        "Please choose a password you have not used here before." to
                "Khetpha phasewete eo u sa e sebelisitseng mona pele.",
        "Please enter a valid email address." to "Kena aterese ea imeile e nepahetseng.",
        "Please enter your password." to "Kena phasewete ea hau.",
        "Please name the category." to "Fana ka lebitso mofutong.",
        "Please name your goal." to "Fana ka lebitso ikemelong la hao.",
        "Please sign in again." to "Hloka hore u kenye hape.",
        "That action is not available." to "Tšebetso eo ha e fumanehe.",
        "That budget no longer exists." to "Tekanyo eo ha e sa le teng.",
        "That current password is not right." to "Phasewete ea hajoale ha e nepahale.",
        "That email or password is not right." to "Imeile kapa phasewete ha e nepahale.",
        "That goal no longer exists." to "Ikemo eo ha e sa le teng.",
        "That transaction no longer exists." to "Papanyapalo eo ha e sa le teng.",
        "Your new password needs at least 10 characters." to
                "Phasewete e ncha ea hau e hloka litlhaka tse 10 kapa ho feta.",
        "Your password needs at least 10 characters." to
                "Phasewete ea hau e hloka litlhaka tse 10 kapa ho feta.",
        "Meet your money's new mate." to "Fumana motsoalle echa wa madi a hao.",
        "Good to see you again." to "Ho monate ho u bona hape.",
        "A fresh start for you and your finances." to "Qalo echa bakeng sa hau le lichelete tsa hao.",
        "Your password" to "Phasewete ea hao",
        "Please wait…" to "Empela…",
        "Enter your current password." to "Kena phasewete ea hao ea hajoale.",
        "Enter an amount like 500, or leave it empty." to "Kena kakaretso joalo ka 500, kapa e siee e le nyane.",
        "Enter a monthly limit, like 2500." to "Kena moeli oa khoeli, joalo ka 2500.",
        "Enter a target amount, like 18000." to "Kena kakaretso ea lebelo, joalo ka 18000.",
        "Your new passwords don't match." to "Diphasewete tsa hao tse ncha ha di dumalane.",
        "Your passwords don't match." to "Diphasewete tsa hao ha di dumalane.",
        "Something went wrong. Please try again." to "Ho etsahale phoso. Leka hape.",
        "Could not reach the MoneyMate server." to "Ha e khone ho fihla ho seva sa MoneyMate.",
        "Could not save" to "Ha ho khone ho boloka",
        "Could not save. Is the server running?" to "Ha ho khone ho boloka. Seva na e sebetsa?",
        "Could not delete" to "Ha ho khone ho tlosa",
        "Could not delete. Is the server running?" to "Ha ho khone ho tlosa. Seva na e sebetsa?",
        "Could not reset" to "Ha ho khone ho khutlisa",
        "Could not change your password." to "Ha ho khone ho fetola phasewete ea hao.",
        "Transaction saved" to "Papanyapalo e bolokiloe",
        "Transaction deleted" to "Papanyapalo e tlositsoe",
        "Demo data reset" to "Data ea demo e khutlisitsoe",
        "Setting saved" to "Tlhophiso e bolokiloe",
        "Reset" to "Khutlisa",
        "Reached!" to "Fihletsoe!",
        "Spent" to "E sehetsoeng",
        "Nothing here yet. Add your first transaction." to "Ha ho letho mona ho fihlela joale. Kenya papanyapalo ea pele.",
        "No budgets yet. Tap + Add to create your first one." to "Ha ho litekanyo ho fihlela joale. Tobetsa + Kenya ho theha ea pele.",
        "Give your saving a target. Tap a goal to add money, rename it, or change the target." to "Fe polokeho ea hao lebelo. Tobetsa ikemo ho eketsa madi, ho fetola lebitso, kapa ho fetola lebelo.",
        "New savings goal" to "Ikemo e ncha ea polokeho",
        "Add to savings (optional)" to "Eketsa polokehong (ha e hlokahale)",
        "Already saved (optional)" to "E bolokiloe (ha e hlokahale)",
        "e.g. 500" to "mohlala. 500",
        "Your password is checked by Firebase Authentication and never stored by MoneyMate. Everything you add is kept in the phone's Room database and copied to Firestore too." to "Phasewete ea hao e hlahlojoa ka Firebase Authentication 'me ha e kae e bolokoe ke MoneyMate. Letho o le eketsang le bolokwa ho database ea Room ea fono 'me le kopisetswa ho Firestore hape.",
        "Simulated demo data, kept in this phone's Room database and copied to Firestore. No real bank account is connected." to "Data ea demo, e bolokiloe ho database ea Room hona foonong ena 'me e kopisitswe ho Firestore. Ha ho na akhawunte ea banka ea 'nete e hokahantsoeng.",
        "Your balances, transactions, budgets and goals go back to the sample values. Your login and settings stay the same." to "Likakaretso, lipapanyapalo, litekanyo le maikemelo a hao li khutla ho boleng ba mohlala. Ho kena le ditlhophiso tsa hao li lula tsoana.",
        "). Deleting it also adjusts your account balance, so the numbers stay truthful." to "). Ho e tlosa ho ntlafatsa kakaretso ea akhawunte ea hau, kahoo lipalo li lula tsa 'nete.",
        "Give this transaction a description, like Woolworths." to "Fana ka tlhaloso papanyapalong ena, joalo ka Woolworths.",
        "Firestore · syncs when you are online" to "Firestore · e lumasana ha u online",
        "Off · this build stays on the phone" to "Off · hlophiso ena e lula foonong ena",
        "Refresh" to "Fusisa",
        "to go" to "e setseng",
        // step 4 + 5: receipts and Google sign-in
        "Sign in with Google" to "Kenya ka-Google",
        "Google sign-in is not set up yet. Use email and password." to "Ho kena ka-Google ha ho balolwa. Sebedisa imeyile le phasewete.",
        "That Google sign-in response could not be read." to "Karabo ya-Google ha e ka balwa.",
        "That Google sign-in did not work. Please try again." to "Ho kena ka-Google ha ho sebedise. Leke hape.",
        "An account with that email already exists. Sign in with your password instead." to "Akhaonte ka imeyilo eo e se e teng. Kenya ka phasewete ya hao.",
        "Receipt" to "Khakanyo",
        "Attach a receipt photo" to "Kenya setshwantsho sa-receipt",
        "Receipt attached" to "Setshwantsho sa-receipt se teng",
        "Remove receipt photo" to "Fosola setshwantsho sa-receipt",
        "View receipt" to "Bona khakanyo",
        "Could not save the receipt photo" to "Ha ke ke ka boloka setshwantsho sa-receipt.",
        "Close" to "Tswala",
    )
}