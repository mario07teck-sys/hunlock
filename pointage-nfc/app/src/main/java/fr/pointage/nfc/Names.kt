package fr.pointage.nfc

import android.content.Context

/** Work types and clients, one per line, kept in the order the user set. */
class Names(context: Context) {
    private val prefs = context.getSharedPreferences("names", Context.MODE_PRIVATE)

    var works: List<String>
        get() = read("works")
        set(value) = write("works", value)

    var clients: List<String>
        get() = read("clients")
        set(value) = write("clients", value)

    private fun read(key: String) = clean(prefs.getString(key, "").orEmpty().lines())
    private fun write(key: String, value: List<String>) =
        prefs.edit().putString(key, clean(value).joinToString("\n")).apply()

    companion object {
        fun clean(names: List<String>) = names.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }
}

/**
 * Work names (column "activity name") and clients (column "record tags") from a
 * Simple Time Tracker CSV export, most used first.
 */
fun namesFromExport(csv: String): Pair<List<String>, List<String>> {
    val rows = parseCsv(csv).drop(1)
    val works = rows.mapNotNull { it.getOrNull(0) }
    // shortcut: STT joins several tags with ", ", so a tag name containing ", " gets split.
    val clients = rows.flatMap { it.getOrNull(5).orEmpty().split(", ") }
    return byUse(works) to byUse(clients)
}

private fun byUse(names: List<String>) = names.map { it.trim() }.filter { it.isNotEmpty() }
    .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }

/** RFC 4180 CSV: quoted fields may hold commas, doubled quotes and line breaks. */
fun parseCsv(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var i = if (text.startsWith('﻿')) 1 else 0
    while (i < text.length) {
        val c = text[i]
        when {
            quoted && c == '"' && text.getOrNull(i + 1) == '"' -> { field.append('"'); i++ }
            c == '"' -> quoted = !quoted
            quoted -> field.append(c)
            c == ',' -> { row += field.toString(); field.clear() }
            c == '\n' || c == '\r' -> {
                if (c == '\r' && text.getOrNull(i + 1) == '\n') i++
                row += field.toString(); field.clear()
                rows += row; row = mutableListOf()
            }
            else -> field.append(c)
        }
        i++
    }
    if (field.isNotEmpty() || row.isNotEmpty()) { row += field.toString(); rows += row }
    return rows
}
