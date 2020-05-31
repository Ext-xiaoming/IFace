package dc.iface.login;



import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.student.StuMainActivity;
import dc.iface.teacher.MainActivity;

import static dc.iface.login.LoginActivity.autoLogin;
import static dc.iface.login.LoginActivity.isRemember;
import static dc.iface.login.LoginActivity.pref;


//待补充
public class LogoActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.logo);

        Intent intent=null;




        if(isRemember && autoLogin){
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            String flag = pref.getString("flag", "");
            String userName = pref.getString("userName", "");


            if(flag.equals( "1" )){
                intent = new Intent(LogoActivity.this, MainActivity.class);//教师
                intent.putExtra("teacherId",account);
                intent.putExtra("userName",userName);
            }else {
                intent = new Intent(LogoActivity.this, StuMainActivity.class);
                intent.putExtra("studentId",account);
                intent.putExtra("userName",userName);
            }
            startActivity(intent);
            finish();
        }else {
            intent = new Intent(LogoActivity.this, LoginActivity.class);//登录界面
            startActivity(intent);
            finish();
        }

    }
}