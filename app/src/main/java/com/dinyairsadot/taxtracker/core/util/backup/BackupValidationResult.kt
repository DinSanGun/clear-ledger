package com.dinyairsadot.taxtracker.core.util.backup

sealed class BackupValidationResult {
    data class Valid(val payload: BackupPayload) : BackupValidationResult()
    data class UnsupportedVersion(val version: Int) : BackupValidationResult()
    data class Invalid(val reason: String) : BackupValidationResult()
}
