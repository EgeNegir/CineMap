package com.register.cinemap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.register.cinemap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var filmListesi: ArrayList<Film>
    private lateinit var filmAdapter: FilmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore

        // Ekle butonu varsayılan gizli (Admin kontrolü açacak)
        binding.fabEkle.visibility = View.GONE

        filmListesi = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        filmAdapter = FilmAdapter(filmListesi)
        binding.recyclerView.adapter = filmAdapter

        binding.btnHaritaAc.setOnClickListener {
            val intent = Intent(this, HaritaActivity::class.java)
            startActivity(intent)
        }


        verileriGetir()

        //
        adminKontrolEt()


        // Eğer veritabanı boşsa
        topluFilmEkle()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        binding.btnSort.setOnClickListener { showSortMenu(it) }

        binding.fabEkle.setOnClickListener {
            startActivity(Intent(this, EkleActivity::class.java))
        }

        binding.btnProfil.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
        }
    }

    private fun adminKontrolEt() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val role = document.getString("role")
                        if (role == "admin") {
                            binding.fabEkle.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }

    private fun verileriGetir(
        siralamaAlani: String = "filmAdi",
        yon: Query.Direction = Query.Direction.ASCENDING,
        filtreTur: String? = null
    ) {
        var query: Query = firestore.collection("Filmler")
        if (filtreTur != null) query = query.whereEqualTo("tur", filtreTur)

        query.orderBy(siralamaAlani, yon)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    filmListesi.clear()
                    for (document in value.documents) {
                        val film = document.toObject(Film::class.java)
                        if (film != null) {
                            // ID'yi kaydetmek çok önemli
                            film.documentId = document.id
                            filmListesi.add(film)
                        }
                    }
                    filmAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun filterList(query: String?) {
        val filteredList = ArrayList<Film>()
        for (film in filmListesi) {
            if (film.filmAdi.lowercase().contains(query?.lowercase() ?: "")) {
                filteredList.add(film)
            }
        }
        filmAdapter.updateList(filteredList)
    }

    private fun showSortMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.sort_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_rating_desc -> verileriGetir("puan", Query.Direction.DESCENDING)
                R.id.sort_rating_asc -> verileriGetir("puan", Query.Direction.ASCENDING)
                R.id.sort_year_desc -> verileriGetir("cikisYili", Query.Direction.DESCENDING)
                R.id.sort_year_asc -> verileriGetir("cikisYili", Query.Direction.ASCENDING)
                R.id.sort_name_asc -> verileriGetir("filmAdi", Query.Direction.ASCENDING)
                R.id.sort_name_desc -> verileriGetir("filmAdi", Query.Direction.DESCENDING)
                R.id.filter_all -> verileriGetir()
                R.id.filter_action -> verileriGetir(filtreTur = "Aksiyon")
                R.id.filter_drama -> verileriGetir(filtreTur = "Dram")
                R.id.filter_sci_fi -> verileriGetir(filtreTur = "Bilim Kurgu")
                R.id.filter_horror -> verileriGetir(filtreTur = "Korku")
                R.id.filter_comedy -> verileriGetir(filtreTur = "Komedi")
            }
            true
        }
        popup.show()
    }

    // FİLMLERİ EKLEYEN FONKSİYON
    private fun topluFilmEkle() {
        val db = Firebase.firestore

        // once içerisi bos mu diye bakıyoruz doluysa ekleme yapmaz
        db.collection("Filmler").get().addOnSuccessListener { result ->
            if (!result.isEmpty) {
                return@addOnSuccessListener
            }


            val filmler = listOf(
                hashMapOf(
                    "filmAdi" to "The Matrix",
                    "yonetmen" to "Lana Wachowski",
                    "puan" to "8.7",
                    "tur" to "Bilim Kurgu",
                    "cikisYili" to 1999,
                    "basroller" to "Keanu Reeves, Laurence Fishburne",
                    "aciklama" to "Bir bilgisayar korsanı, gerçekliğin doğasını ve insanlığı hapseden sistemi keşfeder.",
                    "gorselUrl" to "https://filmpravda.com/wp-content/uploads/2018/07/the-matrix.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "The Godfather",
                    "yonetmen" to "Francis Ford Coppola",
                    "puan" to "9.2",
                    "tur" to "Suç",
                    "cikisYili" to 1972,
                    "basroller" to "Marlon Brando, Al Pacino",
                    "aciklama" to "New York'ta güçlü bir İtalyan mafya ailesinin yaşlanan patriği, imparatorluğunu oğluna devreder.",
                    "gorselUrl" to "https://tr.web.img4.acsta.net/c_310_420/medias/nmedia/18/91/63/78/20155809.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "The Shawshank Redemption",
                    "yonetmen" to "Frank Darabont",
                    "puan" to "9.3",
                    "tur" to "Dram",
                    "cikisYili" to 1994,
                    "basroller" to "Tim Robbins, Morgan Freeman",
                    "aciklama" to "Haksız yere hapse atılan bir adamın, umudunu kaybetmeden yıllar süren mücadelesi.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Inception",
                    "yonetmen" to "Christopher Nolan",
                    "puan" to "8.8",
                    "tur" to "Bilim Kurgu",
                    "cikisYili" to 2010,
                    "basroller" to "Leonardo DiCaprio, Joseph Gordon-Levitt",
                    "aciklama" to "Rüyalara girip insanların sırlarını çalan bir hırsızın, bir fikri zihne yerleştirme görevi.",
                    "gorselUrl" to "https://i.pinimg.com/1200x/89/87/ce/8987ce6a44674f18846622a9cc0e9867.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Forrest Gump",
                    "yonetmen" to "Robert Zemeckis",
                    "puan" to "8.8",
                    "tur" to "Dram",
                    "cikisYili" to 1994,
                    "basroller" to "Tom Hanks, Robin Wright",
                    "aciklama" to "Düşük IQ'ya sahip Forrest Gump'ın, ABD tarihinin önemli olaylarına tanıklık etmesi.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Gladiator",
                    "yonetmen" to "Ridley Scott",
                    "puan" to "8.5",
                    "tur" to "Aksiyon",
                    "cikisYili" to 2000,
                    "basroller" to "Russell Crowe, Joaquin Phoenix",
                    "aciklama" to "İhanete uğrayan bir Roma generalinin, köle gladyatör olarak intikam arayışı.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/ty8TGRuvJLPUmAR1H1nRIsgwvim.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Joker",
                    "yonetmen" to "Todd Phillips",
                    "puan" to "8.2",
                    "tur" to "Suç",
                    "cikisYili" to 2019,
                    "basroller" to "Joaquin Phoenix, Robert De Niro",
                    "aciklama" to "Toplum tarafından dışlanan Arthur Fleck'in, suç dünyasının prensi Joker'e dönüşümü.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "The Lord of the Rings: The Return of the King",
                    "yonetmen" to "Peter Jackson",
                    "puan" to "9.0",
                    "tur" to "Fantastik",
                    "cikisYili" to 2003,
                    "basroller" to "Elijah Wood, Viggo Mortensen",
                    "aciklama" to "Sauron'un ordularına karşı verilen son savaş ve Yüzük'ün yok edilme mücadelesi.",
                    "gorselUrl" to "https://m.media-amazon.com/images/I/71Xle4-8u+L._AC_SY879_.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Fight Club",
                    "yonetmen" to "David Fincher",
                    "puan" to "8.8",
                    "tur" to "Dram",
                    "cikisYili" to 1999,
                    "basroller" to "Brad Pitt, Edward Norton",
                    "aciklama" to "Uykusuzluk çeken bir ofis çalışanı ve karizmatik bir sabun satıcısının kurduğu yeraltı dövüş kulübü.",
                    "gorselUrl" to "https://m.media-amazon.com/images/I/61IgtYrLF5L._AC_SY879_.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Pulp Fiction",
                    "yonetmen" to "Quentin Tarantino",
                    "puan" to "8.9",
                    "tur" to "Suç",
                    "cikisYili" to 1994,
                    "basroller" to "John Travolta, Uma Thurman",
                    "aciklama" to "Los Angeles yeraltı dünyasından iç içe geçmiş, şiddet ve mizah dolu dört hikaye.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Interstellar",
                    "yonetmen" to "Christopher Nolan",
                    "puan" to "8.7",
                    "tur" to "Bilim Kurgu",
                    "cikisYili" to 2014,
                    "basroller" to "Matthew McConaughey, Anne Hathaway",
                    "aciklama" to "İnsanlığın kurtuluşu için solucan deliğinden geçen bir grup astronotun hikayesi.",
                    "gorselUrl" to "https://i.pinimg.com/736x/5b/72/20/5b7220715adb184fe113d899e081694b.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Spider-Man: Into the Spider-Verse",
                    "yonetmen" to "Peter Ramsey",
                    "puan" to "8.4",
                    "tur" to "Animasyon",
                    "cikisYili" to 2018,
                    "basroller" to "Shameik Moore, Jake Johnson",
                    "aciklama" to "Farklı evrenlerden gelen Örümcek Adamların, dünyayı kurtarmak için bir araya gelmesi.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/iiZZdoQBEYBv6id8su7ImL0oCbD.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Back to the Future",
                    "yonetmen" to "Robert Zemeckis",
                    "puan" to "8.5",
                    "tur" to "Bilim Kurgu",
                    "cikisYili" to 1985,
                    "basroller" to "Michael J. Fox, Christopher Lloyd",
                    "aciklama" to "Bir lise öğrencisinin, eksantrik bir bilim adamının icat ettiği zaman makinesiyle geçmişe gitmesi.",
                    "gorselUrl" to "https://upload.wikimedia.org/wikipedia/tr/3/33/Back_to_the_Future_%28film%2C_1985%29.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "The Lion King",
                    "yonetmen" to "Roger Allers",
                    "puan" to "8.5",
                    "tur" to "Animasyon",
                    "cikisYili" to 1994,
                    "basroller" to "Matthew Broderick, Jeremy Irons",
                    "aciklama" to "Genç aslan prens Simba'nın, babasının ölümünden sonra krallığı geri alma mücadelesi.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/sKCr78MXSLixwmZ8DyJLrpMsd15.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Schindler's List",
                    "yonetmen" to "Steven Spielberg",
                    "puan" to "9.0",
                    "tur" to "Biyografi",
                    "cikisYili" to 1993,
                    "basroller" to "Liam Neeson, Ralph Fiennes",
                    "aciklama" to "İkinci Dünya Savaşı sırasında 1000'den fazla Yahudi mülteciyi kurtaran sanayici Oskar Schindler'in hikayesi.",
                    "gorselUrl" to "https://upload.wikimedia.org/wikipedia/tr/3/38/Schindler%27s_List_movie.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "Avengers: Endgame",
                    "yonetmen" to "Anthony Russo",
                    "puan" to "8.4",
                    "tur" to "Aksiyon",
                    "cikisYili" to 2019,
                    "basroller" to "Robert Downey Jr., Chris Evans",
                    "aciklama" to "Thanos'un evrenin yarısını yok etmesinden sonra kalan kahramanların son savaşı.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg"
                ),
                hashMapOf(
                    "filmAdi" to "The Dark Knight",
                    "yonetmen" to "Christopher Nolan",
                    "puan" to "9.0",
                    "tur" to "Aksiyon",
                    "cikisYili" to 2008,
                    "basroller" to "Christian Bale, Heath Ledger",
                    "aciklama" to "Batman, Joker'in Gotham'da yarattığı kaos ve anarşi ile yüzleşmek zorundadır.",
                    "gorselUrl" to "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"
                )
            )

            for (film in filmler) {
                db.collection("Filmler").add(film)
            }
            Toast.makeText(this, "✅  Filmler Yüklendi!", Toast.LENGTH_LONG).show()
        }
    }
}