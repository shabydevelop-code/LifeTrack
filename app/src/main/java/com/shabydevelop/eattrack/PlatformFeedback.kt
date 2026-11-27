package com.shabydevelop.eattrack

import android.content.Context
import android.view.View
import android.view.HapticFeedbackConstants
import android.os.Build
import android.animation.ValueAnimator
import android.provider.Settings

internal object PlatformFeedback {
    fun success(view: View) { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
    fun animationsEnabled(context: Context): Boolean =
        (Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()) &&
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
}
