package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class AssistantAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instanceRef = WeakReference(this)
        _isServiceBound.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val pkg = it.packageName?.toString() ?: ""
            if (pkg.isNotBlank()) {
                _currentPackageName.value = pkg
                com.example.ui.glass.DynamicIslandImpressionController.onForegroundPackageChanged(pkg)
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupt handler
    }

    override fun onDestroy() {
        super.onDestroy()
        instanceRef = null
        _isServiceBound.value = false
    }

    // --- Screen Control & Interaction Helper Functions ---

    /**
     * Traverses current screen window hierarchy and collects all visible text & descriptions.
     */
    fun readScreenText(): String {
        val rootNode = rootInActiveWindow ?: return "Screen content unavailable or empty."
        val textList = mutableListOf<String>()
        collectNodeText(rootNode, textList)
        rootNode.recycle()
        return if (textList.isEmpty()) {
            "No readable text found on current screen."
        } else {
            textList.joinToString("\n")
        }
    }

    private fun collectNodeText(node: AccessibilityNodeInfo?, result: MutableList<String>) {
        if (node == null || !node.isVisibleToUser) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrEmpty()) {
            result.add(text)
        } else if (!desc.isNullOrEmpty()) {
            result.add(desc)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            collectNodeText(child, result)
            child?.recycle()
        }
    }

    /**
     * Find element matching target text and perform click on it or its clickable parent.
     */
    fun findAndClickText(targetText: String, exactMatch: Boolean = false): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val foundNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodesByText(rootNode, targetText, exactMatch, foundNodes)

        for (node in foundNodes) {
            if (performClickOnNodeOrParent(node)) {
                rootNode.recycle()
                return true
            }
        }
        rootNode.recycle()
        return false
    }

    private fun searchNodesByText(
        node: AccessibilityNodeInfo?,
        target: String,
        exactMatch: Boolean,
        result: MutableList<AccessibilityNodeInfo>
    ) {
        if (node == null) return

        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""

        val isMatch = if (exactMatch) {
            text.equals(target, ignoreCase = true) || desc.equals(target, ignoreCase = true)
        } else {
            text.contains(target, ignoreCase = true) || desc.contains(target, ignoreCase = true)
        }

        if (isMatch) {
            result.add(node)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            searchNodesByText(child, target, exactMatch, result)
        }
    }

    private fun performClickOnNodeOrParent(node: AccessibilityNodeInfo?): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }
        return false
    }

    /**
     * Perform tap at specific screen coordinates (x, y).
     */
    fun tapAtCoordinates(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onComplete?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                onComplete?.invoke(false)
            }
        }, null)
    }

    /**
     * Scroll active window forward or backward.
     */
    fun scrollScreen(forward: Boolean): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        val result = rootNode.performAction(action)
        rootNode.recycle()
        return result
    }

    /**
     * Set text on currently focused input field.
     */
    fun typeTextIntoFocusedField(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false

        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        focusedNode.recycle()
        rootNode.recycle()
        return success
    }

    // Global Actions
    fun navigateBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun navigateHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun openRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    fun openQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    fun toggleSplitScreen(): Boolean = performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)

    fun launchSplitScreenWithApps(primaryPackage: String, secondaryPackage: String, context: Context): Boolean {
        val pm = context.packageManager
        val primaryIntent = pm.getLaunchIntentForPackage(primaryPackage)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val secondaryIntent = pm.getLaunchIntentForPackage(secondaryPackage)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        }

        if (primaryIntent != null) {
            context.startActivity(primaryIntent)
        }

        val toggled = toggleSplitScreen()

        if (secondaryIntent != null) {
            context.startActivity(secondaryIntent)
        }
        return toggled
    }

    fun lockScreen(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
    }

    fun findAndClickAnyText(targetTexts: List<String>): Boolean {
        for (target in targetTexts) {
            if (findAndClickText(target, exactMatch = false)) {
                return true
            }
        }
        return false
    }

    fun takeScreenshot(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            false
        }
    }

    companion object {
        private var instanceRef: WeakReference<AssistantAccessibilityService>? = null
        private val _isServiceBound = MutableStateFlow(false)
        val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

        private val _currentPackageName = MutableStateFlow("")
        val currentPackageName: StateFlow<String> = _currentPackageName.asStateFlow()

        fun getInstance(): AssistantAccessibilityService? = instanceRef?.get()

        /**
         * Checks if the Accessibility Service is enabled in system settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedService = "${context.packageName}/${AssistantAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            ) == 1

            return accessibilityEnabled && enabledServices.contains(expectedService, ignoreCase = true)
        }

        /**
         * Intent to open system Accessibility settings page.
         */
        fun openAccessibilitySettingsIntent(): Intent {
            return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
