package com.example.madpractical9_20012021017

import android.app.AlertDialog
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.madpractical9_20012021017.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val SMS_PERMISSION_CODE = 110
    private lateinit var binding: ActivityMainBinding

    private lateinit var al:ArrayList<SMSView>
    private lateinit var lv:ListView
    private lateinit var smsreceiver: smsbroadcastreciever

    private fun requestSMSPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_SMS)) {
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS),
            SMS_PERMISSION_CODE)
    }

    private val isSMSReadPermission: Boolean
        get() = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    private val isSMSWritePermission: Boolean
        get() = ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

    private fun checkRequestPermission():Boolean {
        return if (!isSMSReadPermission || !isSMSWritePermission) {
            requestSMSPermission()
            false
        } else true
    }

    private fun loadSMSInbox() {
        if (!checkRequestPermission()) return
        val uriSMS = Uri.parse("content://sms/inbox")
        val c = contentResolver.query(uriSMS, null, null, null, null)
        al.clear()
        while (c!!.moveToNext()) {
        al.add(SMSView(c.getString(2), c.getString(12)))
        }
        lv.adapter = SMSViewAdapter(this, al)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        al = ArrayList()
        lv = binding.listview

        if (checkRequestPermission()) {
            loadSMSInbox()
        }
        else
        {
            checkRequestPermission()
        }
        smsreceiver = smsbroadcastreciever()
        registerReceiver(smsreceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        smsreceiver.listner = ListenerImplement()
        binding.sendButton.setOnClickListener {
            val phone = binding.phoneno.text.toString()
            val msg = binding.message.text.toString()
            SendSms(phone, msg)
            val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Sent SMS")
            builder.setMessage("SMS is sent.\nPhone No : $phone \n\n Message : $msg")
            builder.setCancelable(true)
            builder.setPositiveButton("OK", null);
            builder.show()
        }
    }

    fun SendSms(sPhoneNo: String, sMsg: String){
        if(!checkRequestPermission()){
//            if you like add toast message
            return
        }
        else
        {
            checkRequestPermission()
        }
        val smsmanager = SmsManager.getDefault()
        if(smsmanager != null) {
            smsmanager.sendTextMessage(sPhoneNo, null, sMsg, null, null)
        }
    }

    inner class ListenerImplement:smsbroadcastreciever.Listerner{
        override fun onTextReceived(sPhoneNo: String, sMsg: String) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("New SMS Received")
            builder.setMessage("$sPhoneNo\n$sMsg")
            builder.setCancelable(true)
            builder.show()
            loadSMSInbox()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsreceiver)
    }
}
