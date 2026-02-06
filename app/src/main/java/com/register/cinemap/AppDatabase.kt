package com.register.cinemap

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [FavoriFilm::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriDao(): FavoriDao
}