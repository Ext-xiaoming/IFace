package dc.iface.student;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.ResultSet;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;

public class AddClassActivity extends BaseActivity {
    private Button subBtn;
    private EditText editText;
    private ImageButton back;
    String number;
    private String studentId;
    private String TAG="AddClassActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.joinclass_main);

        Log.i(TAG,"2."+"查找"+studentId +number);

        back = findViewById(R.id.backBtn);
        editText = findViewById(R.id.classNum);
        subBtn = findViewById(R.id.submit);
        //**********************************************************************************
        //返回键，返回之后要把当前活动结束,
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(AddClassActivity.this, StuMainActivity.class);
                //startActivity(intent);
                finish();
            }
        });
        //**********************************************************************************

        Intent mainToKQ = getIntent();
        studentId = mainToKQ.getStringExtra("studentId");//课程码匹配
        Log.i(TAG,"2."+"查找"+studentId +number);

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final  int student_id=Integer.parseInt( studentId );
                number = editText.getText().toString();//课程码
                if(number.length() == 4){
                    //查询是否已经加入该班级
                    final int course_id= Integer.parseInt(number) ;

                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            DBUtils dbUtils= new DBUtils();
                            String sql = "select student_id from  student_course where course_id ="+course_id;
                            System.out.printf( sql );
                            ResultSet resultSet = dbUtils.excuteSQL( sql );
                            try
                            {
                                while(resultSet.next()){
                                    if(student_id==resultSet.getInt( "student_id" )){
                                        //学生已经加课不用重复加入
                                        Looper.prepare();
                                        Toast.makeText(AddClassActivity.this, "已经加入，不用重复加入！", Toast.LENGTH_SHORT).show();
                                        Looper.loop();
                                        resultSet.getStatement().getConnection().close();
                                        break;
                                    }
                                }


                                final  int stu_course_id = QueryStuCourseId()+1;
                                //可以加课
                                sql = "insert into student_course  (stu_course_id , course_id,student_id)" +
                                        "values ("+ stu_course_id + "," + course_id + "," + student_id  + ")";
                                System.out.printf( sql );
                                int count = dbUtils.excuteSQLToADU( sql );

                                if(count!=0) {
                                    //********************************************************************************************
                                    //这里如果添加成功的话，结束当前添加课程的界面，进入主页面显示加入后的班级列表情况

                                    Looper.prepare();
                                    Toast.makeText(AddClassActivity.this, "加入成功", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                    finish();
                                    //********************************************************************************************
                                }else {
                                    Looper.prepare();
                                    Toast.makeText(AddClassActivity.this, "加课码错误，请重新输入！", Toast.LENGTH_LONG).show();
                                    Looper.loop();
                                }

                            }catch(Exception e){
                                e.printStackTrace();
                                System.out.printf( e.getMessage() );
                            }
                        }
                    } ).start();

                } else{
                    Toast.makeText(AddClassActivity.this, "课堂验证码四位哦", Toast.LENGTH_LONG).show();
                    editText.setText("");//清空
                }
            }
        });
    }

    public int QueryStuCourseId()
    {
        int stu_course_id=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select max(stu_course_id) from student_course ";
        System.out.printf( sql );
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            resultSet.next();
            if(resultSet!=null) {
                stu_course_id=resultSet.getInt( "max(stu_course_id)" );
                resultSet.getStatement().getConnection().close();
            }
            else{
                stu_course_id=0;
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return stu_course_id;
    }
}
