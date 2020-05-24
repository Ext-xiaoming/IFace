package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;


/**
 * 发布签到
 *
 * **/
public class QiandaoPatternActiviy extends BaseActivity {
    private ImageButton back;
    private TextView biaoti;
    private Button fabuBtn;
    private ImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.fabuqiandao_main);

        biaoti=findViewById(R.id.title_sec);
        biaoti.setText("签到口令/二维码");

        back=findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(QiandaoPatternActiviy.this,FabuqiandaoActivity.class);
                startActivity(intent);
            }
        });

        imageView=findViewById(R.id.erweima_paattern);

        fabuBtn=findViewById(R.id.sub_fabu);
        fabuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
