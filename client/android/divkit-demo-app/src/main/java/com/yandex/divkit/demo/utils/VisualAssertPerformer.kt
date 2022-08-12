package com.yandex.divkit.demo.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnAttach
import com.yandex.div.core.util.Assert
import com.yandex.div.core.utils.UiThreadHandler
import org.jetbrains.anko.longToast
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.Queue
import java.util.WeakHashMap

/**
 * When assert happens, shows a dialog to user with options to suppress the assert
 */
class VisualAssertPerformer constructor(private val application: Application) : Assert.AssertPerformer {
    private val suppressed by lazy { application.getSharedPreferences(ASSERT_PREFERENCES, Context.MODE_PRIVATE) }
    private val activityHook = CurrentActivityHook() // we need activity to show dialog
    private val dialogInjector = AssertionsDialogInjector()

    init {
        application.registerActivityLifecycleCallbacks(activityHook)
    }

    override fun performFail(assertionError: AssertionError) {
        if (suppressed.contains(assertionError.key())) {
            softFail(assertionError)
            return
        }

        UiThreadHandler.executeOnMainThread {
            dialogInjector.add(assertionError)
        }
    }

    fun resetSuppressed() = suppressed.edit().clear().apply()

    private fun createSuppressCheckBox(activity: Activity, assertionError: AssertionError): CheckBox {
        return CheckBox(activity).apply {
            text = ASSERT_DIALOG_SUPPRESS
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    suppressed.edit().putBoolean(assertionError.key(), true).apply()
                } else {
                    suppressed.edit().remove(assertionError.key()).apply()
                }
            }
        }
    }

    private fun softFail(assertionError: AssertionError) {
        assertionError.printStackTrace()
        UiThreadHandler.executeOnMainThread {
            application.longToast(assertionError.messageAndTrace())
        }
    }

    private fun AssertionError.key() = findFirstNotAssertStackEntry().toString()

    private fun AssertionError.findFirstNotAssertStackEntry(): StackTraceElement? {
        return stackTrace.find { !it.className.contains("Assert") }
    }

    private fun AssertionError.messageAndTrace() = "$message\n${findFirstNotAssertStackEntry()}"

    private inner class CurrentActivityHook : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityDestroyed(activity: Activity) = dialogInjector.onActivityDestroyed(activity)

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

        override fun onActivityResumed(activity: Activity) = dialogInjector.injectActivity(activity)
    }


    private inner class AssertionsDialogInjector {
        private val assertionsToShow: Queue<AssertionError> = LinkedList()
        private var seenActivities = WeakHashMap<Activity, MutableList<WeakReference<AlertDialog>>>()
        private val scheduledDialogs = mutableMapOf<AssertionError, MutableList<WeakReference<AlertDialog>>>()

        fun add(a: AssertionError) {
            assertionsToShow.add(a)
            tryShowAlertDialog()
        }

        private fun tryShowAlertDialog() {
            val error = assertionsToShow.peek() ?: return

            seenActivities.keys.forEach { activity ->
                activity.scheduleAlertDialogShowing(error)?.also { shownDialog ->
                    seenActivities.getOrPut(activity) { mutableListOf() }
                        .add(WeakReference(shownDialog))
                }
            }
        }

        private fun Activity.scheduleAlertDialogShowing(assertionError: AssertionError): AlertDialog? {
            if (!this.isAttachedToWindow) {
                return null
            }
            val createSuppressCheckBox = createSuppressCheckBox(this, assertionError)
            val dialog = AlertDialog.Builder(this)
                .setTitle(ASSERT_DIALOG_TITLE)
                .setMessage(assertionError.messageAndTrace())
                .setView(createSuppressCheckBox)
                .setNegativeButton(ASSERT_DIALOG_SKIP) { _, _ -> assertionError.printStackTrace() }
                .setPositiveButton(ASSERT_DIALOG_CRASH) { _, _ -> throw assertionError }
                .setOnDismissListener { remove(assertionError) }
                .show()

            scheduledDialogs.getOrPut(assertionError) { mutableListOf() }
                .add(WeakReference(dialog))

            return dialog
        }

        private fun remove(assertionError: AssertionError) {
            assertionsToShow.remove(assertionError)
            scheduledDialogs.remove(assertionError)?.forEach {
                it.get()?.hide() // not dismiss cause it may throw "not attached to window exception"
            }
        }

        fun injectActivity(activity: Activity) {
            seenActivities.getOrPut(activity) { mutableListOf() }

            if (activity.isAttachedToWindow) {
                tryShowAlertDialog()
                return
            }

            activity.window.decorView.doOnAttach {
                tryShowAlertDialog()
            }
        }

        fun onActivityDestroyed(destroyedActivity: Activity) {
            val dialogsToDismiss = seenActivities.remove(destroyedActivity)
            dialogsToDismiss?.forEach { it.get()?.dismiss() }
        }
    }
}

private val Activity.isAttachedToWindow: Boolean
    get() = window.decorView.isAttachedToWindow

private const val ASSERT_DIALOG_TITLE = "ASSERT"
private const val ASSERT_DIALOG_CRASH = "Crash"
private const val ASSERT_DIALOG_SKIP = "Skip"
private const val ASSERT_DIALOG_SUPPRESS = "Suppress in the future"
private const val ASSERT_PREFERENCES = "assert_prefs"
