package com.lmx.heartbeatratemonitor.database;

import android.app.Application;
import android.os.AsyncTask;
import android.se.omapi.Session;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class HeartRateRepository {
    public final HeartRateDao heartRateDao;
    private final AppDatabase database;
    static MutableLiveData<Long> sessionId=new MutableLiveData<>();
    static MutableLiveData<Long> ReadingId=new MutableLiveData<>();
    static List<HeartRateSession> allsessions;
    static List<Float> heartreadreadings;
    static List<HeartRateSession> sessions;
    public HeartRateRepository(Application application) {
        database = AppDatabase.getInstance(application);
        heartRateDao = database.heartRateDao();
    }

    // 获取所有心率会话
    public List<HeartRateSession> getAllSessions() throws ExecutionException, InterruptedException {

//        heartRateDao.getAllSessions().observeForever(new Observer<List<HeartRateSession>>()
//        {
//
//            @Override
//            public void onChanged(List<HeartRateSession> heartRateSessions) {
//                allsessions.setValue(heartRateSessions);
//            }
//        });
        return   new GetSessionAsyncTask(heartRateDao).execute().get();
    }
    public List<Float >getHeartRateBySession(long sessionId) throws ExecutionException, InterruptedException {

        return  new GetHeartRateBySessionAsyncTask(heartRateDao).execute(sessionId).get();
    }
    public LiveData<Long> getInsertResult() {
        return  sessionId;
    }

    // 插入一个新的心率会话
    public MutableLiveData<Long> insertSession(HeartRateSession session) {
        new InsertSessionAsyncTask(heartRateDao).execute(session);
//        sessionId.postValue(heartRateDao.insertSession(session));
//        new Thread(() -> {//使用new thread还是会导致在主线程执行插入操作，不知道为什么
//            try {
//                long id = heartRateDao.insertSession(session);
//                sessionId.postValue(id);
//            } catch (Exception e) {
//                Log.e("HeartRateRepository", "Error inserting session", e);
//            }
//        }).start();
        return sessionId;
    }
    public void insertReading(HeartRateReading reading) {
        new InsertReadingAsyncTask(heartRateDao).execute(reading);
//        heartRateDao.insertReading(reading);
    }


//     Async task for inserting session (if not using reactive approach like above)
    private static class InsertSessionAsyncTask extends AsyncTask<HeartRateSession, HeartRateSession, Long> {
        private final HeartRateDao dao;

        private InsertSessionAsyncTask(HeartRateDao dao) {
            this.dao = dao;
        }

        @Override
        protected Long doInBackground(HeartRateSession... sessions) {

            return dao.insertSession(sessions[0]);
        }
        @Override
        protected void onPostExecute(Long result) {
            sessionId.postValue(result);
        }
    }
    private static class GetSessionAsyncTask extends AsyncTask<Void, Void, List<HeartRateSession>>{
        private HeartRateDao dao;

        private GetSessionAsyncTask(HeartRateDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<HeartRateSession> doInBackground(Void... voids) {
            return dao.getAllSessions();
        }
        @Override
        protected void onPostExecute(List<HeartRateSession>  result) {
//            sessions.setValue(result);
            allsessions=result;
        }
    }
    private static class GetHeartRateBySessionAsyncTask extends AsyncTask<Long, Void, List<Float>>{
        private HeartRateDao dao;

        private GetHeartRateBySessionAsyncTask(HeartRateDao dao) {
            this.dao = dao;
        }

        @Override
        protected List<Float> doInBackground(Long... id) {
            return dao.getHeartRateBySession(id[0]);
        }
        @Override
        protected void onPostExecute(List<Float>  result) {
//            sessions.setValue(result);
            heartreadreadings=result;
        }
    }
    private static class InsertReadingAsyncTask extends AsyncTask<HeartRateReading, HeartRateReading, Long> {
        private final HeartRateDao dao;

        private InsertReadingAsyncTask(HeartRateDao dao) {
            this.dao = dao;
        }

        @Override
        protected Long doInBackground(HeartRateReading... readings) {


            return dao.insertReading(readings[0]);
        }
        @Override
        protected void onPostExecute(Long result) {
            ReadingId.setValue(result);
        }
    }
}
