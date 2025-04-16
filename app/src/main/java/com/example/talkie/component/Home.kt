package com.example.talkie.component

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.talkie.R
import com.example.talkie.datamodels.Contact
import com.example.talkie.ui.theme.Yellow65
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Home(navController: NavHostController, current: Context) {
    var contacts by remember {
        mutableStateOf(mutableListOf<Contact>())
    }
    var loading by remember {
        mutableStateOf(true)
    }

    val db = FirebaseFirestore.getInstance()
    val uId= Firebase.auth.currentUser?.uid.toString()
    LaunchedEffect(key1 = 1) {
        db.collection("users").document(uId).collection("contacts").get().addOnSuccessListener {
            if (!it.isEmpty){
                for (e in it.documents){
                    e.toObject(Contact::class.java)?.let { it1 -> contacts.add(it1) }
                }
                loading=false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row (
            Modifier
                .fillMaxWidth()
                .padding(15.dp)){
            Text(text = "Talkie", fontWeight = FontWeight.Bold, fontSize = 30.sp, fontFamily = FontFamily.Monospace)
        }
        LazyColumn (Modifier.fillMaxWidth()){
            if (loading){
                item {
                    Column (Modifier.fillMaxWidth()){
                        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }else{
                items(contacts){
                    AccBox(it,navController)
                }
//                Toast.makeText(current, "$contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun AccBox(C: Contact, navController: NavHostController) {
    var dp by remember {
        mutableStateOf("")
    }
    var lastMsg by remember {
        mutableStateOf(C.lastMsg.toString())
    }
    var name by remember {
        mutableStateOf(C.name.toString())
    }
    var new by remember {
        mutableStateOf(C.new)
    }
    if (lastMsg.length>30){
        lastMsg=lastMsg.substring(0, 30)+"..."
    }
    val db=FirebaseFirestore.getInstance()
    db.collection("users").whereEqualTo("number", C.number).get().addOnSuccessListener {
        if (!it.isEmpty){
            dp=it.documents[0].get("dp").toString()
            name=it.documents[0].get("name").toString()
        }
    }
    Row (
        Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .clickable {
                navController.navigate("/chatting/${C.number}")
            }){
        Column (Modifier.align(Alignment.CenterVertically)){
            if (dp=="default"){
                Image(painter = painterResource(id = R.drawable.user), contentDescription ="",
                    Modifier
                        .width(45.dp)
                        .align(Alignment.CenterHorizontally))
            }else{
                AsyncImage(model = dp, contentDescription = "",
                    Modifier
                        .width(45.dp)
                        .align(Alignment.CenterHorizontally))
            }
        }
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (new==true){
                Text(text = lastMsg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }else{
                Text(text = lastMsg, fontSize = 14.sp, fontWeight = FontWeight.Normal)
            }
        }
        Column (
            Modifier
                .fillMaxWidth()
                .align(Alignment.Top)){
            if (new==true){
                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier
                    .width(10.dp)
                    .height(10.dp)
                    .align(Alignment.End)
                    .background(color = Yellow65, shape = RoundedCornerShape(5.dp))) {

                }
            }
        }
    }
}