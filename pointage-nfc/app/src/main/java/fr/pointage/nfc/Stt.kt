package fr.pointage.nfc

import android.content.Context
import android.content.Intent

/** Drives Simple Time Tracker through its automation broadcasts (Réglages > Automatisation in STT). */
object Stt {
    private const val PACKAGE = "com.razeeman.util.simpletimetracker"
    private const val ACTION = "com.razeeman.util.simpletimetracker.ACTION_"

    /** Stops whatever is running, then starts [work] (tagged with [client]) unless it is null. */
    fun switchTo(context: Context, work: String?, client: String?) {
        // STT handles manifest broadcasts one at a time, in order, so the stop lands before the start.
        send(context, "STOP_ALL_ACTIVITIES")
        if (work != null) send(context, "START_ACTIVITY") {
            putExtra("extra_activity_name", work)
            if (client != null) putExtra("extra_record_tag", client)
        }
    }

    private fun send(context: Context, action: String, extras: Intent.() -> Unit = {}) =
        context.sendBroadcast(Intent(ACTION + action).setPackage(PACKAGE).apply(extras))
}
