package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
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


public class AddcourseActivity extends BaseActivity {
    private ImageButton back;
    private EditText coursename;
    private EditText yanzhengma;
    private Button saveBtn;
    private String teacherId;
    private String courseName;
    private String VerificationCode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.addcourse_main);

        //返回键
        back=findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(AddcourseActivity.this,MainActivity.class);
                //startActivity(intent);
                finish();
            }
        });


        Intent mainToAcourse = getIntent();
        teacherId = mainToAcourse.getStringExtra("teacherId");
        saveBtn = findViewById(R.id.save_course);
        coursename = findViewById(R.id.course_nametext);
        yanzhengma = findViewById(R.id.coursesign_text);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    new Thread( new Runnable() {
                        @Override
                        public void run() {
                            courseName = coursename.getText().toString();
                            VerificationCode = yanzhengma.getText().toString();
                            if (VerificationCode.length() == 4 && !courseName.isEmpty()){
                                //①、课程验证码四位，名称不为空
                                //②、查询是否已经存在该验证码
                            try
                            {
                                DBUtils dbUtils= new DBUtils();
                                String sql = "select course_id from  course where course_id ="+VerificationCode;
                                System.out.printf( sql );
                                ResultSet resultSet = dbUtils.excuteSQL( sql );

                                if(resultSet.next()){
                                    //以及存在该注册码，不能再次注册
                                    Looper.prepare();
                                    Toast.makeText(AddcourseActivity.this, "课证码已经被注册！", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                    resultSet.getStatement().getConnection().close();
                                }else {
                                    //该注册码可以注册
                                    sql = "insert into course  (course_id , course_name,teacher_id)" +
                                            "values ("+ VerificationCode + ",'" + courseName + "','" + teacherId +"')";

                                    System.out.printf( sql );
                                    int count = dbUtils.excuteSQLToADU( sql );

                                    if(count!=0) {
                                        Looper.prepare();
                                        Toast.makeText(AddcourseActivity.this, "加入成功", Toast.LENGTH_LONG).show();
                                        Looper.loop();
                                        Intent intent=new Intent(AddcourseActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        Looper.prepare();
                                        Toast.makeText(AddcourseActivity.this, "发布课程失败！" , Toast.LENGTH_LONG).show();
                                        Looper.loop();
                                    }
                                }
                            }catch(Exception e){
                                e.printStackTrace();
                                System.out.printf( e.getMessage() );
                            }

                        }else{
                            Looper.prepare();
                            Toast.makeText(AddcourseActivity.this, "课证码4位哦,并且所填项不能为空哦！", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            }
                       }
                    } ).start();


            }
        });
    }
}
