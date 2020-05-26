package dc.iface.teacher;

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



//https://blog.csdn.net/lalallallalla/article/details/86478106 参考的底部菜单栏

/**
 * 1、MainActivity为 教师端的主页面活动
 * 2、加载布局  activity_main
 * 3、toolbar 上 的  “+” 添加课堂活动 AddcourseActivity
 *
 * */
public class MainActivity extends BaseActivity {
    private RadioGroup MenuBarBelow;
    private ImageButton addClassbtn;
    private String teacherId;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.activity_main);
        MenuBarBelow = findViewById(R.id.MenuBarBelowQ);//下方菜单栏
        Intent intent =getIntent();
        teacherId = intent.getStringExtra( "teacherId" );
        userName = intent.getStringExtra( "userName" );
        Log.i("MainActivity","1." + teacherId);

        setIndexSelected(0);//默认class
        //底部菜单栏
        MenuBarBelow.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {//菜单栏下方按钮选择后跳转
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0;i<MenuBarBelow.getChildCount();i++){
                    RadioButton rb = (RadioButton)group.getChildAt(i);
                    if(rb.isChecked()){
                        setIndexSelected(i);      //进行底部菜单栏的设置 i 不同加载不同的Fragment
                        break;
                    }
                }
            }
        });

        //教师端添加课堂功能  AddcourseActivity
        addClassbtn = findViewById(R.id.btn_addcs);
        addClassbtn.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                Intent intent=new Intent( MainActivity.this, AddcourseActivity.class);
                intent.putExtra("teacherId",teacherId);
                startActivity(intent);
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
                changeFragment(new MainFragmentClass().getMainFragment(teacherId));
                text_title.setText("课堂");//将当前Fragment的toolbar设置为相应的字样 如 ：“课堂”
                break;
            case 1:
                changeFragment(new MainFragmentUser().getMainFragment(teacherId,userName));
                text_title.setText("个人中心");
                break;
            default:
                break;
        }
    }
}
