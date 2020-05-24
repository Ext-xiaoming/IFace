package dc.iface.student;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;



public class NumqiandaoActivity extends BaseActivity {
    private TextView biaoti;
    private EditText editText;
    private ImageButton back;
    private Button button;

    private String studentId;
    private String courseCode;
    private String sbjingdu2;
    private String sbweidu2;

    private String TAG = "NumqiandaoActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.num_qaindao_sec);

        biaoti=findViewById(R.id.text_titlesec);
        biaoti.setText("数字签到");

        //返回键
        back=findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(NumqiandaoActivity.this,QiandaoActivity.class);
                //intent.putExtra("courseId",courseCode);
                //startActivity(intent);
                finish();
            }
        });

        Intent intent2 = getIntent();
        courseCode = intent2.getStringExtra("courseId");
        studentId = intent2.getStringExtra("studentId");

        sbjingdu2 = intent2.getStringExtra("sbjingdu2");//经纬度
        sbweidu2 = intent2.getStringExtra("sbweidu2");//string类型

        Log.i(TAG,"1."+"进入"+courseCode+" "+studentId+" "+sbjingdu2 + " "+sbweidu2);

        editText=findViewById(R.id.et_shuziNum);
        button=findViewById(R.id.num_submit);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //获取 sign_id post_id  student_id sign_date post_type  sign-经纬度
                //依靠course_id 从 post_check_in 中找到post_type  post_id post-经纬度 post_date post_num

                new Thread( new Runnable() {
                    @Override
                    public void run() {

                        //定义变量
                        final String  course_id =  courseCode ;
                        final String  student_id=  studentId;
                        String  post_id = editText.getText().toString();
                        Long post_date = Long.valueOf(0); //待查询
                        String  post_num ="";//待查询
                        double  post_longitude = 25.325341;//待查询
                        double  post_latitude =  110.422284;//待查询


                        DBUtils dbUtils= new DBUtils();

                        //首先查查是否已经签到了，方法：post_id是已知的，student_id已知 只需要查 sign_in是否有 有 就已经签到了不用再签了
                        String sql = "select * from sign_in where post_id="+post_id+" and student_id="+studentId;
                        System.out.printf( sql );
                        ResultSet resultSet = dbUtils.excuteSQL( sql );
                        try{

                            if(resultSet.next()) {
                                resultSet.getStatement().getConnection().close();
                                Looper.prepare();
                                Toast.makeText(NumqiandaoActivity.this, "已经签到成功，不用重复签到！", Toast.LENGTH_LONG).show();
                                Looper.loop();
                            }
                            else{
                                //在post_check_in 中查询 course_id 为 123 中post_num最大的那一条

                                sql = "select post_id , post_date, post_num , post_longitude , post_latitude  " +
                                        "from  post_check_in  where  post_num in( select max(post_num) from post_check_in where post_id ="+post_id+ ")";
                                System.out.printf( sql );
                                resultSet = dbUtils.excuteSQL( sql );

                                try{
                                    while(resultSet.next()){
                                        post_id=resultSet.getString( "post_id" );
                                        post_date=resultSet.getLong( "post_date" );
                                        post_num=resultSet.getString( "post_num" );
                                        post_longitude=resultSet.getDouble( "post_longitude" );
                                        post_latitude=resultSet.getDouble( "post_latitude" );
                                    }
                                    resultSet.getStatement().getConnection().close();
                                }catch (Exception e){
                                    e.printStackTrace();
                                    System.out.printf( e.getMessage() );
                                }

                                /***
                                 *查询结束后需要对比数据   时间  和 位置
                                 */
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
                                Date curDate = new Date(System.currentTimeMillis());
                                final String curTime = formatter.format(curDate);
                                long timeCha = Long.parseLong(curTime) - post_date;

                                //发布签到距离10分钟失效，所以在10分钟内签到才有效
                                if (timeCha <= 10 && timeCha >= 0) {

                                    GPSUtils  gpsUtils = new GPSUtils();
                                    double Distance = gpsUtils.getDistance( post_latitude, post_longitude,  Double.parseDouble(sbweidu2), Double.parseDouble(sbjingdu2));
                                    //学生签到的距离测试,距离大于100米则显示签到失败
                                    System.out.printf( "距离= "+Distance );
                                    if (Distance  < 100) {
                                        //表明可以成功签到 ，插入信息

                                        System.out.printf( "QuerySignNum()="+QuerySignNum() );
                                        System.out.printf( "student_id()="+student_id );

                                        QuerySignIN(QuerySignNum(),student_id,0,post_id,post_num);
                                    }else{
                                        Looper.prepare();
                                        Toast.makeText(NumqiandaoActivity.this, "距离超过100米无法签到！", Toast.LENGTH_LONG).show();
                                        Looper.loop();
                                    }
                                } else{
                                    Looper.prepare();
                                    Toast.makeText(NumqiandaoActivity.this, "已超过签到时间10分钟！", Toast.LENGTH_LONG).show();
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

        });
    }

    public String  QuerySignNum()
    {
        String sign_id="";

        DBUtils dbUtils= new DBUtils();
        String sql = "select max(sign_id) from sign_in ";
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            if(resultSet.next()){
                sign_id=resultSet.getString( "max(sign_id)" );
                resultSet.getStatement().getConnection().close();

            }else {
                System.out.printf( "" +
                        "111111111111111" );
            }

        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return sign_id;
    }


    public void QuerySignIN(String sign_id,String student_id,int post_type,String post_id ,String post_num)
    {
        /***
         *添加学生签到信息 首先需要确定 sign_in 的值 查询当前最大值，+1
         */
        System.out.printf( "启动！" );


        DBUtils dbUtils = new DBUtils();
        String sql = "insert into sign_in  (sign_id ,student_id,post_type,post_id, post_num)" +
                " values ("+ Integer.parseInt( sign_id )+1 + "," + student_id + "," + post_type  +"," +  post_id + "," + post_num +")";
        System.out.printf( sql );
        int count = dbUtils.excuteSQLToADU( sql );

        if(count>0){
            Looper.prepare();
            Toast.makeText(NumqiandaoActivity.this, "签到成功!", Toast.LENGTH_LONG).show();
            Looper.loop();
        }else{
            Looper.prepare();
            Toast.makeText(NumqiandaoActivity.this, "未成功签到！请检查网络！", Toast.LENGTH_LONG).show();
            Looper.loop();
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
}
