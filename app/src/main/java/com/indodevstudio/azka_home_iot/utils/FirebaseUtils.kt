package com.indodevstudio.azka_home_iot.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
object FirebaseUtils {
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
}