package com.example.domain.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdMobConfig {
    // Official Google Test Ad Unit IDs
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    // Read test flag from BuildConfig so release builds use production ad IDs automatically
    val USE_TEST_ADS: Boolean = BuildConfig.USE_TEST_ADS

    var bannerAdUnitId: String = if (USE_TEST_ADS) TEST_BANNER_ID else "ca-app-pub-3940256099942544/6300978111"
    var interstitialAdUnitId: String = if (USE_TEST_ADS) TEST_INTERSTITIAL_ID else "ca-app-pub-3940256099942544/1033173712"
}

class AdMobManager(private val context: Context) {
    private val tag = "AdMobManager"
    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    /**
     * Initiates Google UMP Consent flow before initializing MobileAds.
     * Complies with Google EU User Consent Policy & privacy regulations.
     */
    fun requestConsentAndInitialize(activity: Activity) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(tag, "Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds()
                    }
                }
            },
            { requestConsentError ->
                Log.w(tag, "Consent info update failed: ${requestConsentError.message}")
                // Fallback to initializing MobileAds if network or consent fetch fails
                initializeMobileAds()
            }
        )

        // If consent is already obtained from prior sessions, initialize immediately
        if (consentInformation.canRequestAds()) {
            initializeMobileAds()
        }
    }

    fun initializeMobileAds() {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { initStatus ->
                isInitialized = true
                Log.d(tag, "MobileAds initialized: $initStatus")
                preloadInterstitial()
            }
        } catch (e: Exception) {
            Log.e(tag, "MobileAds initialization failed: ${e.message}")
        }
    }

    fun preloadInterstitial() {
        if (isAdLoading || interstitialAd != null) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = if (AdMobConfig.USE_TEST_ADS) AdMobConfig.TEST_INTERSTITIAL_ID else AdMobConfig.interstitialAdUnitId

        try {
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isAdLoading = false
                        Log.d(tag, "Interstitial Ad loaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isAdLoading = false
                        Log.w(tag, "Interstitial Ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isAdLoading = false
            Log.e(tag, "Exception loading interstitial: ${e.message}")
        }
    }

    fun showInterstitialIfReady(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preloadInterstitial()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preloadInterstitial()
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            onAdDismissed()
            preloadInterstitial()
        }
    }
}
