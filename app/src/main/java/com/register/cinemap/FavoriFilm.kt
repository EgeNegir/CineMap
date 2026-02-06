package com.register.cinemap

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoriler")
data class FavoriFilm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val filmAdi: String,
    val yonetmen: String,
    val puan: String,
    val gorselUrl: String,
    val aciklama: String,
    val documentId: String,

    val tur: Int = 1
)