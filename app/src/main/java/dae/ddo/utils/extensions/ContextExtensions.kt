package dae.ddo.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import timber.log.Timber

fun Context.launch(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    when (this) {
        is Activity -> this.startActivity(intent)
        is Fragment -> this.startActivity(intent)
        else -> Timber.w("Cannot launch from context type: $this")
    }
}