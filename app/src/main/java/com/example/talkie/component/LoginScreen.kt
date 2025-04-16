package com.example.talkie.component

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.talkie.datamodels.ProfileData
import com.example.talkie.ui.theme.Yellow65
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(current: Context, navController: NavHostController) {

    var loading by remember {
        mutableStateOf(false)
    }
    var UName by remember {
        mutableStateOf("")
    }
    var Pass by remember {
        mutableStateOf("")
    }
    var Email by remember {
        mutableStateOf("")
    }
    var PNo by remember {
        mutableStateOf("")
    }

    Row(Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth()
            .align(Alignment.CenterVertically)) {
            if (loading==false){
                OutlinedTextField(value = PNo, onValueChange ={PNo=it},modifier = Modifier.align(Alignment.CenterHorizontally), label = { Text(text = "Phone Number")}, shape = RoundedCornerShape(8.dp) )
                OutlinedTextField(value = UName, onValueChange ={UName=it},modifier = Modifier.align(Alignment.CenterHorizontally), label = { Text(text = "User Name")}, shape = RoundedCornerShape(8.dp) )
                OutlinedTextField(value = Email, onValueChange ={Email=it},modifier = Modifier.align(Alignment.CenterHorizontally), label = { Text(text = "E-mail")}, shape = RoundedCornerShape(8.dp) )
                OutlinedTextField(value = Pass, onValueChange ={Pass=it},modifier = Modifier.align(Alignment.CenterHorizontally), label = { Text(text = "Password")}, shape = RoundedCornerShape(8.dp) )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {
                    if (Email!="" && Pass!=""){
                        loading=true
                        Firebase.auth.createUserWithEmailAndPassword(Email, Pass).addOnCompleteListener{ task ->

                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                Login(Email, Pass, PNo, UName, current, navController, true)
                            } else {
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Login(Email, Pass, PNo, UName, current, navController,false)
                            }
                            loading=false
                        }
                    }else{
                        Toast.makeText(current, "Give All information.", Toast.LENGTH_SHORT).show()
                    }


                }, colors = ButtonDefaults.buttonColors(Yellow65),modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(text = "Login", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }else{
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
fun Login(
    Email: String,
    Pass: String,
    Phone: String,
    UName: String,
    current: Context,
    navController: NavHostController,
    s: Boolean,
) {
    Firebase.auth.signInWithEmailAndPassword(Email, Pass).addOnCompleteListener {task ->
        if (task.isSuccessful) {
            // Sign in success, update UI with the signed-in user's information
            //Log.d(TAG, "signInWithEmail:success")
            val user = Firebase.auth.currentUser
            //navController.navigate("/nav")
            if (s==true){
                storeUserDataToFirestore(user, Phone, UName, navController, current)
            }else{
                Toast.makeText(current,"You already have an account.", Toast.LENGTH_SHORT,).show()
                navController.navigate("/nav")
            }
            //Toast.makeText(current,"Signin ${user}.", Toast.LENGTH_SHORT,).show()
            //updateUI(user)
        } else {
            // If sign in fails, display a message to the user.
            //Log.w(TAG, "signInWithEmail:failure", task.exception)
            Toast.makeText(current,"Authentication failed.", Toast.LENGTH_SHORT,).show()
            // updateUI(null)
        }
    }
}
fun storeUserDataToFirestore(
    user: FirebaseUser?,
    Phone: String,
    UName: String,
    navController: NavHostController,
    current: Context,
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(user!!.uid)
    Toast.makeText(current,"It take sometime...", Toast.LENGTH_SHORT).show()
//    val userData = hashMapOf(
//        "email" to user!!.email,
//        "Phone" to Phone,
//        "Name" to UName,
//        // Add other user data as needed
//    )

    userRef.set(ProfileData(UName,Phone,user.email,"",))
        .addOnSuccessListener {
                navController.navigate("/nav")
        }
        .addOnFailureListener {

        }
}
