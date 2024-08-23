package com.indodevstudio.azka_home_iot


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.indodevstudio.azka_home_iot.utils.FirebaseUtils.firebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.InputStream
import java.net.URL
import java.io.IOException
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class MainActivity :  AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    var image : Bitmap? = null
    lateinit var inputStream : InputStream
    private lateinit var auth : FirebaseAuth
    private lateinit var mFirebaseUser : FirebaseUser
    lateinit var mGoogleSignInClient: GoogleSignInClient

    //private lateinit var googleSignInClient : GoogleSignInClient
    var picture3 = ""
    var token = ""
    var key = "AIzaSyBA1Zxdi5fKu8dKgLhdtKa31M0uG0Xe6zk"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        requestPermission()
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val headerView = navigationView.getHeaderView(0)
        val nama = headerView.findViewById<TextView>(R.id.nama)
        val profilepc = headerView.findViewById<ImageView>(R.id.logo_p)
        val em = headerView.findViewById<TextView>(R.id.emailGet)
        val status = headerView.findViewById<ImageView>(R.id.status22)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        mbuh()

        auth = FirebaseAuth.getInstance()
        mFirebaseUser = auth.currentUser!!;

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        if(auth.currentUser!!.isEmailVerified == true){
            status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.verified))
        }else{
            status.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.unverified))
            auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                Toast.makeText(this, "Please verify your email!" , Toast.LENGTH_SHORT).show()
            }?.addOnFailureListener{
                Toast.makeText(this, "Something went wrong" , Toast.LENGTH_SHORT).show()
            }
        }

        val picture = FirebaseAuth.getInstance().currentUser?.photoUrl
        val picture2 = mFirebaseUser.photoUrl.toString();
        val picture5 = mFirebaseUser.photoUrl
        val email = intent.getStringExtra("email")
        val displayName = intent.getStringExtra("name")
        val photo = intent.getStringExtra("photop")
        picture3 = FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()
        //val xxy = URL("https://lh3.googleusercontent.com/a/ACg8ocIdS4yQkO_r9lcFAMcoA1yVFRa3N5IC9rz3CE47mYsenze49A=s96-c")
        //profilepc.setImageURI(picture)
        if (picture3 == null) {
            profilepc.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.azkahomeiot))
        } else {
            Glide.with(this).load(picture3).into(profilepc);

        }
        Log.i("INFO_PC", picture3 + " AND "+ picture)
        val handler = Handler(Looper.getMainLooper())
        val ur = URL(picture3)
        try {
            val `in` = ur.openStream()
            image = BitmapFactory.decodeStream(`in`)
            handler.post {
                profilepc.setImageBitmap(image)
            }
        }
        catch (e:java.lang.Exception){
            e.printStackTrace()
        }

        profilepc.setOnClickListener{ view->


        var inflater = LayoutInflater.from(this)
        var popupview = inflater.inflate(R.layout.popup_grafik, null,false)
        var imagee = popupview.findViewById<ImageView>(R.id.imageGrafikPop)
        var close = popupview.findViewById<ImageView>(R.id.close)
        var builder = PopupWindow(popupview, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
        //imagee.setImageBitmap(image)
        Glide.with(this).load(picture3).into(imagee);
        builder.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                this,
                R.drawable.background
            )
        )
        builder.animationStyle=R.style.DialogAnimation
        builder.showAtLocation(this.findViewById(R.id.drawer_layout), Gravity.CENTER, 0 ,0)
            close.setOnClickListener{
                builder.dismiss()
            }
        }




        nama.text = displayName;
        em.text = email;
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {


            R.id.nav_home -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_home);
            }

            R.id.nav_taman -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TamanFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_taman)
            }

            R.id.nav_masjid -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MasjidFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_masjid)
            }

            R.id.nav_tandon_uti -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TandonUtiFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_tandon_uti)
            }

            R.id.nav_tandon_bunda -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TandonBundaFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_tandon_bunda)
            }

            R.id.nav_listrik_masjid -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ListrikMasjidFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_listrik_masjid)
            }

            R.id.nav_grafik -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GrafikMasjidFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_grafik)
            }

            R.id.nav_taman_p -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TamanProdFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_taman_p)
            }

            R.id.nav_about -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AboutFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_about)
            }

            R.id.nav_listrik_uti -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ListrikUtiFregment()).commit()
                navigationView.setCheckedItem(R.id.nav_listrik_uti)
            }

            R.id.nav_listrik_bunda -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ListrikBundaFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_listrik_bunda)
            }

            R.id.nav_grafik_bunda -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GrafikBundaFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_grafik_bunda)
            }

            R.id.nav_grafik_uti -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GrafikUtiFregment()).commit()
                navigationView.setCheckedItem(R.id.nav_grafik_uti)
            }

            R.id.nav_book -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, manual_book_fragment()).commit()
                navigationView.setCheckedItem(R.id.nav_book)
            }
            //R.id.nav_update -> supportFragmentManager.beginTransaction()
             //   .replace(R.id.fragment_container, UpdateLogFragment()).commit()

            R.id.nav_logout -> {
                //auth.signOut()
                //startActivity(Intent(this , SignInActivity::class.java))


                firebaseAuth.signOut()
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    val intent= Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Successfully logout!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                //googleSignInClient.signOut();
                //startActivity(Intent(this, SignInActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK))
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) //makesure user cant go back
                //finish()

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun requestPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                //Nothing
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }else{
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FirebaseLog", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                token = task.result

                // Log and toast
                Log.w("FirebaseLog", "Fetching FCM registration token success $token")
            })
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FirebaseLog", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                token = task.result

                // Log and toast
                Log.w("FirebaseLog", "Fetching FCM registration token success $token")
            })
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun mbuh(){
        sendPushNotification(token, "Test", "Test", "Test")
    }

    fun sendPushNotification(token: String, title: String, subtitle: String, body: String, data: Map<String, String> = emptyMap()) {
        val url = "https://fcm.googleapis.com/fcm/send"

        val bodyJson = JSONObject()
        bodyJson.put("to", token)
        bodyJson.put("notification",
            JSONObject().also {
                it.put("title", title)
                it.put("subtitle", subtitle)
                it.put("body", body)
            }
        )
        bodyJson.put("data", JSONObject(data))

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "key=$key")
            .post(
                bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    Log.i("MAINN","Received data: ${response.body?.string()}")
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.i("MAINN",e.message.toString())
                }
            }
        )

    }
}