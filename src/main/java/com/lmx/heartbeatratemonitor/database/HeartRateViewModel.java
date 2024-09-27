package com.lmx.heartbeatratemonitor.database;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Observer;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class HeartRateViewModel extends AndroidViewModel {
    public HeartRateRepository repository;


    private CompositeDisposable disposables  = new CompositeDisposable();
//    private MutableLiveData<List<HeartRateSession>> allSessionsLiveData=new MutableLiveData<>() ;
    private LiveData<Long> lastInsertedSessionId = new MutableLiveData<>(-1L);
    MutableLiveData<Long> sessionId=new MutableLiveData<>();
    public HeartRateViewModel(@NonNull Application application) {
        super(application);
        // 注意：不要在这里直接调用 observeSessionsFromRepository()，因为repository还未初始化
    }

    // 带有依赖项的构造函数
    public HeartRateViewModel(@NonNull Application application, HeartRateRepository repository) {
        super(application);
        this.repository = repository;

    }


    @Override
    protected void onCleared() {
        super.onCleared();

    }
    public List<HeartRateSession> getAllSessions() throws ExecutionException, InterruptedException {
//        if (repository.getAllSessions() != null) {
//            repository.getAllSessions().observeForever(new Observer<List<HeartRateSession>>() {
//                @Override
//                public void onChanged(List<HeartRateSession> HeartRateSessions) {
//                    allSessionsLiveData.setValue(HeartRateSessions);
//                    repository.getAllSessions().removeObserver(this);
//                }
//            });
//        }else {
//            Log.e("HeartRateViewModel", "repositorySessionId is null");
//        }
        if (repository.getAllSessions() != null){
            return repository.getAllSessions();
        }
        else{
            Log.e("HeartRateViewModel", "repositorySessionId is null");
            return repository.getAllSessions();//如果为空还是返回，但是同时日志输出

        }
    }
    public List<Float> getHeartRateBySession(HeartRateSession session) throws ExecutionException, InterruptedException {

        return repository.getHeartRateBySession(session.getId());
    }
    public LiveData<Long> insertSession(HeartRateSession session) {
//        new Thread(() -> {


                // 在后台线程执行插入操作
                sessionId=repository.insertSession(session);
        if (sessionId != null) {
            sessionId.observeForever(new Observer<Long>() {
                @Override
                public void onChanged(Long id) {
                    sessionId.setValue(id);
                    sessionId.removeObserver(this);
                }
            });
        }else {
            Log.e("HeartRateViewModel", "repositorySessionId is null");
        }
                lastInsertedSessionId=repository.getInsertResult();
                // 插入成功后发送true到LiveData

//        }).start();
        return sessionId;
    }
    public LiveData<Long> getLastInsertedSessionId() {
        return sessionId;
    }
    public void insertReading(HeartRateReading reading) {
//        new Thread(() -> {

                // 在后台线程执行插入操作
                repository.insertReading(reading);
                // 插入成功后发送true到LiveData

//        }).start();
    }



}