package com.register.cinemap

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.register.cinemap.databinding.ActivityEkleBinding
import java.util.UUID

class EkleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEkleBinding
    private var secilenGorselUri: Uri? = null


    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>


    private var guncellemeModu = false
    private var guncellenecekFilm: Film? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEkleBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firestore = Firebase.firestore
        storage = Firebase.storage
        registerLauncher()


        val turler = arrayOf("Aksiyon", "Dram", "Bilim Kurgu", "Korku", "Komedi", "Animasyon", "Belgesel")


        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, turler)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTur.adapter = adapter

        guncellemeModu = intent.getBooleanExtra("guncellemeModu", false)
        if (guncellemeModu) {
            guncellenecekFilm = intent.getSerializableExtra("mevcutFilm") as? Film
            bilgileriDoldur(turler)
        }

        binding.imgFilmAfis.setOnClickListener {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intentToGallery)
        }

        binding.btnKaydet.setOnClickListener {
            yuklemeIsleminiBaslat()
        }
    }

    private fun bilgileriDoldur(turler: Array<String>) {
        guncellenecekFilm?.let {
            binding.edtFilmAdi.setText(it.filmAdi)
            binding.edtYonetmen.setText(it.yonetmen)
            binding.edtPuan.setText(it.puan)
            binding.edtAciklama.setText(it.aciklama)
            binding.edtCikisYili.setText(it.cikisYili.toString())
            binding.edtBasroller.setText(it.basroller)

            val turIndex = turler.indexOf(it.tur)
            if (turIndex >= 0) {
                binding.spinnerTur.setSelection(turIndex)
            }


            binding.btnKaydet.text = "BİLGİLERİ GÜNCELLE"
            Glide.with(this).load(it.gorselUrl).into(binding.imgFilmAfis)
        }
    }


    private fun yuklemeIsleminiBaslat() {
        val filmAdi = binding.edtFilmAdi.text.toString()
        val yonetmen = binding.edtYonetmen.text.toString()
        val puan = binding.edtPuan.text.toString()
        val aciklama = binding.edtAciklama.text.toString()
        val cikisYili = binding.edtCikisYili.text.toString().toIntOrNull() ?: 0
        val basroller = binding.edtBasroller.text.toString()
        val tur = binding.spinnerTur.selectedItem.toString()

        if (filmAdi.isNotEmpty() && puan.isNotEmpty()) {
            binding.btnKaydet.isEnabled = false


            if (secilenGorselUri == null && guncellemeModu) {
                veritabaninaKaydet(filmAdi, yonetmen, puan, aciklama, guncellenecekFilm!!.gorselUrl, cikisYili, basroller, tur)
            }

            else if (secilenGorselUri != null) {
                val uuid = UUID.randomUUID()
                val gorselReference = storage.reference.child("filmGorselleri").child("$uuid.jpg")

                gorselReference.putFile(secilenGorselUri!!).addOnSuccessListener {

                    gorselReference.downloadUrl.addOnSuccessListener { uri ->
                        veritabaninaKaydet(filmAdi, yonetmen, puan, aciklama, uri.toString(), cikisYili, basroller, tur)
                    }
                }
            } else {
                Toast.makeText(this, "Lütfen bir resim seçin!", Toast.LENGTH_SHORT).show()
                binding.btnKaydet.isEnabled = true
            }
        }
    }


    private fun veritabaninaKaydet(ad: String, yon: String, puan: String, aciklama: String, gorselUrl: String, yil: Int, basroller: String, tur: String) {
        val filmMap = hashMapOf<String, Any>(
            "filmAdi" to ad,
            "yonetmen" to yon,
            "puan" to puan,
            "aciklama" to aciklama,
            "gorselUrl" to gorselUrl,
            "cikisYili" to yil,
            "basroller" to basroller,
            "tur" to tur,
            "sonGuncellenme" to com.google.firebase.Timestamp.now()
        )


        if (guncellemeModu && guncellenecekFilm != null) {
            firestore.collection("Filmler").document(guncellenecekFilm!!.documentId!!)
                .update(filmMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Film güncellendi! ✅", Toast.LENGTH_SHORT).show()
                    finish()
                }
        } else {
            filmMap["eklenmeTarihi"] = com.google.firebase.Timestamp.now()
            firestore.collection("Filmler").add(filmMap).addOnSuccessListener {
                Toast.makeText(this, "Film başarıyla eklendi! 🎉", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    private fun registerLauncher() {
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                secilenGorselUri = result.data?.data
                binding.imgFilmAfis.setImageURI(secilenGorselUri)
            }
        }
    }
}