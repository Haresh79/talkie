package com.example.talkie.datamodels

import com.google.firebase.Timestamp

data class Chat(
    var sender:String?=null,
    var receiver:String?=null,
    var text:String?=null,
    var timestamp: String?=null,
)
