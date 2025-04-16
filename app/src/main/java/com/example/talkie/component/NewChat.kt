package com.example.talkie.component

import android.content.Context
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import com.example.talkie.datamodels.Contact
import com.example.talkie.ui.theme.Yellow65
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NewChat(navController: NavHostController, current: Context) {
    var Receiver by remember {
        mutableStateOf("")
    }
    var Name by remember {
        mutableStateOf("")
    }
    var loading by remember {
        mutableStateOf(false)
    }
    val db=FirebaseFirestore.getInstance()

    Row(
        Modifier
            .fillMaxSize()
            .padding(10.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.CenterVertically)
                .background(color = Color.LightGray, RoundedCornerShape(10.dp))) {
            Spacer(modifier = Modifier.height(40.dp))
            if (loading==false){
                OutlinedTextField(value = Receiver, onValueChange ={ Receiver=it }, Modifier.align(Alignment.CenterHorizontally))
                Row (
                    Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 50.dp, vertical = 10.dp)){
                    TextButton(onClick = {
                        if (Receiver!=""){
                            loading=true
                            db.collection("users").whereEqualTo("number", Receiver).get().addOnSuccessListener {
                                if (it.isEmpty){
                                    Toast.makeText(current, "No user exist.", Toast.LENGTH_LONG).show()
                                }else{
                                    Name=it.documents[0].get("name").toString()
                                    val uId = Firebase.auth.currentUser?.uid.toString()
                                    db.collection("users").document(uId).get().addOnSuccessListener {// check user exist or not
                                        if (it.exists()){
                                            db.collection("users").document(uId).collection("contacts")
                                                .document(Receiver).set(Contact(Receiver, "Hello", Name)).addOnSuccessListener {
                                                    navController.navigate("/chatting/$Receiver")
                                                    loading=false
                                                }
                                        }else {
                                            // Handle case where current user data doesn't exist
                                            Toast.makeText(current, "Current user data not found.", Toast.LENGTH_LONG).show()
                                        }
                                    }.addOnFailureListener {
                                        loading=false
                                    }
                                }
                            }.addOnFailureListener {
                                Toast.makeText(current, "Try again.", Toast.LENGTH_SHORT).show()
                                loading=false
                            }
                        }else{
                            Toast.makeText(current, "Enter a valid number.", Toast.LENGTH_LONG).show()
                        }
                    }, modifier = Modifier
                        .background(Yellow65, RoundedCornerShape(20.dp))
                        .padding(horizontal = 5.dp)) {
                        Text(text = "Chat now", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 16.sp )
                    }
                }
            }else{
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}