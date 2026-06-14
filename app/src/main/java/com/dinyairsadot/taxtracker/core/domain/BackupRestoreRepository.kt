package com.dinyairsadot.taxtracker.core.domain

import com.dinyairsadot.taxtracker.core.util.backup.BackupPayload

interface BackupRestoreRepository {
    suspend fun restoreFromBackup(payload: BackupPayload)
}
