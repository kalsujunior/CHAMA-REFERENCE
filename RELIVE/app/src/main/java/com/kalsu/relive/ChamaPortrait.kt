package com.kalsu.relive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

/**
 * Identity-safe Chama presentation.
 *
 * The original portrait is displayed as an immutable image. No generated face,
 * mesh deformation, eye/mouth animation, face reconstruction or lip-sync is
 * permitted here. Body animation can be reintroduced only as separate layers
 * that do not touch or transform the source portrait's facial pixels.
 */
@Composable
fun ChamaPortrait(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.chama_portrait),
            contentDescription = "Portrait original de Chama — visage non modifié",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
