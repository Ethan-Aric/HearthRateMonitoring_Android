package com.lmx.heartbeatratemonitor;

import android.content.Context;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartView;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType;
import com.lmx.heartbeatratemonitor.R;
import com.lmx.heartbeatratemonitor.database.HeartRateSession;

import java.util.ArrayList;
import java.util.List;

public class HeartRateSessionAdapter  extends BaseExpandableListAdapter {
    private List<HeartRateSession> sessions;
    private ArrayList<Float> values;
    Context myContext;
//    ProcessingData pd;

    public HeartRateSessionAdapter(Context myContext, List<HeartRateSession> sessions, ArrayList<Float>  values) {
        this.myContext = myContext;
        this.sessions = sessions;
        this.values=values;
        Log.d("HeartRateSessionAdapter", "Adapter initialized with " + sessions.size() + " items.");
    }
    @Override
    public int getGroupCount() {
        Log.e("heartratesessionadapte", "getGroupCount: "+sessions.size());
        return sessions.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
//        Log.e("heartratesessionadapte", "getChildrenCount: "+values.get(groupPosition).size());
        return 1;
    }
    @Override
    public HeartRateSession getGroup(int groupPosition) {
        return sessions.get(groupPosition);
    }

    @Override
    public Float getChild(int groupPosition, int childPosition) {
        return values.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    //取得用于显示给定分组的视图. 这个方法仅返回分组的视图对象
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolderGroup groupHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(myContext).inflate(
                    R.layout.item_heart_rate_session, parent, false);
            groupHolder = new ViewHolderGroup();
            groupHolder.tvStartTime= (TextView) convertView.findViewById(R.id.tvStartTime);
            groupHolder.tvEndTime= (TextView) convertView.findViewById(R.id.tvEndTime);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (ViewHolderGroup) convertView.getTag();
        }
        groupHolder.tvStartTime.setText(sessions.get(groupPosition).getStartTime());
        groupHolder.tvEndTime.setText(sessions.get(groupPosition).getEndTime());
        Log.e("heartratesessionadapter", sessions.get(groupPosition).getStartTime());

        return convertView;
    }

    //取得显示给定分组给定子位置的数据用的视图
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem itemHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(myContext).inflate(
                    R.layout.item_heart_rate_reading, parent, false);
            itemHolder = new ViewHolderItem();
            itemHolder.tv_endtime =  convertView.findViewById(R.id.tv_endtime);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolderItem) convertView.getTag();
        }
        itemHolder.tv_endtime.setText("心率平均值是"+values.get(groupPosition).toString() );
//        pd = new ProcessingData(values.get(groupPosition), itemHolder.AAChartView);
//        pd.updatetable(values.get(groupPosition));
        return convertView;
    }


        @Override
        public boolean isChildSelectable ( int groupPosition, int childPosition){
            return true;
        }

//        public void setSessions (List < HeartRateSession > sessions) {
//            this.sessions.clear();
////        if (sessions!=null || !sessions.isEmpty()){
//            this.sessions.addAll(sessions);
//            Log.d("HeartRateSessionAdapter", "Data updated. New item count: " + sessions.size());
//            notifyDataSetChanged();
//
////        }else{
////            Log.e("HeartRateSessionAdapter", "session为空！");
////        }
//
//        }

//        public class ProcessingData {
//            AAChartModel aaChartModel;
//            AAChartView aaChartView;
//            List<Object> dataList = new ArrayList<>();
//            boolean updated = false;
//            AASeriesElement[] dataset = new AASeriesElement[]{
//                    new AASeriesElement()
//                            .name("心率值")
//
//            };
//
//            public ProcessingData(ArrayList<Float> values, AAChartView aaChartView) {
//                this.aaChartView = aaChartView;
//                Log.e("heartratesessionadapter", "getChildView:" + values.get(0));
//                    dataList.add(values);
//                    dataset = new AASeriesElement[]{
//                            new AASeriesElement()
//                                    .name("心率值")
//                                    .data(dataList.toArray()),
//
//                    };
//                    aaChartModel = new AAChartModel()
//                            .chartType(AAChartType.Line)
//                            .title("动态心率图")
//                            .subtitle("Virtual Data")
//                            .backgroundColor("#ffffff")
//                            .dataLabelsEnabled(false)
//                            .yAxisGridLineWidth(0f)
//                            .yAxisLabelsEnabled(true)
//                            .yAxisVisible(true)
////                    .markerSymbol("triangle-down")
////                    .yAxisLineWidth(200)
//                            .zoomType("AAChartZoomTypeX")
////                    .yAxisGridLineWidth(200)
//                            .yAxisLabelsEnabled(true)
//                            .series(dataset);
//                    aaChartView.aa_drawChartWithChartModel(aaChartModel);
//
//                    updated = true;
//                }
//
//                public void updatetable(ArrayList<Float> values){
//                    dataList.clear();
//                    dataList.add(values);
//                    dataset = new AASeriesElement[]{
//                            new AASeriesElement()
//                                    .name("心率值")
//                                    .data(dataList.toArray()),
//
//                    };
//                    aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(dataset);
//                }
//            }



        private static class ViewHolderGroup {
            TextView tvStartTime;
            TextView tvEndTime;
        }

        private static class ViewHolderItem {
            TextView tv_endtime;

        }
    }