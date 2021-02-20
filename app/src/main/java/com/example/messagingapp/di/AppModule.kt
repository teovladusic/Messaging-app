package com.example.messagingapp.di

import android.app.Application
import androidx.room.Room
import com.example.messagingapp.data.firebase.FirebaseRepository
import com.example.messagingapp.data.room.ChatDatabase
import com.example.messagingapp.data.room.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application
    ) = Room.databaseBuilder(app, ChatDatabase::class.java, "ChatDB.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideChatDao(db: ChatDatabase) = db.getDao()

    @Singleton
    @Provides
    fun provideRepository(db: ChatDatabase) = Repository(db)

    @Singleton
    @Provides
    fun provideFirebaseRepository() = FirebaseRepository()

}