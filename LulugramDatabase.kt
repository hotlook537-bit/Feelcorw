package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        Post::class,
        Story::class,
        StoryHighlight::class,
        Reel::class,
        Conversation::class,
        DirectMessage::class,
        Comment::class,
        Like::class,
        Follow::class,
        StoryLike::class,
        StoryView::class,
        SavedPost::class,
        SavedCollection::class,
        Block::class,
        Mute::class,
        Report::class,
        AdminAction::class,
        LuluNotification::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LulugramDatabase : RoomDatabase() {
    abstract fun lulugramDao(): LulugramDao

    companion object {
        @Volatile
        private var INSTANCE: LulugramDatabase? = null

        fun getInstance(context: Context): LulugramDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LulugramDatabase::class.java,
                    "lulugram_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
