package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.sql.ResultSet;
import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.StatisClass;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.login.LoginActivity;
import dc.iface.student.ChangePswActivity;



public class MainFragmentUser extends Fragment {
    private static MainFragmentUser mf;
    private int id=0;
    private String userId;
    private String userName;
    //单例模式
    public static MainFragmentUser getMainFragment(String teacherId){
        if(mf == null){
            mf = new MainFragmentUser();
        }
        mf.id=Integer.parseInt( teacherId );
        return mf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.main_fragment_user ,container, false);//fragment_message为底部签到栏的界面

        TextView xuehao_text = view.findViewById(R.id.xuehao_text);
        TextView name_text = view.findViewById(R.id.name_text);



        new Thread( new Runnable() {
            @Override
            public void run() {


                //建立一个查询操作
                DBUtils dbUtils= new DBUtils();
                String sql = "select  teacher_name from  teacher where teacher_id ="+id;
                ResultSet resultSet = dbUtils.excuteSQL( sql );
                try{
                    resultSet.next();
                    System.out.println( resultSet.getString("teacher_name") );
                    if(resultSet!=null) {
                        userId =String.valueOf( id );
                        userName =resultSet.getString( "teacher_name" );
                        resultSet.getStatement().getConnection().close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.printf( e.getMessage() );
                }

            }
        } ).start();



        xuehao_text.setText(userId);
        name_text.setText(userName);

        TextView zhuxiao_text = view.findViewById(R.id.zhuxiao_text);
        TextView xiugai_text  = view.findViewById(R.id.xiugai_text);


        //TextView shangchuanzp_text  = view.findViewById(R.id.shangchuanzp_text);


          /*  //上传个人照片功能（上传到服务器，人脸识别功能）
            shangchuanzp_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //进入修改密码的功能界面
                    Intent intent=new Intent(getActivity(), TakePhotoActivity.class);
                    startActivity(intent);
                }
            });*/


        //修改密码功能
        xiugai_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进入修改密码的功能界面
                Intent intent=new Intent(getActivity(), ChangePswActivity.class);
                intent.putExtra("userId",userId);
                intent.putExtra("flag","T");
                startActivity(intent);
            }
        });

        //注销登陆功能
        zhuxiao_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean data = true;
                StatisClass statisClass = new StatisClass();
                statisClass.setCancel_automatic_login(data);
                //首先关闭所有的活动
                ActivityCollectorUtil.finishAllActivity();
                Intent intent=new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

            }
        });
        /*
        主页面菜单栏上(fragment)的事件在这里写，注意内部控件寻找要view.findViewById(R.id.XXX)如下
        Button buttonM = view.findViewById(R.id.buttonT);//返回根目录寻找id，并触发事件
         */
        return view;
    }
}
