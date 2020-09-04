package com.example.assgnmnt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.postDelayed
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.light_main.*
import java.lang.Math.floor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class LightActivity : AppCompatActivity() {
    lateinit var btnOn: Button
    lateinit var txtLight: TextView
    lateinit var txtCondition: TextView
    lateinit var autoSwitch: Switch
    lateinit var notifySwitch: Switch
    lateinit var spnBrightness: Spinner
    lateinit var txtTime: EditText
    lateinit var txtSuggestion: TextView
    lateinit var btnSave: Button
    lateinit var btnClear: Button
    lateinit var homeSwitch: Switch
    var auto :String = ""
    var notify : String  = ""
    var home: String = ""
    var brightness: String = ""
    var ultra: Int = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.light_main)
        btnOn = findViewById(R.id.btnOn)
        btnOn.setBackgroundColor(Color.parseColor("#008000"))
        txtLight = findViewById(R.id.txtLight)
        autoSwitch = findViewById(R.id.autoSwitch)
        txtCondition = findViewById(R.id.txtCondition)
        txtSuggestion = findViewById(R.id.txtSuggestion)
        txtTime = findViewById(R.id.txtTime)
        spnBrightness = findViewById(R.id.spnBrightness)
        notifySwitch = findViewById(R.id.notifySwitch)
        btnSave = findViewById(R.id.btnSave)
        btnClear = findViewById(R.id.btnClear)
        homeSwitch = findViewById(R.id.homeSwitch)
        spnBrightness.setSelection(0)
        loadPrefer()
        val handler1 = Handler()
        handler1.postDelayed(object: Runnable{
            override fun run(){
                getData()
                checkLight()
                if (autoSwitch.isChecked){
                    autoLight()
                }
                if(notifySwitch.isChecked){
                    lightNotification()
                }
                if(homeSwitch.isChecked){
                    homeAuto()
                }
                handler1.postDelayed(this, 10000)
            }
        }, 10000)


        btnOn.setOnClickListener{
            checkLight()
            if(btnOn.text.toString() == "ON"){
                val ref1 = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL").child("relay")
                ref1.setValue("1")
                checkLight()
                Toast.makeText(applicationContext, "Light Turned On", Toast.LENGTH_LONG).show()
            }
            else{
                val ref1 = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL").child("relay")
                ref1.setValue("0")
                checkLight()
                Toast.makeText(applicationContext, "Light Turned Off", Toast.LENGTH_LONG).show()
            }
        }

        btnSave.setOnClickListener{
            save()
        }

        btnClear.setOnClickListener{
            autoSwitch.isChecked = false
            notifySwitch.isChecked = false
            homeSwitch.isChecked = false
            txtTime.setText("")
            spnBrightness.setSelection(0)
            save()
            Toast.makeText(applicationContext, "Cleared.", Toast.LENGTH_LONG).show()
        }

    }

    private fun getData(){
        val sdf = SimpleDateFormat("yyyyMMdd")
        val frmt = DecimalFormat("00")
        val date = sdf.format(Date())
        val ddbdate = "PI_03_" + date
        val hour = SimpleDateFormat("HH").format(Date())
        val min = SimpleDateFormat("mm").format(Date())
        val sec = SimpleDateFormat("ss").format(Date())
        val minSec = min + frmt.format((floor(sec.toDouble()/10)*10).toInt())

        val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
        val lastQuery = ref.child(hour).child(minSec)
        lastQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                autoSwitch = findViewById(R.id.autoSwitch)
                spnBrightness = findViewById(R.id.spnBrightness)
                val lightReading = p0.child("light").value.toString()
                ultra = p0.child("ultra").value.toString().toInt()

                if(lightReading != null){
                    txtLight.text = lightReading
                }
                if(txtLight.text.toString().toInt() <= 256){
                    txtCondition.text = "Dark"
                    txtSuggestion.text = "ON"
                }
                else{
                    txtCondition.text = "Bright"
                    txtSuggestion.text = "OFF"
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    private fun autoLight(){
        val hour = SimpleDateFormat("HH").format(Date())
        val min = SimpleDateFormat("mm").format(Date())
        val hourMin = hour + min
        var target: Int = 0
        if(spnBrightness.selectedItem.toString()=="0%") {
            target = 0 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="25%"){
            target = 25 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="50%"){
            target = 50 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="75%"){
            target = 75 * 1024 / 100
        }

        if(txtLight.text.toString().toInt() <= target && btnOn.text.toString() == "ON"){
            val ref1 = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL").child("relay")
            ref1.setValue("1")
            checkLight()
        }
        if(hourMin == txtTime.text.toString() && btnOn.text.toString() == "OFF"){
            val ref1 = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL").child("relay")
            ref1.setValue("0")
            autoSwitch.isChecked = false
            checkLight()
        }
    }

    private fun lightNotification(){
        var target: Int = 0
        if(spnBrightness.selectedItem.toString()=="0%") {
            target = 0 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="25%"){
            target = 25 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="50%"){
            target = 50 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="75%"){
            target = 75 * 1024 / 100
        }

        if(txtLight.text.toString().toInt() <= target && btnOn.text.toString() == "ON"){
            var content = "Light Reading: " +  txtLight.text.toString() + ". It is Dark in the room."
            var builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Low Light in the room.")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(this)){
                notify(1234,builder.build())
            }
        }
    }

    private fun checkLight(){
        var relay: Int = 1
        val dbref = FirebaseDatabase.getInstance().getReference().child("PI_03_CONTROL")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                relay = p0.child("relay").value.toString().toInt()
                btnOn = findViewById<Button>(R.id.btnOn)
                if(relay == 0){
                    btnOn.text = "ON"
                    btnOn.setBackgroundColor(Color.parseColor("#008000"))
                }
                else{
                    btnOn.text = "OFF"
                    btnOn.setBackgroundColor(Color.parseColor("#FF0000"))
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

    }

    private fun save(){
        auto = autoSwitch.isChecked.toString()
        notify = notifySwitch.isChecked.toString()
        home = homeSwitch.isChecked.toString()
        brightness = spnBrightness.selectedItemPosition.toString()
        savedPrefer("1", auto)
        savedPrefer("2", notify)
        savedPrefer("3", home)
        savedPrefer("4", brightness)
        savedPrefer("5", txtTime.toString())
        Toast.makeText(applicationContext, "Saved.", Toast.LENGTH_LONG).show()
    }

    private fun savedPrefer(key:String, value:String){
        val sharedPrefer: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefer.edit()
        editor.putString(key,value)
        editor.apply()
    }

    private fun loadPrefer(){
        val sharedPrefer: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val auto1 = sharedPrefer.getString("1",null)
        if(auto1!=null){
            if(sharedPrefer.getString("1", auto)=="true"){
                autoSwitch.isChecked = true
            }
            else if(sharedPrefer.getString("1", auto)=="false"){
                autoSwitch.isChecked = false
            }
            if(sharedPrefer.getString("2", notify)=="true"){
                notifySwitch.isChecked = true
            }
            else if(sharedPrefer.getString("2", notify)=="false"){
                notifySwitch.isChecked = false
            }
            if(sharedPrefer.getString("3", home)=="true"){
                homeSwitch.isChecked = true
            }
            else if(sharedPrefer.getString("3", home)=="false"){
                homeSwitch.isChecked = false
            }
            spnBrightness.setSelection(sharedPrefer.getString("4", brightness).toString().toInt())
        }
    }

    private fun homeAuto(){
        var target: Int = 0
        if(spnBrightness.selectedItem.toString()=="0%") {
            target = 0 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="25%"){
            target = 25 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="50%"){
            target = 50 * 1024 / 100
        }
        else if(spnBrightness.selectedItem.toString()=="75%"){
            target = 75 * 1024 / 100
        }
        if(txtLight.text.toString().toInt() <= target && btnOn.text.toString() == "ON" && ultra < 100){
            val ref1 = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL").child("relay")
            ref1.setValue("1")
            checkLight()
        }
    }
}
