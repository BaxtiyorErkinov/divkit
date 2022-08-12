package com.yandex.div.view.pooling

import android.view.View
import com.yandex.div.util.getOrThrow
import java.util.concurrent.ConcurrentHashMap

class PseudoViewPool : ViewPool {

    private val factoryMap = ConcurrentHashMap<String, ViewFactory<out View>>()

    override fun <T : View> register(tag: String, factory: ViewFactory<T>, capacity: Int) {
        factoryMap[tag] = factory
    }

    override fun unregister(tag: String) {
        factoryMap.remove(tag)
    }

    override fun <T : View> obtain(tag: String): T {
        @Suppress("UNCHECKED_CAST")
        return factoryMap.getOrThrow(tag).createView() as T
    }
}
