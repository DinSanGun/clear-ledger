package com.dinyairsadot.clearledger.core.domain

import com.dinyairsadot.clearledger.core.util.backup.BackupPayload

interface BackupRestoreRepository {
    suspend fun restoreFromBackup(payload: BackupPayload)
}
