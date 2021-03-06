/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.animation.core.samples

import androidx.annotation.Sampled
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Sampled
@Composable
fun AlphaAnimationSample() {
    @Composable
    fun alphaAnimation(visible: Boolean) {
        // Animates to 1f or 0f based on [visible].
        // This [animateState] returns a State<Float> object. The value of the State object is
        // being updated by animation. (This method is overloaded for different parameter types.)
        // Here we use the returned [State] object as a property delegate.
        val alpha: Float by animateAsState(if (visible) 1f else 0f)
        Box(modifier = Modifier.background(Color.Red).alpha(alpha))
    }
}

data class MySize(val width: Dp, val height: Dp)

@Sampled
@Composable
fun ArbitraryValueTypeTransitionSample() {
    @Composable
    fun ArbitraryValueTypeAnimation(enabled: Boolean) {
        // Sets up the different animation target values based on the [enabled] flag.
        val mySize = remember(enabled) {
            if (enabled) {
                MySize(500.dp, 500.dp)
            } else {
                MySize(100.dp, 100.dp)
            }
        }

        // Animates a custom type value to the given target value, using a [TwoWayConverter]. The
        // converter tells the animation system how to convert the custom type from and to
        // [AnimationVector], so that it can be animated.
        val animSize: MySize by animateAsState<MySize, AnimationVector2D>(
            mySize,
            TwoWayConverter(
                convertToVector = { AnimationVector2D(it.width.value, it.height.value) },
                convertFromVector = { MySize(it.v1.dp, it.v2.dp) }
            )
        )
        Box(Modifier.preferredSize(animSize.width, animSize.height).background(color = Color.Red))
    }
}

@Sampled
@Composable
fun DpAnimationSample() {
    @Composable
    fun HeightAnimation(collapsed: Boolean) {
        // Animates a height of [Dp] type to different target values based on the [collapsed] flag.
        val height: Dp by animateAsState(if (collapsed) 10.dp else 20.dp)
        Box(Modifier.fillMaxWidth().height(height).background(color = Color.Red))
    }
}

@Sampled
@Composable
@Suppress("UNUSED_VARIABLE")
fun AnimateOffsetSample() {
    @Composable
    fun OffsetAnimation(selected: Boolean) {
        // Animates the offset depending on the selected flag.
        // [animateAsState] returns a State<Offset> object. The value of the State object is
        // updated by the animation. Here we use that State<Offset> as a property delegate.
        val offset: Offset by animateAsState(
            if (selected) Offset(0f, 0f) else Offset(20f, 20f)
        )

        // In this example, animateAsState returns a State<IntOffset>. The value of the returned
        // State object is updated by the animation.
        val intOffset: IntOffset by animateAsState(
            if (selected) IntOffset(0, 0) else IntOffset(50, 50)
        )
    }
}
