package dc.iface.teacher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;


public class FaqianActivity extends BaseActivity {
    private double sbjingdu2 = 25.325341;//25.325341
    private double sbweidu2 =110.422284;//经纬度

    private MapView mapView=null;
    private BaiduMap baiduMap;
    private LocationClient locationClient;
    boolean isFirstLocte=true;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private ImageButton back;
    private TextView biaoti;
    private String RandNum ;//4位随机数的存储
    private String TAG = "FaqianActivity";
    private String courseCode ;//课堂码
    private Button okqd;
    public LocationClient mLocationClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mLocationClient = new LocationClient( getApplicationContext() );
        mLocationClient.registerLocationListener( new MyLocationListener() );
        SDKInitializer.initialize( getApplicationContext() );

        setContentView( R.layout.faqianz);
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("发布签到");
        back = findViewById(R.id.btn_back);


        mapView=findViewById(R.id.bmapView2);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission( FaqianActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(FaqianActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(FaqianActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(FaqianActivity.this,
                Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_SETTINGS);
        }
        if (!permissionList.isEmpty()){
            String[] permissions =permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(FaqianActivity.this, permissions, 1);
        }else {
            requestLocation();
        }


        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
//                Intent intent2 = new Intent(FaqianActivity.this, Kaoqin.class);
//                startActivity(intent2);
                finish();
            }
        });

        RandNum = getSmallLetter (4);//生成8位随机字符串

        TextView faqianNumber =findViewById(R.id.faqianNumber);
        faqianNumber.setText("签到码："+RandNum);

        Intent KQToStatus = getIntent();
        courseCode = KQToStatus.getStringExtra("courseId");//课程码匹配

        okqd =findViewById(R.id.okQiandao);//确认签到
        okqd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {toPostNumCheck();}

        });
    }


    public void toPostNumCheck() {
        SimpleDateFormat formatter   =   new   SimpleDateFormat("yyyyMMddHHmm");
        Date curDate =  new Date(System.currentTimeMillis());
        final String   curTime   =   formatter.format(curDate);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    FormBody body = new FormBody.Builder()
                            .add("post_id",RandNum)
                            .add("post_date",curTime)
                            .add("course_id",courseCode)
                            .add("post_longitude", String.valueOf( sbjingdu2 ) )
                            .add("post_latitude", String.valueOf( sbweidu2 ) )
                            .build();

                    final Request request = new Request.Builder()
                            .url(server+"teaPostNumCheck/")
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
                                    Toast.makeText( FaqianActivity.this, "网络请求失败！" , Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()){
                                final String result = response.body().string();
                                parseJSONWithJSONObject(result);
                                Log.d( "FaqianActivity", result );
                            }
                        }
                    });

                }
            }).start();

    }

    //json解析
    private void parseJSONWithJSONObject(String jsonData){
        try {
            JSONObject jsonObject= new JSONObject( jsonData );
            int res =jsonObject.getInt( "RESULT" );
            HandleResponse(res);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void HandleResponse(final int res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(res==1){
                    Toast.makeText( FaqianActivity.this, "发布数字签到成功！" , Toast.LENGTH_LONG).show();
                }else if(res==0){
                    Toast.makeText( FaqianActivity.this, "数字签到已经发布！" , Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText( FaqianActivity.this, "发布签到失败！" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public static String getSmallLetter(int size){//随机生成size位数字字符串
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        for(int i=0; i<size;i++){
            buffer.append( random.nextInt(9) +1);
        }
        return buffer.toString();
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
                   // positionText.setText( currentPosition );
                }
            } );

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
                            //Toast.makeText( this,"必须同意权限",Toast.LENGTH_LONG ).show();
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
    public class GPSUtils {
        private double EARTH_RADIUS = 6378.137;

        private double rad(double d) {
            return d * Math.PI / 180.0;
        }

        /**
         * Lat1 Lung1 表示A点经纬度，Lat2 Lung2 表示B点经纬度； a=Lat1 – Lat2 为两点纬度之差 b=Lung1
         * -Lung2 为两点经度之差； 6378.137为地球半径，单位为千米；  计算出来的结果单位为千米。
         * 通过经纬度获取距离(单位：千米)
         *
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



      /* // teacher_id（通过查询 course_id得到）
    public int QueryTeacherId(int course_id)
    {
        int teacher_id=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select teacher_id from course where course_id="+course_id;
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{

            if(resultSet.next()) {
                teacher_id=resultSet.getInt( "teacher_id" );
                resultSet.getStatement().getConnection().close();
            }
            else{
                System.out.printf("未知错误");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return teacher_id;
    }

    // // （查询，如果有则加1 ，没有则 默认从0 开始）
    // post_num(通过查询post_check_in 中同一个course_id 下的最大 post_num)
    public int QueryPostNum(int course_id)
    {
        int postNum=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select max(post_num) from post_check_in where course_id= "+course_id;
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{

            if(resultSet.next()) {
                postNum=resultSet.getInt( "max(post_num)" );
                resultSet.getStatement().getConnection().close();
            }
            else{
                postNum=0;
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return postNum;
    }*/
}
