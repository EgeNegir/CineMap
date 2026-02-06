package com.register.cinemap

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.register.cinemap.databinding.RecyclerRowBinding


class FavoriAdapter(val favoriListesi: List<FavoriFilm>) : RecyclerView.Adapter<FavoriAdapter.FavoriHolder>() {


    class FavoriHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriHolder {
        // her satir icin recycler_row tasarimini bagliyoruz
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriHolder(binding)
    }

    override fun getItemCount(): Int {

        return favoriListesi.size
    }

    override fun onBindViewHolder(holder: FavoriHolder, position: Int) {
        val favoriFilm = favoriListesi[position]


        holder.binding.recyclerFilmAdi.text = favoriFilm.filmAdi
        holder.binding.recyclerYonetmen.text = "Yönetmen: ${favoriFilm.yonetmen}"
        holder.binding.recyclerPuan.text = "IMDb: ${favoriFilm.puan}"


        Glide.with(holder.itemView.context)
            .load(favoriFilm.gorselUrl)
            .into(holder.binding.recyclerImageView)


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetayActivity::class.java)


            val tasinacakFilm = Film(
                filmAdi = favoriFilm.filmAdi,
                yonetmen = favoriFilm.yonetmen,
                puan = favoriFilm.puan,
                gorselUrl = favoriFilm.gorselUrl,
                aciklama = favoriFilm.aciklama,
                documentId = favoriFilm.documentId
            )

            intent.putExtra("secilenFilm", tasinacakFilm)
            holder.itemView.context.startActivity(intent)
        }
    }
}