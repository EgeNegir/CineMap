package com.register.cinemap

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.register.cinemap.databinding.RowYorumBinding


class YorumAdapter(
    val yorumListesi: ArrayList<Yorum>,
    private val isAdmin: Boolean,
    val onSilClick: (Yorum) -> Unit
) : RecyclerView.Adapter<YorumAdapter.YorumHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class YorumHolder(val binding: RowYorumBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YorumHolder {
        val binding = RowYorumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return YorumHolder(binding)
    }

    override fun getItemCount(): Int = yorumListesi.size

    override fun onBindViewHolder(holder: YorumHolder, position: Int) {
        val yorum = yorumListesi[position]
        val currentUser = Firebase.auth.currentUser
        val currentUserId = currentUser?.uid ?: ""


        holder.binding.txtYorumYapan.text = yorum.yapan
        holder.binding.txtYorumIcerik.text = yorum.yorum
        holder.binding.ratingYorumRow.rating = yorum.puan


        val toplamOy = (yorum.upvotes - yorum.downvotes).toInt()
        holder.binding.txtOySayisi.text = toplamOy.toString()

        val userVote = yorum.votedUsers[currentUserId] ?: 0L


        when (userVote) {
            1L -> {
                holder.binding.btnUpvote.setColorFilter(Color.parseColor("#FF4500"))
                holder.binding.btnDownvote.setColorFilter(Color.parseColor("#AAAAAA"))
            }
            -1L -> {
                holder.binding.btnDownvote.setColorFilter(Color.parseColor("#7193FF"))
                holder.binding.btnUpvote.setColorFilter(Color.parseColor("#AAAAAA"))
            }
            else -> {
                holder.binding.btnUpvote.setColorFilter(Color.parseColor("#AAAAAA"))
                holder.binding.btnDownvote.setColorFilter(Color.parseColor("#AAAAAA"))
            }
        }


        holder.binding.btnUpvote.setOnClickListener {
            if (currentUserId.isNotEmpty()) oyVer(yorum, 1, currentUserId)
        }

        holder.binding.btnDownvote.setOnClickListener {
            if (currentUserId.isNotEmpty()) oyVer(yorum, -1, currentUserId)
        }


        if (currentUser?.email != null && (currentUser.email == yorum.email || isAdmin)) {
            holder.binding.btnYorumSil.visibility = View.VISIBLE
            holder.binding.btnYorumSil.setOnClickListener { onSilClick(yorum) }
        } else {
            holder.binding.btnYorumSil.visibility = View.GONE
        }
    }


    private fun oyVer(yorum: Yorum, oyYonu: Int, userId: String) {
        val yorumRef = db.collection("Filmler").document(yorum.filmId)
            .collection("Yorumlar").document(yorum.yorumId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(yorumRef)

            val votedUsers = snapshot.get("votedUsers") as? MutableMap<String, Long> ?: mutableMapOf()
            var upvotes = snapshot.getLong("upvotes") ?: 0L
            var downvotes = snapshot.getLong("downvotes") ?: 0L

            val eskiOy = votedUsers[userId] ?: 0L


            if (eskiOy == oyYonu.toLong()) {
                votedUsers.remove(userId)
                if (oyYonu == 1) upvotes-- else downvotes--
            } else {
                if (eskiOy == 1L) upvotes-- else if (eskiOy == -1L) downvotes--

                votedUsers[userId] = oyYonu.toLong()
                if (oyYonu == 1) upvotes++ else downvotes++
            }


            transaction.update(yorumRef, "upvotes", upvotes)
            transaction.update(yorumRef, "downvotes", downvotes)
            transaction.update(yorumRef, "votedUsers", votedUsers)

            null
        }
    }
}