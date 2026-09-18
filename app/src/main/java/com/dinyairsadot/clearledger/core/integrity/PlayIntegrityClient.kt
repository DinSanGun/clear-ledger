package com.dinyairsadot.clearledger.core.integrity

import android.content.Context
import com.dinyairsadot.clearledger.BuildConfig
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException


/**
 * Thin wrapper around Play Integrity **Standard** API.
 *
 * This stage only prepares (warms up) a [StandardIntegrityTokenProvider] and keeps it in
 * memory. Token requests and `requestHash` binding are intentionally not implemented yet —
 * add a coroutine-friendly `requestIntegrityToken(requestHash: String)` beside
 * [prepareTokenProvider] when the AI invoice scan / backend path lands.
 *
 * Not wired into UI or product flows — construct when a later stage needs it.
 *
 * Never log integrity tokens or other sensitive attestation payloads.
 */
class PlayIntegrityClient(
    context: Context,
    private val cloudProjectNumber: Long = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER,
) {
    private val applicationContext = context.applicationContext
    private val standardIntegrityManager: StandardIntegrityManager =
        IntegrityManagerFactory.createStandard(applicationContext)

    private val prepareMutex = Mutex()

    @Volatile
    private var tokenProvider: StandardIntegrityTokenProvider? = null

    /**
     * Prepares (or returns the already-prepared) Standard Integrity token provider.
     *
     * Safe to call repeatedly: concurrent callers share one in-flight prepare, and a
     * successful provider is reused until the process dies or [invalidatePreparedProvider]
     * is used (e.g. after `INTEGRITY_TOKEN_PROVIDER_INVALID` in a later stage).
     *
     * @throws PlayIntegrityException.PreparationFailed on warm-up failure
     */
    suspend fun prepareTokenProvider(): StandardIntegrityTokenProvider {
        tokenProvider?.let { return it }
        return prepareMutex.withLock {
            tokenProvider?.let { return it }
            if (cloudProjectNumber == 0L) {
                throw PlayIntegrityException.PreparationFailed(
                    PlayIntegrityException.Kind.CONFIGURATION,
                )
            }
            try {
                val request = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(cloudProjectNumber)
                    .build()
                val provider = standardIntegrityManager.prepareIntegrityToken(request).await()
                tokenProvider = provider
                provider
            } catch (e: PlayIntegrityException.PreparationFailed) {
                throw e
            } catch (e: PlayIntegrityException.PreparationFailed) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                throw PlayIntegrityException.PreparationFailed(mapFailureKind(e), e)
            }
        }
    }

    /**
     * Returns the in-memory provider if [prepareTokenProvider] already succeeded in this
     * process; otherwise `null`. Does not trigger network / prepare.
     */
    fun peekPreparedProvider(): StandardIntegrityTokenProvider? = tokenProvider

    /**
     * Drops the cached provider so the next [prepareTokenProvider] call warms up again.
     * Reserved for later stages (e.g. provider expiry).
     */
    fun invalidatePreparedProvider() {
        tokenProvider = null
    }

    private fun mapFailureKind(throwable: Throwable): PlayIntegrityException.Kind {
        val root = findStandardIntegrityException(throwable) ?: unwrapRoot(throwable)
        if (root is IOException) {
            return PlayIntegrityException.Kind.NETWORK
        }
        val integrity = root as? StandardIntegrityException
            ?: return PlayIntegrityException.Kind.UNKNOWN
        return when (integrity.errorCode) {
            StandardIntegrityErrorCode.NETWORK_ERROR,
            StandardIntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE,
            -> PlayIntegrityException.Kind.NETWORK

            StandardIntegrityErrorCode.PLAY_SERVICES_NOT_FOUND,
            StandardIntegrityErrorCode.PLAY_SERVICES_VERSION_OUTDATED,
            StandardIntegrityErrorCode.PLAY_STORE_NOT_FOUND,
            StandardIntegrityErrorCode.PLAY_STORE_VERSION_OUTDATED,
            StandardIntegrityErrorCode.CANNOT_BIND_TO_SERVICE,
            StandardIntegrityErrorCode.CLIENT_TRANSIENT_ERROR,
            -> PlayIntegrityException.Kind.PLAY_SERVICES

            StandardIntegrityErrorCode.TOO_MANY_REQUESTS,
            -> PlayIntegrityException.Kind.RATE_LIMITED

            StandardIntegrityErrorCode.CLOUD_PROJECT_NUMBER_IS_INVALID,
            StandardIntegrityErrorCode.APP_NOT_INSTALLED,
            StandardIntegrityErrorCode.APP_UID_MISMATCH,
            StandardIntegrityErrorCode.API_NOT_AVAILABLE,
            -> PlayIntegrityException.Kind.CONFIGURATION

            else -> PlayIntegrityException.Kind.UNKNOWN
        }
    }

    private fun findStandardIntegrityException(throwable: Throwable): StandardIntegrityException? {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is StandardIntegrityException) return current
            current = current.cause
        }
        return null
    }

    private fun unwrapRoot(throwable: Throwable): Throwable {
        var current = throwable
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }

    private suspend fun <T> Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        }
}
