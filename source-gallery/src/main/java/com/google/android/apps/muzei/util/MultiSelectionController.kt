/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.muzei.util

import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import java.util.HashSet

/**
 * Utilities for storing multiple selection information in collection views.
 */
class MultiSelectionController(
        savedStateRegistryOwner: SavedStateRegistryOwner
) : DefaultLifecycleObserver, SavedStateRegistry.SavedStateProvider, OnBackPressedCallback(false) {

    companion object {
        private const val STATE_SELECTION = "selection"
    }

    private val lifecycle = savedStateRegistryOwner.lifecycle
    private val savedStateRegistry = savedStateRegistryOwner.savedStateRegistry

    val selection = HashSet<Long>()
    var callbacks: Callbacks? = null

    val selectedCount: Int
        get() = selection.size

    init {
        lifecycle.addObserver(this)
        savedStateRegistry.registerSavedStateProvider(STATE_SELECTION, this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        savedStateRegistry.consumeRestoredStateForKey(STATE_SELECTION)?.run {
            selection.clear()
            val savedSelection = getLongArray(STATE_SELECTION)
            if (savedSelection?.isNotEmpty() == true) {
                for (item in savedSelection) {
                    selection.add(item)
                }
            }
        }

        isEnabled = selectedCount > 0
        callbacks?.onSelectionChanged(restored = true, fromUser = false)
    }

    override fun handleOnBackPressed() {
        reset(true)
    }

    override fun saveState() = bundleOf(STATE_SELECTION to selection.toLongArray())

    fun toggle(item: Long, fromUser: Boolean) {
        if (selection.contains(item)) {
            selection.remove(item)
        } else {
            selection.add(item)
        }

        isEnabled = selectedCount > 0
        callbacks?.onSelectionChanged(false, fromUser)
    }

    fun reset(fromUser: Boolean) {
        selection.clear()
        isEnabled = false
        callbacks?.onSelectionChanged(false, fromUser)
    }

    fun isSelected(item: Long): Boolean {
        return selection.contains(item)
    }

    fun interface Callbacks {
        fun onSelectionChanged(restored: Boolean, fromUser: Boolean)
    }
}