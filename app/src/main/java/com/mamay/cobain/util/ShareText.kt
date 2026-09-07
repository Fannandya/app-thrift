package com.mamay.cobain.util

import android.content.Context
import android.content.Intent

/**
 * Shares plain text through the system chooser. Deliberately text/plain and not a
 * file: a text receipt goes straight into WhatsApp or SMS with no FileProvider, no
 * storage permission, and nothing left behind on disk.
 */
fun shareText(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Bagikan struk"))
}
