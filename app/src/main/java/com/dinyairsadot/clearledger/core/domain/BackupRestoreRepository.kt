package com.dinyairsadot.clearledger.core.domain

import android.content.Context
import com.dinyairsadot.clearledger.core.util.backup.BackupPayload

interface BackupRestoreRepository {
    suspend fun restoreFromBackup(payload: BackupPayload)
    suspend fun resetAllData(context: Context)
}
