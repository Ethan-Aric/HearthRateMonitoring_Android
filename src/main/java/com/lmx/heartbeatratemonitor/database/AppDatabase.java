package com.lmx.heartbeatratemonitor.database;

import android.content.Context;

import androidx.room.Database;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {HeartRateReading.class, HeartRateSession.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE; // 使用volatile关键字确保线程安全

    public abstract HeartRateDao heartRateDao();

    // 单例模式获取数据库实例
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "heart_rate_database")
                            // 如果需要在主线程查询数据，请取消注释以下行（但请注意这可能导致UI阻塞）
//                             .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}