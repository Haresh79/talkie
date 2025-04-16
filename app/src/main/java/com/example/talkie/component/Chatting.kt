package com.example.talkie.component

//import java.security.Timestamp
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.talkie.R
import com.example.talkie.datamodels.Chat
import com.example.talkie.ui.theme.Yellow65
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Chatting(navController: NavHostController, current: Context, receiver: String) {

    var input by remember {
        mutableStateOf("")
    }
    var Sender by remember {
        mutableStateOf("")
    }
    var Receiver by remember {
        mutableStateOf("")
    }
    var Name by remember {
        mutableStateOf("")
    }
    var Time by remember {
        mutableStateOf("")
    }
    var chats by remember {
        mutableStateOf(mutableListOf<Chat>())
    }
    var loading by remember {
        mutableStateOf(true)
    }
    var timePicker by remember {
        mutableStateOf(false)
    }


    val db=FirebaseFirestore.getInstance()
    val uId=Firebase.auth.currentUser?.uid.toString()
    fun loadInitialChats() {

        db.collection("chats").orderBy("timestamp").addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore", "Error listening for chat updates: ${error.message}")
                loading=true
                return@addSnapshotListener
            }
            if (value != null) {
                loading=false

                chats.clear()
                for (doc in value.documents) {
                    val chat = Chat(
                        doc.get("sender").toString(),
                        doc.get("receiver").toString(),
                        doc.get("text").toString(),
                        doc.get("timestamp").toString()
                    )
                    if ((chat.sender == Sender && chat.receiver == Receiver) ||
                        (chat.sender == Receiver && chat.receiver == Sender)) {
                        chats.add(chat)
                    }
                }
                Log.d("doc", chats.toString())
                if (chats.size!=0){
                    loading=true
                    GlobalScope.launch {
                        delay(50L)
                        loading=false
                    }
                }
            } else {
                Log.w("Firestore", "Received null value from snapshot listener")
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        Receiver=receiver
        db.collection("users").whereEqualTo("number", receiver).get().addOnSuccessListener {
            Name= it.documents[0].get("name").toString()
        }
        db.collection("users").document(uId).get().addOnSuccessListener {
            Sender=it.get("number").toString()
        }.addOnCompleteListener {
            loadInitialChats() // Load initial chat messages
            db.collection("users").document(uId).collection("contacts").document(Receiver).update("new", false)
        }
    }
    BackHandler {
        navController.popBackStack()
    }
    DisposableEffect(key1 = Unit) {
        val chatListener = db.collection("chats")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for chat updates: ${error.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    chats.clear()
                    for (doc in value.documents) {
                        val chat = Chat(
                            doc.get("sender").toString(),
                            doc.get("receiver").toString(),
                            doc.get("text").toString(),
                            doc.get("timestamp").toString()
                        )
                        if (chat.sender==Sender || chat.sender==Receiver){
                            if (chat.receiver==Sender || chat.receiver==Receiver){
                                chats.add(chat)
                            }
                        }
                    }
                } else {
                    Log.w("Firestore", "Received null value from snapshot listener")
                }
            }
        onDispose {
            chatListener.remove()
        }
    }


    Row {
        if (timePicker==false) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "",
                        Modifier
                            .padding(horizontal = 10.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                navController.popBackStack()
                            })
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "",
                        Modifier.width(50.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(Modifier.align(Alignment.CenterVertically)) {
                        Text(text = "$Name", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "+91 $Receiver",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Call, contentDescription = "",
                            Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 10.dp)
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.3.dp)
                        .background(Color.Gray)
                )
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(10.dp),
                    reverseLayout = true
                ) {
                    if (loading == false) {
                        chats = chats.asReversed()
                        items(chats) { chat ->
//                        Toast.makeText(current, chat.text, Toast.LENGTH_SHORT).show()
                            chatBox(chat, Sender)
                        }
                    } else {
                        item {
                            CircularProgressIndicator()
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.3.dp)
                        .background(Color.Gray)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.DateRange, contentDescription = "",
                        Modifier
                            .align(Alignment.CenterVertically)
                            .clickable {
                                if (input != "") {
                                    timePicker = true
                                }
                            })
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(imageVector = Icons.Outlined.Send, contentDescription = "",
                        Modifier
                            .align(Alignment.CenterVertically)
                            .clickable {
                                if (input != "") {
                                    var time = Calendar.getInstance().time
                                    if (Time!=""){
                                        val calendar = Calendar.getInstance()
                                        calendar.time = stringToCalendar(Time)
                                        time=calendar.time
                                    }
                                    db
                                        .collection("chats")
                                        .document()
                                        .set(
                                            Chat(
                                                Sender,
                                                Receiver,
                                                input,
                                                Timestamp(date = time).toString()
                                            )
                                        )
                                        .addOnSuccessListener {
                                            var tempIn=input
                                            GlobalScope.launch {
                                                db.collection("users").document(uId).collection("contacts").document(Receiver).update("lastMsg", tempIn)

                                                var ReceiverUID=db.collection("users").whereEqualTo("number", Receiver).get().await()
                                                if (ReceiverUID.isEmpty) {
                                                    null
                                                } else {
                                                    db.collection("users").document(ReceiverUID.documents[0].id).collection("contacts").document(Sender).update("lastMsg", tempIn)
                                                    db.collection("users").document(ReceiverUID.documents[0].id).collection("contacts").document(Sender).update("new", true)
                                                }
                                            }

                                            input = ""
                                            chats.clear()
                                            loading = true
                                            loadInitialChats()
                                            Time=""
                                        }
                                }
                            })
                }
            }
        }else{
            var timeSel by remember {
                mutableStateOf(false)
            }

            val datePickerState = rememberDatePickerState()
            val TimePickerState = rememberTimePickerState(initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY), initialMinute = Calendar.getInstance().get(Calendar.MINUTE))

            if (timeSel==false){
                DatePickerDialog(onDismissRequest = { timePicker=false }, confirmButton = { TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis?.let { Date(it) }
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
                    Time += formattedDate
                    timeSel=true
                }) {
                    Text("OK")
                } }) {
                    DatePicker(state = datePickerState)
                }
            }

            if (timeSel==true){
                Dialog(onDismissRequest = { timeSel=false }) {
                    Column {
                        TimePicker(state = TimePickerState)
                        Button(onClick = {

                            Time += " "+TimePickerState.hour.toString()+":"+TimePickerState.minute.toString()
                            val calendar = Calendar.getInstance()
                            calendar.time = stringToCalendar(Time)

                            Toast.makeText(current, "${calendar.time} $Time", Toast.LENGTH_SHORT).show()
                            timeSel=false
                            timePicker=false
                        }) {
                            Text(text = "Set")
                        }
                    }
                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun chatBox(chat: Chat, Sender: String) {
    var text by remember {
        mutableStateOf(chat.text)
    }

    val timestampString=chat.timestamp
    val seconds: Long = timestampString?.substring(
        timestampString.indexOf("seconds=") + 8,
        timestampString.indexOf(",")
    )!!.toLong()
    val nanoseconds: Long = timestampString?.substring(
        timestampString.indexOf("nanoseconds=") + 12,
        timestampString.indexOf(")")
    )!!.toLong()

// Create a Date object from seconds and nanoseconds
    val date = Date(seconds * 1000 + nanoseconds / 1000000)

    val sdf = SimpleDateFormat("HH:mm dd-MM-yy", Locale.getDefault())
    val formattedDate = sdf.format(date)

    val zoneId = ZoneId.systemDefault()
    val timestamp = LocalDateTime.now().atZone(zoneId).toInstant().epochSecond
    if(seconds < timestamp){
        Column(
            Modifier
                .fillMaxWidth()
                .padding(1.dp)) {
            if (chat.sender==Sender){
                Row (
                    Modifier
                        .background(
                            Yellow65,
                            RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                        .align(Alignment.End)){
                    text?.let { Text(text = it, color = Color.Black ,modifier = Modifier) }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "$formattedDate", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Bottom))
                }
            }else{
                Row (
                    Modifier
                        .background(
                            Color.LightGray,
                            RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                        .align(Alignment.Start)){
                    text?.let { Text(text = it, color = Color.Black ,modifier = Modifier) }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "$formattedDate", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.Bottom))
                }
            }
        }
    }

}

fun stringToCalendar(dateTimeString: String): Date {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    return dateFormat.parse(dateTimeString) ?: Date()
}


