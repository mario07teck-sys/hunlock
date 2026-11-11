package fr.pointage.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

/** Settings: import names from an STT export, edit them, write the van tag. */
class MainActivity : Activity() {
    private lateinit var names: Names
    private lateinit var works: EditText
    private lateinit var clients: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        names = Names(this)
        val column = column()
        column.text(
            "1. Dans Simple Time Tracker : Réglages > Exporter en CSV (toute la période).\n" +
                "2. Importe ce fichier ici.\n" +
                "3. Écris le tag NFC du camion.",
        )
        column.button("Importer l'export CSV") {
            startActivityForResult(
                Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"),
                IMPORT,
            )
        }
        column.text("Travaux, un par ligne.\nAppui long sur le tag : 3 s = 1er, 5 s = 2e, 7 s = 3e.")
        works = column.editor(names.works)
        column.text("Clients, un par ligne (étiquettes STT).")
        clients = column.editor(names.clients)
        column.button("Écrire le tag NFC") { waitForTagToWrite() }
        column.button("Tester sans tag") { save(); startActivity(Intent(this, TapActivity::class.java)) }
    }

    override fun onPause() {
        save()
        NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
        super.onPause()
    }

    private fun save() {
        names.works = works.text.lines()
        names.clients = clients.text.lines()
    }

    @Deprecated("Activity result API needs AndroidX; plain Activity keeps the app dependency-free.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri = data?.data
        if (requestCode != IMPORT || resultCode != RESULT_OK || uri == null) return
        val csv = runCatching { contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() } }
            .getOrElse { return toast("Lecture impossible : ${it.message}") }
        val (importedWorks, importedClients) = namesFromExport(csv)
        if (importedWorks.isEmpty()) return toast("Aucun travail trouvé : est-ce bien l'export CSV de STT ?")
        // Keep the user's order on re-import, append what is new.
        works.setText(Names.clean(works.text.lines() + importedWorks).joinToString("\n"))
        clients.setText(Names.clean(clients.text.lines() + importedClients).joinToString("\n"))
        save()
        toast("${importedWorks.size} travaux, ${importedClients.size} clients importés")
    }

    private fun waitForTagToWrite() {
        val nfc = NfcAdapter.getDefaultAdapter(this) ?: return toast("Pas de NFC sur ce téléphone")
        if (!nfc.isEnabled) return toast("Active le NFC dans les réglages")
        toast("Pose le téléphone sur le tag vierge…")
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V
        nfc.enableReaderMode(this, { tag ->
            val result = runCatching { write(tag) }
            runOnUiThread {
                nfc.disableReaderMode(this)
                toast(result.fold({ "Tag écrit ✔" }, { "Échec de l'écriture : ${it.message}" }))
            }
        }, flags, null)
    }

    private fun write(tag: Tag) {
        val message = NdefMessage(
            NdefRecord.createUri("pointage://camion"),
            NdefRecord.createApplicationRecord(packageName),
        )
        Ndef.get(tag)?.use { it.connect(); it.writeNdefMessage(message); return }
        NdefFormatable.get(tag)?.use { it.connect(); it.format(message); return }
        error("tag non compatible NDEF")
    }

    private fun LinearLayout.editor(lines: List<String>) = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        setText(lines.joinToString("\n"))
    }.also(::addView)

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private companion object {
        const val IMPORT = 1
    }
}
