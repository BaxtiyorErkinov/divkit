package com.yandex.div.core.expression.variables

import androidx.annotation.MainThread
import com.yandex.div.core.Disposable
import com.yandex.div.core.dagger.DivScope
import com.yandex.div.core.expression.ExpressionsRuntimeProvider
import com.yandex.div.core.view2.Div2View
import com.yandex.div.core.view2.errors.ErrorCollectors
import com.yandex.div.data.Variable
import javax.inject.Inject

@DivScope
internal class TwoWayStringVariableBinder @Inject constructor(
    errorCollectors: ErrorCollectors,
    expressionsRuntimeProvider: ExpressionsRuntimeProvider
) : TwoWayVariableBinder<String>(errorCollectors, expressionsRuntimeProvider) {

    interface Callbacks : TwoWayVariableBinder.Callbacks<String>

    override fun String.toStringValue() = this
}

@DivScope
internal class TwoWayIntegerVariableBinder @Inject constructor(
    errorCollectors: ErrorCollectors,
    expressionsRuntimeProvider: ExpressionsRuntimeProvider
) : TwoWayVariableBinder<Int>(errorCollectors, expressionsRuntimeProvider) {

    interface Callbacks : TwoWayVariableBinder.Callbacks<Int>

    override fun Int.toStringValue() = toString()
}

internal abstract class TwoWayVariableBinder<T>(
    private val errorCollectors: ErrorCollectors,
    private val expressionsRuntimeProvider: ExpressionsRuntimeProvider
) {

    interface Callbacks<T> {
        @MainThread
        fun onVariableChanged(value: T?)
        fun setViewStateChangeListener(valueUpdater: (T) -> Unit)
    }

    fun bindVariable(divView: Div2View, variableName: String, callbacks: Callbacks<T>): Disposable {
        val data = divView.divData ?: return Disposable.NULL

        var pendingValue: T? = null
        val tag = divView.dataTag
        var variable: Variable? = null
        val variableController =
            expressionsRuntimeProvider.getOrCreate(tag, data).variableController
        callbacks.setViewStateChangeListener { value ->
            if (pendingValue == value) return@setViewStateChangeListener
            pendingValue = value
            (variable
                ?: variableController.getMutableVariable(variableName)
                    .also { variable = it })
                ?.set(value.toStringValue())
        }

        return subscribeToVariable<T>(
            variableName,
            errorCollectors.getOrCreate(tag, data),
            variableController,
            invokeChangeOnSubscription = true
        ) { value: T? ->
            if (pendingValue == value) return@subscribeToVariable
            pendingValue = value
            callbacks.onVariableChanged(value)
        }
    }

    abstract fun T.toStringValue(): String
}
