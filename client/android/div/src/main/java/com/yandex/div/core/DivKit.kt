package com.yandex.div.core

import android.content.Context
import androidx.annotation.AnyThread
import com.yandex.div.core.annotations.PublicApi
import com.yandex.android.beacon.SendBeaconManager
import com.yandex.div.core.dagger.DaggerDivKitComponent
import com.yandex.div.core.dagger.DivKitComponent
import com.yandex.div.core.util.Assert
import com.yandex.div.histogram.DivParsingHistogramReporter

@PublicApi
class DivKit private constructor(
    context: Context,
    configuration: DivKitConfiguration
) {

    internal val component: DivKitComponent = DaggerDivKitComponent.builder()
        .applicationContext(context.applicationContext)
        .configuration(configuration)
        .build()

    val sendBeaconManager: SendBeaconManager?
        get() = component.sendBeaconManager

    val parsingHistogramReporter: DivParsingHistogramReporter
        get() = component.parsingHistogramReporter

    companion object {

        private val DEFAULT_CONFIGURATION = DivKitConfiguration.Builder().build()

        private var configuration: DivKitConfiguration? = null
        @Volatile
        private var instance: DivKit? = null

        @JvmStatic
        @AnyThread
        fun configure(configuration: DivKitConfiguration) {
            synchronized(this) {
                if (this.configuration == null) {
                    this.configuration = configuration
                } else {
                    Assert.fail("DivKit already configured")
                }
            }
        }

        @JvmStatic
        @AnyThread
        fun getInstance(context: Context): DivKit {
            instance?.let {
                return it
            }

            synchronized(this) {
                instance?.let {
                    return it
                }

                val divKit = DivKit(context, configuration ?: DEFAULT_CONFIGURATION)
                return divKit.also { instance = it }
            }
        }
    }
}
