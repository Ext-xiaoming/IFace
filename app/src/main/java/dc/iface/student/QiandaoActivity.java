package dc.iface.student;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
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
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;


import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;


public class QiandaoActivity {
    /*private String studentId;
    private String courseCode;
    private double sbjingdu2;
    private double sbweidu2;
    private String TAG = "QiandaoActivity";

    private MapView mapView=null;
    private BaiduMap baiduMap;

    private LocationClient locationClient;

    public MyLocationListener myListener=new MyLocationListener();

    boolean isFirstLoc=true;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;

    private TextView scanResult;

    private Button shuziBtn;
    private Button erweimaBtn;
    private TextView biaoti;
    private ImageButton back;

    private String content = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        SDKInitializer.initialize(getApplicationContext());
        setContentView( R.layout.qiandao_mian);

        Intent mainToKQ = getIntent();
        courseCode = mainToKQ.getStringExtra("courseId");//课程码匹配
        studentId = mainToKQ.getStringExtra("studentId");//学生学号

        shuziBtn = findViewById(R.id.shuziBtn);
        shuziBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//数字签到
                //这里直接关闭该活动，如果在NumqiandaoActivity中返回则会回到kaoqin页面
                Intent intent2=new Intent(QiandaoActivity.this,NumqiandaoActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("studentId",studentId);
                intent2.putExtra("sbjingdu2",String.valueOf(sbjingdu2) );
                intent2.putExtra("sbweidu2",String.valueOf(sbweidu2));
                startActivity(intent2);
               // finish();
            }
        });

        //地图操作
        mapView=findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();

        baiduMap.setMyLocationEnabled(true);

        List<String>permissionList=new ArrayList<>();

        if (ContextCompat.checkSelfPermission(QiandaoActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(QiandaoActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(QiandaoActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(QiandaoActivity.this,
                Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_SETTINGS);
        }
        if (!permissionList.isEmpty()){
            String[] permissions =permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(QiandaoActivity.this, permissions, 1);

        }

        locationClient=new LocationClient(getApplicationContext());

        locationClient.registerLocationListener(myListener);

        LocationClientOption option=new LocationClientOption();
        option.setLocationMode( LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        locationClient.start();

        shuziBtn=findViewById(R.id.shuziBtn);
        erweimaBtn=findViewById(R.id.erweiBtn);

        back = findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(QiandaoActivity.this,KaoqinActivity.class);
                intent.putExtra("courseId",courseCode);
                startActivity(intent);
                finish();
            }
        });

        biaoti=findViewById(R.id.text_titlesec);
        biaoti.setText("签到");

        scanResult = findViewById(R.id.ersign_num);

        erweimaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.erweiBtn:
                        //动态权限申请
                        if (ContextCompat.checkSelfPermission(QiandaoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(QiandaoActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                        } else {
                            goScan();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    *
     * 跳转到扫码界面扫码

    private void goScan(){
        Intent intent = new Intent(QiandaoActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，可能无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {

            if (data != null) {
                //返回的文本内容
                content = data.getStringExtra(DECODED_CONTENT_KEY);//content 为 课程的签到码
                //返回的BitMap图像
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);

                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        //获取 sign_id post_id  student_id sign_date post_type  sign-经纬度
                        //依靠course_id 从 post_check_in 中找到post_type  post_id post-经纬度 post_date post_num

                        //定义变量
                        final int  course_id = Integer.parseInt( courseCode );
                        final int  student_id= Integer.parseInt( studentId );
                        int  post_id = Integer.parseInt( content );
                        Long post_date = Long.valueOf(0); //待查询
                        int  post_num = 0;//待查询
                        double  post_longitude = 25.325341;//待查询
                        double  post_latitude =  110.422284;//待查询
                        DBUtils dbUtils= new DBUtils();

                        //首先查查是否已经签到了，方法：post_id是已知的，student_id已知 只需要查 sign_in是否有 有 就已经签到了不用再签了
                        String sql = "select * from sign_in where post_id="+post_id+"student_id="+student_id;
                        System.out.printf(sql );
                        ResultSet resultSet = dbUtils.excuteSQL( sql );
                        try{
                            resultSet.next();
                            if(resultSet!=null) {
                                resultSet.getStatement().getConnection().close();
                                Looper.prepare();
                                Toast.makeText(QiandaoActivity.this, "已经签到成功，不用重复签到！", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }
                            else{
                                //在post_check_in 中查询 course_id 为 123 中post_num最大的那一条

                                sql = "select post_id , post_date, post_num , post_longitude , post_latitude  " +
                                        "from  post_check_in  where  post_num in( select max(post_num) from post_check_in where post_id ="+post_id+ ")";
                                System.out.printf(sql );
                                resultSet = dbUtils.excuteSQL( sql );

                                try{
                                    while(resultSet.next()){
                                        post_id=resultSet.getInt( "post_id" );
                                        post_date=resultSet.getLong( "post_date" );
                                        post_num=resultSet.getInt( "post_num" );
                                        post_longitude=resultSet.getDouble( "post_longitude" );
                                        post_latitude=resultSet.getDouble( "post_latitude" );
                                    }
                                    resultSet.getStatement().getConnection().close();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    System.out.printf( e.getMessage() );
                                }

                                **
                                 *查询结束后需要对比数据   时间  和 位置

                                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
                                Date curDate = new Date(System.currentTimeMillis());
                                final String curTime = formatter.format(curDate);
                                long timeCha = Long.parseLong(curTime) - post_date;

                                //发布签到距离10分钟失效，所以在10分钟内签到才有效
                                if (timeCha <= 10 && timeCha >= 0) {

                                    GPSUtils  gpsUtils = new GPSUtils();
                                    double Distance = gpsUtils.getDistance( post_latitude, post_longitude, sbweidu2, sbjingdu2 );
                                    //学生签到的距离测试,距离大于100米则显示签到失败
                                    if (Distance < 100) {
                                        //表明可以成功签到 ，插入信息
                                        final  int sign_id=QuerySignNum();
                                        QuerySignIN(sign_id,student_id,0,post_id,post_num);
                                    }else{
                                        Looper.prepare();
                                        Toast.makeText(QiandaoActivity.this, "距离超过100米无法签到！", Toast.LENGTH_LONG).show();
                                        Looper.loop();
                                    }
                                } else{
                                    Looper.prepare();

                                    Toast.makeText(QiandaoActivity.this, "已超过签到时间10分钟！", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.printf( e.getMessage() );
                        }

                    }
                } ).start();

            }
            scanResult.setText(content);
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location==null || mapView==null){
                return;
            }
            MyLocationData locationData=new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locationData);

////////////////////////////////////////////////////////////////////////////////////得到现在的经纬度
            sbjingdu2=location.getLongitude();
            sbweidu2=location.getLatitude();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            isFirstLoc=false;
            LatLng latLng=new LatLng(location.getLatitude(),
                    location.getLongitude());

            MapStatus.Builder builder=new MapStatus.Builder();
            builder.target(latLng).zoom(15f);
            baiduMap.animateMapStatus( MapStatusUpdateFactory.newMapStatus(builder.build()));


        }

    }

    public int QuerySignNum()
    {
        int sign_id=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select max(sign_id) from sign_in ";
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            sign_id=Integer.parseInt( resultSet.getString( "max(sign_id)" ));
            resultSet.getStatement().getConnection().close();
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return sign_id+1;
    }


    public void QuerySignIN(int sign_id,int student_id,int post_type,int post_id ,int post_num)
    {
        **
         *添加学生签到信息 首先需要确定 sign_in 的值 查询当前最大值，+1

        DBUtils dbUtils = new DBUtils();
        String sql = "insert into sign_in  (sign_id ,student_id,post_type,post_id, post_num)" +
                "values ("+ sign_id + "," + student_id + "," + post_type  +"," +  post_id + "," + post_num +")";
        System.out.printf(sql );
        int count = dbUtils.excuteSQLToADU( sql );

        if(count>0){
            Looper.prepare();
            Toast.makeText(QiandaoActivity.this, "签到成功!", Toast.LENGTH_LONG).show();
            Looper.loop();
        }else{
            Looper.prepare();
            Toast.makeText(QiandaoActivity.this, "未成功签到！请检查网络！", Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }


    public class GPSUtils {//利用经纬度计算距离
        private double EARTH_RADIUS = 6378.137;

        private double rad(double d) {
            return d * Math.PI / 180.0;
        }

        *
         * Lat1 Lung1 表示A点经纬度，Lat2 Lung2 表示B点经纬度； a=Lat1 – Lat2 为两点纬度之差 b=Lung1
         * -Lung2 为两点经度之差； 6378.137为地球半径，单位为千米；  计算出来的结果单位为千米。
         * 通过经纬度获取距离(单位：千米)
         * @param lat1
         * @param lng1
         * @param lat2
         * @param lng2
         * @return

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
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView=null;
        super.onDestroy();
    }*/
}
