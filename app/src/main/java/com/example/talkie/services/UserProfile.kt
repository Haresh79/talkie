package com.example.talkie.services

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class UserProfile {
    private val db = FirebaseFirestore.getInstance()
    fun getProfile(userId:String, callback:(UserProfile)->Unit){
        db.collection("users").document(userId).get().addOnSuccessListener {
            if (it.exists()){
                val result=it.toObject(UserProfile::class.java)
                callback(result!!)
            }
        }.addOnFailureListener {

        }
    }
}