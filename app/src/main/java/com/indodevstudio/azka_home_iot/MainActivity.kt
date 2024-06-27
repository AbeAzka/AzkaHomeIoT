package com.indodevstudio.azka_home_iot

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.indodevstudio.azka_home_iot.utils.FirebaseUtils.firebaseAuth


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            R.id.nav_logout -> {
                firebaseAuth.signOut()
                startActivity(Intent(this, SignInActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) //makesure user cant go back
                finish()
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}