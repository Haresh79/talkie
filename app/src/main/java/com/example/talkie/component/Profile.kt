package com.example.talkie.component

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.talkie.R
import com.example.talkie.datamodels.ProfileData
import com.example.talkie.services.UserProfile
import com.example.talkie.ui.theme.Yellow65
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(navController: NavHostController) {
    var UserName by remember {
        mutableStateOf("")
    }
    var Email by remember {
        mutableStateOf("unknown")
    }
    var Number by remember {
        mutableStateOf("")
    }
    var Pic by remember {
        mutableStateOf("default")
    }
    var dialog by remember {
        mutableStateOf(false)
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            selectedImageUri=uri
        }
    }

    val db=FirebaseFirestore.getInstance()
    //val obj=UserProfile()
    val uId=Firebase.auth.currentUser?.uid.toString()
//    obj.getProfile(uId){user->
//        if (user!=null){
//            UserName= user.toString()
//        }
//    }
    GlobalScope.launch {
        db.collection("users").document(uId).get().addOnSuccessListener {
            if (it.exists()){
                val user=it.toObject(ProfileData::class.java)
                UserName=user?.name.toString()
                Email=user?.mail.toString()
                Number=user?.number.toString()
                Pic=user?.dp.toString()
            }
        }
    }

    if (Email!="unknown"){
        Row (modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)) {
                Column(Modifier.fillMaxWidth()){
                    BadgedBox(badge = { Badge(containerColor = Color.Transparent){
                        Icon(imageVector = Icons.Filled.Create, contentDescription ="", modifier = Modifier.clickable {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            val uuid=UUID.randomUUID()
                            selectedImageUri?.let {
                                Firebase.storage.reference.child("images/$uuid").putFile(
                                    it
                                ).addOnSuccessListener {
                                    Pic= it.metadata?.reference?.downloadUrl.toString()
                                    db.collection("users").document(uId).update("dp", Pic)
                                }
                            }

                        })
                    } },Modifier.align(Alignment.CenterHorizontally)) {
                        if (Pic=="default" || Pic==null){
                            Image(painter = painterResource(id= R.drawable.user), contentDescription ="IMG", modifier = Modifier.width(100.dp))
                        }else{
                            AsyncImage(model = Pic, contentDescription = "IMG",  modifier = Modifier.width(100.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(text = "+91 ${Number}", Modifier.align(Alignment.CenterHorizontally))
                    Text(text = "${Email}", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.align(Alignment.CenterHorizontally)) {
                        Text(text = "Name:", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "@${UserName}", color = Color.Gray)
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(imageVector = Icons.Filled.Create, contentDescription ="", modifier = Modifier.clickable {
                            dialog=true
                        })
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                    Button(onClick = {
                        Firebase.auth.signOut()
                        navController.navigate("/")
                    }, colors = ButtonDefaults.buttonColors(Yellow65), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(text = "Logout", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (dialog==true){
            var usrnm by remember {
                mutableStateOf(UserName)
            }
            Dialog(onDismissRequest = { dialog=false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                   Column (
                       Modifier
                           .padding(20.dp)
                           .align(Alignment.CenterHorizontally)){
                       OutlinedTextField(value = usrnm, onValueChange = {usrnm=it})
                       TextButton(
                           onClick = { db.collection("users").document(uId).update("name", usrnm)
                                        UserName=usrnm
                                        dialog=false
                                     },
                           modifier = Modifier
                               .padding(8.dp)
                               .align(Alignment.End),
                       ) {
                           Text(text = "Save")
                       }
                   }
                }
            }
        }
    }else{
        Row (Modifier.fillMaxSize()){
            Column (
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)){
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }



}