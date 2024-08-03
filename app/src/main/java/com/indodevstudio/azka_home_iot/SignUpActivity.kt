package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.indodevstudio.azka_home_iot.databinding.ActivitySignUpBinding


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        val uid = firebaseAuth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://azka-home-iot-default-rtdb.firebaseio.com")

        binding.btnLogRegister.setOnClickListener {
            onBackPressed()
        }

        binding.Register.setOnClickListener {
            val email = binding.emailET.text.toString()
            val pass = binding.passET.text.toString()
            val username = binding.usernameET.text.toString()

            val user = UserModel(username, email,pass)
            if (uid != null){
                databaseReference.child(uid).setValue(user).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this@SignUpActivity, "Success!", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this@SignUpActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if (username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            /*if (firebaseUser.isEmailVerified){
                                Toast.makeText(applicationContext, "Email already verified!", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this, SignInActivity::class.java)
                                startActivity(intent)
                                overridePendingTransition(
                                    R.anim.slide_from_right,
                                    R.anim.slide_to_left
                                )
                            }else{*/

                                /*firebaseAuth.currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Please verify your Email!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this, SignInActivity::class.java)
                                        startActivity(intent)
                                        overridePendingTransition(
                                            R.anim.slide_from_left,
                                            R.anim.slide_to_right
                                        )
                                    }
                                    ?.addOnFailureListener() {
                                        Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                                    }*/
                            val user = FirebaseAuth.getInstance().currentUser

                            val profileUpdates =
                                UserProfileChangeRequest.Builder().setDisplayName(username)
                                    .build()

                            user!!.updateProfile(profileUpdates)
                            Toast.makeText(applicationContext, "Success!", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.slide_from_right,
                                R.anim.slide_to_left
                            )
                            //}


                        }else{
                            Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(applicationContext, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }
    }



    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
    }
}