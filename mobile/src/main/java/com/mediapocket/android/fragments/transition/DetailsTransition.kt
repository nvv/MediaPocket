package com.mediapocket.android.fragments.transition

import android.os.Build
import androidx.annotation.RequiresApi
import android.transition.*

/**
 * @author Vlad Namashko
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class DetailsTransition : TransitionSet() {

    init {
        ordering = ORDERING_TOGETHER
        addTransition(ChangeBounds()).
                addTransition(ChangeTransform()).
                addTransition(ChangeImageTransform())
    }
}