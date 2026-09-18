package com.dinyairsadot.clearledger.core.integrity

/**
 * App-level Play Integrity failures. Callers (ViewModels / UI) should map [kind] to
 * user-facing copy — do not surface Google Play / GMS exception text.
 */
sealed class PlayIntegrityException(
    val kind: Kind,
    cause: Throwable? = null,
) : Exception(kind.name, cause) {

    /** Token-provider warm-up ([PlayIntegrityClient.prepareTokenProvider]) failed. */
    class PreparationFailed(
        kind: Kind,
        cause: Throwable? = null,
    ) : PlayIntegrityException(kind, cause)

    /** Standard Integrity token request ([PlayIntegrityClient.requestIntegrityToken]) failed. */
    class TokenRequestFailed(
        kind: Kind,
        cause: Throwable? = null,
    ) : PlayIntegrityException(kind, cause)

    enum class Kind {
        /** Device offline or Play Integrity could not reach Google servers. */
        NETWORK,

        /** Play Store / Play services unavailable, outdated, or otherwise unusable. */
        PLAY_SERVICES,

        /** Client called too frequently (Play Integrity rate limits). */
        RATE_LIMITED,

        /** Cloud project number missing/invalid, API not enabled, or bad requestHash. */
        CONFIGURATION,

        /** Anything else; treat as a generic integrity failure. */
        UNKNOWN,
    }
}
