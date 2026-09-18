package com.dinyairsadot.clearledger.core.integrity

import java.security.MessageDigest
import java.util.Base64

/**
 * Helpers for Play Integrity Standard API `requestHash` values.
 *
 * Correct Stage 6 flow (when AI scan networking exists):
 * 1. Build the **protected scan payload** (the fields that must be bound to the attestation).
 * 2. Deterministically serialize those bytes.
 * 3. [sha256Base64Url] → `requestHash`.
 * 4. Request a Play Integrity token with that `requestHash`.
 * 5. Send **protected scan payload + integrity token** to the backend.
 *
 * The Play Integrity token itself must **not** be included in the hashed bytes — the hash is
 * computed before the token exists. The backend later re-hashes the same protected payload and
 * compares it to `requestHash` from the verified token.
 *
 * Do **not** invent the final serialization format here: the AI scan DTO / network layer does
 * not exist yet. Input must contain no secrets in plaintext — hash digests only.
 */
object IntegrityRequestHash {

    /**
     * SHA-256 of [payload], encoded as URL-safe Base64 without padding.
     * Suitable for [com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest].
     */
    fun sha256Base64Url(payload: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(payload)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}
