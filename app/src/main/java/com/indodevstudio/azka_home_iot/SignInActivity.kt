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
import com.indodevstudio.azka_home_iot.API.DeviceSharingService
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
            //val loginUrl = "https://games.abeazka.my.id/u/login?redirect=$redirectUrl"
            val loginUrl = "https://www.indodevstudio.my.id/u/login?redirect=$redirectUrl"

            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, Uri.parse(loginUrl))



        val intent2 = Intent(this , MainActivity::class.java)
        val sharedPref = this.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("isFirebase", false.toString())
        intent2.putExtra("isFirebase", false)

    }
}


private fun checkAutoLogin() {
    val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
    val token = prefs.getString("auth_token", null)

    if (token != null) {
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
    }else{
        Toast.makeText(this, result.toString() , Toast.LENGTH_SHORT).show()
        Log.d("GOOGLE", result.toString())
    }
}

private fun handleResults(task: Task<GoogleSignInAccount>) {
    if (task.isSuccessful){
        val account : GoogleSignInAccount? = task.result
        Toast.makeText(
            applicationContext,
            "Welcome",
            Toast.LENGTH_SHORT
        ).show()
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
            val user = FirebaseAuth.getInstance().currentUser
            val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.result?.token // atau customToken kamu

//                val accounts = token?.let { it1 ->
//                    AccountData(
//                        email = user?.email ?: "",
//                        token = it1,
//                        provider = AuthProvider.CUSTOM, // atau AuthProvider.FIREBASE
//                        avatarUrl = user?.photoUrl.toString(),
//                        username = user?.displayName,
//                        isVerified = true
//                    )
//                }
//
//                if (accounts != null) {
//                    AccountManager.saveAccount(this, accounts)
//                }
//                AccountManager.setCurrentAccount(this, user?.email ?: "")

            bundle.putString("name", account.displayName)
            intent.putExtra("email" , account.email)
            intent.putExtra("name" , account.displayName)
            intent.putExtra("isFirebase", true)
            account.email?.let { it1 -> account.displayName?.let { it2 ->
                DeviceSharingService.addUser(it1,
                    it2
                )
            } };
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
            user!!.email?.let { user!!.displayName?.let { it1 ->
                DeviceSharingService.addUser(it,
                    it1
                )
            } }
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