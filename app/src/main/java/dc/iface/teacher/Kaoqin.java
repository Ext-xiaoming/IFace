package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.TakePhotos.FaceActivity;
import dc.iface.TakePhotos.PhotoActivity;
import dc.iface.TakePhotos.TakePhotoActivity;
import dc.iface.object.AdapterKaoqin;
import dc.iface.object.ListItemKaoqin;



/**
 *教师端 考勤页面
 */

public class Kaoqin extends BaseActivity {
    private ImageButton back;
    private Button kaoqinBtn;
    private Button faceBtn;

    private TextView biaoti;
    private ListView lvListView ;
    private String courseCode;//从主界面来的课程码
    private String teacherId;
    private String TAG = "Kaoqin";
    private AdapterKaoqin adapterKaoqin;
    private List<ListItemKaoqin> listItemKaoqin = new ArrayList<>();//ListItem课程集

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.kaoqin);

        lvListView = findViewById(R.id.kaoqin_list);

        Intent mainToKQ = getIntent();
        courseCode = mainToKQ.getStringExtra("courseId");//课程码匹配
        teacherId=mainToKQ.getStringExtra("teacherId");

        Log.i(TAG,"1."+courseCode);

        //将toolbar转为 “考勤”  字样
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("考勤");
        //back键
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                ActivityCollectorUtil.finishActivity( MainActivity.class);
                Intent intent=new Intent(Kaoqin.this,MainActivity.class);
                startActivity(intent);
                ActivityCollectorUtil.finishActivity(Kaoqin.class);
                finish();
            }
        });

        //发布签到按钮 --> 点击 转到 发布数字码签到
        kaoqinBtn=findViewById(R.id.fabuqiandaoBtn);
        kaoqinBtn.setOnClickListener(new View.OnClickListener() {//进入考勤的按钮
            @Override
            public void onClick(View view) {
                Log.i(TAG , "1"+ "进入跳转" );
                Intent intent2 = new Intent(Kaoqin.this, PhotoActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("teacherId",teacherId);
                intent2.putExtra("flag","1");

                startActivity(intent2);
            }
        });

        //拍照人脸签到按钮 --> 点击 转到 拍照功能
        faceBtn=findViewById(R.id.facebtn);
        faceBtn.setOnClickListener(new View.OnClickListener() {//进入考勤的按钮
            @Override
            public void onClick(View view) {
                Log.i(TAG , "1"+ "进入跳转" );
                Intent intent2 = new Intent(Kaoqin.this, FaceActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("teacherId",teacherId);

                startActivity(intent2);
            }
        });


        new Thread( new Runnable() {
            @Override
            public void run() {

                //***********************************************************************************************************
                //签到情况展示（补充未签到成功的同学名单）
                //***********************************************************************************************************
                //签到第几次 + 时间  + 签到成功人数
                //签到次序 和 时间

                DBUtils dbUtils= new DBUtils();

                String sql = "select post_id , post_date , post_num from post_check_in where course_id ="+courseCode +" order by post_num desc";
                System.out.printf( sql );
                ResultSet resultSet = dbUtils.excuteSQL( sql );

                try{
                    while(resultSet.next()){
                        ListItemKaoqin item = new ListItemKaoqin();
                        item.setCheckNumber(resultSet.getString( "post_num" ));
                        item.setTime(resultSet.getString("post_date"));
                        item.setPostId( resultSet.getString( "post_id" ) );
                        item.setQiandaoNumber(String.valueOf( QueryNum(resultSet.getInt( "post_id" )) ));
                        listItemKaoqin.add(item);
                    }
                    adapterKaoqin = new AdapterKaoqin(Kaoqin.this,
                            R.layout.item_kaoqian, listItemKaoqin);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println( "list.size="+listItemKaoqin.size()+"--00000000000000\n");
                            for (int i = 0; i < listItemKaoqin.size(); i++) {
                                ListItemKaoqin s = (ListItemKaoqin)listItemKaoqin.get(i);
                                System.out.println(i+"输出："+s.getCheckNumber()+"  "+s.getPostId()+"  "+s.getQiandaoNumber()+"\n");
                            }
                            lvListView.setAdapter(adapterKaoqin);
                        }
                    });
                    resultSet.getStatement().getConnection().close();
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.printf( e.getMessage() );
                }
            }
        } ).start();





        lvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                Intent intent = new Intent(Kaoqin.this , QiandaoStatus.class);//考勤界面,传入第几次考勤
                intent.putExtra("checkNum", listItemKaoqin.get(position).getCheckNumber());//第几次考勤
                intent.putExtra("courseId",courseCode);
                intent.putExtra("postId",listItemKaoqin.get(position).getPostId());
                startActivity(intent);
            }
        });
    }

    public int QueryNum(int post_id)
    {
        int num=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select student_id from sign_in where post_id ="+post_id;
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            while(resultSet.next()){
                //System.out.println( resultSet.getString("course_name") );
                num++;//统计人数
            }

            resultSet.getStatement().getConnection().close();
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return num;
    }

}
