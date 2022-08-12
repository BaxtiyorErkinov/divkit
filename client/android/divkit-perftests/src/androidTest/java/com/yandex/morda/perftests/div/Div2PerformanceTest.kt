package com.yandex.morda.perftests.div

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.yandex.divkit.perftests.DIV_BENCHMARK_ACTIVITY
import com.yandex.divkit.perftests.PACKAGE_NAME
import com.yandex.divkit.perftests.divBenchmarkActivityExtras
import com.yandex.divkit.perftests.report
import com.yandex.divkit.perftests.startActivity
import com.yandex.perftests.runner.PerfTest
import com.yandex.perftests.runner.PerfTestJUnit4Runner
import com.yandex.perftests.runner.PerfTestParameter
import com.yandex.perftests.runner.PerfTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(PerfTestJUnit4Runner::class)
@PerfTest(
    packageName = PACKAGE_NAME,
    description = "Div2 performance test",
    owners = ["gulevsky"],
    defaultRepeat = 21,
    skipFirstResult = true,
    timeoutSeconds = 1200
)
class Div2PerformanceTest {

    private val utils = PerfTestUtils(PACKAGE_NAME)

    @Before
    fun setUp() {
        utils.forceStop()
    }

    @PerfTestParameter(
        requiredMetrics = [
            "Div.Parsing.JSON.Cold",
            "Div.Parsing.JSON.Warm",
            "Div.Parsing.Total.Cold",
            "Div.Parsing.Total.Warm",
            "Div.Parsing.Data.Cold",
            "Div.Parsing.Data.Warm",
            "Div.Parsing.Templates.Cold",
            "Div.Parsing.Templates.Warm",
            "Div.Context.Create.Cold",
            "Div.View.Create.Cold",
            "Div.View.Create.Warm",
            "Div.Binding.Cold",
            "Div.Binding.Warm",
            "Div.Render.Measure.Cold",
            "Div.Render.Measure.Warm",
            "Div.Render.Layout.Cold",
            "Div.Render.Layout.Warm",
            "Div.Render.Draw.Cold",
            "Div.Render.Draw.Warm",
            "Div.Render.Total.Cold",
            "Div.Render.Total.Warm",
        ]
    )
    @Test
    fun withTemplates() {
        utils.run {
            report(packageName = PACKAGE_NAME, tag = "with_templates") {
                startActivity(
                    packageName = PACKAGE_NAME,
                    activityClass = DIV_BENCHMARK_ACTIVITY,
                    extras = divBenchmarkActivityExtras(
                        assetName = "div2-perf/benchmark-with-templates.json"
                    ),
                    waitCondition = Until.findObject(By.textContains("Finished"))
                )
            }
        }
    }

    @PerfTestParameter(
        requiredMetrics = [
            "Div.Parsing.JSON.Cold",
            "Div.Parsing.JSON.Warm",
            "Div.Parsing.Total.Cold",
            "Div.Parsing.Total.Warm",
            "Div.Parsing.Data.Cold",
            "Div.Parsing.Data.Warm",
            "Div.Context.Create.Cold",
            "Div.View.Create.Cold",
            "Div.View.Create.Warm",
            "Div.Binding.Cold",
            "Div.Binding.Warm",
            "Div.Render.Measure.Cold",
            "Div.Render.Measure.Warm",
            "Div.Render.Layout.Cold",
            "Div.Render.Layout.Warm",
            "Div.Render.Draw.Cold",
            "Div.Render.Draw.Warm",
            "Div.Render.Total.Cold",
            "Div.Render.Total.Warm",
        ]
    )
    @Test
    fun withoutTemplates() {
        utils.run {
            report(packageName = PACKAGE_NAME, tag = "without_templates") {
                startActivity(
                    packageName = PACKAGE_NAME,
                    activityClass = DIV_BENCHMARK_ACTIVITY,
                    extras = divBenchmarkActivityExtras(
                        assetName = "div2-perf/benchmark-without-templates.json"
                    ),
                    waitCondition = Until.findObject(By.textContains("Finished"))
                )
            }
        }
    }
}
