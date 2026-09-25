package com.example.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdMobManager {
    private const val TAG = "AdMobManager"

    // Configured AdMob credentials
    const val CONFIGURED_APP_ID = "ca-app-pub-8757057682803457~4510077253"
    const val DEFAULT_USER_AD_UNIT_ID = "ca-app-pub-8757057682803457/8417707683"

    // Google AdMob standard test ad unit IDs (for development safety)
    const val DEFAULT_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val DEFAULT_TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private const val PREFS_NAME = "admob_settings"
    private const val KEY_CUSTOM_BANNER_ID = "custom_banner_id"
    private const val KEY_CUSTOM_REWARDED_ID = "custom_rewarded_id"
    private const val KEY_USE_TEST_ADS = "use_test_ads"
    private const val KEY_REWARD_CREDITS = "reward_credits"
    private const val KEY_REWARDED_WATCHED = "rewarded_watched_count"

    private var isInitialized = false
    private var rewardedAd: RewardedAd? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    private val _rewardCredits = MutableStateFlow(3) // Start with 3 free video export passes
    val rewardCredits: StateFlow<Int> = _rewardCredits.asStateFlow()

    private val _rewardedWatchedCount = MutableStateFlow(0)
    val rewardedWatchedCount: StateFlow<Int> = _rewardedWatchedCount.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized with status: $status")
            }
            isInitialized = true

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _rewardCredits.value = prefs.getInt(KEY_REWARD_CREDITS, 3)
            _rewardedWatchedCount.value = prefs.getInt(KEY_REWARDED_WATCHED, 0)

            loadRewardedAd(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun normalizeAdUnitId(rawId: String): String {
        val trimmed = rawId.trim()
        if (trimmed.isEmpty()) return DEFAULT_USER_AD_UNIT_ID
        // In case an AdMob App ID format (with ~) was entered as an ad unit, convert ~ to /
        return trimmed.replace("~", "/")
    }

    fun getBannerAdUnitId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useTestAds = prefs.getBoolean(KEY_USE_TEST_ADS, true)
        val customId = prefs.getString(KEY_CUSTOM_BANNER_ID, DEFAULT_USER_AD_UNIT_ID) ?: DEFAULT_USER_AD_UNIT_ID
        val normalized = normalizeAdUnitId(customId)
        return if (!useTestAds && normalized.isNotBlank()) normalized else DEFAULT_TEST_BANNER_ID
    }

    fun getRewardedAdUnitId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val useTestAds = prefs.getBoolean(KEY_USE_TEST_ADS, true)
        val customId = prefs.getString(KEY_CUSTOM_REWARDED_ID, DEFAULT_USER_AD_UNIT_ID) ?: DEFAULT_USER_AD_UNIT_ID
        val normalized = normalizeAdUnitId(customId)
        return if (!useTestAds && normalized.isNotBlank()) normalized else DEFAULT_TEST_REWARDED_ID
    }

    fun saveAdMobConfig(
        context: Context,
        customBannerId: String,
        customRewardedId: String,
        useTestAds: Boolean
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CUSTOM_BANNER_ID, normalizeAdUnitId(customBannerId))
            .putString(KEY_CUSTOM_REWARDED_ID, normalizeAdUnitId(customRewardedId))
            .putBoolean(KEY_USE_TEST_ADS, useTestAds)
            .apply()

        // Reload ad with new unit id
        loadRewardedAd(context)
    }

    fun isUsingTestAds(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_TEST_ADS, true)
    }

    fun getSavedCustomBannerId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_BANNER_ID, DEFAULT_USER_AD_UNIT_ID) ?: DEFAULT_USER_AD_UNIT_ID
    }

    fun getSavedCustomRewardedId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_REWARDED_ID, DEFAULT_USER_AD_UNIT_ID) ?: DEFAULT_USER_AD_UNIT_ID
    }

    fun loadRewardedAd(context: Context) {
        if (_isAdLoading.value || _isRewardedAdLoaded.value) return
        _isAdLoading.value = true

        val adUnitId = getRewardedAdUnitId(context)
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "RewardedAd successfully loaded")
                    rewardedAd = ad
                    _isRewardedAdLoaded.value = true
                    _isAdLoading.value = false

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            _isRewardedAdLoaded.value = false
                            // Preload next rewarded ad
                            loadRewardedAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e(TAG, "Ad failed to show: ${adError.message}")
                            rewardedAd = null
                            _isRewardedAdLoaded.value = false
                            loadRewardedAd(context)
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    _isAdLoading.value = false
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int) -> Unit,
        onAdUnavailable: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                val amount = rewardItem.amount.coerceAtLeast(1)
                addCredits(activity, amount)
                onRewardEarned(amount)
            }
        } else {
            // If ad is not ready (e.g. offline/testing), allow fallback reward so user experience is not blocked
            onAdUnavailable()
        }
    }

    fun consumeCredit(context: Context): Boolean {
        if (_rewardCredits.value > 0) {
            _rewardCredits.value -= 1
            saveCredits(context)
            return true
        }
        return false
    }

    fun addCredits(context: Context, amount: Int) {
        _rewardCredits.value += amount
        _rewardedWatchedCount.value += 1
        saveCredits(context)
    }

    private fun saveCredits(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_REWARD_CREDITS, _rewardCredits.value)
            .putInt(KEY_REWARDED_WATCHED, _rewardedWatchedCount.value)
            .apply()
    }
}
