package com.register.cinemap

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.register.cinemap.databinding.RowProfilOzetBinding


class ProfilOzetAdapter(private val filmListesi: List<FavoriFilm>) :
    RecyclerView.Adapter<ProfilOzetAdapter.OzetHolder>() {

    class OzetHolder(val binding: RowProfilOzetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OzetHolder {

        val binding = RowProfilOzetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OzetHolder(binding)
    }

    override fun getItemCount(): Int = filmListesi.size

    override fun onBindViewHolder(holder: OzetHolder, position: Int) {
        val favoriFilm = filmListesi[position]


        Glide.with(holder.itemView.context)
            .load(favoriFilm.gorselUrl)
            .into(holder.binding.imgOzetFilm)


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