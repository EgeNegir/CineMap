package com.register.cinemap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.register.cinemap.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth


        binding.txtSifremiUnuttum.setOnClickListener {
            val emailKutusu = android.widget.EditText(this)
            emailKutusu.hint = "E-posta adresinizi girin"
            emailKutusu.background = null
            emailKutusu.setPadding(50, 20, 50, 20)

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Şifre Sıfırlama")
                .setMessage("Şifrenizi sıfırlamak için kayıtlı e-posta adresinizi girin. Size bir link göndereceğiz.")
                .setView(emailKutusu)
                .setPositiveButton("GÖNDER") { _, _ ->
                    val email = emailKutusu.text.toString().trim()
                    if (email.isNotEmpty()) {

                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                android.widget.Toast.makeText(this, "Sıfırlama linki mailinize gönderildi! 📩", android.widget.Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                android.widget.Toast.makeText(this, "Hata: ${it.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        android.widget.Toast.makeText(this, "Lütfen mail adresinizi yazın.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }


        binding.txtKayitOl.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }


        binding.btnGiris.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val sifre = binding.edtSifre.text.toString()


            if (email.isEmpty() || sifre.isEmpty()) {
                Toast.makeText(this, "Lütfen e-posta ve şifreyi girin!", Toast.LENGTH_SHORT).show()
            } else {

                auth.signInWithEmailAndPassword(email, sifre)
                    .addOnSuccessListener {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { hata ->
                        Toast.makeText(this, "Giriş Başarısız: ${hata.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}