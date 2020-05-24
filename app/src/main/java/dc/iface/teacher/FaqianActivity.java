package dc.iface.teacher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;


public class FaqianActivity extends BaseActivity {
    private double sbjingdu2 = 25.325341;//25.325341
    private double sbweidu2 =110.422284;//经纬度

    private MapView mapView=null;
    private BaiduMap baiduMap;

    private LocationClient locationClient;

    public MyLocationListener myListener=new MyLocationListener();//myListener=new MyLocationListener()

    boolean isFirstLoc=true;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final int REQUEST_CODE_SCAN = 0x0000;


    private ImageButton back;
    private TextView biaoti;


    private String RandNum ;//4位随机数的存储
    private String TAG = "FaqianActivity";
    private String courseCode ;//课堂码
    private Button okqd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        SDKInitializer.initialize(getApplicationContext());
        setContentView( R.layout.faqianz);
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("发布签到");
        back = findViewById(R.id.btn_back);

        mapView=findViewById(R.id.bmapView2);
        baiduMap=mapView.getMap();

        baiduMap.setMyLocationEnabled(true);

        List<String>permissionList=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(FaqianActivity.this,
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

        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        RandNum = getSmallLetter (4);//生成8位随机字符串
        //createQRImage(RandNum);

        TextView faqianNumber =findViewById(R.id.faqianNumber);
        faqianNumber.setText("签到码："+RandNum);



        Intent KQToStatus = getIntent();
        courseCode = KQToStatus.getStringExtra("courseId");//课程码匹配
        final int coursecode=Integer.parseInt(courseCode);

        okqd =findViewById(R.id.okQiandao);//确认签到
        okqd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 需要有post_id（查询，如果有则加1 ，没有则 默认从0 开始）


                System.out.printf("FaqianActivity  开启数据库线程！\n");
                new Thread( new Runnable() {
                    @Override
                    public void run() {

                        System.out.printf("FaqianActivity  开启数据库线程！\n");
                        final int postID=Integer.parseInt( RandNum );

                        //生成当前时间
                        SimpleDateFormat formatter   =   new   SimpleDateFormat("yyyyMMddHHmm");
                        Date curDate =  new Date(System.currentTimeMillis());
                        final String   curTime   =   formatter.format(curDate);

                        // teacher_id（通过查询 course_id得到）
                        final int teacherID = QueryTeacherId(coursecode);

                        // post_type（通过直接指定）
                        final  int post_type =0;//普通签到
                        // post_num(通过查询post_check_in 中同一个course_id 下的最大 post_num)
                        final  int postNum = QueryPostNum(coursecode)+1;

                        DBUtils dbUtils= new DBUtils();
                        String sql = "insert into post_check_in  (post_id , post_date,teacher_id,post_num,course_id," +
                                "post_type,post_longitude,post_latitude)" +
                                "values ("+ postID + ",'" + curTime + "'," + teacherID +","+ postNum+ ","+coursecode +","+post_type+",'"
                                + sbjingdu2+"','"+ sbweidu2+ "')";
                        System.out.printf( sql );


                        int count  = dbUtils.excuteSQLToADU( sql );

                        try{
                            if(count!=0) {
                                Looper.prepare();
                                Toast.makeText(FaqianActivity.this, "发布签到成功", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }else {
                                Looper.prepare();
                                Toast.makeText(FaqianActivity.this, "发布签到失败", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.printf( e.getMessage() );
                        }

                    }
                } ).start();

            }

        });
    }

    /*// 需要有post_id（查询，如果有则加1 ，没有则 默认从0 开始）
    public int QueryPostId()
    {
        int postId=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select max(post_id) from post_check_in ";
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            if(resultSet.next()) {
                postId=Integer.parseInt(resultSet.getString( "max(post_id)" ) );
                resultSet.getStatement().getConnection().close();
            }
            else{
                postId=0;
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return postId;
    }*/

    // teacher_id（通过查询 course_id得到）
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


    //
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
    }

    public static String getSmallLetter(int size){//随机生成size位数字字符串
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        for(int i=0; i<size;i++){
            buffer.append( (random.nextInt(10) ) );
        }
        return buffer.toString();
    }



    //要转换的地址或字符串,可以是中文
    public void createQRImage(String url)
    {


    }


    public class MyLocationListener implements BDLocationListener {//经纬度的确认
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

            sbjingdu2=location.getLongitude();
            sbweidu2=location.getLatitude();
            Log.i(TAG,"1."+sbjingdu2);
            Log.i(TAG,"1."+sbweidu2);

            if (isFirstLoc){
                isFirstLoc=false;
                LatLng latLng=new LatLng(location.getLatitude(),
                        location.getLongitude());

                MapStatus.Builder builder=new MapStatus.Builder();
                builder.target(latLng).zoom(15f);
                baiduMap.animateMapStatus( MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

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
    }
}