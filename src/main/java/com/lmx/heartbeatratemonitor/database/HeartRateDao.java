package com.lmx.heartbeatratemonitor.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

// HeartRateDao.java
@Dao
public interface HeartRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertReading(HeartRateReading reading);

    @Insert
    Long insertSession(HeartRateSession session);
    @Query("SELECT * FROM heart_rate_session")
    List<HeartRateSession> getAllSessions();

    @Query("SELECT heartRateValue FROM heart_rate_reading WHERE sessionId = :sessionid")
    List<Float>getHeartRateBySession(Long sessionid);



    // 其他可能需要的查询方法
}