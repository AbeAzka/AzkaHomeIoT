package com.indodevstudio.azka_home_iot


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.indodevstudio.azka_home_iot.databinding.ActivitySignInBinding



class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    lateinit var image_google : TextView
    lateinit var acct : GoogleSignInAccount
    //lateinit var  userTXT : TextView
    //lateinit var  emailTXT : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val sharedPreferenceManger = SharedPreferenceManger(this)
//        AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[sharedPreferenceManger.theme])

        checkAutoLogin()
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

        binding.gSignInPct.setOnClickListener{


                // do whatever we wish!
                signInGoogle()

        }

        val btnLoginWebsite = findViewById<Button>(R.id.idsLogin)
        btnLoginWebsite.setOnClickListener {

                val redirectUrl = "myapp://link_success"  // Deep link kembali ke aplikasi
                val loginUrl = "https://games.abeazka.my.id/users/login.php?redirect=$redirectUrl"

                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(loginUrl))



            val intent2 = Intent(this , MainActivity::class.java)
            val sharedPref = this.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("isFirebase", false.toString())
            intent2.putExtra("isFirebase", false)

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

    private fun checkAutoLogin() {
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val token = prefs.getString("auth_token", null)

        if (token != null) {
            // Token ada, langsung masuk ke HomeActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
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
            /*Toast.makeText(
                applicationContext,
                "Welcome",
                Toast.LENGTH_SHORT
            ).show()*/
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
                val bundle = Bundle()
                bundle.putString("name", account.displayName)
                intent.putExtra("email" , account.email)
                intent.putExtra("name" , account.displayName)
                intent.putExtra("isFirebase", true)
                saveEmailToSharedPref(this, account.email.toString())
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
                val bundle = Bundle()
                bundle.putString("name", user!!.displayName)
                intent.putExtra("email", user!!.email)
                intent.putExtra("name", user!!.displayName)
                startActivity(intent)
                /*Toast.makeText(
                    applicationContext,
                    "Welcome back " + user!!.email,
                    Toast.LENGTH_SHORT
                ).show()*/
                saveEmailToSharedPref(this, user!!.email.toString())

                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                //finish()
            }

        }
    }




    fun saveEmailToSharedPref(context: Context, email: String) {
        val sharedPref = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("EMAIL", email)
        editor.apply()
        Log.i("FCM", " $email")
    }

}