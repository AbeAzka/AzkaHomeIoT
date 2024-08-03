package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.indodevstudio.azka_home_iot.databinding.ActivitySignInBinding


class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var  userTXT : TextView
    lateinit var  emailTXT : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnRegLogin.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
        }

        binding.textView10.setOnClickListener {
            startActivity(Intent(this,ResetPwActivity::class.java))
            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
        }

        binding.button.setOnClickListener {
            val email = binding.emailET.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        //if (firebaseAuth.currentUser?.isEmailVerified == true){

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_SHORT).show()
                            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
                        //}
                        //else{
                            //val intent = Intent(this, VerificationActivity::class.java)
                            //overridePendingTransition(R.anim.slide_to_left,R.anim.slide_to_right)
                            //startActivity(intent)
                            //Toast.makeText(applicationContext, "Please verify your Email first!", Toast.LENGTH_SHORT).show()
                        //}
                    } else {
                        Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(applicationContext, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onStart() {
        super.onStart()
        /*if(firebaseAuth.currentUser != null && firebaseAuth.currentUser?.isEmailVerified == true){

            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(applicationContext, "Welcome back", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(applicationContext, "Please verify your Email first!", Toast.LENGTH_SHORT).show()
        }*/
        if(firebaseAuth.currentUser != null) {

            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(applicationContext, "Welcome back!", Toast.LENGTH_SHORT).show()
            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
        }
    }


    //==override fun onStart() {
    //    super.onStart()
    //    if(firebaseAuth.currentUser != null){
    //        val intent = Intent(this, MainActivity::class.java)
    //        startActivity(intent)
    //    }
   // }
}