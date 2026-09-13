package com.example.hilorama

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize().background(color = TextPrimary).navigationBarsPadding().displayCutoutPadding()) { _ ->
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screens.Menu.route) {
                    composable(route = Screens.Menu.route){
                        MainMenu(navController)
                    }
                    composable(route = Screens.Core.route){
                        Core(navController)
                    }
                    composable(route = Screens.Step.route){
                        StepsCore(navController)
                    }
                    composable(route = Screens.BookMark.route){
                        BookmarkMenu()
                    }
                }
            }
        }
    }
}



