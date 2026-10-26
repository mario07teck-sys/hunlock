package fr.pointage.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import kotlin.concurrent.thread

/**
 * Opened by the van tag. Holding the phone on the tag picks the 1st/2nd/3rd work
 * (a buzz at each step); lifting it sooner shows the full list instead.
 */
class TapActivity : Activity() {
    private lateinit var names: Names
    private var watcher: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        names = Names(this)
        handle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    override fun onDestroy() {
        watcher?.interrupt()
        super.onDestroy()
    }

    private fun handle(intent: Intent) {
        watcher?.interrupt()
        val works = names.works
        @Suppress("DEPRECATION")
        val ndef = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let(Ndef::get)
        if (ndef == null || works.isEmpty()) return showWorks(works)

        val levels = HOLD_SECONDS.take(works.size)
        val column = column()
        column.text("Garde le téléphone sur le tag…", 24f)
        levels.forEachIndexed { i, s -> column.text("$s s → ${works[i]}") }
        val status = column.text("", 34f)

        watcher = thread {
            val start = SystemClock.elapsedRealtime()
            var level = 0
            try {
                ndef.connect()
                while (level < levels.size) {
                    ndef.ndefMessage // throws once the tag has left the field
                    val held = (SystemClock.elapsedRealtime() - start) / 1000
                    val reached = levels.count { held >= it }
                    if (reached > level) {
                        level = reached
                        buzz()
                        runOnUiThread { status.text = "→ ${works[level - 1]}" }
                    }
                    Thread.sleep(200)
                }
            } catch (e: InterruptedException) {
                return@thread // a new tap or the screen closed
            } catch (e: Exception) {
                // tag removed (or the phone dropped the connection): go with what was held so far
            }
            runCatching { ndef.close() }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                if (level == 0) showWorks(works) else showClients(works[level - 1])
            }
        }
    }

    private fun showWorks(works: List<String>) {
        val column = column()
        if (works.isEmpty()) {
            column.text("Aucun travail : ouvre Pointage NFC et importe l'export de Simple Time Tracker.")
            return
        }
        column.text("Travail suivant ?", 24f)
        works.forEach { work -> column.button(work) { showClients(work) } }
        column.button("⏹ Tout arrêter (fin de journée)") { start(null, null) }
    }

    private fun showClients(work: String) {
        val clients = names.clients
        if (clients.isEmpty()) return start(work, null)
        val column = column()
        column.text("$work — client ?", 24f)
        column.button("Sans client") { start(work, null) }
        val search = EditText(this).apply { hint = "Rechercher un client" }.also(column::addView)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }.also(column::addView)
        fun fill(query: String) {
            list.removeAllViews()
            clients.filter { it.contains(query, ignoreCase = true) }.take(MAX_CLIENTS_SHOWN)
                .forEach { client -> list.button(client) { start(work, client) } }
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) = fill(s.toString())
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        })
        fill("")
    }

    private fun start(work: String?, client: String?) {
        // Most recent client first next time.
        if (client != null) names.clients = listOf(client) + names.clients
        Stt.switchTo(this, work, client)
        val message = if (work == null) "⏹ Tout arrêté" else "▶ $work" + (client?.let { " — $it" } ?: "")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun buzz() = getSystemService(Vibrator::class.java)
        ?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))

    private companion object {
        val HOLD_SECONDS = listOf(3, 5, 7)
        const val MAX_CLIENTS_SHOWN = 40
    }
}
