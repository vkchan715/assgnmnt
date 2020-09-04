package com.example.assgnmnt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnAir = findViewById<ImageButton>(R.id.imgBtnAir)
        val btnLight = findViewById<ImageButton>(R.id.imgBtnLight)
        btnAir.setOnClickListener{
            val intent = Intent(this@MainActivity, AirCondActivity::class.java)
            startActivity(intent)
        }
        btnLight.setOnClickListener{
            val intent = Intent(this@MainActivity, LightActivity::class.java)
            startActivity(intent)
        }
    }
}
