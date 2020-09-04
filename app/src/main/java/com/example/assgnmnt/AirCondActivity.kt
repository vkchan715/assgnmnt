package com.example.assgnmnt

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

class AirCondActivity : AppCompatActivity() {

    private var swOn:String =""
    private var swOff:String =""
    private var notifyOn: String=""
    private var relay=""
    lateinit var notificationManager : NotificationManager
    lateinit var btnSw : ToggleButton
    lateinit var btnSa: Button
    lateinit var btnCl:Button
    lateinit var editOn:EditText
    lateinit var editOff:EditText
    lateinit var editNo:EditText
    lateinit var swAu: Switch
    lateinit var swNo: Switch
    lateinit var txtTempeVal:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aircond_activity)
        btnSw = findViewById(R.id.btnSwitch)
        btnSa = findViewById(R.id.btnSave)
        btnCl = findViewById(R.id.btnClear)
        editOn = findViewById(R.id.editSwOn)
        editOff = findViewById(R.id.editSwOff)
        editNo = findViewById(R.id.editNotify)
        swAu = findViewById(R.id.swAuto)
        swNo = findViewById(R.id.swNotify)

        val ref2 = FirebaseDatabase.getInstance().getReference().child("PI_03_CONTROL")
        ref2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot){
                relay = p0.child("relay").value.toString()// get relay
                if(relay == "0"){
                    btnSw.isChecked = false

                }
                else if(relay == "1"){
                    btnSw.isChecked = true
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        getData()

        var handler = Handler()
        handler.postDelayed(object : Runnable{
            override fun run() {
                getData()
                handler.postDelayed(this,10000)
            }
        },10000)

        loadSaved()
        if(!swAu.isChecked) {
            editOn.isEnabled = false
            editOff.isEnabled = false
            swAu.text = "Off"
        }
        if(!swNo.isChecked){
            editNo.isEnabled =  false
            swNo.text = "Off"
        }

        swAu.setOnClickListener {
            setAuto()
        }
        swNo.setOnClickListener {
            setNotify()
        }
        btnSa.setOnClickListener {
            save()
            getData()
        }

        btnCl.setOnClickListener {
            editOn.setText("0")
            editOff.setText("0")
            editNo.setText("0")
            auto = "false"
            notify = "false"
            swAu.isChecked = false
            editOn.isEnabled = false
            editOff.isEnabled = false
            swAu.text = "Off"
            swNo.isChecked = false
            editNo.isEnabled =  false
            swNo.text = "Off"
            save()
        }

        btnSw.setOnClickListener{
            if(btnSw.isChecked){
                updateDb("1")
            }
            else if(!btnSw.isChecked){
                updateDb("0")
            }
        }
    }

    private var auto: String = ""
    private var notify: String = ""

    private fun setAuto(){
        editOn = findViewById(R.id.editSwOn)
        editOff = findViewById(R.id.editSwOff)
        editNo = findViewById(R.id.editNotify)
        swAu = findViewById(R.id.swAuto)
        if(!swAu.isChecked){
            editOn.isEnabled = false
            editOff.isEnabled = false
            swAu.text = "Off"
            auto = "false"
        }
        else{
            editOn.isEnabled = true
            editOff.isEnabled = true
            swAu.text = "On"
            auto = "true"
        }
    }

    private fun setNotify(){
        editOn = findViewById(R.id.editSwOn)
        editOff = findViewById(R.id.editSwOff)
        editNo = findViewById(R.id.editNotify)
        swNo = findViewById(R.id.swNotify)
        if(!swNo.isChecked){
            editNo.isEnabled =  false
            swNo.text = "Off"
            notify = "false"
        }
        else{
            editNo.isEnabled =  true
            swNo.text ="On"
            notify = "true"
        }
    }

    private fun save(){
        editOn = findViewById(R.id.editSwOn)
        editOff = findViewById(R.id.editSwOff)
        editNo = findViewById(R.id.editNotify)
        savedPrefes("1",editOn.text.toString())
        savedPrefes("2",editOff.text.toString())
        savedPrefes("3",editNo.text.toString())
        savedPrefes("4",auto)
        savedPrefes("5",notify)
        Toast.makeText(applicationContext, "Saved", Toast.LENGTH_LONG).show()
    }

    private fun loadSaved(){
        editOn = findViewById(R.id.editSwOn)
        editOff = findViewById(R.id.editSwOff)
        editNo = findViewById(R.id.editNotify)
        swAu = findViewById(R.id.swAuto)
        swNo = findViewById(R.id.swNotify)
        val sharedPrefes: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val swOn1 = sharedPrefes.getString("1",null)
        if(swOn1!= null) {
            editOn.setText(sharedPrefes.getString("1", swOn))
            editOff.setText(sharedPrefes.getString("2", swOff))
            editNo.setText(sharedPrefes.getString("3", notifyOn))
            if (sharedPrefes.getString("4", auto) == "true") {
                swAu.isChecked = true
            } else if (sharedPrefes.getString("4", auto) == "false") {
                swAu.isChecked = false
            }
            if (sharedPrefes.getString("5", notify) == "true") {
                swNo.isChecked = true
            } else if (sharedPrefes.getString("5", notify) == "false") {
                swNo.isChecked = false
            }
        }
    }

    private fun savedPrefes(key:String, value:String) {
        val sharedPrefes: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPrefes.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private var tempe: String = ""

    private fun performTask(temp: String){
        var tempeValue : Double
        btnSw = findViewById(R.id.btnSwitch)
        btnSa = findViewById(R.id.btnSave)
        btnCl = findViewById(R.id.btnClear)
        editOn = findViewById(R.id.editSwOn)
        editOff = findViewById(R.id.editSwOff)
        editNo = findViewById(R.id.editNotify)
        swAu = findViewById(R.id.swAuto)
        swNo = findViewById(R.id.swNotify)
        txtTempeVal = findViewById(R.id.txtTempeValue)
        if(txtTempeVal.text == "null"){ tempeValue= 0.00 }else { tempeValue = temp.toDouble() }

        var swOnValue= editOn.text.toString().toDouble()
        var swOffValue= editOff.text.toString().toDouble()
        var notifyValue = editNo.text.toString().toDouble()
        if(swNo.isChecked) {
            if(notifyValue == 0.00){
                btnSw.isChecked = btnSw.isChecked
            }
            else if (tempeValue >= notifyValue) {
                var content = "Environment Temperature : " + tempeValue + "°C"
                var builder = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_message)
                    .setContentTitle("Temperature Detected")
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(this)) {
                    notify(1234, builder.build())
                }
            }
        }

        if(swAu.isChecked) {
            if(swOnValue == 0.00){
                btnSw.isChecked = btnSw.isChecked
            }
            else if (tempeValue >= swOnValue) {
                btnSw.isChecked = true
                //updateDb("1")
            } else if (tempeValue <= swOffValue && tempeValue != 0.00) {
                btnSw.isChecked = false
                //updateDb("0")
            } else if (tempeValue == 0.00) {
                btnSw.isChecked = btnSw.isChecked
            }
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
        val minSec = min + frmt.format((floor(sec.toDouble()/10) *10).toInt())
        txtTempeVal = findViewById(R.id.txtTempeValue)
        val ref = FirebaseDatabase.getInstance().getReference().child(ddbdate)
        val lastQuery = ref.child(hour).child(minSec)
        lastQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot){
                tempe = p0.child("tempe").value.toString()// get temperature
                if(tempe != null){
                    txtTempeVal.text = tempe
                }
                performTask(tempe)
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    private fun updateDb(relayValue: String){
        var relay = relayValue

        val dbref = FirebaseDatabase.getInstance().getReference("PI_03_CONTROL").child("relay")
        dbref.setValue(relay)
    }

}
