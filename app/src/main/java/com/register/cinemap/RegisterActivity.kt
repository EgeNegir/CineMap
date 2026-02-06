package com.register.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.register.cinemap.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth //fb yetki

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //yetkiliyi baslat
        auth = Firebase.auth

        // Giriş Yapa basınca geri dön
        binding.txtGirisYap.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //kayol
        binding.btnKayitOl.setOnClickListener {
            val email = binding.edtKayitEmail.text.toString()
            val sifre = binding.edtKayitSifre.text.toString()

            // Kutular boş mu kontrol et
            if (email.isEmpty() || sifre.isEmpty()) {
                Toast.makeText(this, "Lütfen alanları doldurun!", Toast.LENGTH_SHORT).show()
            } else {
                // FIREBASE kayıt butonu
                auth.createUserWithEmailAndPassword(email, sifre)
                    .addOnSuccessListener {
                        // Başarılı olursa
                        Toast.makeText(this, "Kayıt Başarılı! Hoşgeldiniz.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { hata ->

                        Toast.makeText(this, "Hata: ${hata.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}