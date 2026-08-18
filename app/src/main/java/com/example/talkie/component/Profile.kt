package com.example.talkie.component

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
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
import androidx.compose.runtime.LaunchedEffect

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(navController: NavHostController) {
    val context = LocalContext.current
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

    val db=FirebaseFirestore.getInstance()
    val uId = Firebase.auth.currentUser?.uid.toString()

    fun uploadToCloudinary(uri: Uri) {
        try {
            MediaManager.get()
        } catch (e: Exception) {
            // Initialize Cloudinary with your configuration
            // Replace these with your actual credentials
            val config = mapOf(
                "cloud_name" to "dvq9o0i8b",
                "api_key" to "623646212227694",
                "api_secret" to "h_qpuF8fKqXogZczEfqgUU_8UF8"
            )
            MediaManager.init(context, config)
        }

        MediaManager.get().upload(uri).callback(object : UploadCallback {
            override fun onStart(requestId: String) {}
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val url = resultData["secure_url"] as? String
                if (url != null) {
                    Pic = url
                    db.collection("users").document(uId).update("dp", url)
                }
            }
            override fun onError(requestId: String, error: ErrorInfo) {
                Log.e("Cloudinary", "Upload failed: ${error.description}")
            }
            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uploadToCloudinary(uri)
        }
    }
    
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black

    LaunchedEffect(uId) {
        db.collection("users").document(uId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(ProfileData::class.java)
                Log.d("ProfileFetch", "Data received: $user")
                if (user != null) {
                    UserName = user.name ?: ""
                    Email = user.mail ?: ""
                    Number = user.number ?: ""
                    Pic = user.dp ?: "default"
                }
            } else {
                Log.d("ProfileFetch", "No such document for uId: $uId")
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileFetch", "Error getting document: ", exception)
        }
    }

    if (Email!="unknown"){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.height(30.dp))
            Row (modifier = Modifier.fillMaxSize()){
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)) {
                Column(Modifier.fillMaxWidth()){
                    BadgedBox(badge = { Badge(containerColor = Color.Transparent){
                        Icon(imageVector = Icons.Filled.Create, contentDescription ="", modifier = Modifier.clickable {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        })
                    } },Modifier.align(Alignment.CenterHorizontally)) {
                        if (Pic=="default" || Pic==null){
                            Image(painter = painterResource(id= R.drawable.user), contentDescription ="IMG", modifier = Modifier.width(100.dp).height(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        }else{
                            AsyncImage(model = Pic, contentDescription = "IMG",  modifier = Modifier.width(100.dp).height(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
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
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                   Column (
                       Modifier
                           .padding(20.dp)
                           .align(Alignment.CenterHorizontally)){
                       OutlinedTextField(
                           value = usrnm, 
                           onValueChange = {usrnm=it},
                           colors = TextFieldDefaults.colors(
                               focusedTextColor = textColor,
                               unfocusedTextColor = textColor,
                               focusedContainerColor = Color.Transparent,
                               unfocusedContainerColor = Color.Transparent,
                               cursorColor = textColor,
                               focusedLabelColor = textColor,
                               unfocusedLabelColor = textColor,
                               focusedIndicatorColor = Yellow65,
                               unfocusedIndicatorColor = Yellow65
                           ),
                           label = { Text("User Name", color = textColor) }
                       )
                       TextButton(
                           onClick = { db.collection("users").document(uId).update("name", usrnm)
                                        UserName=usrnm
                                        dialog=false
                                     },
                           modifier = Modifier
                               .padding(8.dp)
                               .align(Alignment.End),
                       ) {
                           Text(text = "Save", color = Yellow65, fontWeight = FontWeight.Bold)
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
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally), color = Yellow65)
            }
        }
    }



}