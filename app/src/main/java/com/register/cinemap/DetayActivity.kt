package com.register.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.register.cinemap.databinding.ActivityDetayBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetayBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase
    private lateinit var favoriDao: FavoriDao

    private lateinit var yorumAdapter: YorumAdapter
    private var yorumListesi = ArrayList<Yorum>()
    private var isAdmin = false

    private var secilenFilm: Film? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Veritabanı bağlantılarını başlatıyorum ve diğer sayfadan intent ile gelen film objesini alıyorum
        firestore = Firebase.firestore
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "CinemapDB").build()
        favoriDao = db.favoriDao()

        secilenFilm = intent.getSerializableExtra("secilenFilm") as? Film

        binding.recyclerYorumlar.layoutManager = LinearLayoutManager(this)

        if (secilenFilm != null) {
            verileriEkranaBas(secilenFilm!!)
            butonlariKontrolEt()


            if (!secilenFilm!!.documentId.isNullOrEmpty()) {
                adminKontrolEtVeYorumlariYukle(secilenFilm!!.documentId!!)
                kullanicininEskiPuaniniGetir(secilenFilm!!.documentId!!)


                gercekPuanlamayiDinle(secilenFilm!!.documentId!!)
            } else {
                filmIdyiBulVeVerileriYukle()
            }
        }


        binding.fabFavori.setOnClickListener { listeIslemiYap(1) }
        binding.fabListem.setOnClickListener { listeIslemiYap(2) }
        binding.fabIzledim.setOnClickListener { listeIslemiYap(3) }


        binding.ratingBarYorum.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser && rating > 0) {
                //telefonuna listeye kaydet
                izlenenlereOtomatikEkle(rating)

                //internete firestorea kaydet
                puaniFirestoreaKaydet(rating)
            }
        }

        //Admin Film Silme Butonu
        binding.fabSil.setOnClickListener { filmiSilOnayi() }

        binding.btnGonder.setOnClickListener {
            val yorum = binding.edtYorum.text.toString()
            if (yorum.isNotEmpty()) yorumYap(yorum)
            else Toast.makeText(this, "Boş yorum gönderemezsin!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun puaniFirestoreaKaydet(puan: Float) {
        val user = Firebase.auth.currentUser ?: return
        val filmId = secilenFilm?.documentId ?: return

        val puanMap = hashMapOf(
            "puan" to puan,
            "email" to user.email,
            "tarih" to FieldValue.serverTimestamp()
        )

        firestore.collection("Filmler").document(filmId)
            .collection("Puanlar").document(user.uid)
            .set(puanMap)
            .addOnSuccessListener {
                // Başarılı
            }
    }


    private fun filmIdyiBulVeVerileriYukle() {
        val filmAdi = secilenFilm?.filmAdi ?: return

        firestore.collection("Filmler")
            .whereEqualTo("filmAdi", filmAdi)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val realId = doc.id

                    //ID'yi güncelledik
                    secilenFilm?.documentId = realId

                    //Artık ID var islemlere devam ediyoruz
                    adminKontrolEtVeYorumlariYukle(realId)
                    kullanicininEskiPuaniniGetir(realId)

                    //ID bulunduktan sonra puana bak
                    gercekPuanlamayiDinle(realId)
                }
            }
    }


    private fun kullanicininEskiPuaniniGetir(filmId: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        firestore.collection("Filmler").document(filmId)
            .collection("Puanlar").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val eskiPuan = document.getDouble("puan")?.toFloat() ?: 0f
                    binding.ratingBarYorum.rating = eskiPuan
                } else {
                    binding.ratingBarYorum.rating = 0f
                }
            }
    }

    //kullanıcı puan verince arka planda  yerel veritabanındaki İzlediklerim listesini güncelliyorum
    private fun izlenenlereOtomatikEkle(verilenPuan: Float) {
        if (secilenFilm == null) return

        CoroutineScope(Dispatchers.IO).launch {
            val tumKayitlar = favoriDao.tumKayitlariGetir()
            val mevcutKayit = tumKayitlar.find { it.filmAdi == secilenFilm!!.filmAdi && it.tur == 3 }

            if (mevcutKayit == null) {
                // yoksa yeni ekle
                val yeniKayit = FavoriFilm(
                    filmAdi = secilenFilm!!.filmAdi,
                    yonetmen = secilenFilm!!.yonetmen,
                    puan = verilenPuan.toString(),
                    gorselUrl = secilenFilm!!.gorselUrl,
                    aciklama = secilenFilm!!.aciklama,
                    documentId = secilenFilm!!.documentId ?: "",
                    tur = 3
                )
                favoriDao.listeyeEkle(yeniKayit)
            } else {
                // varsa sil ve tekrar ekle
                favoriDao.listedenSil(mevcutKayit)

                val guncelKayit = mevcutKayit.copy(puan = verilenPuan.toString())
                favoriDao.listeyeEkle(guncelKayit)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@DetayActivity, "Puanınız ($verilenPuan) İzlenenlere Kaydedildi! ✅", Toast.LENGTH_SHORT).show()
                butonlariKontrolEt()
            }
        }
    }


    private fun adminKontrolEtVeYorumlariYukle(filmId: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                isAdmin = doc.getString("role") == "admin"

                if (isAdmin) {
                    binding.fabGuncelle.visibility = View.VISIBLE
                    binding.fabSil.visibility = View.VISIBLE
                }

                yorumAdapter = YorumAdapter(yorumListesi, isAdmin) { silinecekYorum ->
                    yorumuSil(silinecekYorum)
                }
                binding.recyclerYorumlar.adapter = yorumAdapter

                yorumlariDinle(filmId)
            }
            .addOnFailureListener {
                yorumlariDinle(filmId)
            }

        binding.fabGuncelle.setOnClickListener {
            val intent = Intent(this, EkleActivity::class.java)
            intent.putExtra("guncellemeModu", true)
            intent.putExtra("mevcutFilm", secilenFilm)
            startActivity(intent)
        }
    }

    private fun filmiSilOnayi() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Filmi Sil")
        builder.setMessage("Bu filmi veritabanından tamamen silmek istediğinize emin misiniz?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Evet, Sil") { _, _ ->
            secilenFilm?.documentId?.let { docId ->
                firestore.collection("Filmler").document(docId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Film başarıyla silindi 👋", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Silme hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        builder.setNegativeButton("Vazgeç") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun butonlariKontrolEt() {
        if (secilenFilm == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val tumKayitlar = favoriDao.tumKayitlariGetir()
            val favoride = tumKayitlar.any { it.filmAdi == secilenFilm!!.filmAdi && it.tur == 1 }
            val listemde = tumKayitlar.any { it.filmAdi == secilenFilm!!.filmAdi && it.tur == 2 }
            val izlendi = tumKayitlar.any { it.filmAdi == secilenFilm!!.filmAdi && it.tur == 3 }

            withContext(Dispatchers.Main) {
                if (favoride) binding.fabFavori.setImageResource(android.R.drawable.btn_star_big_on)
                else binding.fabFavori.setImageResource(android.R.drawable.btn_star_big_off)

                binding.fabListem.setColorFilter(if (listemde) android.graphics.Color.GREEN else android.graphics.Color.WHITE)
                binding.fabIzledim.setColorFilter(if (izlendi) android.graphics.Color.CYAN else android.graphics.Color.WHITE)
            }
        }
    }

    private fun listeIslemiYap(tur: Int) {
        if (secilenFilm == null) return
        CoroutineScope(Dispatchers.IO).launch {
            val tumKayitlar = favoriDao.tumKayitlariGetir()
            val mevcutKayit = tumKayitlar.find { it.filmAdi == secilenFilm!!.filmAdi && it.tur == tur }

            withContext(Dispatchers.Main) {
                if (mevcutKayit != null) {
                    launch(Dispatchers.IO) { favoriDao.listedenSil(mevcutKayit) }
                    val mesaj = when(tur) {
                        1 -> "Favorilerden çıkarıldı"
                        2 -> "İzleme listesinden çıkarıldı"
                        else -> "İzleme geçmişinden çıkarıldı"
                    }
                    Toast.makeText(this@DetayActivity, mesaj, Toast.LENGTH_SHORT).show()
                } else {
                    val yeniKayit = FavoriFilm(
                        filmAdi = secilenFilm!!.filmAdi,
                        yonetmen = secilenFilm!!.yonetmen,
                        puan = secilenFilm!!.puan,
                        gorselUrl = secilenFilm!!.gorselUrl,
                        aciklama = secilenFilm!!.aciklama,
                        documentId = secilenFilm!!.documentId ?: "",
                        tur = tur
                    )
                    launch(Dispatchers.IO) { favoriDao.listeyeEkle(yeniKayit) }
                    Toast.makeText(this@DetayActivity, "Listeye eklendi ✅", Toast.LENGTH_SHORT).show()
                }
                butonlariKontrolEt()
            }
        }
    }

    private fun yorumuSil(yorum: Yorum) {
        secilenFilm?.documentId?.let { filmId ->
            firestore.collection("Filmler").document(filmId)
                .collection("Yorumlar").document(yorum.yorumId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Yorum silindi 👋", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun yorumYap(yorumMetni: String) {
        val userEmail = Firebase.auth.currentUser?.email ?: "Anonim"
        val nickname = userEmail.substringBefore("@")
        val puan = try { binding.ratingBarYorum.rating } catch (e: Exception) { 0f }

        val yorumMap = hashMapOf<String, Any>(
            "yorum" to yorumMetni,
            "yapan" to nickname,
            "email" to userEmail,
            "puan" to puan,
            "tarih" to FieldValue.serverTimestamp(),
            "upvotes" to 0L,
            "downvotes" to 0L,
            "votedUsers" to hashMapOf<String, Long>(),
            "filmAdi" to (secilenFilm?.filmAdi ?: ""),
            "filmGorsel" to (secilenFilm?.gorselUrl ?: "")
        )

        secilenFilm?.documentId?.let { id ->
            firestore.collection("Filmler").document(id).collection("Yorumlar").add(yorumMap)
                .addOnSuccessListener {
                    binding.edtYorum.text.clear()
                    Toast.makeText(this, "Yorum gönderildi! 🌟", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun yorumlariDinle(filmId: String) {
        firestore.collection("Filmler").document(filmId)
            .collection("Yorumlar")
            .orderBy("tarih", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error == null && value != null) {
                    yorumListesi.clear()


                    for (doc in value.documents) {
                        val puan = (doc.getDouble("puan") ?: 0.0).toFloat()
                        val yeniYorum = Yorum(
                            yorumId = doc.id,
                            filmId = filmId,
                            yorum = doc.getString("yorum") ?: "",
                            yapan = doc.getString("yapan") ?: "",
                            email = doc.getString("email") ?: "",
                            puan = puan,
                            upvotes = doc.getLong("upvotes") ?: 0L,
                            downvotes = doc.getLong("downvotes") ?: 0L,
                            votedUsers = doc.get("votedUsers") as? Map<String, Long> ?: HashMap(),
                            filmAdi = doc.getString("filmAdi") ?: "",
                            filmGorsel = doc.getString("filmGorsel") ?: ""
                        )
                        yorumListesi.add(yeniYorum)
                    }
                    yorumAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun verileriEkranaBas(film: Film) {
        binding.txtDetayBaslik.text = film.filmAdi
        binding.txtDetayAciklama.text = film.aciklama
        binding.txtDetayTur.text = if (!film.tur.isNullOrEmpty()) film.tur else "Tür Yok"
        binding.txtDetayYil.text = if (film.cikisYili != 0) film.cikisYili.toString() else "Yıl Bilgisi Yok"
        binding.txtDetayBasroller.text = if (film.basroller.isNotEmpty()) "Başroller: ${film.basroller}" else "Oyuncu Bilgisi Yok"


        Glide.with(this)
            .load(film.gorselUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(binding.imgDetayResim)
    }


    private fun gercekPuanlamayiDinle(filmId: String) {
        firestore.collection("Filmler").document(filmId)
            .collection("Puanlar")
            .addSnapshotListener { value, error ->
                if (error == null && value != null) {
                    var toplamPuan = 0.0
                    val oyVerenSayisi = value.size()

                    for (doc in value) {
                        toplamPuan += (doc.getDouble("puan") ?: 0.0)
                    }


                    binding.txtCineMapPuan.text = if (oyVerenSayisi > 0) {
                        val ort = String.format("%.1f", toplamPuan / oyVerenSayisi)
                        binding.txtCineMapPuan.setTextColor(android.graphics.Color.GREEN)
                        "CineMap: ⭐ $ort ($oyVerenSayisi oy)"
                    } else {

                        binding.txtCineMapPuan.setTextColor(android.graphics.Color.YELLOW)
                        "IMDb: ⭐ ${secilenFilm?.puan ?: "0.0"}"
                    }
                }
            }
    }
}