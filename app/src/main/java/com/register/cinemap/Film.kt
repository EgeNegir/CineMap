package com.register.cinemap
import java.io.Serializable

data class Film(
    val filmAdi: String = "",
    val yonetmen: String = "",
    val puan: String = "",
    val gorselUrl: String = "",
    val aciklama: String = "",
    var documentId: String? = null,
    val cikisYili: Int = 0,
    val basroller: String = "",
    val tur: String = ""
) : Serializable