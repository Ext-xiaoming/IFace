package dc.iface.student;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.BaseActivity.StatisClass;
import dc.iface.R;
import dc.iface.login.LoginActivity;
import dc.iface.login.SignupActiviy;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ChangePswActivity extends BaseActivity implements View.OnClickListener{

    private ImageButton back;
    private EditText newPsw;
    private EditText surePsw;
    private Button sureBtn;
    private TextView biaoti;
    private boolean confirm;
    private String new_password="";
    private String userId="";
    private String flag="";

    private boolean data = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.changepsw);
        newPsw=findViewById(R.id.new_psw);
        surePsw=findViewById(R.id.sure_psw);
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("修改密码");
        //设置密码不可见
        newPsw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT );//设置密码不可见
        surePsw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT );//设置密码不可见

        //设置返回按键
        back=findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              finish();
            }
        });

        //为确定按钮设置点击事件
        sureBtn=findViewById(R.id.sure_btn);
        sureBtn.setOnClickListener(this);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        flag=intent.getStringExtra( "flag" );

    }

    @Override
    public void onClick(View v) {
        String str_1=newPsw.getText().toString();
        String str_2=surePsw.getText().toString();
        if(!str_1.equals(str_2)){
            //如果上下不一致
            Toast.makeText(ChangePswActivity.this, "密码不一致，请重新输入！", Toast.LENGTH_LONG).show();
        }else if (str_1.equals("")||str_2.equals("")) {
            Toast.makeText(ChangePswActivity.this, "密码不能为空！", Toast.LENGTH_LONG).show();
        } else {
            //如果上下密码一致，从数据库中修改
            //************************************************************************************************
            //new_password=str_2;
            ChangePassword();


            //************************************************************************************************

        }
    }

    private void ChangePassword(){
        new_password=surePsw.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("userId",userId)
                        .add("flag",flag)
                        .add("new_password",new_password)
                        .build();

                final Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/changePassward/")
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
                                //Toast.makeText( ChangePswActivity.this, "修改失败！" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();
                            parseJSONWithJSONObject(result);
                            Log.d( "ChangePswActivity", result );
                        }
                    }
                });

            }
        }).start();
    }

    //json解析
    private void parseJSONWithJSONObjectArray(String jsonData){
        try {
            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i=0 ;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject( i );
                String falg=jsonObject.getString( "flag" );
                String id =jsonObject.getString( "id" );
                Log.d( "ChangePswActivity","flag is"+falg );
                Log.d( "ChangePswActivity","id is"+id );
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //json解析
    private void parseJSONWithJSONObject(String jsonData){
        try {
            JSONObject jsonObject= new JSONObject( jsonData );
            int res =jsonObject.getInt( "RESULT" );
            Log.d( "ChangePswActivity","RESULT is "+res );
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
                    Toast.makeText( ChangePswActivity.this,"修改成功",Toast.LENGTH_SHORT ).show();
                    //修改成功后,关闭所有的活动，回到登陆页面
                    StatisClass statisClass = new StatisClass();
                    statisClass.setCancel_automatic_login(data);
                    ActivityCollectorUtil.finishAllActivity();
                    Intent intent=new Intent(ChangePswActivity.this, LoginActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText( ChangePswActivity.this,"修改失败",Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }


}
