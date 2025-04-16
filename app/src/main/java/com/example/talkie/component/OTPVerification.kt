package com.example.talkie.component

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talkie.ui.theme.Yellow65
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

val auth=Firebase.auth
var varificationId=""

@Composable
fun OTPVerification(current: Context) {
    var PNo by remember {
        mutableStateOf("")
    }
    var OTP by remember {
        mutableStateOf("")
    }


    Row(
        Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .padding(20.dp)) {
        Column (Modifier.align(Alignment.CenterVertically)){
            TextField(value = PNo, onValueChange = {PNo=it}, modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, color = Color.Black)),
                placeholder = { Text(text = "Enter yor phone number")}, colors = TextFieldDefaults.colors(
                    Color.Black), prefix = { Text(text = "+91 ")})
            Spacer(modifier = Modifier.height(5.dp))
            Button(onClick = {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("+91"+PNo) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(current as Activity) // Activity (for callback binding)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // This callback will be invoked in two situations:
                            // 1 - Instant verification. In some cases the phone number can be instantly
                            //     verified without needing to send or enter a verification code.
                            // 2 - Auto-retrieval. On some devices Google Play services can automatically
                            //     detect the incoming verification SMS and perform verification without
                            //     user action.
                            Log.d(TAG, "onVerificationCompleted:$credential")
                            Toast.makeText(current, "credential: ${credential}", Toast.LENGTH_SHORT).show()
                            signInWithPhoneAuthCredential(credential, current)
                            Toast.makeText(current, "Go to next page", Toast.LENGTH_SHORT).show()
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            // This callback is invoked in an invalid request for verification is made,
                            // for instance if the the phone number format is not valid.
                            Log.w(TAG, "onVerificationFailed", e)

                            if (e is FirebaseAuthInvalidCredentialsException) {
                                // Invalid request
                                Toast.makeText(current, "Invalid request", Toast.LENGTH_SHORT).show()
                            } else if (e is FirebaseTooManyRequestsException) {
                                // The SMS quota for the project has been exceeded
                                Toast.makeText(current, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show()
                            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                                // reCAPTCHA verification attempted with null Activity
                                Toast.makeText(current, "reCAPTCHA verification attempted with null Activity", Toast.LENGTH_SHORT).show()
                            }

                            // Show a message and update the UI
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken,
                        ) {
                            // Save verification ID and resending token so we can use them later
                            varificationId = verificationId
                            //resendToken = token
                        }
                    }) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
                auth.setLanguageCode("fr")
                Toast.makeText(current, "OTP Send", Toast.LENGTH_SHORT).show()

            },
                Modifier
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Yellow65, RoundedCornerShape(20.dp)), colors = ButtonDefaults.buttonColors(
                    Color.Transparent)) {
                Text(text = "Get OTP", color = Yellow65)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "|", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(text = "|",textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                TextField(value = OTP, onValueChange = {OTP=it},
                    Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black), textStyle = TextStyle(letterSpacing = 20.sp, fontSize = 20.sp))
                Button(onClick = {
                    if (OTP.isEmpty()){
                        Toast.makeText(current, "Give The OTP", Toast.LENGTH_LONG).show()
                    }else{

                        val credential = PhoneAuthProvider.getCredential(varificationId!!, OTP)
                        Toast.makeText(current, "${credential}", Toast.LENGTH_SHORT).show()
                        signInWithPhoneAuthCredential(credential,current as Activity)
                    }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Yellow65)) {
                    Text(text = "Verify")
                }
            }
        }

    }


}
fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, current: Activity) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener(current) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(current, "Go to next page", Toast.LENGTH_SHORT).show()
                val user = task.result?.user
            } else {
                // Sign in failed, display a message and update the UI
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    Toast.makeText(current, "${task.exception}", Toast.LENGTH_SHORT).show()
                }
                // Update UI
            }
        }
}