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

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.teacher.AddcourseActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;

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
                toAddClass();
            }
        });
    }

    public void toAddClass() {
        number = editText.getText().toString();//课程码
        if(number.length() == 4){
            //查询是否已经加入该班级
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    FormBody body = new FormBody.Builder()
                            .add("courseId",number)
                            .add("studentId",studentId)
                            .build();

                    final Request request = new Request.Builder()
                            .url(server+"stuAddCourse/")
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
                                    Toast.makeText( AddClassActivity.this, "网络请求失败！" , Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()){
                                final String result = response.body().string();
                                parseJSONWithJSONObject(result);
                                Log.d( "AddClassActivity", result );
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
                    Toast.makeText( AddClassActivity.this, "加入课程成功！" , Toast.LENGTH_LONG).show();
                    //加课完成直接结束页面
                    finish();
                }else if(res==0){
                    Toast.makeText( AddClassActivity.this, "课程已经加入！" , Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText( AddClassActivity.this, "加入课程失败！" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }


   /* public int QueryStuCourseId()
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
    }*/
}
