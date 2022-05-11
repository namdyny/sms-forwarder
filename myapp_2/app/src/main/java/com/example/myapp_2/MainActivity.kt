package com.example.myapp_2

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.runBlocking
//import org.litote.kmongo.*
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*



class MainActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var tv: TextView = findViewById(R.id.textView)
        tv.text = "XXX"

        fun updateText(msg:String){
            var t = System.currentTimeMillis() / 1000
            tv.text = "{'$t': '$msg'}"

            data class OTP(val time: String, val otp: String)
            var client = KMongo.createClient("mongodb://192.168.1.101:27017").coroutine
            var database = client.getDatabase("sms_forwarder")
            var col = database.getCollection<OTP>("last_otp")
            runBlocking {
                col.insertOne(OTP(t.toString(), msg))
            }
        }

        class MyReceiver()  : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                var smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0].messageBody
                val regexString = "[\\d]{6}".toRegex()
                var match = regexString.find(smsMessages)?.groupValues?.get(0)
                if (match != null) {
                    updateText(match)
                    Log.d("SMS1", match)
                    Toast.makeText(context, "SMS $match received", Toast.LENGTH_LONG).show()
                }
            }
        }
        this.registerReceiver(MyReceiver(), IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }
//    override
}