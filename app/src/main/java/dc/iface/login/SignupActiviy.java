package dc.iface.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.MyUser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;




public class SignupActiviy extends BaseActivity {
    private RadioGroup group;
    private RadioButton teacherRa;
    private RadioButton studentRa;
    private EditText et_id;
    private EditText et_user;
    private EditText et_password;
    private EditText et_phone;
    private Button fanhuiBtn;

    private String    id="";
    //private int    index=0;
    private String user="";
    private String password="";
    private String phone="";
    private boolean flag=true;//ture为教师

    private Button regBtn;

    MyUser myUser;
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.sign_up);

        et_id=findViewById(R.id.et_user_id);
        et_user=findViewById(R.id.et_user_name);
        et_password=findViewById(R.id.et_psw);
        et_phone=findViewById(R.id.et_phone);



        regBtn=findViewById(R.id.btn_register);
        myUser=new MyUser();
        group=findViewById(R.id.myRadioGroup);
        teacherRa=findViewById(R.id.check_teacher);
        studentRa=findViewById(R.id.check_student);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                if (teacherRa.getId()==checkId){
                    flag=true;//教师
                }
                if (studentRa.getId()==checkId) {
                    flag=false;
                }
            }
        });

        //返回操作
        fanhuiBtn=findViewById(R.id.btn_fanhui);
        fanhuiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent5=new Intent(SignupActiviy.this,LoginActivity.class);
                startActivity(intent5);
                finish();
            }
        });


        //注册操作--向数据库添加账号即可
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient();
                        FormBody body = new FormBody.Builder()
                                .add("id",et_id.getText().toString())
                                .add("user",et_user.getText().toString())
                                .add("password",et_password.getText().toString())
                                .add("phone",et_phone.getText().toString())
                                .add("flag",String.valueOf(flag))
                                .build();

                        final Request request = new Request.Builder()
                                .url("http://10.34.15.176:8000/signUp/")
                                .post(body)
                                .build();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SignupActiviy.this, "注册失败！" , Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if(response.isSuccessful()){
                                    final String result = response.body().string();

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {



                                                if(true){
                                                    Toast.makeText(SignupActiviy.this, "注册成功！" , Toast.LENGTH_LONG).show();
                                                }else {
                                                    Toast.makeText(SignupActiviy.this, "注册失败！" , Toast.LENGTH_LONG).show();
                                                }


                                            }
                                        });
                                }
                            }
                        });

                    }
                }).start();


            }
        });

    }
}
   /*  new Thread( new Runnable() {
                    @Override
                    public void run() {

                        final String _id= et_id.getText().toString() ;
                        final String _user=et_user.getText().toString();
                        final String _password=et_password.getText().toString();
                        final  String _phone=et_phone.getText().toString();
                        DBUtils dbUtils= new DBUtils();
                        String sql=null;
                        //验证该账号是否被注册过
                        //////////////////////////////////////////////////////////////////////////


                        //////////////////////////////////////////////////////////////////////////
                        //拼接sql
                        if (flag) //true 为教师
                        {
                            sql = "insert into teacher  (teacher_id,teacher_name,teacher_password,teacher_phone)" +
                                    "values (" + _id + ", '" + _user + "' , '" + _password + "' , '" + _phone + "' )";

                            System.out.printf( sql );
                        }else {
                            sql = "insert into student  (student_id , student_name,student_password,student_phone)" +
                                    "values ("+ _id + ",'" + _user + "','" + _password + "','" + _phone + "')";
                            System.out.printf( sql );
                        }

                        int count = dbUtils.excuteSQLToADU( sql );
                        if(count!=0) {

                            Looper.prepare();
                            Toast.makeText(SignupActiviy.this, "注册成功！" , Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }else {
                            Looper.prepare();
                            Toast.makeText(SignupActiviy.this, "注册失败！" , Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }

                    }
                } ).start();*/