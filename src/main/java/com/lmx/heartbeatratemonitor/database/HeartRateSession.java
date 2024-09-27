package com.lmx.heartbeatratemonitor.database;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

// HeartRateSession.java
@Entity(tableName = "heart_rate_session"
        )
public class HeartRateSession {
    @PrimaryKey(autoGenerate = true)
    public long Id;

    public String StartTime;

    public String EndTime;


    public HeartRateSession(String StartTime,String EndTime){
        this.StartTime=StartTime;
        this.EndTime=EndTime;
    }
    public void setStartTime(String StartTime) {
        this.StartTime = StartTime;
    }
    public void setEndTime(String EndTime) {
        this.EndTime = EndTime;
    }
    public String getStartTime() {
        if(StartTime==null){
            String notexist="数据为空";
            return notexist;
        }
        return StartTime;
    }
    public String getEndTime() {
        if(StartTime==null){
            String notexist="数据为空";
            return notexist;
        }
        return EndTime;
    }
    public Long getId() {
        Log.e("heartratesession", "getId: "+Id);
        return Id;
    }
}