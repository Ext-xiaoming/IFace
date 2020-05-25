package dc.iface.student;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NumqiandaoActivity extends BaseActivity  implements View.OnClickListener{
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;


    private MapView mapView=null;
    private BaiduMap baiduMap;
    private LocationClient locationClient;
    private boolean isFirstLocte=true;
    private TextView biaoti;
    private TextView positionText;
    private EditText editText;
    private ImageButton back;
    private Button button;

    private String studentId;
    private String courseId;
    private String postId;

    private double sbjingdu2;
    private double sbweidu2;

    private String TAG = "NumqiandaoActivity";
    public LocationClient mLocationClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        mLocationClient = new LocationClient( getApplicationContext() );
        mLocationClient.registerLocationListener( new MyLocationListener() );
        SDKInitializer.initialize( getApplicationContext() );
        setContentView( R.layout.num_qaindao_sec);

        positionText=findViewById(R.id.positionText);
        editText=findViewById( R.id.et_shuziNum );
        biaoti=findViewById(R.id.text_titlesec);
        biaoti.setText("数字签到");

        //返回键
        back=findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        postId = editText.getText().toString();
        Intent intent2 = getIntent();
        courseId = intent2.getStringExtra("courseId");
        studentId = intent2.getStringExtra("studentId");

        mapView=(MapView) findViewById(R.id.bmapView3);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission( NumqiandaoActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(NumqiandaoActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(NumqiandaoActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(NumqiandaoActivity.this,
                Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_SETTINGS);
        }
        if (!permissionList.isEmpty()){
            String[] permissions =permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(NumqiandaoActivity.this, permissions, 1);
        }else {
            requestLocation();
        }

        Log.i(TAG,"1."+"进入"+courseId+" "+studentId+" "+sbjingdu2 + " "+sbweidu2);
        editText=findViewById(R.id.et_shuziNum);
        button=findViewById(R.id.num_submit);
        button.setOnClickListener( this );
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption(  );
        option.setCoorType("bd09ll");// 坐标类型
        option.setScanSpan(5000);
        option.setLocationMode( LocationClientOption.LocationMode.Device_Sensors);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.num_submit:
                qiandao(view);
                break;

            default:
                break;
        }
    }

    private void qiandao(View view) {
        //获取 sign_id post_id  student_id sign_date post_type  sign-经纬度
        //依靠course_id 从 post_check_in 中找到post_type  post_id post-经纬度 post_date post_num
        //参数传递：course_id
        //首先查查是否已经签到了，方法：post_id是已知的（输入的签到码），student_id已知 只需要查 sign_in是否有 有 就已经签到了不用再签了
        //顺带返回最新发布的签到的经纬度以及时间+签到码到本地  和学生端的比较 （如果已经签到当然不用比较）
        //比较地址和时间-以及签到码是否正确--符合则进行数据发送 sign_id post_id  student_id sign_date post_type

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("courseId",courseId)
                        .add("studentId",studentId)
                        .add("qiandaoNum",editText.getText().toString())
                        .build();

                final Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/verIsCheck/")
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.printf( "失败！" );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText( NumqiandaoActivity.this, "网络请求失败！" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();
                            parseJSONWithJSONObject1(result);
                            Log.d( "NumqiandaoActivity", result );
                        }
                    }
                });

            }
        }).start();
    }

    //json解析
    private void parseJSONWithJSONObject1(String jsonData){
        try {
            JSONObject jsonObject= new JSONObject( jsonData );
            int res =jsonObject.getInt( "RESULT" );
            String post_longitude =jsonObject.getString( "post_longitude" );
            String post_latitude =jsonObject.getString( "post_latitude" );
            String post_num =jsonObject.getString( "post_num" );
            String post_date =jsonObject.getString( "post_date" );
            String post_id =jsonObject.getString( "post_id" );

            if(res==-1){
                //已经签到过了
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NumqiandaoActivity.this, "已经签到过了" , Toast.LENGTH_LONG).show();
                    }
                });
            }else if(res==1){
                //还未签到
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
                Date curDate = new Date(System.currentTimeMillis());
                final String curTime = formatter.format(curDate);
                long timeCha = Long.parseLong(curTime) - Long.parseLong(post_date);
                //发布签到距离10分钟失效，所以在10分钟内签到才有效
                if (timeCha <= 10 && timeCha >= 0){
                    //比较位置信息
                    GPSUtils  gpsUtils = new GPSUtils();
                    double Distance = gpsUtils.getDistance( Double.valueOf(post_latitude),Double.valueOf(post_longitude), sbweidu2, sbjingdu2);
                    //学生签到的距离测试,距离大于100米则显示签到失败
                    System.out.printf( "距离= "+Distance );
                    if(Distance<100){
                        //如果res==1 表明签到码正确  添加学生签到信息
                        HandleResponse(post_id,post_num);

                    }else{
                        //位置超过100米
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(NumqiandaoActivity.this, "位置超过100米" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }else{
                    //超过十分钟
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NumqiandaoActivity.this, "超过十分钟" , Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }else{
                //签到码错误 res==0
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NumqiandaoActivity.this, "签到码错误" , Toast.LENGTH_LONG).show();
                    }
                });
            }
            Log.d( "LoginActivity","RESULT is "+res );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void HandleResponse(final String post_id, final String post_num) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("post_id",post_id)
                        .add("post_num",post_num)
                        .add("post_type","0")//1 代表人脸 0 为数字签到
                        .add("student_id",studentId)
                        .build();

                final Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/numQiandaoS/")
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.printf( "失败！" );
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText( NumqiandaoActivity.this, "网络请求失败！" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();
                            parseJSONWithJSONObject2(result);
                            Log.d( "NumqiandaoActivity", result );
                        }
                    }
                });

            }
        }).start();
    }

    //json解析
    private void parseJSONWithJSONObject2(String jsonData){
        try {
            JSONObject jsonObject= new JSONObject( jsonData );
            final int res =jsonObject.getInt( "RESULT" );
            Log.d( "NumqiandaoActivity","RESULT is "+res );
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(res!=-1){
                        Toast.makeText(NumqiandaoActivity.this, "签到成功" , Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(NumqiandaoActivity.this, "签到信息存入失败" , Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        switch (requestCode){
            case 1 :
                if(grantResults.length>0 )  //&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                {
                    for(int result :grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText( this,"必须同意权限",Toast.LENGTH_LONG ).show();
                            //finish();
                            //return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText( this,"未知错误",Toast.LENGTH_SHORT ).show();
                    finish();
                }
                break;

            default:
        }

    }


    public class GPSUtils {
        private double EARTH_RADIUS = 6378.137;
        private double rad(double d) {
            return d * Math.PI / 180.0;
        }

        /**
         * Lat1 Lung1 表示A点经纬度，Lat2 Lung2 表示B点经纬度； a=Lat1 – Lat2 为两点纬度之差 b=Lung1
         * -Lung2 为两点经度之差； 6378.137为地球半径，单位为千米；  计算出来的结果单位为千米。
         * 通过经纬度获取距离(单位：千米)
         * @param lat1
         * @param lng1
         * @param lat2
         * @param lng2
         * @return
         */
        public double getDistance(double lat1, double lng1, double lat2,
                                  double lng2) {
            double radLat1 = rad(lat1);
            double radLat2 = rad(lat2);
            double a = radLat1 - radLat2;
            double b = rad(lng1) - rad(lng2);
            double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(b / 2), 2)));
            s = s * EARTH_RADIUS;
            s = Math.round(s * 10000d) / 10000d;
            // s = s*1000;    乘以1000是换算成米
            return s;
        }
    }

    public class MyLocationListener implements BDLocationListener {//经纬度的确认
        @Override
        public void onReceiveLocation(final BDLocation location) {

            if(location.getLocType()==BDLocation.TypeGpsLocation ||location.getLocType()==BDLocation.TypeNetWorkLocation )
            {
                navigateTo(location);
            }

            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    sbjingdu2=location.getLongitude();
                    sbweidu2=location.getLatitude();

                    StringBuilder currentPosition= new StringBuilder();
                    currentPosition.append( "纬度" ).append( location.getLatitude() ).append( "\n" );
                    currentPosition.append( "经度" ).append( location.getLongitude() ).append( "\n" );
                    currentPosition.append( "定位方式： " );
                    if(location.getLocType()==BDLocation.TypeGpsLocation){
                        currentPosition.append( "Gps" );
                    }else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                        currentPosition.append( "网络" );
                    }
                    positionText.setText( currentPosition );
                }
            } );

        }
    }

    private void navigateTo(BDLocation location) {
        if(isFirstLocte){
            LatLng ll = new LatLng( location.getLatitude(),location.getLongitude() );
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng( ll );
            baiduMap.animateMapStatus( update );
            update = MapStatusUpdateFactory.zoomTo( 16f );
            baiduMap.animateMapStatus( update );
            isFirstLocte=false;
        }

        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude( location.getLatitude() );
        locationBuilder.longitude( location.getLongitude() );
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
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
