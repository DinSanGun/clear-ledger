package com.dinyairsadot.clearledger.core.domain

import android.content.Context
import java.io.InputStream

interface BackupRestoreRepository {
    /**
     * Restores categories, invoices and attachment files from a backup ZIP. [openZip] is called
     * once per pass over the ZIP (the backup is re-validated here, not just at selection time);
     * each stream it returns is closed by the repository. Throws on any problem, in which case
     * existing data and attachment files are left intact.
     */
    suspend fun restoreFromBackup(openZip: () -> InputStream)
    suspend fun resetAllData(context: Context)
}
