package com.lmx.heartbeatratemonitor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartView;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType;

import java.util.ArrayList;
import java.util.List;


public class HeartRateHistoryShow extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private static final String ARG_VALUES = "arrayvalues";
    private ArrayList<Float> values;
    AAChartView aaChartView;
    ProcessingData pd;

    public static HeartRateHistoryShow newInstance(ArrayList<Float> ArrayListvalues ) {
        HeartRateHistoryShow showmetable= new HeartRateHistoryShow();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VALUES, ArrayListvalues);
        showmetable.setArguments(args);
        return showmetable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            values = (ArrayList<Float>) getArguments().getSerializable(ARG_VALUES);
            Log.e("heartratehistoryshow", "onCreate: "+values );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_heart_rate_history_show, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        aaChartView=view.findViewById(R.id.AAChartView);
        pd=new ProcessingData(values);
    }

    public class ProcessingData {
        AAChartModel aaChartModel;

        List<Object> dataList = new ArrayList<>();

        AASeriesElement[] dataset = new AASeriesElement[]{
                new AASeriesElement()
                        .name("心率值")

        };

        public ProcessingData(ArrayList<Float> values) {

            Log.e("heartratehistoryshow", "getChildView:" + values.get(0));
            dataList.addAll(values);
            Log.e("heartratehistoryshow", "dataList:" + dataList);
            dataset = new AASeriesElement[]{
                    new AASeriesElement()
                            .name("心率值")
                            .data(dataList.toArray()),

            };
            aaChartModel = new AAChartModel()
                    .chartType(AAChartType.Line)
                    .title("历史心率数据")
                    .subtitle("Virtual Data")
                    .backgroundColor("#ffffff")
                    .dataLabelsEnabled(false)
                    .yAxisGridLineWidth(0f)
                    .yAxisLabelsEnabled(true)
                    .yAxisVisible(true)
//                    .markerSymbol("triangle-down")
//                    .yAxisLineWidth(200)
                    .zoomType("AAChartZoomTypeX")
//                    .yAxisGridLineWidth(200)
                    .yAxisLabelsEnabled(true)
                    .series(dataset);
            aaChartView.aa_drawChartWithChartModel(aaChartModel);

        }

        public void updatetable(ArrayList<Float> values){
            dataList.clear();
            dataList.add(values);
            dataset = new AASeriesElement[]{
                    new AASeriesElement()
                            .name("心率值")
                            .data(dataList.toArray()),

            };
            aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(dataset);
        }
    }
}