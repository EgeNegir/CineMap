package com.register.cinemap

import java.io.Serializable

data class Yorum(
    val yorumId: String = "",
    val filmId: String = "",
    val yorum: String = "",
    val yapan: String = "",
    val email: String = "",
    val puan: Float = 0f,
    // Oylama sistemi için gerekli alanlar
    val upvotes: Long = 0,
    val downvotes: Long = 0,
    val votedUsers: Map<String, Long> = HashMap(),
    // Profil sayfası için film bilgileri
    val filmAdi: String = "",
    val filmGorsel: String = ""
) : Serializable