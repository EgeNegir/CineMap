package com.register.cinemap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.register.cinemap.databinding.ActivityFavorilerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavorilerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavorilerBinding
    private lateinit var db: AppDatabase
    private lateinit var favoriDao: FavoriDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavorilerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "CinemapDB").build()
        favoriDao = db.favoriDao()

        binding.recyclerFavoriler.layoutManager = LinearLayoutManager(this)


        val tur = intent.getIntExtra("listeTuru", 1)

        val baslik = when(tur) {
            1 -> "FAVORİLERİM"
            2 -> "İZLEME LİSTEM"
            3 -> "İZLEME GEÇMİŞİM"
            else -> "LİSTELERİM"
        }


        binding.txtBaslikFavori.text = baslik

        verileriGetir()
    }

    private fun verileriGetir() {
        val tur = intent.getIntExtra("listeTuru", 1)

        CoroutineScope(Dispatchers.IO).launch {
            val filmListesi = favoriDao.getListeByTur(tur)

            withContext(Dispatchers.Main) {
                val adapter = FavoriAdapter(filmListesi)
                binding.recyclerFavoriler.adapter = adapter
            }
        }
    }
}