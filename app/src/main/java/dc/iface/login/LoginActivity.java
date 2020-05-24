package dc.iface.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.BaseActivity.StatisClass;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.MyUser;
import dc.iface.student.ForgetPswActivity;
import dc.iface.student.StuMainActivity;
import dc.iface.teacher.MainActivity;


public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private TextView signUp;
    private EditText editTextName;
    private EditText editTextPwd;
    private Button loginBtn;
    private ImageView ImageView_pwd_switch;
    private TextView TextView_forget_pwd;
    //使用SharedPreferences实现记住密码
    //******************************************************
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;//记住密码选项框
    //******************************************************
    String TAG = "LoginActivity";
    public MyUser myUser = new MyUser();
    private Intent intent;
    private String jobNum;
    private String password;
    private int flag=-1;
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
        boolean isRemember = pref.getBoolean("rememberPassword", false);
        boolean autoLogin = pref.getBoolean("autologin", false);

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
                denglu(cv);
                Toast.makeText( LoginActivity.this,"自动登录成功！", Toast.LENGTH_SHORT).show();
            }
        }

        //******************************************************************************

        //******************************************************************************
        //实现记住密码功能，首先要将账号密码提取为字符串形式，

        //String getAccount = editTextName.getText().toString();
        //String getPassword= editTextPwd.getText().toString();
        //******************************************************************************

    }



    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.denglu:
                denglu(view);
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

    public void denglu(final View view) {
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
    }

    public void GoToActivity()
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

}




/*
*{

            final String jobNum = editTextName.getText().toString();
            final String password = editTextPwd.getText().toString();

            if(jobNum.isEmpty() || password.isEmpty()){

                Toast.makeText( LoginActivity.this,"账号密码不能为空！", Toast.LENGTH_LONG).show();
            }
            else {
                Log.i(TAG, "1 " + jobNum + " ");
                int flag=0;//判断是学生还是老师
                String name = editTextName.getText().toString();
                    try{
                        DBUtils dbUtils= new DBUtils();
                        String sql = "select  student_password from  student where student_id ="+jobNum;
                        System.out.printf( sql );
                        ResultSet resultSet = dbUtils.excuteSQL( sql );
                        /// resultSet.next();

                        if(resultSet.next()){
                            System.out.println("+++++++++");
                            //如果结果不为空，则证明该账号是学生
                            myUser.setFlag(false);//学生
                            if(password.equals(resultSet.getString("student_password"))){

                                System.out.printf( resultSet.getString("student_password") );

                                //如果密码框内的密码和 查询出来的密码一样，则表明是密码正确
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
                                //***关闭数据库连接
                                resultSet.getStatement().getConnection().close();
                                System.out.printf( " 开始跳转 " );
                                //******************************************************************************
                               Intent intent = new Intent(LoginActivity.this, StuMainActivity.class);//false为学生
                                intent.putExtra("studentId",jobNum);
            //关闭当前的登陆activity

            }else{
        //此时已经证明这个是学生
        //如果密码不一致，则表明输入的密码错误
        System.out.printf( "学生账号 密码错误 请重新输入 " );
        Looper.prepare();
        Toast.makeText(LoginActivity.this, "学生账号 密码错误 请重新输入" , Toast.LENGTH_LONG).show();
        Looper.loop();
        //***关闭数据库连接
        resultSet.getStatement().getConnection().close();

        }

        }else{
        resultSet.getStatement().getConnection().close();

        try{
        //如果结果为空，则表明没有查到该账号，则继续进行教师账号的查询
        sql = "select  teacher_password from  teacher where teacher_id ="+jobNum;
        System.out.printf( sql );

        resultSet = dbUtils.excuteSQL( sql );
        if(resultSet.next()){
        System.out.printf( "132132" );
        //表明账号是教师
        myUser.setFlag(true);//老师
        try {
        System.out.printf( "24354646\n" );
        if(password.equals(resultSet.getString("teacher_password"))){
        //如果密码框内的密码和 查询出来的密码一样，则表明是密码正确
        System.out.printf( password );
        //******************************************************************************
        //editor =pref.edit();//获取SharedPreferences.Editor对象，编辑操作的对象
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
        //***关闭数据库连接
        resultSet.getStatement().getConnection().close();

        System.out.printf( "111111111" );
        runOnUiThread(new Runnable() {
@Override
public void run() {
        //******************************************************************************
        intent=new Intent( LoginActivity.this, MainActivity.class);//true为老师
        intent.putExtra("teacherId",jobNum);
        //关闭当前的登陆activity
        finish();
        }
        });
        System.out.printf( "1333333333331" );
        }else{
        System.out.printf( "114444444441" );
        //此时已经证明这个是学生
        //如果密码不一致，则表明输入的密码错误
        Looper.prepare();
        Toast.makeText(LoginActivity.this, "教师账号 密码错误 请重新输入" , Toast.LENGTH_LONG).show();
        Looper.loop();
        //***关闭数据库连接
        resultSet.getStatement().getConnection().close();
        }
        }catch (SQLException e){
        Log.d(TAG,"SQL异常"+password+password);

        e.printStackTrace();

        }

        }else{
        resultSet.getStatement().getConnection().close();

        //如果教师和学生都不是，那就是该账号不存在了
        Looper.prepare();
        Toast.makeText(LoginActivity.this, "账号不存在 请重新输入" , Toast.LENGTH_LONG).show();
        Looper.loop();
        }

        // resultSet.getStatement().getConnection().close();
        }catch(Exception e){
        resultSet.getStatement().getConnection().close();
        e.printStackTrace();
        System.out.printf( e.getMessage() );
        }
        }

        // resultSet.getStatement().getConnection().close();
        }catch (Exception e){
        //resultSet.getStatement().getConnection().close();
        e.printStackTrace();
        System.out.printf( e.getMessage() );
        }

        }
        }
        */
