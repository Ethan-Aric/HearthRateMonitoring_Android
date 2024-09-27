package com.lmx.heartbeatratemonitor;

import static android.widget.Toast.makeText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartView;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType;
import com.lmx.heartbeatratemonitor.database.HeartRateReading;
import com.lmx.heartbeatratemonitor.database.HeartRateRepository;
import com.lmx.heartbeatratemonitor.database.HeartRateSession;
import com.lmx.heartbeatratemonitor.database.HeartRateViewModel;
import com.lmx.heartbeatratemonitor.database.HeartRateViewModelFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.EasyPermissions;


public class HeartRateFragment extends Fragment{
    boolean firstreceivedata = true;

    private static final long SCAN_PERIOD = 10; // 扫描周期1秒
    // 注意：这里的MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT是一个应用定义的int常量，
    // 用于识别请求权限的结果，在onRequestPermissionsResult中会用到。
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 2;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 3;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 4;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADVERTISE = 5;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 6;
    private static final int MY_PERMISSIONS = 0;
    String uuid_service="0000180D-0000-1000-8000-00805F9B34FB";
    String uuid_characteristic_notify="00002a37-0000-1000-8000-00805f9b34fb";
    Boolean PerimmisionRequired = true;
    BluetoothGatt gatt = null;
    Handler bleHandler = new Handler(Looper.getMainLooper());
    List<Object> dataList = new ArrayList<>();
    ProcessingData pd;
    String formattedstartTime;
    String formattedendTime;
    HeartRateViewModel heartrateviewmodel;
    public BleDevice BleDevice;
    //组件声明

    private TextView heartratevalue;
    //绘图组件，先申明，都在OnCreate中初始化
    AAChartView aaChartView;
    private Button startButton;
    private Button stopButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for getActivity() fragment
        return inflater.inflate(R.layout.fragment_heart_rate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HeartRateRepository repository = new HeartRateRepository(getActivity().getApplication());
        heartrateviewmodel = new ViewModelProvider(this, new HeartRateViewModelFactory(requireActivity().getApplication(), repository))
                .get(HeartRateViewModel.class);
//        mLineChart=findViewById(R.id.chart);//MPAndroidChart，不支持动态更新图表，改用AAchart了

        heartratevalue = view.findViewById(R.id.heartratevalue);
        aaChartView = view.findViewById(R.id.AAChartView);
        startButton = view.findViewById(R.id.startButton);
        stopButton = view.findViewById(R.id.stopButton);

        heartratevalue.setVisibility(View.GONE); // 数据为空时隐藏

        requestPermissions();//检查蓝牙权限

    }

    private void BuildBleManger() {
        BleManager.getInstance().init(requireActivity().getApplication());
        if(!BleManager.getInstance().isSupportBle()){
            makeText(getActivity(), "不支持蓝牙的安卓设备！", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }
        UUID singleUUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB");
        UUID[] serviceUuids = new UUID[] { singleUUID };
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(0, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setAutoConnect(false)
                .setServiceUuids(serviceUuids)
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            BleManager.getInstance().scan(new BleScanCallback() {
                @Override
                public void onScanStarted(boolean success) {

                }

                @Override
                public void onScanning(BleDevice bleDevice) {

                    Log.i("Notification", "扫描中");
                }

                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
                    if(!scanResultList.isEmpty()){
                        BleDevice= scanResultList.get(0);
                        ConnectToDevice(scanResultList);
                    }

                }
            });

        } else {

            BleManager.getInstance().stopNotify(BleDevice,uuid_service, uuid_characteristic_notify);

        }
    }

    private void ConnectToDevice(List<com.clj.fastble.data.BleDevice> scanResultList) {
        BleManager.getInstance().connect(scanResultList.get(0), new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }
            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.e("heartratefrgment", "onConnectFail: 连接失败" );
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
             SetNotification(bleDevice);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

                Log.e("heartratefrgment", "onDisConnected: "+status );

            }
        });
    }

    private void SetNotification(com.clj.fastble.data.BleDevice bleDevice) {
        Log.e("heartratefrgment", "SetNotification: start" );
        BleManager.getInstance().notify(
                bleDevice,
                uuid_service,
                uuid_characteristic_notify,
                false,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Log.e("Notification", "通知设置成功，开始接受心率消息");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.e("Notification", "通知设置失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {

                        bleHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 解析数据
                                int heartRateValue = parseHeartRateValue(data);
                                // 收集数据，添加到一个列表中用于后续绘制图表
                                pd.updateHeartRate(heartRateValue);
                                // 更新UI（如果在主线程可以直接更新，否则需要使用Handler或LiveData等机制）
                                updateUI(heartRateValue);
                            }
                        });
                        Log.i("Notification", "收到通知" + data);
                    }
                });
    }

    private void startHeartRateMeasurement() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        formattedstartTime = now.format(formatter);
        if(  BleManager.getInstance().getConnectState(BleDevice)==0){
            scanLeDevice(true);
        }
        else if (BleManager.getInstance().getConnectState(BleDevice)==2) {
            SetNotification(BleDevice);
        }

    }



    @SuppressLint("MissingPermission")
    private void stopHeartRateMeasurement() {
        scanLeDevice(false);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        formattedendTime = now.format(formatter);
        Log.e("data", "开始将数据存入数据库中");
        // 使用ViewModel来处理数据库操作
        saveHeartRateSession(dataList, formattedstartTime, formattedendTime);
        dataList = new ArrayList<>();
        Log.e("data", "已经将数据存入数据库中");


    }
    private void updateUI(int heartRateValue) {
        heartratevalue.setVisibility(View.VISIBLE); // 数据不为空时可见
        // 确保Activity非空
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                heartratevalue.setText("心率值是" + heartRateValue + "bpm");
            });

        }
    }

    private int parseHeartRateValue(byte[] data) {
        if (data.length >= 2) {
            // 获取第二个字节（索引为1，因为数组索引从0开始）
            byte heartRateByte = data[1];

            // 将字节转换为无符号整数，因为byte类型在Java中是有符号的，范围是-128到127
            // 使用& 0xFF 来确保转换为正数

            return heartRateByte & 0xFF;
        } else {
            // 数据长度不足，无法解析心率
            throw new IllegalArgumentException("Data array too short to contain heart rate value.");
        }
    }




    @SuppressLint({"NewApi", "CheckResult"})
    public void saveHeartRateSession(List<Object> dataList, String startTime, String endTime) {
        final MutableLiveData<Long> sessionIdLive = new MutableLiveData<>();

        // 插入Session并获取ID
        HeartRateSession session = new HeartRateSession(startTime, endTime);
        heartrateviewmodel.insertSession(session).observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long id) {
                if (id != null && id != -1) {
                    sessionIdLive.setValue(id);
                    Log.d("HeartRateFragment", "Session inserted with id: " + id);
                } else {
                    Log.e("HeartRateFragment", "Failed to insert session or invalid session id: " + id);
                }
            }
        });

        // 观察sessionIdLive，一旦可用则开始插入Reading
        sessionIdLive.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long sessionId) {
                if (sessionId != null && sessionId != -1) {
                    Log.d("HeartRateFragment", "Valid session id received: " + sessionId);
                    // 开始插入心率数据
                    for (Object heartRate : dataList) {
                        HeartRateReading reading = new HeartRateReading((float) heartRate, sessionId);
                        heartrateviewmodel.insertReading(reading);
                        Log.d("HeartRateFragment", "Inserted HeartRateReading: " + heartRate + " for sessionId: " + sessionId);
                    }
                } else {
                    Log.e("HeartRateFragment", "Invalid session id received: " + sessionId);
                }
            }
        });
    }




    //表格绘制
    public class ProcessingData {
        AAChartModel aaChartModel;
        AASeriesElement[] dataset = new AASeriesElement[]{
                new AASeriesElement()
                        .name("心率值")

        };

        public ProcessingData() {
            aaChartModel = new AAChartModel()
                    .chartType(AAChartType.Line)
                    .title("动态心率图")
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

        @SuppressLint("NewApi")
        public void updateHeartRate(float heartRate) {

            if (firstreceivedata) {
                firstreceivedata = false;
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                formattedstartTime = now.format(formatter);
            }
            dataList.add(heartRate);
            Log.e("heartrate", dataList.toString());
            dataset = new AASeriesElement[]{
                    new AASeriesElement()
                            .name("心率值")
                            .data(dataList.toArray()),

            };


//            aaChartModel.series=dataset;
            aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(dataset);
//            aaChartView.aa_addPointToChartSeriesElement(index++,heartRate);
//            aaChartView.aa_refreshChartWithChartModel(aaChartModel);
//            aaChartView.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(aaSeriesElements);
        }
    }
    public void onDestroy() {
        super.onDestroy();
        // 确保在这里调用时，数据库操作是安全的，比如Context还未被销毁

    }
    // 请求蓝牙权限的函数
    private void requestPermissions() {
        Log.e("heartratefragment", "RequestPermissions");
//        String[] perms = {Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT};
//        if (EasyPermissions.hasPermissions(getActivity(), perms)) {
//
//        } else {
//            // Do not have permissions, request them now
//            int RC_Bluetooth_Request=MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT;
//            EasyPermissions.requestPermissions(this, getString(R.string.Bluetoothrequest), RC_Bluetooth_Request, perms);
//        }
        List<String> mPermissionList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12及以上
            // 请求新的蓝牙权限模型

            mPermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
            mPermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            mPermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
            Log.d("heartratefragment", "requestPermissions: 安卓12");

//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
//                makeText(getActivity(), "需要蓝牙连接权限", Toast.LENGTH_SHORT).show();
//            }
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
//            }
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, MY_PERMISSIONS_REQUEST_BLUETOOTH_ADVERTISE);
//            }
        } else { // Android 11及以下
            // 在Android 11及更低版本中，需要ACCESS_FINE_LOCATION权限来执行蓝牙扫描
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
                makeText(getActivity(), "安卓12以下需要位置权限", Toast.LENGTH_SHORT).show();
            }

            //  Android 11及以下才需要BLUETOOTH权限
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(Manifest.permission.BLUETOOTH);
                makeText(getActivity(), "需要蓝牙权限", Toast.LENGTH_SHORT).show();
            }
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                makeText(getActivity(), "安卓12以下需要粗略位置权限", Toast.LENGTH_SHORT).show();
            }
        }
        Log.d("heartratefragment", "请求权限");
        requestPermissions(mPermissionList.toArray(new String[0]), MY_PERMISSIONS);

    }

    // 重写onRequestPermissionsResult方法处理用户响应
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("heartratefragment", "onRequestPermissionsResult.");
            // Forward results to EasyPermissions
//            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            // 所有权限已被授予，可以进行蓝牙操作
            BuildBleManger();
            //监听按钮
            startButton.setOnClickListener(v -> startHeartRateMeasurement());
            stopButton.setOnClickListener(v -> stopHeartRateMeasurement());
            pd = new ProcessingData();
        } else {
            // 权限被拒绝，显示提示或采取相应操作
            Log.e("heartratefragment", "Bluetooth permissions denied.");
        }
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_BLUETOOTH:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // 用户同意了BLUETOOTH权限，可以进行基本的蓝牙操作
//
//                } else {
//                    // 用户拒绝了权限，处理逻辑
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        makeText(getActivity(), "需要蓝牙权限以进行操作", Toast.LENGTH_SHORT).show();
//                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH}, MY_PERMISSIONS_REQUEST_BLUETOOTH);
//
//                    }
//
//                }
//                break;
//
//            case MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    // 用户拒绝了权限，处理逻辑
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        makeText(getActivity(), "需要蓝牙连接权限以发现和连接设备", Toast.LENGTH_SHORT).show();
//                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
//                    }
//                }
//                break;
//
//            case MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    // 用户拒绝了权限，处理逻辑
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
//
//                    }
//                }
//                break;
//            case MY_PERMISSIONS_REQUEST_BLUETOOTH_ADVERTISE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    // 用户拒绝了权限，处理逻辑
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, MY_PERMISSIONS_REQUEST_BLUETOOTH_ADVERTISE);
//
//                    }
//                }
//                break;
//            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    // 用户拒绝了权限，处理逻辑
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//                        makeText(getActivity(), "安卓12以下需要粗略位置权限以被发现", Toast.LENGTH_SHORT).show();
//                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
//                    }
//                }
//                break;
//            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
//                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    // 用户拒绝了权限，根据需求提示用户或禁用相关功能
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//                        makeText(getActivity(), "安卓12以下需要定位权限以被发现", Toast.LENGTH_SHORT).show();
//                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//
//                    }
//                } else {
//
//
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }

//         根据权限请求结果，决定是否重新尝试执行之前因权限不足而未能执行的操作
    }

    ///垃圾的MPAndroidChart，不支持动态图表，不用了
    /*public class ProcessingData{

        private int lastIndex=0; // 记录添加到数据集中的最后一个条目的索引
        List<Entry> hList = new ArrayList<Entry>();
        LineDataSet hDataSet=new LineDataSet(hList, "心率数据");
        List<ILineDataSet> LILineDataSet = new ArrayList<ILineDataSet>();
        public ProcessingData() {
            hDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            LILineDataSet.add(hDataSet);
        }
        public void updateHeartRate(float heartRate){
            if (lastIndex >= 100) {
                hList.remove(0); // 移除最旧的数据点
            }
            // 添加新的心率数据
            Entry newEntry = new Entry(lastIndex++, heartRate);
            hList.add(newEntry);

            // 更新数据集
            hDataSet.notifyDataSetChanged(); // 通知数据集有变化
            LineData data = new LineData(LILineDataSet);
            mLineChart.setData(data);
            mLineChart.notifyDataSetChanged(); // 通知图表数据集有变化
            mLineChart.setVisibleXRangeMaximum(100); // 设置X轴最大显示数据点数
            mLineChart.moveViewToX(data.getEntryCount() - 1); // 移动视图到最后一个数据点
            mLineChart.invalidate(); // 刷新图表
            *//*Entry c2e1 = new Entry(lastIndex++, heartRate);
            hList.add(c2e1);
            hDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            LILineDataSet.add(hDataSet);
            LineData data = new LineData(LILineDataSet);
            mLineChart.setData(data);
            mLineChart.invalidate(); // refresh*//*
        }
    }*/
   /* public class ProcessingData {

        private LineData lineData;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault()); // 初始化时间格式
        private int lastIndex=0; // 记录添加到数据集中的最后一个条目的索引

        public ProcessingData() {
            // 初始化图表和数据集的代码...

            // 配置数据集
            ArrayList<Entry> entries = new ArrayList<>();
            LineDataSet dataSet = new LineDataSet(entries, "Heart Rate"); // 创建数据集
            dataSet.setDrawCircles(false);
            dataSet.setColor(Color.BLUE);
            dataSet.setLineWidth(2f);

            // 配置Y轴
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setGranularityEnabled(true); // 启用粒度控制
            leftAxis.setGranularity(1f); // 设置Y轴刻度间隔
            leftAxis.setAxisMinimum(0f); // 心率通常为正数，设置Y轴最小值为0
            // 禁用右侧Y轴，避免出现两个Y轴标尺
            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);
            lineData = new LineData(dataSet);
            chart.setData(lineData);

            // 配置X轴显示时间标签
            configureXAxis(chart.getXAxis(), entries);
        }

        // 新增方法以配置X轴
        private void configureXAxis(XAxis xAxis, ArrayList<Entry> entries) {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if (value < entries.size()) { // 确保索引有效
                        Date date = new Date((long) entries.get((int) value).getX()); // 获取对应索引的日期
                        return dateFormat.format(date); // 格式化日期为字符串
                    }
                    return ""; // 超出范围时返回空字符串
                }
            });
        }

        public void updateHeartRate(float heartRate, long timestampInMillis) {
            // ... （之前的检查和移除旧数据点的代码）
            // 确保数据集大小不超过限制
            if (lineData.getEntryCount() >= 30) {
                for (ILineDataSet dataSet : lineData.getDataSets()) {
                    dataSet.removeFirst(); // 移除最早的数据点
                }
            }
*//*
            // 确保不会添加重复的时间戳（示例性检查，实际需根据业务逻辑调整）
            boolean isDuplicate = false;
            for (Entry e : ((LineDataSet) lineData.getDataSets().get(0)).getValues()) {
                if (e.getX() == timestampInMillis) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                Entry entry = new Entry(timestampInMillis, heartRate);
                ((LineDataSet) lineData.getDataSets().get(0)).addEntry(entry);
                lastIndex++;
            }
*//*
            // 添加新的心率数据
            Entry entry = new Entry(timestampInMillis, heartRate); // 使用时间戳作为X值
            lineData.addEntry(entry, lastIndex); // 将新条目添加到数据集中
            lastIndex++; // 更新索引

            // 更新图表以反映变化
            chart.notifyDataSetChanged(); // 通知数据已改变
            chart.setVisibleXRangeMaximum(30f); // 设置X轴最大可见数据点数
            chart.moveViewToX(lineData.getEntryCount() - 1); // 滚动到最新的数据点
        }
    }
*/


}

