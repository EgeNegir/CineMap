package com.register.cinemap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.register.cinemap.databinding.ActivityProfilBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    private lateinit var db: AppDatabase
    private lateinit var favoriDao: FavoriDao


    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.txtProfilEmail.text = Firebase.auth.currentUser?.email


        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "CinemapDB").build()
        favoriDao = db.favoriDao()


        binding.btnSifreDegistir.setOnClickListener {
            val yeniSifreKutusu = android.widget.EditText(this)
            yeniSifreKutusu.hint = "Yeni şifrenizi girin (En az 6 karakter)"
            yeniSifreKutusu.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            yeniSifreKutusu.setPadding(50, 20, 50, 20)

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Şifre Değiştir")
                .setMessage("Lütfen yeni şifrenizi belirleyin.")
                .setView(yeniSifreKutusu)
                .setPositiveButton("GÜNCELLE") { _, _ ->
                    val yeniSifre = yeniSifreKutusu.text.toString().trim()

                    if (yeniSifre.length >= 6) {

                        val user = Firebase.auth.currentUser

                        user?.updatePassword(yeniSifre)
                            ?.addOnSuccessListener {
                                Toast.makeText(this, "Şifreniz başarıyla değiştirildi! ✅", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(this, "Hata: ${e.localizedMessage}. Lütfen çıkış yapıp tekrar girdikten sonra deneyin.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Şifre en az 6 karakter olmalı!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Vazgeç", null)
                .show()
        }


        fotoYukle()
        registerLauncher()


        binding.imgProfilFoto.setOnClickListener {
            val intentToGallery = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intentToGallery.addCategory(Intent.CATEGORY_OPENABLE)
            intentToGallery.type = "image/*"
            activityResultLauncher.launch(intentToGallery)
        }


        setupRecyclers()
        verileriYukle()


        binding.btnTumunuGorFavori.setOnClickListener { tumunuGoster(1) }
        binding.btnTumunuGorListem.setOnClickListener { tumunuGoster(2) }
        binding.btnTumunuGorGecmis.setOnClickListener { tumunuGoster(3) }


        binding.btnCikisYap.setOnClickListener {

            Firebase.auth.signOut()


            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()

            Toast.makeText(this, "Başarıyla çıkış yapıldı.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if (imageData != null) {

                        contentResolver.takePersistableUriPermission(imageData, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        binding.imgProfilFoto.setImageURI(imageData)
                        fotoKaydet(imageData)
                    }
                }
            }
        }
    }


    private fun fotoKaydet(uri: Uri) {
        val sharedPreferences = getSharedPreferences("com.register.cinemap", MODE_PRIVATE)
        sharedPreferences.edit().putString("profilFotoUri", uri.toString()).apply()
        Toast.makeText(this, "Profil fotoğrafı güncellendi!", Toast.LENGTH_SHORT).show()
    }


    private fun fotoYukle() {
        val sharedPreferences = getSharedPreferences("com.register.cinemap", MODE_PRIVATE)
        val uriString = sharedPreferences.getString("profilFotoUri", null)
        if (uriString != null) {
            try {
                val uri = Uri.parse(uriString)
                binding.imgProfilFoto.setImageURI(uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun setupRecyclers() {
        binding.recyclerFavoriOzet.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerListemOzet.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerGecmisOzet.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }


    private fun verileriYukle() {
        CoroutineScope(Dispatchers.IO).launch {
            val favs = favoriDao.getListeByTur(1).reversed().take(5)
            val watchlist = favoriDao.getListeByTur(2).reversed().take(5)
            val history = favoriDao.getListeByTur(3).reversed().take(5)

            withContext(Dispatchers.Main) {
                binding.recyclerFavoriOzet.adapter = ProfilOzetAdapter(favs)
                binding.recyclerListemOzet.adapter = ProfilOzetAdapter(watchlist)
                binding.recyclerGecmisOzet.adapter = ProfilOzetAdapter(history)
            }
        }
    }

    private fun tumunuGoster(tur: Int) {
        val intent = Intent(this, FavorilerActivity::class.java)
        intent.putExtra("listeTuru", tur)
        startActivity(intent)
    }
}