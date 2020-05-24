package dc.iface.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import dc.iface.object.AdapterKaoqin;
import dc.iface.object.ListItemKaoqin;


//***********************************************
//学生考勤页面 （签到  /  人脸识别签到）
//************************************************
public class KaoqinActivity extends BaseActivity {
    private ImageButton back;
    private Button kaoqinBtn;
    private Button facebtn;

    private TextView biaoti;
    ListView lvListView ;
    private String courseCode;//从主界面来的课程码
    private String studentId;//从主界面来的当前学生学号

    private String TAG = "KaoqinActivity";
    private AdapterKaoqin adapterKaoqin;
    private List<ListItemKaoqin> listItemKaoqin = new ArrayList<>();//ListItem课程集

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.stukaoqin);

        lvListView = findViewById(R.id.stukaoqin_list);
        kaoqinBtn = findViewById(R.id.stufabuqiandaoBtn);

        kaoqinBtn.setText("签到");

        Intent mainToKQ = getIntent();
        courseCode = mainToKQ.getStringExtra("courseId");//课程码匹配
        studentId = mainToKQ.getStringExtra("studentId");

        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("考勤");
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                //点击返回键后，从新打开新的StuMainActivity活动，之前的StuMainActivity活动需要关闭
                ActivityCollectorUtil.finishActivity(StuMainActivity.class);

                Intent intent=new Intent(KaoqinActivity.this, StuMainActivity.class);
                intent.putExtra("courseId",courseCode);
                startActivity(intent);
                ActivityCollectorUtil.finishActivity(KaoqinActivity.class);
                finish();
            }
        });

        //考勤按键-->进去签到页面
        kaoqinBtn.setOnClickListener(new View.OnClickListener() {//进入签到的按钮
            @Override
            public void onClick(View view) {
                Log.i(TAG , "1"+ "进入签到" );
                Intent intent2 = new Intent(KaoqinActivity.this, NumqiandaoActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("studentId",studentId);
                startActivity(intent2);
            }
        });


        new Thread( new Runnable() {
            @Override
            public void run() {
                //签到次序+ 时间+ 是否出勤

                //首先将所有的发布的签到 拿到
                DBUtils dbUtils= new DBUtils();

                String sql = "select post_id,post_num , post_date from post_check_in  where course_id ="+courseCode
                        +" order by post_num desc ";
                System.out.printf( sql );
                ResultSet resultSet = dbUtils.excuteSQL( sql );
                try{
                    while(resultSet.next()){

                        ListItemKaoqin item = new ListItemKaoqin();
                        item.setCheckNumber( resultSet.getString("post_num") );
                        System.out.printf(  "post_num= "+ resultSet.getString("post_num") );
                        item.setPostId( resultSet.getString("post_id") );
                        item.setTime(resultSet.getString("post_date"));
                        System.out.printf(  "post_date= "+resultSet.getString("post_date") );

                        if(IsAttendanceS(resultSet.getString("post_id") ,studentId))
                        {
                            item.setQiandaoNumber("出勤");//其实这是出勤状况 默认出勤
                        }else{
                            item.setQiandaoNumber("缺勤");
                        }

                        listItemKaoqin.add(item);
                    }

                    for (int i = 0; i < listItemKaoqin.size(); i++) {
                        ListItemKaoqin s = (ListItemKaoqin)listItemKaoqin.get(i);
                        System.out.println(i+"输1出："+s.getCheckNumber()+"  "+s.getPostId()+"  "+s.getQiandaoNumber()+"\n");
                    }


                    adapterKaoqin = new AdapterKaoqin(KaoqinActivity.this,
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

    }

    public boolean IsAttendanceS(String postId,String studentid){
        boolean flag=true;//出勤

        DBUtils dbUtils= new DBUtils();
        String sql = "select sign_id from  sign_in where student_id ="+studentid+" and post_id ="+postId;
        System.out.printf( sql );
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            if(resultSet.next()) {
                //查询结果不为空，则出勤
                System.out.println( resultSet.getString("sign_id") );
                flag=true;
                resultSet.getStatement().getConnection().close();
            }else{
                //查询结果为空，表明没有出勤
                System.out.println("学生id= "+studentid+" 未签到" );
                flag=false;
            }

        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }

        return  flag;
    }
}
