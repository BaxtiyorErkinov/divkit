@file:JvmName("ScreenshotTestRules")

package com.yandex.div

import com.yandex.divkit.demo.Container
import com.yandex.test.idling.waitForIdlingResource
import com.yandex.test.rules.ClosePopupsRule
import com.yandex.test.rules.LogcatReportRule
import com.yandex.test.rules.NoAnimationsRule
import com.yandex.test.rules.WindowHierarchyRule
import com.yandex.test.screenshot.ScreenshotRule
import com.yandex.test.screenshot.WaitScreenshotActivityRule
import com.yandex.test.util.chain
import org.junit.rules.TestRule
import ru.tinkoff.allure.android.FailshotRule

fun screenshotRule(
    relativePath: String = "",
    name: String = "",
    key: String = "",
    casePath: String = "",
    skipScreenshotCapture: Boolean = false,
    innerRule: () -> TestRule,
): TestRule {
    return FailshotRule()
        .chain(LogcatReportRule(reportOnSuccess = true))
        .chain(NoAnimationsRule())
        .chain(ClosePopupsRule())
        .chain(innerRule())
        .chain(WindowHierarchyRule(reportOnSuccess = true))
        .chain(
            if (skipScreenshotCapture) {
                WaitScreenshotActivityRule(caseKey = key)
            } else {
                ScreenshotRule(relativePath, name, casePath).apply {
                    beforeScreenshotTaken {
                        try {
                            waitForIdlingResource(ImageLoadingIdlingResource(Container.imageLoader))
                        } catch (e: Exception) {
                            Container.imageLoader.resetIdle()
                            throw e
                        }
                    }
                }
            }
        )
}
