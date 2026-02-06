package com.register.cinemap

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface FavoriDao {

    @Insert
    suspend fun listeyeEkle(favoriFilm: FavoriFilm)

    @Delete
    suspend fun listedenSil(favoriFilm: FavoriFilm)

    @Query("SELECT * FROM favoriler WHERE tur = :turId")
    suspend fun getListeByTur(turId: Int): List<FavoriFilm>


    @Query("SELECT * FROM favoriler")
    suspend fun tumKayitlariGetir(): List<FavoriFilm>
}