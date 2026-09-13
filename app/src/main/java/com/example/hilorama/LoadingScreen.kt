package com.example.hilorama

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun LoadingScreen(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SoftBackground)
            .zIndex(4f),
        contentAlignment = Alignment.Center
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            CircularProgressIndicator()
            Text(
                text = "Loading...",
                fontFamily = Jakarta,
                fontSize = 28.sp
            )
        }
    }
}