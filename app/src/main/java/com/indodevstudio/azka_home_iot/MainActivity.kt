package com.indodevstudio.azka_home_iot


import android.Manifest
import com.jakewharton.threetenabp.AndroidThreeTen
import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.privacysandbox.tools.core.model.Method
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.indodevstudio.azka_home_iot.Model.UnderMaintenance
import com.indodevstudio.azka_home_iot.databinding.ActivityMainBinding
import com.indodevstudio.azka_home_iot.utils.FirebaseUtils.firebaseAuth
import info.mqtt.android.service.MqttAndroidClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
//import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.indodevstudio.azka_home_iot.utils.FirebaseUtils.firebaseUser


class MainActivity :  AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {
    val msg = "messageMQTT"
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding : ActivityMainBinding
    var foregoundServiceIntent : Intent? = null
    var image : Bitmap? = null
    lateinit var inputStream : InputStream
    private lateinit var auth : FirebaseAuth

    lateinit var mGoogleSignInClient: GoogleSignInClient


    private lateinit var mqttAndroidClient: MqttAndroidClient
    var messageMQTT = ""

    //lateinit var binding : ActivityMainBinding

    private val CHANNEL_ID = "channelId"
    private val CHANNEL_NAME = "channelName"
    private val NOTIFICATION_ID = 0

    //private lateinit var googleSignInClient : GoogleSignInClient
    var picture3 = ""
    var token = ""
    var key = "AIzaSyBA1Zxdi5fKu8dKgLhdtKa31M0uG0Xe6zk"


    var db = "keypad_ard"
    var ip = "103.127.99.151"
    var port = "3306"
    var username = "azka"
    var password = "misxB@T.ErHPMS/2"
    var email = ""
    private lateinit var nama : TextView
    private lateinit var em : TextView
    private lateinit var profilepc : ImageView
    private var isFirebase = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()
        val mFirebaseUser = FirebaseAuth.getInstance().currentUser
        mFirebaseUser?.let { user ->
            // Lanjutkan proses jika user tidak null
            Log.d("Firebase", "User is logged in: ${user.uid}")
        } ?: run {
            // Handle kasus ketika user null (belum login)
            Log.e("Firebase", "No user is logged in")
        }
        //verifyToken()
        val test = Intent(this, MQTT_Service::class.java)
        //startService(test)
        connectToFirebase()
        val sharedPreferenceManger = SharedPreferenceManger(this)
        AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[sharedPreferenceManger.theme])
        //connect(this)
        Log.i("MQTT", "MAIN ACTIVITY MQTT RUN")

        //notificationChannel()
//        mqtt = MQTT()
//        mqtt.connect(this@MainActivity)
//        mqtt.receiveMessages()
        createNotificationChannel(applicationContext)

        val notificationManager = getSystemService(NotificationManager::class.java)
        val channels = notificationManager.notificationChannels
        for (channel in channels) {
            Log.d("FCM", "Channel ID: ${channel.id}, Name: ${channel.name}")
        }


        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "Gagal subscribe ke topic")
                }
            }

        val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = this.packageName

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Pengaturan tidak ditemukan di perangkat ini", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Aplikasi sudah dikecualikan dari optimasi baterai", Toast.LENGTH_SHORT).show()
        }
        //subscribe_mqtt("sending_telemetri2")
        //connect(this)
        requestPermission()
        requestPermissionMain()
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)


        // Cek jika Activity dibuka dari notifikasi
        if (intent.hasExtra("openFragment") && intent.getStringExtra("openFragment") == "EventFragment") {
            openFragment(EventFragment())
        }

        // Inisialisasi Logger
        // Mulai service logcat
        Logger.init(this)
        Logger.log("MainActivity", "Aplikasi dimulai!")





//        foregoundServiceIntent = Intent(this, MQTT_Service::class.java)
////        startService(foregoundServiceIntent)
//        startForegroundService(foregoundServiceIntent)
        Options()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar)



        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val headerView = navigationView.getHeaderView(0)
        nama = headerView.findViewById<TextView>(R.id.namas)
        val ipAddress = headerView.findViewById<TextView>(R.id.ipAddress)
        profilepc = headerView.findViewById<ImageView>(R.id.logo_p)

        val localIpAddresses = getLocalUnicastIpAddresses()
        ipAddress.text = "$localIpAddresses"

        em = headerView.findViewById<TextView>(R.id.emailGet)
        val status = headerView.findViewById<ImageView>(R.id.status22)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.settings_menu, menu)
                menuInflater.inflate(R.menu.menu_action_bar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {


                // Handle the menu selection
                when (menuItem.itemId){
                    R.id.action_settings ->{
                        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.menu_hotlist ->{
                        val intent = Intent(this@MainActivity, InboxActivity::class.java)
                        startActivity(intent)
                    }
                }
                return true
            }
        })

//        ui_hot = view.findViewById(R.id.hotlist_hot)

        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser != null){
            Toast.makeText(this, "User login dengan akun Google", Toast.LENGTH_SHORT).show()
            email = intent.getStringExtra("email").toString()
            val displayName = intent.getStringExtra("name")
            val photo = intent.getStringExtra("photop")
            sendFCMTokenToServer(applicationContext, email)
            val picture3 = mFirebaseUser?.photoUrl?.toString() ?: ""
            profilepc.tooltipText = email
            if (picture3 == null && firebaseUser != null) {
                profilepc.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.azkahomeiot))

            }
            if (picture3 != null && firebaseUser != null ){
                Glide.with(this).load(picture3).into(profilepc)

            }
            nama.text = displayName;

            val obfuscatedEmail = email?.let { obfuscateEmail(it) }
            em.text = obfuscatedEmail;
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            Options()
            navigationView.setCheckedItem(R.id.nav_home)
        }

        AndroidThreeTen.init(this) // Inisialisasi ThreeTenABP
        // Cek apakah notifikasi membawa data untuk membuka EventFragment
        if (intent?.getStringExtra("FRAGMENT") == "EventFragment") {
            openFragment(EventFragment())
        }
        val userData = getUserData()



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)


            if(mFirebaseUser?.isEmailVerified == true || userData["isVerified"] == "true"){
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



        isFirebase = intent.getBooleanExtra("isFirebase", false)


        //val xxy = URL("https://lh3.googleusercontent.com/a/ACg8ocIdS4yQkO_r9lcFAMcoA1yVFRa3N5IC9rz3CE47mYsenze49A=s96-c")
        //profilepc.setImageURI(picture)



        Log.i("INFO_PC", picture3 + " AND "+ picture)
        val handler = Handler(Looper.getMainLooper())
        if (picture3.isNullOrEmpty()) {
            Log.e("ERROR", "URL is null or empty")
        } else {
            val ur = URL(picture3)
            try {
                if (picture3.isNullOrEmpty() || !picture3.startsWith("http")) {
                    Log.e("ERROR", "Invalid or empty URL: $picture3")
                    profilepc.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.azkahomeiot))
                } else {
                    val ur = URL(picture3)
                    val `in` = ur.openStream()
                    val image = BitmapFactory.decodeStream(`in`)
                    handler.post {
                        profilepc.setImageBitmap(image)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ERROR", "Failed to load image: ${e.message}")
                profilepc.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.azkahomeiot))
            }

        }


        profilepc.setOnClickListener{ view->


        var inflater = LayoutInflater.from(this)
        var popupview = inflater.inflate(R.layout.popup_grafik, null,false)
        var imagee = popupview.findViewById<ImageView>(R.id.imageGrafikPop)
        var close = popupview.findViewById<ImageView>(R.id.close)
        var builder = PopupWindow(popupview, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)
        //imagee.setImageBitmap(image)
            if(firebaseUser != null) {
                val picture3 = mFirebaseUser?.photoUrl?.toString() ?: ""
                if (picture3 == null && firebaseUser != null) {
                    profilepc.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.azkahomeiot))

                }
                if (picture3 != null && firebaseUser != null ){
                    Glide.with(this).load(picture3).into(imagee)

                }
            }
            if(authToken != null) {
                val avatarPath = userData["avatar"].toString()

// Base URL untuk server
                val baseUrl = "https://games.abeazka.my.id/u/"
                val baseUrl2 = "https://games.abeazka.my.id/u/images/"

                /*// Pastikan path benar
                val fullAvatarUrl = when {
                    avatarPath.startsWith("uploads/") -> baseUrl + avatarPath
                    avatarPath.startsWith("avatar/") -> baseUrl + avatarPath
                    avatarPath.startsWith("http") -> avatarPath // Jika sudah full URL
                    else -> baseUrl2 + "user.png" // Jika path tidak diketahui
                }*/
                val fullAvatarUrl = "https://games.abeazka.my.id/u/$avatarPath"

                Log.d("AvatarURL", "Final URL2: $fullAvatarUrl") // Debugging

                // Gunakan Glide untuk memuat gambar
                Glide.with(this)
                    .load(fullAvatarUrl)
                    .into(imagee)
            }

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





        if (userData["token"] != null) {
            //Toast.makeText(this, "Halo, ${userData["username"]}!", Toast.LENGTH_SHORT).show()
        }

        if (isUserLoggedIn() ) {

        } else {

            openWebLogin(this)  // Arahkan ke login jika belum login
        }

        if(authToken != null) {
            Toast.makeText(this, "User login dengan akun IndodevStudio", Toast.LENGTH_SHORT).show()
            nama.text = userData["username"]
            val obfuscatedEmail = userData["email"]?.let { obfuscateEmail(it) }
            email = userData["email"].toString()
            em.text = obfuscatedEmail
            // Ambil avatar dari userData
            val avatarPath = userData["avatar"].toString()
            userData["email"]?.let { sendFCMTokenToServer(applicationContext,it) }
// Base URL untuk server
            val baseUrl = "https://games.abeazka.my.id/u/"
            val baseUrl2 = "https://games.abeazka.my.id/u/images/"

            // Pastikan path benar
            /*val fullAvatarUrl = when {
                avatarPath.startsWith("uploads/") -> baseUrl + avatarPath
                avatarPath.startsWith("avatar/") -> baseUrl + avatarPath
                avatarPath.startsWith("http") -> avatarPath // Jika sudah full URL
                else -> baseUrl2 + "user.png" // Jika path tidak diketahui
            }*/

            val fullAvatarUrl = "https://games.abeazka.my.id/u/$avatarPath"

            Log.d("AvatarURL", "Final URL: $fullAvatarUrl") // Debugging

            // Gunakan Glide untuk memuat gambar
            Glide.with(this)
                .load(fullAvatarUrl)
                .into(profilepc)

            profilepc.tooltipText = userData["email"]

        }


    }

    fun openWebLogin(context: Context) {
        val redirectUrl = "myapp://link_success"  // Deep link kembali ke aplikasi
        val loginUrl = "https://games.abeazka.my.id/u/login?redirect=$redirectUrl"

        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(loginUrl))
        finish()
    }

    fun setToolbarForFragment(showBackButton: Boolean) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            if (showBackButton) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back) // Ganti dengan ikon back
            } else {
                actionBar.setDisplayHomeAsUpEnabled(false) // Tampilkan hamburger kembali
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack() // Kembali ke fragment sebelumnya
        } else {
            finish() // Jika tidak ada fragment lagi, keluar dari aplikasi
        }
        return true
    }





    private fun getUserData(): Map<String, String?> {
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        return mapOf(
            "token" to prefs.getString("auth_token", null),
            "username" to prefs.getString("username", null),
            "email" to prefs.getString("email", null),
            "avatar" to prefs.getString("avatar", null),
            "isVerified" to prefs.getString("isVerified", null)
        )
    }


    private fun isUserLoggedIn(): Boolean {
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        val authToken = prefs.getString("auth_token", null)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        return authToken != null || firebaseUser != null
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_reminder",  // HARUS SAMA DENGAN channel_id di PHP
                "Event Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk pengingat event"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendFCMTokenToServer(ctx: Context,email: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Gagal mendapatkan token")
                return@addOnCompleteListener
            }
            val fcmToken = task.result
            sendTokenToServer(ctx, email, fcmToken)
        }
    }

    fun sendTokenToServer(context: Context, email: String, token: String) {
        val url = "https://ahi.abeazka.my.id/api/events/save_fcm_token.php"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("fcm_token", token)
        }

        Log.d("FCM", "EMAIL: $email ----- TOKEN: $token")

        val request = object : JsonObjectRequest(Method.POST, url, jsonBody,
            { response ->
                Log.d("FCM", "Token berhasil dikirim: $response")
            },
            { error ->
                Log.e("FCM", "Gagal mengirim token: ${error.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/json")
            }
        }

        Volley.newRequestQueue(context.applicationContext).add(request)
    }






    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }



    fun obfuscateEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email  // Return the email as is if it's not in a valid format

        val username = parts[0]
        val domain = parts[1]

        // Keep the first two characters of the username and replace the rest with asterisks
        val obfuscatedUsername = if (username.length > 2) {
            username.substring(0, 2) + "*".repeat(username.length - 2)
        } else {
            username
        }

        return "$obfuscatedUsername@$domain"
    }


    private fun getLocalUnicastIpAddresses(): String {
        val ipAddresses = StringBuilder()
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    val inetAddresses = networkInterface.inetAddresses
                    while (inetAddresses.hasMoreElements()) {
                        val inetAddress = inetAddresses.nextElement()
                        // Check if the address is not a loopback and is a unicast address
                        if (!inetAddress.isLoopbackAddress && !inetAddress.isLinkLocalAddress) {
                            if (inetAddress is InetAddress) {
                                if (inetAddress.hostAddress.indexOf(':') == -1) {
                                    // IPv4
                                    ipAddresses.append("IPv4: ${inetAddress.hostAddress}\n")
                                } else {
                                    // IPv6
                                    ipAddresses.append("IPv6: ${inetAddress.hostAddress}\n")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (ipAddresses.isNotEmpty()) ipAddresses.toString() else "No Unicast IP Address found"
    }




    override fun onResume() {
        Options()
        super.onResume()
    }





    private fun Options(){
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val notif = prefs.getBoolean("notif", false)
        val mqtt = prefs.getBoolean("mqtt", false)
        binding.apply {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC) == PackageManager.PERMISSION_GRANTED
            ) {
                    if (notif){

                            val test = Intent(this@MainActivity, MQTT_Service::class.java)
                            startService(test)
        //                val org.eclipse.paho.android.service = MQTT_Service()
        //                org.eclipse.paho.android.service.startForegroundService()

                            Log.i("MQTT", "SERVICE START FROM SWITCH")

                    }else{
                        val test = Intent(this@MainActivity, MQTT_Service::class.java)

        //                val org.eclipse.paho.android.service = MQTT_Service()
        //                org.eclipse.paho.android.service.stopForegroundService()

                        stopService(test)
                        Log.i("MQTT", "SERVICE STOP FROM SWITCH")
                    }
            }else{
                Log.i("MQTT", "No permissions FOREGROUND_SERVICE and FOREGROUND_SERVICE_DATA_SYNC!")
            }

//            if (mqtt){
//                val org.eclipse.paho.android.service = MQTT_Service()
//                org.eclipse.paho.android.service.connect(this@MainActivity)
//
//                Log.i("MQTT", "MQTT START FROM SWITCH")
//            }else{
//                val org.eclipse.paho.android.service = MQTT_Service()
//                org.eclipse.paho.android.service.disconnect()
//                Log.i("MQTT", "MQTT STOP FROM SWITCH")
//            }

//            if (mode){
//               AppCompatDelegate.setDefaultNightMode(
//                   AppCompatDelegate.MODE_NIGHT_YES
//               )
//            }else{
//                AppCompatDelegate.setDefaultNightMode(
//                    AppCompatDelegate.MODE_NIGHT_NO
//                )
//            }
        }
    }



    /*
    =====================================================================================
    ============MQTT MQTT MQTT BEGIN=================
    =====================================================================================
    =====================================================================================
    =====================================================================================
    =====================================================================================
     */

    fun connect(applicationContext : Context) {


        mqttAndroidClient = MqttAndroidClient ( applicationContext,"tcp://103.127.99.151:1883","19453" )
        mqttAndroidClient.setCallback(object : MqttCallback{
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Receive message: ${message.toString()} from topic: $topic")
                messageMQTT = message.toString()
//                notif()
                val service = CounterNotificationService(applicationContext)
                service.showNotification(message.toString())

            }

            override fun connectionLost(cause: Throwable?) {
                Log.d("MQTT", "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "Complete ${token.toString()}")
            }
        })
        try {
            val token = mqttAndroidClient.connect()
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken)                        {
                    Log.i("MQTT", "success ")
                    subscribe("brankas1")
                    //connectionStatus = true
                    // Give your callback on connection established here
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    //connectionStatus = false
                    Log.i("MQTT", "failure")
                    // Give your callback on connection failure here
                    exception.printStackTrace()
                }
            }
        } catch (e: MqttException) {
            // Give your callback on connection failure here
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttAndroidClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttAndroidClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {
        try {
            mqttAndroidClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttAndroidClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("MQTT", "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    /*
    =====================================================================================
    ============MQTT MQTT MQTT END=================
    =====================================================================================
    =====================================================================================
    =====================================================================================
    =====================================================================================
     */


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


                R.id.peforma_sholat -> {
                    if (email == "azka.jsiswanto@gmail.com" || email == "linda.jsiswanto@gmail.com" || email == "jsusilo4444@gmail.com") {
                        val navigationView = findViewById<NavigationView>(R.id.nav_view)
                        navigationView.setNavigationItemSelectedListener(this)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, SholatFragment()).commit()
                        navigationView.setCheckedItem(R.id.peforma_sholat)
                    }else{
                        val navigationView = findViewById<NavigationView>(R.id.nav_view)
                        navigationView.setNavigationItemSelectedListener(this)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, Sholat2Fragment()).commit()
                        navigationView.setCheckedItem(R.id.peforma_sholat)
                    }


                }
            R.id.finansial_plan -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FinansialFragment()).commit()
                navigationView.setCheckedItem(R.id.finansial_plan)
            }
            R.id.events_ -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, EventFragment()).commit()
                navigationView.setCheckedItem(R.id.events_)
            }
            R.id.nav_device -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DeviceListFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_book)
            }
            R.id.nav_log -> {
                val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.setNavigationItemSelectedListener(this)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, UpdateLogFragment()).commit()
                navigationView.setCheckedItem(R.id.nav_book)
            }

            //R.id.nav_update -> supportFragmentManager.beginTransaction()
             //   .replace(R.id.fragment_container, UpdateLogFragment()).commit()

            R.id.nav_logout -> {
                //auth.signOut()
                //startActivity(Intent(this , SignInActivity::class.java))

                showConfirmationDialog()

                //googleSignInClient.signOut();
                //startActivity(Intent(this, SignInActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK))
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) //makesure user cant go back
                //finish()

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun showConfirmationDialog() {
        // Build the AlertDialog
        val isNightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Proceed with the action (e.g., delete the data)

                // Hapus token dari SharedPreferences (untuk login via web)
                val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE).edit()
                prefs.remove("auth_token")  // Hapus token login web
                prefs.apply()

                // Sign out dari Firebase Authentication (untuk login via Google)
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                Toast.makeText(this, "Successfully logout!", Toast.LENGTH_SHORT).show()
                finish()

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    fun requestPermissionMain(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted.
            Log.i("PERMIT", "PERMITED FOR READ_EXTERNAL_STORAGE")
        } else {
            Log.i("PERMIT", "NOT PERMITED FOR READ_EXTERNAL_STORAGE")
            // Permission is not granted.
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted.
            Log.i("PERMIT", "PERMITED FOR WRITE_EXTERNAL_STORAGE")
        } else {
            Log.i("PERMIT", "NOT PERMITED FOR WRITE_EXTERNAL_STORAGE")
            // Permission is not granted.
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted.
            Log.i("PERMIT", "PERMITED FOR POST_NOTIFICATIONS")
        } else {
            Log.i("PERMIT", "NOT PERMITED FOR POST_NOTIFICATIONS")
            // Permission is not granted.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permission is already granted.
//            Log.i("PERMIT", "PERMITED FOR POST_NOTIFICATIONS")
//        } else {
//            Log.i("PERMIT", "NOT PERMITED FOR POST_NOTIFICATIONS")
//            // Permission is not granted.
//            requestPermissionLauncher.launch(Manifest.permission.REQUEST_COMPANION_START_FOREGROUND_SERVICES_FROM_BACKGROUND)
//        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.REORDER_TASKS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted.
            Log.i("PERMIT", "PERMITED FOR POST_NOTIFICATIONS")
        } else {
            Log.i("PERMIT", "NOT PERMITED FOR POST_NOTIFICATIONS")
            // Permission is not granted.
            requestPermissionLauncher.launch(Manifest.permission.REORDER_TASKS)


        }



    }

    fun requestPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                //Nothing
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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


/*
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

        val URL : String ="https://abeazka.my.id/arduino_keypad/input.php?token=$token"

        if (URL.isNotEmpty()){
            val http = OkHttpClient()
            val request = Request.Builder()
                .url(URL)
                .build()
            //myWebView.loadUrl(URL)

            http.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace();
                }

                override fun onResponse(call: Call, response: Response) {
                    val response: Response = http.newCall(request).execute()
                    val responseCode = response.code
                    val results = response.body!!.string()

                    println("Success " + response.toString())
                    println("Success " + response.message.toString())
                    println("Success " + results)
                    Log.i("KODE", "CODE: "+ responseCode)
                    Log.i("Response", "Received response from server. Response")
                    if (response.code == 200){


                    }else{

                        Log.e(
                            "HTTP Error",
                            "Something didn't load, or wasn't succesfully"
                        )

                        return
                    }
                }
            })
        }

    }
*/

    private fun connectToFirebase() {
        val database = FirebaseDatabase.getInstance()
        val reference = database.reference
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val underMaintenance = dataSnapshot.getValue(
                    UnderMaintenance::class.java
                ) ?: return
                if (underMaintenance.is_under_maintenance) {
                    showUnderMaintenanceDialog(underMaintenance.under_maintenance_message)
                } else {
                    dismissUnderMaintenanceDialog()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun showUnderMaintenanceDialog(underMaintenanceMessage: String) {

        val builder = AlertDialog.Builder(this)

        builder.setTitle("AzkaHomeIoT")
        builder.setMessage("$underMaintenanceMessage")

//        builder.setPositiveButton("OK") { dialog, which ->
//            // handle OK button click
//        }



        builder.setCancelable(false)
        val dialog = builder.create()

        dialog.show()

        /*//Inflate the dialog as custom view
        val messageBoxView = LayoutInflater.from(this).inflate(R.layout.message_box, null)
        val x = messageBoxView.findViewById<TextView>(R.id.message_box_header)
        val y = messageBoxView.findViewById<TextView>(R.id.message_box_content)
        //AlertDialogBuilder
        val messageBoxBuilder = AlertDialog.Builder(this).setView(messageBoxView)

        //setting text values
        x.text = "AzkaHomeIoT"
        y.text = "$underMaintenanceMessage"


        //show dialog
        val  messageBoxInstance = messageBoxBuilder.show()

        //set Listener
        messageBoxView.setOnClickListener(){
            //close dialog
            messageBoxInstance.dismiss()
        }*/
    }
    private fun dismissUnderMaintenanceDialog() {
        val dialog = Dialog(this)
        if (dialog != null && dialog.isShowing) dialog.dismiss()
    }


}