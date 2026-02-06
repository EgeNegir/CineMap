package com.register.cinemap

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // Kullanıcı kontrolü yapıyoruz
            val auth = Firebase.auth
            val currentUser = auth.currentUser

            if (currentUser != null) {
                // Eğer kullanıcı zaten giriş yapmışsa direkt Ana Sayfaya git
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Giriş yapmamışsa Login sayfasına git
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 3000)
    }
}