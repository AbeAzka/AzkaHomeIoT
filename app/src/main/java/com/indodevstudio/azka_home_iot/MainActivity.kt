package com.indodevstudio.azka_home_iot


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import com.indodevstudio.azka_home_iot.databinding.ActivityMainBinding
import com.indodevstudio.azka_home_iot.utils.FirebaseUtils.firebaseAuth
import info.mqtt.android.service.MqttAndroidClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
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
import java.net.URL


class MainActivity :  AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {
    val msg = "messageMQTT"
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding : ActivityMainBinding
    var foregoundServiceIntent : Intent? = null
    var image : Bitmap? = null
    lateinit var inputStream : InputStream
    private lateinit var auth : FirebaseAuth
    private lateinit var mFirebaseUser : FirebaseUser
    lateinit var mGoogleSignInClient: GoogleSignInClient

    private var hot_number = 0
    private var ui_hot: TextView? = null

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




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val test = Intent(this, MQTT_Service::class.java)
        //startService(test)

        val sharedPreferenceManger = SharedPreferenceManger(this)
        AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[sharedPreferenceManger.theme])
        //connect(this)
        Log.i("MQTT", "MAIN ACTIVITY MQTT RUN")

        //notificationChannel()
//        mqtt = MQTT()
//        mqtt.connect(this@MainActivity)
//        mqtt.receiveMessages()

        connectDB()
        //subscribe_mqtt("sending_telemetri2")
        //connect(this)
        requestPermission()
        requestPermissionMain()
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)


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
        val nama = headerView.findViewById<TextView>(R.id.nama)

        val profilepc = headerView.findViewById<ImageView>(R.id.logo_p)

        val em = headerView.findViewById<TextView>(R.id.emailGet)
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

        ui_hot = view.findViewById(R.id.hotlist_hot)





        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            Options()
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
            Glide.with(this).load(picture3).into(profilepc)

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
        catch (e: Exception){
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

    




    override fun onResume() {
        Options()
        super.onResume()
    }

    fun connectDB(){


    }

    fun updateHotCount(new_hot_number : Int){
        hot_number = new_hot_number
        if(ui_hot == null) return
        runOnUiThread {
            if (new_hot_number === 0) ui_hot!!.visibility = View.INVISIBLE else {
                ui_hot!!.visibility = View.VISIBLE
                ui_hot!!.text = Integer.toString(new_hot_number)
            }
        }
    }

    private fun Options(){
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val notif = prefs.getBoolean("notif", true)
        val mqtt = prefs.getBoolean("mqtt", false)
        binding.apply {
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
}