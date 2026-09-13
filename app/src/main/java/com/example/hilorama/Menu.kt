package com.example.hilorama

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun MainMenu(controller: NavHostController){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SoftBackground),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier.fillMaxSize().displayCutoutPadding()
        ){
            Button(
              onClick = {
                  controller.navigate(Screens.Core.route)
              }
            ){
                Text(
                    text = "CORE"
                )
            }
            Button(
                onClick = {
                    controller.navigate(Screens.Step.route)
                }
            ){
                Text(
                    text = "STEP"
                )
            }
            Button(
                onClick = {
                    controller.navigate(Screens.BookMark.route)
                }
            ){
                Text(
                    text = "BOOKMARK"
                )
            }
        }
    }
}

