package com.lmx.heartbeatratemonitor.database;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lmx.heartbeatratemonitor.databinding.ItemHeartRateSessionBinding;

import java.util.ArrayList;
import java.util.List;

// HeartRateReading.java
@Entity(tableName = "heart_rate_reading",
        foreignKeys = @ForeignKey(entity = HeartRateSession.class,
        parentColumns = "Id",
        childColumns = "sessionId"),
        indices = {@Index(value = "sessionId")})
public class HeartRateReading {
    @PrimaryKey(autoGenerate = true)
    public long Id;

    public float heartRateValue;
    public long sessionId;

    public HeartRateReading(float heartRateValue, long sessionId) {
        this.heartRateValue = heartRateValue;
        this.sessionId = sessionId;
    }
    // 可以添加记录时间等其他字段


}