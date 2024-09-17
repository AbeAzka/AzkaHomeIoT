package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(OnClickListener {
            //What to do on back clicked
            onBackPressed()
        })
        val getData = intent.getParcelableExtra<DataClass>("android")
        if (getData != null) {
            val detailTitle: TextView = findViewById(R.id.detailTitle)
            val detailDesc: TextView = findViewById(R.id.detailDesc)
            //val detailImage: ImageView = findViewById(R.id.detailImage)
            detailTitle.text = getData.dataTitle
            detailDesc.text = getData.dataDesc
            //detailImage.setImageResource(getData.dataDetailImage)
        }

        getSupportActionBar()?.setTitle(getData?.dataTitle)
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, manual_book_fragment()).commit()
        onBackPressedDispatcher.onBackPressed()
    }
}