package dc.iface.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.BaseActivity.StatisClass;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.MyUser;
import dc.iface.student.ChangePswActivity;
import dc.iface.student.ForgetPswActivity;
import dc.iface.student.StuMainActivity;
import dc.iface.teacher.MainActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;

public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private TextView signUp;
    private EditText editTextName;
    private EditText editTextPwd;
    private Button loginBtn;
    private ImageView ImageView_pwd_switch;
    private TextView TextView_forget_pwd;
    //使用SharedPreferences实现记住密码
    //******************************************************
    public  static SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;//记住密码选项框
    //******************************************************
    String TAG = "LoginActivity";
    public MyUser myUser = new MyUser();
    private Intent intent;
    private String jobNum;
    private String userName;//名称

    private String password;
    private int flag=-1;

    public static  boolean isRemember;
    public static  boolean autoLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        ImageView_pwd_switch = findViewById( R.id.ImageView_pwd_switch);
        ImageView_pwd_switch.setOnClickListener(this);
        editTextName = findViewById(R.id.EditText_JobNumber);
        editTextPwd = findViewById(R.id.EditText_Password);
        loginBtn = findViewById(R.id.denglu);
        loginBtn.setOnClickListener(this);
        signUp = findViewById(R.id.text_sign_up);
        signUp.setOnClickListener(this);

        TextView_forget_pwd=findViewById(R.id.TextView_forget_pwd);
        TextView_forget_pwd.setOnClickListener(this);
        //******************************************************************************
        //记住密码功能
        pref = PreferenceManager.getDefaultSharedPreferences(this);//获取SharedPreferences
        rememberPass = findViewById(R.id.CheckBox_remember_pwd);
        editor =pref.edit();//获取SharedPreferences.Editor对象，编辑操作的对象

        //抽取SharedPreferences文件中的数据，判断是否勾选记住密码
        isRemember = pref.getBoolean("rememberPassword", false);
        autoLogin = pref.getBoolean("autologin", false);

        //**********************************************************
        StatisClass statisClass = new StatisClass();
        if(statisClass.isCancel_automatic_login()){
            //如果是取消自动登录状态，就将记住密码框关掉
            editor.putBoolean("rememberPassword",false);
            isRemember=false;
            //回到登陆界面后还需将在注销页面设置的取消登陆恢复原样
            boolean data = false;
            statisClass.setCancel_automatic_login(data);
        }
        //**********************************************************

        if (isRemember) {
            //如果勾选记住密码，将账号密码添加到文本框
            loginBtn.setEnabled(false);
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            editTextName.setText(account);
            editTextPwd.setText(password);
            rememberPass.setChecked(true);//记住密码复选框设置为选中状态
            //如果勾选自动登录
            if (autoLogin) {
                //Toast.makeText(LoginActivity.this,"autoLogin执行！", Toast.LENGTH_SHORT).show();
                View cv = getWindow().getDecorView();
                // loginBtn.performClick();
                // Toast.makeText(LoginActivity.this," loginBtn.performClick();",Toast.LENGTH_SHORT).show();
                toLogin(cv);
                Toast.makeText( LoginActivity.this,"自动登录成功！", Toast.LENGTH_SHORT).show();
            }
        }else{
            loginBtn.setEnabled(true);
        }

    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.denglu:
                toLogin(view);
                break;
            case R.id.text_sign_up:
                signUp();
                break;
            case R.id.TextView_forget_pwd:
                forgetpwd();
                break;
            case R.id.ImageView_pwd_switch:
                //设置密码可见于不可见
                Password_is_visible();
            default:
                break;
        }
    }

    //忘记密码
    private void forgetpwd() {
        Intent intent = new Intent( LoginActivity.this, ForgetPswActivity.class);
        startActivity(intent);
        //关闭当前的登陆activity
        //finish();
    }

    //密码可见于不可见
    public void Password_is_visible(){
        if(ImageView_pwd_switch.isSelected()){
            ImageView_pwd_switch.setSelected(false);
            editTextPwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT );//设置密码不可见
        }else{
            ImageView_pwd_switch.setSelected(true);
            editTextPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);//设置密码可见
        }
    }

    public void signUp() {
        Intent intent = new Intent( LoginActivity.this, SignupActiviy.class);
        startActivity(intent);
        finish();
    }


    public void toLogin(final View view) {
        //获取输入的账号密码
        jobNum = editTextName.getText().toString();
        password = editTextPwd.getText().toString();

        if(jobNum.isEmpty() || password.isEmpty()){
            Toast.makeText( LoginActivity.this,"账号密码不能为空！", Toast.LENGTH_LONG).show();
        }else{

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient();
                    FormBody body = new FormBody.Builder()
                            .add("userId",jobNum)
                            .add("password",password)
                            .build();

                    final Request request = new Request.Builder()
                            .url(server+"login/")
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
                                    Toast.makeText( LoginActivity.this, "网络请求失败！" , Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()){
                                final String result = response.body().string();
                                parseJSONWithJSONObject(result);
                                Log.d( "LoginActivity", result );
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
            userName =jsonObject.getString( "userName" );
            Log.d( "LoginActivity","RESULT is "+res );
            Log.d( "LoginActivity","userName is "+userName );
            HandleResponse(res);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void HandleResponse(final int res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(res!=-1){
                    //记住密码
                    if(rememberPass.isChecked()){//如果勾选
                        editor.putBoolean("rememberPassword",true);
                        editor.putString("account",jobNum);
                        editor.putString("password",password);
                        editor.putString("userName",userName);
                        editor.putBoolean("autologin",true);
                        if(res==1){
                            editor.putString("flag","1");
                        }
                        if(res==0){
                            editor.putString("flag","0");
                        }

                    }
                    else{
                        editor.clear();
                    }
                    editor.apply();

                    if(res==1){
                        System.out.printf( "//启动ActivityT  " );
                        intent = new Intent(LoginActivity.this, MainActivity.class);//教师
                        intent.putExtra("teacherId",jobNum);
                        intent.putExtra("userName",userName);

                        startActivity(intent);
                        finish();
                    }else{
                        System.out.printf( "//启动ActivityS  " );
                        intent = new Intent(LoginActivity.this, StuMainActivity.class);//1为学生
                        intent.putExtra("studentId",jobNum);
                        intent.putExtra("userName",userName);
                        startActivity(intent);
                        finish();
                    }

                }else{
                    Toast.makeText(LoginActivity.this, "账号不存在 请重新输入" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}













 /*public void denglu(final View view) {
        //获取输入的账号密码
        jobNum = editTextName.getText().toString();
        password = editTextPwd.getText().toString();

        if(jobNum.isEmpty() || password.isEmpty()){
            Toast.makeText( LoginActivity.this,"账号密码不能为空！", Toast.LENGTH_LONG).show();
        }else{

            new Thread( new Runnable() {
                @Override
                public void run() {
                    System.out.printf( "线程启动\n" );
                    //判断账号是学生还是老师0为教师 1为学生 -1为不存在
                    if(QueryIsStudent()){
                        flag=1;
                    }else if(QueryIsTeacher()){
                        flag=0;
                    }else {
                        flag=-1;
                    }
                    System.out.printf( "账号认定结束\n" );
                    //转到相应的Activity
                    GoToActivity();

                }
            } ).start();
        }

    }

    public boolean  QueryIsStudent(){
        boolean isStudent=false;
        DBUtils dbUtils= new DBUtils();
        String sql = "select  student_password from  student where student_id ="+jobNum;
        System.out.printf( sql );
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try {
            if(resultSet.next()){
                //如果结果不为空，则为学生
                isStudent=true;
            }else{
                isStudent=false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.printf( "学生查询异常"+e.getMessage() );
        }

        try {
            resultSet.getStatement().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.printf( "学生账号认定结束  "+isStudent );
        return isStudent;
    }

    public boolean  QueryIsTeacher(){
        boolean isTeacher=false;

        DBUtils dbUtils= new DBUtils();
        String sql = "select  teacher_password from  teacher where teacher_id ="+jobNum;
        System.out.printf( sql );
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try {
            if(resultSet.next()){
                //如果结果不为空，则为教师
                isTeacher=true;
            }else{
                isTeacher=false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.printf( "教师查询异常"+e.getMessage() );
        }

        try {
            resultSet.getStatement().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.printf( "教师账号认定结束  "+isTeacher );
        return isTeacher;
    }

    public boolean  PswIsRight(String id,int user){
        boolean flag=false;//密码不正确
        String sql;
        if(user==0){
            sql="select * from teacher where teacher_id= "+id;

            DBUtils dbUtils= new DBUtils();
            ResultSet resultSet = dbUtils.excuteSQL( sql );
            try {
                if(resultSet.next()){
                    if(password.equals( resultSet.getString( "teacher_password" ) )){
                        flag=true;
                    }
                    else {
                        flag=false;
                    }
                }

                resultSet.getStatement().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            sql="select * from student where student_id= "+id;

            DBUtils dbUtils= new DBUtils();
            ResultSet resultSet = dbUtils.excuteSQL( sql );
            try {
                if(resultSet.next()){
                    if(password.equals( resultSet.getString( "student_password" ) )){
                        flag=true;
                    }
                    else {
                        flag=false;
                    }
                }

                resultSet.getStatement().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }*/

   /* public void GoToActivity()
    {
        System.out.printf( String.valueOf( flag ) );
        System.out.printf( "//记住密码处理 " );

        if(flag==0 && PswIsRight(jobNum,0)){
            //验证密码

            //记住密码
            if(rememberPass.isChecked()){//如果勾选
                editor.putBoolean("rememberPassword",true);
                editor.putString("account",jobNum);
                editor.putString("password",password);
                editor.putBoolean("autologin",true);
                System.out.printf( "//记住密码T  " );
            }
            else{
                editor.clear();
            }
            editor.apply();
            //启动Activity
            System.out.printf( "//启动ActivityT  " );
            intent = new Intent(LoginActivity.this, MainActivity.class);//教师
            intent.putExtra("teacherId",jobNum);
            startActivity(intent);
            finish();
        }else if(flag==1 && PswIsRight(jobNum,1)){
            //记住密码
            if(rememberPass.isChecked()){//如果勾选
                editor.putBoolean("rememberPassword",true);
                editor.putString("account",jobNum);
                editor.putString("password",password);
                editor.putBoolean("autologin",true);
            }
            else{
                editor.clear();
            }
            editor.apply();
            //启动Activity
            System.out.printf( "//启动ActivityS  " );
            intent = new Intent(LoginActivity.this, StuMainActivity.class);//1为学生
            intent.putExtra("studentId",jobNum);
            startActivity(intent);
            finish();

        }else{
            System.out.printf( "//账号不存在 请重新输入  " );
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(LoginActivity.this, "账号不存在 请重新输入" , Toast.LENGTH_LONG).show();
                }
            });
        }
    }
*/