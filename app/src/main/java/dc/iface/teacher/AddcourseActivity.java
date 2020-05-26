package dc.iface.teacher;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;


import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.login.LoginActivity;
import dc.iface.object.CourseListItem;
import dc.iface.student.KaoqinActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;


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
                toAddCourse();
            }
        });
    }


    public void toAddCourse() {
        courseName = coursename.getText().toString();
        VerificationCode = yanzhengma.getText().toString();
        if (VerificationCode.length() == 4 && !courseName.isEmpty()){

            //①、课程验证码四位，名称不为空
            //②、查询是否已经存在该验证码
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    FormBody body = new FormBody.Builder()
                            .add("courseId",VerificationCode)
                            .add("courseName",courseName)
                            .add("teacherId",teacherId)

                            .build();

                    final Request request = new Request.Builder()
                            .url(server+"teaPostCourse/")
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
                                    Toast.makeText( AddcourseActivity.this, "网络请求失败！" , Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()){
                                final String result = response.body().string();
                                parseJSONWithJSONObject(result);
                                Log.d( "AddcourseActivity", result );
                            }
                        }
                    });

                }
            }).start();
        }
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
                    Toast.makeText( AddcourseActivity.this, "发布课程成功！" , Toast.LENGTH_LONG).show();
                }else if(res==0){
                    Toast.makeText( AddcourseActivity.this, "课程码已经被注册！" , Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText( AddcourseActivity.this, "发布课程失败！" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }



}
