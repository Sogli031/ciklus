package com.injekcije.app.data.backup

import android.app.Application
import java.lang.ref.WeakReference

object GoogleDriveBackupProvider {
    @Volatile
    private var instanceRef: WeakReference<GoogleDriveBackup>? = null

    fun get(app: Application): GoogleDriveBackup {
        instanceRef?.get()?.let { return it }

        return synchronized(this) {
            instanceRef?.get() ?: GoogleDriveBackup(app.applicationContext).also {
                instanceRef = WeakReference(it)
            }
        }
    }
}
