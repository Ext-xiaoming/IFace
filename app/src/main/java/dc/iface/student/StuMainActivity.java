package dc.iface.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;


public class StuMainActivity  extends BaseActivity {
    private RadioGroup MenuBarBelow;
    private ImageButton addClassbtn;
    //private String studentId = "160031111";

    private String studentId="";
    private String TAG = "StuMainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.activity_main);

        Log.i(TAG,"1.**************************");
        MenuBarBelow = findViewById(R.id.MenuBarBelowQ);//下方菜单栏
        setIndexSelected(0);//默认class

        Intent intent =getIntent();
        studentId = intent.getStringExtra( "studentId" );
        Log.i(TAG,"1.**************************"+studentId);


        //Toast.makeText(StuMainActivity.this,  studentId, Toast.LENGTH_LONG).show();
        MenuBarBelow.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {//菜单栏下方按钮选择后跳转
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0;i<MenuBarBelow.getChildCount();i++){
                    RadioButton rb = (RadioButton)group.getChildAt(i);
                    if(rb.isChecked()){
                        setIndexSelected(i);
                        break;
                    }
                }
            }
        });
        Log.i("StuMainActivity","1."+studentId);
        addClassbtn = findViewById(R.id.btn_addcs);
        addClassbtn.setOnClickListener(new View.OnClickListener() {//加入课程
            @Override
            public void onClick(View view) {
                Log.i("StuMainActivity","1."+studentId);
                Intent intent=new Intent(StuMainActivity.this, AddClassActivity.class);
                intent.putExtra("studentId",studentId);
                startActivity(intent);
               // finish();
            }
        });

    }

    private void changeFragment(Fragment fragment){//选择Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();//开启事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentQ , fragment);
        transaction.commit();
    }

    //通过index判断当前加载哪个界面
    public void setIndexSelected(int index){
        TextView text_title =findViewById(R.id.text_title);
        switch (index) {
            case 0:
                System.out.printf( "----studentId="+studentId );
                changeFragment(new StuMainFragmentClass().getMainFragment(studentId));
                //Toast.makeText(StuMainActivity.this,  "学生", Toast.LENGTH_LONG).show();
                text_title.setText("学生课堂");
                break;
            case 1:
                changeFragment(new StuMainFragmentUser().getMainFragment(studentId));
                //Toast.makeText(StuMainActivity.this,  "老师", Toast.LENGTH_LONG).show();
                text_title.setText("个人中心");
                break;
            default:
                break;
        }
    }
}
