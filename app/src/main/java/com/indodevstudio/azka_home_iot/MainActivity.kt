package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
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
import java.io.FileInputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
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

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        mbuh()

        //emailGet = findViewById(R.layout.nav_header.)



    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            R.id.nav_taman -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TamanFragment()).commit()
            R.id.nav_masjid -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MasjidFragment()).commit()
            R.id.nav_tandon_uti -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TandonUtiFragment()).commit()
            R.id.nav_tandon_bunda -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TandonBundaFragment()).commit()
            R.id.nav_listrik_masjid -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListrikMasjidFragment()).commit()
            R.id.nav_grafik -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GrafikMasjidFragment()).commit()
            R.id.nav_taman_p -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TamanProdFragment()).commit()
            R.id.nav_about -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutFragment()).commit()
            R.id.nav_listrik_uti -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListrikUtiFregment()).commit()
            R.id.nav_listrik_bunda -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListrikBundaFragment()).commit()
            R.id.nav_grafik_bunda -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GrafikBundaFragment()).commit()
            R.id.nav_grafik_uti -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GrafikUtiFregment()).commit()
            //R.id.nav_update -> supportFragmentManager.beginTransaction()
             //   .replace(R.id.fragment_container, UpdateLogFragment()).commit()

            R.id.nav_logout -> {
                firebaseAuth.signOut()
                startActivity(Intent(this, SignInActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK))
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) //makesure user cant go back
                finish()
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
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