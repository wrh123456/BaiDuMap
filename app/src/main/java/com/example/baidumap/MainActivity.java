package com.example.baidumap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient mLocationClient;//移动客户端
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());//初始化

        setContentView(R.layout.activity_main);
        ApplyPower();//获取权限

        positionText=(TextView)findViewById(R.id.text);
        mapView=(MapView)findViewById(R.id.bmapView) ;
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//一定要打开，不然不显示位置
    }

    private void requestLocation(){
        initLocation();//初始化位置信息
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//修改定位模式
        option.setIsNeedAddress(true);//是否获取详细信息
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
    }
    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if((bdLocation.getLocType() == BDLocation.TypeGpsLocation) || (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation)){
                navigateTo(bdLocation);
            }
//            StringBuilder currentPosition=new StringBuilder();
//            currentPosition.append("维度：").append(bdLocation.getLatitude()).append("\n");
//            currentPosition.append("经线：").append(bdLocation.getLongitude()).append("\n");
//            currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
//            currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
//            currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
//            currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
//            currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
//            currentPosition.append("定位方式：");
//            if(bdLocation.getLocType()==BDLocation.TypeGpsLocation){
//                currentPosition.append("GPS");
//            }
//            else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
//                currentPosition.append("网络");
//            }
//            positionText.setText(currentPosition.toString());
        }
    }
    private void navigateTo(BDLocation bdLocation){
        if(isFirstLocate){
            LatLng ll=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);//地图状态更新工厂
            baiduMap.animateMapStatus(update);//地图状态更新
            update=MapStatusUpdateFactory.zoomTo(16f);//设置缩放级别
            baiduMap.animateMapStatus(update);//地图状态更新
            isFirstLocate=false;
        }
        MyLocationData.Builder builder=new MyLocationData.Builder();
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData locationData=builder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void ApplyPower(){ //申请权限
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
        else{
            requestLocation();//请求位置
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
                default:break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
