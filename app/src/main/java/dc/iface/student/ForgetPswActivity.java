package dc.iface.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.BaseActivity.StatisClass;
import dc.iface.R;
import dc.iface.login.LoginActivity;


public class ForgetPswActivity extends BaseActivity implements View.OnClickListener{

    private ImageButton back;
    private EditText newPsw;
    private EditText surePsw;
    private Button commit_btn;
    private Button yanzhen;
    private TextView biaoti;
    private boolean confirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.forget_password);
        newPsw=findViewById(R.id.new_psword);
        surePsw=findViewById(R.id.sure_psword);
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("找回密码");
        //设置密码不可见
        newPsw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT );//设置密码不可见
        surePsw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT );//设置密码不可见

        /*//设置返回按键
        back=findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        //为确定按钮设置点击事件
        //提交按钮
        commit_btn=findViewById(R.id.commit_btn);
        commit_btn.setOnClickListener(this);
        //获取验证码
        yanzhen=findViewById(R.id.yanzhen);
        yanzhen.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.denglu:
                commit();
                break;
            case R.id.text_sign_up:
                get_verification_code();
                break;

            default:
                break;
        }
    }
    //提交按键点击事件
    public void commit(){
        String str_1=newPsw.getText().toString();
        String str_2=surePsw.getText().toString();
        if(!str_1.equals(str_2)){
            //如果上下不一致
            Toast.makeText(ForgetPswActivity.this, "密码不一致，请重新输入！", Toast.LENGTH_LONG).show();
        }else if (str_1.equals("")||str_2.equals("")) {
            Toast.makeText(ForgetPswActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();

        } else if(isVerification()){
            //判断验证码是否正确，如果不正确
            Toast.makeText(ForgetPswActivity.this, "验证码错误！", Toast.LENGTH_LONG).show();

        }else {


            //如果上下密码一致，从数据库中修改
            //************************************************************************************************


            Toast.makeText(ForgetPswActivity.this, "修改成功！", Toast.LENGTH_LONG).show();
            //************************************************************************************************
            //修改成功后,关闭所有的活动，回到登陆页面
            boolean data = true;
            StatisClass statisClass = new StatisClass();
            statisClass.setCancel_automatic_login(data);
            //首先关闭所有的活动
            ActivityCollectorUtil.finishAllActivity();
            Intent intent=new Intent(ForgetPswActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }


    //获取验证码
    public void get_verification_code(){
        //从数据库查询改账户

        //发送手机验证码

    }
    //判断验证码是否正确
    public boolean isVerification(){
        boolean verification = false;
        //***********************************************

        //***********************************************
        return verification;
    }



}

