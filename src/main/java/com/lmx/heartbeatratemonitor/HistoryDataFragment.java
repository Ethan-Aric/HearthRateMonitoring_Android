package com.lmx.heartbeatratemonitor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.lmx.heartbeatratemonitor.database.HeartRateRepository;
import com.lmx.heartbeatratemonitor.database.HeartRateSession;
import com.lmx.heartbeatratemonitor.database.HeartRateViewModel;
import com.lmx.heartbeatratemonitor.database.HeartRateViewModelFactory;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class HistoryDataFragment extends Fragment {
    private HeartRateViewModel viewModel;
    List<Object> dataList = new ArrayList<>();
    private HeartRateSessionAdapter adapter;
    private ExpandableListView rvHeartRateRecords;
    private TextView tvNoData;
    private  ArrayList<ArrayList<Float>> ArrayListvalues ;
    private final MediatorLiveData<List<Float>> heartRateReadings = new MediatorLiveData<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_data, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        rvHeartRateRecords = view.findViewById(R.id.rvHeartRateRecords);
        tvNoData = view.findViewById(R.id.tvNoData);
        // 根据数据是否存在切换提示文本的可见性
        try {
            setupViewModel();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        rvHeartRateRecords.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("historydatafragment", "Child item clicked. Group: " + groupPosition + ", Child: " + childPosition);
                HeartRateHistoryShow showmetable = HeartRateHistoryShow.newInstance(ArrayListvalues.get(groupPosition));
                if (showmetable != null) {
                    Log.d("historydatafragment", "Successfully created HeartRateHistoryShow instance.");
                    FragmentManager fragmentManager = getParentFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.HistoryDataFragment, showmetable);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    Log.d("historydatafragment", "Fragment transaction committed.");
                    return true;
                } else {
                    Log.e("historydatafragment", "Failed to create HeartRateHistoryShow instance.");
                    return false;
                }
            }
        });
    }

    private void updateNoDataVisibility(List<HeartRateSession> sessions) {

        tvNoData.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupViewModel() throws ExecutionException, InterruptedException {
        HeartRateRepository repository = new HeartRateRepository(getActivity().getApplication());
        MediatorLiveData<String> viewLiveData = new MediatorLiveData<>();
        viewModel = new ViewModelProvider(this, new HeartRateViewModelFactory(requireActivity().getApplication(), repository))
                .get(HeartRateViewModel.class);
        Log.e("historydatafragment", "onchanged0");
//        viewLiveData.addSource(viewModel.getAllSessions(), new Observer<List<HeartRateSession>>() {
//            @Override
//            public void onChanged(List<HeartRateSession> heartRateSessions) {
        List<HeartRateSession> heartRateSessions = viewModel.getAllSessions();
        if (heartRateSessions == null || heartRateSessions.isEmpty()) {
            updateNoDataVisibility(heartRateSessions);
            return;
        }
        Log.e("historydatafragment", "onchanged1");
        List<Float> values = new ArrayList<>();
        ArrayListvalues = new ArrayList<>();

        for (HeartRateSession session : heartRateSessions) {
            List<Float> heartRateReadings = viewModel.getHeartRateBySession(session);

            Log.e("historydatafragment", "onchanged2");
//                    heartRateReadings.observe(getViewLifecycleOwner(), new Observer<List<Float>>() {
//                        @Override
//                        public void onChanged(List<Float> floats) {

            Log.e("historydatafragment", "onchanged3");
            if(heartRateReadings == null ){
                Log.e("historydatafragment", "setupViewModel: getHeartRateBySession(session)exception ");
                return;
            }
            if (heartRateReadings.isEmpty()) {
                Log.e("historydatafragment", "setupViewModel: session " + session.getId() + " has no heart rate readings");
//                return;
            }

            float sum = 0;
            for (Float value : heartRateReadings) {
                sum += value;
            }

            float average = sum / heartRateReadings.size();
            values.add(average);
            ArrayListvalues.add(new ArrayList<>(heartRateReadings));

            // Update adapter when all data is loaded
            if (values.size() == heartRateSessions.size()) {
                adapter = new HeartRateSessionAdapter(getContext(), heartRateSessions, (ArrayList<Float>) values);
                rvHeartRateRecords.setAdapter(adapter);
                updateNoDataVisibility(heartRateSessions);
            }
//                        }
//                    });
//                }
//            }
//        });
        }
    }

//        @SuppressLint("NewApi")
//        public void updateHeartRate(float heartRate) {
//
//
//
//
////            aaChartModel.series=dataset;
//            aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(dataset);
////            aaChartView.aa_addPointToChartSeriesElement(index++,heartRate);
////            aaChartView.aa_refreshChartWithChartModel(aaChartModel);
////            aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(aaSeriesElements);
//        }

//    private void updateChart(HeartRateSession session) {
//        aaChartView.setVisibility(View.VISIBLE);
//        AAChartModel aaChartModel = new AAChartModel()
//                .chartType(AAChartType.Line)
//                .title("Heart Rate Over Time")
//                .backgroundColor("#ffffff")
//                .dataLabelsEnabled(true)
//                .series(new AASeriesElement[]{
//                        new AASeriesElement()
//                                .name("Heart Rate")
//                                .data(record.getHeartRateData())
//                });
//        aaChartView.aa_drawChartWithChartModel(aaChartModel);
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        viewModel.getAllSessions().removeObservers(getViewLifecycleOwner());
    }
}

