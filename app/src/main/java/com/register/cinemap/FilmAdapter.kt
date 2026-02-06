package com.register.cinemap

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.register.cinemap.databinding.RecyclerRowBinding


class FilmAdapter(private var filmListesi: ArrayList<Film>) : RecyclerView.Adapter<FilmAdapter.FilmHolder>() {


    class FilmHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmHolder {

        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilmHolder(binding)
    }

    override fun getItemCount(): Int {

        return filmListesi.size
    }

    override fun onBindViewHolder(holder: FilmHolder, position: Int) {

        val film = filmListesi[position]


        holder.binding.recyclerFilmAdi.text = film.filmAdi
        holder.binding.recyclerYonetmen.text = "Yönetmen: ${film.yonetmen}"
        holder.binding.recyclerPuan.text = "IMDb: ${film.puan}"


        Glide.with(holder.itemView.context)
            .load(film.gorselUrl)
            .into(holder.binding.recyclerImageView)


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetayActivity::class.java)
            intent.putExtra("secilenFilm", film)
            holder.itemView.context.startActivity(intent)
        }
    }


    fun updateList(newList: ArrayList<Film>) {
        filmListesi = newList
        notifyDataSetChanged()
    }
}