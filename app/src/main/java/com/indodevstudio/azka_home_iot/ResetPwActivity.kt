package com.indodevstudio.azka_home_iot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.indodevstudio.azka_home_iot.databinding.ActivitySignInBinding

class ResetPwActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var mAuth: FirebaseAuth
    lateinit var btnResetPW : Button
    lateinit var back : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_pw)
        supportActionBar?.hide()
        btnResetPW = findViewById(R.id.btnResetPW)
        btnResetPW.setOnClickListener(this)
        back = findViewById(R.id.back)
        back.setOnClickListener(this)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.back -> {
                startActivity(Intent(this,SignInActivity::class.java))
                onBackPressed()
            }
            R.id.btnResetPW -> forgotPassword()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
    }

    private fun forgotPassword() {
        var email = findViewById<TextInputEditText?>(R.id.resetEmailPW).text.toString()
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, getString(R.string.error_text),  Toast.LENGTH_SHORT).show()
            Toast.makeText(this, getString(R.string.error_text), Toast.LENGTH_SHORT).show()
        }else{
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener{
                if (it.isSuccessful){
                    Toast.makeText(this, "Check email to reset password", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this,SignInActivity::class.java))
                    overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
                }else{
                    Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}