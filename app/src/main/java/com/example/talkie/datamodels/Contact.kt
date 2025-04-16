package com.example.talkie.datamodels

data class Contact(
    var number: String?="",
    var lastMsg: String?="",
    var name:String?="",
    var new:Boolean?=false
)
