package com.indodevstudio.azka_home_iot


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.indodevstudio.azka_home_iot.databinding.ActivitySignInBinding
import com.indodevstudio.azka_home_iot.utils.FirebaseUtils


class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    lateinit var image_google : ImageView
    lateinit var acct : GoogleSignInAccount
    //lateinit var  userTXT : TextView
    //lateinit var  emailTXT : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)
        image_google = findViewById(R.id.gSignInPct)
        image_google.isClickable = true
        binding.gSignInPct.setOnClickListener{ view ->
            signInGoogle()
        }
//        findViewById<Button>(R.id.gSignInBtn).setOnClickListener {
//            signInGoogle()
//        }

//        binding.btnRegLogin.setOnClickListener {
//            startActivity(Intent(this,SignUpActivity::class.java))
//            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
//        }
//
//        binding.tvForgotPassword.setOnClickListener {
//            startActivity(Intent(this,ResetPwActivity::class.java))
//            overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left)
//        }
//
//        binding.loginbtn.setOnClickListener {
//            userLogin()
//            val email = binding.emailET.text.toString()
//            val pass = binding.passET.text.toString()
//
//            if (email.isNotEmpty() && pass.isNotEmpty()) {
//                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        //if (firebaseAuth.currentUser?.isEmailVerified == true){
//
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_SHORT).show()
//                            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
//                        //}
//                        //else{
//                            //val intent = Intent(this, VerificationActivity::class.java)
//                            //overridePendingTransition(R.anim.slide_to_left,R.anim.slide_to_right)
//                            //startActivity(intent)
//                            //Toast.makeText(applicationContext, "Please verify your Email first!", Toast.LENGTH_SHORT).show()
//                        //}
//                    } else {
//                        Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
//
//                    }
//                }
//            } else {
//                Toast.makeText(applicationContext, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
//
//            }
//        }
    }

//    private fun userLogin()
//    {
//        val email2 = binding?.etSinInEmail?.text.toString()
//        val password = binding?.etSinInPassword?.text.toString()
//        if (validateForm(email, password))
//        {
//            showProgressBar()
//            auth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this){task->
//                    if (task.isSuccessful)
//                    {
//                        startActivity(Intent(this,MainActivity::class.java))
//                        finish()
//                    }
//                    else
//                    {
//                        binding?.btnSignIn?.text = "Login"
//                        Toast.makeText(this,"Oops! Something went wrong", Toast.LENGTH_SHORT).show()
//                    }
//                    hideProgressBar()
//                }
//        }

//        val email = binding.emailET.text.toString()
//        val pass = binding.passET.text.toString()
//
//        if (email.isNotEmpty() && pass.isNotEmpty()) {
//            firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
//                if (it.isSuccessful) {
//                    //if (firebaseAuth.currentUser?.isEmailVerified == true){
//
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(applicationContext, "Welcome", Toast.LENGTH_SHORT).show()
//                    overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
//
//                } else {
//                    //Toast.makeText(applicationContext, it.exception.toString(), Toast.LENGTH_SHORT).show()
//                    Toast.makeText(this,"Oops! Something went wrong", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } else {
//            Toast.makeText(applicationContext, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
//
//        }
//    }
    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                val intent = Intent(this , MainActivity::class.java)
                intent.putExtra("email" , account.email)
                intent.putExtra("name" , account.displayName)
                if(firebaseAuth.currentUser!!.isEmailVerified == true){
                    Log.i("Status", "Account verified for " + account.email)
                }else{
                    Log.i("Status", "Account unverified for " + account.email)
                    firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                        Toast.makeText(this, "Please verify your email!" , Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener{
                        Toast.makeText(this, "Something went wrong" , Toast.LENGTH_SHORT).show()
                    }
                }
                startActivity(intent)

            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser != null) {
            val user = FirebaseAuth.getInstance().currentUser
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("email", user!!.email)
                intent.putExtra("name", user!!.displayName)
                startActivity(intent)
                Toast.makeText(
                    applicationContext,
                    "Welcome back " + user!!.email,
                    Toast.LENGTH_SHORT
                ).show()
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                //finish()
            }

        }

//        if(firebaseAuth.currentUser != null) {
//            val user = FirebaseAuth.getInstance().currentUser
//            val intent  = Intent(this , MainActivity::class.java)
//            intent.putExtra("email" , user!!.email)
//            intent.putExtra("name" , user!!.displayName)
//
//            startActivity(intent)
//            Toast.makeText(applicationContext, "Welcome back " + user!!.email, Toast.LENGTH_SHORT).show()
//            overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right)
//        }
    }


    //==override fun onStart() {
    //    super.onStart()
    //    if(firebaseAuth.currentUser != null){
    //        val intent = Intent(this, MainActivity::class.java)
    //        startActivity(intent)
    //    }
   // }
}