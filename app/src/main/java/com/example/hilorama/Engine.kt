package com.example.hilorama

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun MainMenu(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SoftBackground),
        contentAlignment = Alignment.Center
    ){
        Core()
    }
}

