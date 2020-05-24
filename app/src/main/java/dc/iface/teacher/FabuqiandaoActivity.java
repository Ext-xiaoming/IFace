package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;


public class FabuqiandaoActivity extends BaseActivity {
    private ImageButton back;
    private TextView biaoti;
    private Button shuziBtn;
    private Button erweimaBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.fabuqiandao_main);

        biaoti=findViewById(R.id.title_sec);
        biaoti.setText("发布签到");

        back=findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(FabuqiandaoActivity.this,Kaoqin.class);
                startActivity(intent);
            }
        });

        shuziBtn=findViewById(R.id.fabu_shuzi);
        shuziBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3=new Intent(FabuqiandaoActivity.this,QiandaoPatternActiviy.class);
                startActivity(intent3);
            }
        });

        erweimaBtn=findViewById(R.id.fabu_erweima);
        erweimaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3=new Intent(FabuqiandaoActivity.this,QiandaoPatternActiviy.class);
                startActivity(intent3);
            }
        });
    }
}