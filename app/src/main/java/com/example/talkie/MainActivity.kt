package com.example.talkie

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.talkie.component.Chatting
import com.example.talkie.component.Home
import com.example.talkie.component.LoginScreen
import com.example.talkie.component.NewChat
import com.example.talkie.component.Profile
import com.example.talkie.navigationbar.navBar
import com.example.talkie.ui.theme.TalkieTheme
import com.example.talkie.ui.theme.Yellow65
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TalkieTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    auth = Firebase.auth
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "/") {
                        composable("/") {
                            Flash(LocalContext.current, navController)
                        }
                        composable("/login"){
                            LoginScreen(LocalContext.current, navController)
                        }
                        composable("/home"){
                            Home(navController, LocalContext.current)
                        }
                        composable("/new"){
                            NewChat(navController, LocalContext.current)
                        }
                        composable("/profile"){
                            Profile(navController)
                        }
                        composable("/nav") {
                            navBar(navController)
                        }
                        composable("/chatting/{number}") {
                            val receiver=it.arguments?.getString("number")?:""
                            Chatting(navController, LocalContext.current, receiver)
                        }
                    }


                        //OTPVerification(LocalContext.current)
//                    val currentUser = auth.currentUser
//                    if (currentUser != null) {
//                        LoginScreen(LocalContext.current)
//                    }

                }
            }
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun Flash(current: Context, navController: NavHostController) {
        Row (
            Modifier
                .fillMaxSize()
                .background(Yellow65)){
            Column(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)) {
                Text(text = "Talkie", fontSize = 50.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
        LaunchedEffect(key1 = true){
            delay(2000)
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        navController.navigate("/nav"){
                            popUpTo("/") { inclusive=true }
                        }
                    }else{
                        navController.navigate("/login"){
                            popUpTo("/") { inclusive=true }
                        }
                    }
        }
    }

}

