package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.AdapterKaoqin;
import dc.iface.object.AdapterQStatus;
import dc.iface.object.ListItemKaoqin;
import dc.iface.object.QStatusItem;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;


public class QiandaoStatus extends BaseActivity {

    private TextView biaoti;
    private ImageButton back;

    private String checkNum;//第几次签到
    private String courseCode;//课程码
    private String TAG ="QiandaoStatus";
    private String postId;
    private List<QStatusItem> ListQStatusItem ;
    private AdapterQStatus adapterQStatus;
    private RecyclerView recyclerView;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView( R.layout.qiandao_status);

        //Log.i(TAG , "1.进入" );

        Intent KQToStatus = getIntent();
        checkNum = KQToStatus.getStringExtra("checkNum");//第几次签到
        courseCode = KQToStatus.getStringExtra("courseId");//课程码匹配
        postId=KQToStatus.getStringExtra("postId");

        recyclerView = findViewById(R.id.statusItem);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("第"+checkNum+"次签到");

        back = findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(QiandaoStatus.this, Kaoqin.class);
                //intent.putExtra("courseId",courseCode);
                //startActivity(intent);
                finish();
            }
        });

        LodeListView();

    }



    public void LodeListView(){
        /**
         * 传递  courseId
         * 获取  student_id student_name CheckStatu
         * */
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("courseId",courseCode)
                        .add("postId",postId)

                        .build();

                final Request request = new Request.Builder()
                        .url(server+"whoQiandao/")
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
                                System.out.printf( "网络请求是失败" );
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();
                            parseJSONWithJSONObjectArray(result);
                            Log.d( "QiandaoStatus", result );
                        }
                    }
                });

            }
        }).start();
    }
    //json解析  + 适配器数据分发
    private void parseJSONWithJSONObjectArray(String jsonData){
        ListQStatusItem=new ArrayList<>( );
        try {

            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i=0 ;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject( i );

                String student_id=jsonObject.getString( "student_id" );
                String student_name =jsonObject.getString( "student_name" );
                String CheckStatu =jsonObject.getString( "CheckStatu" );
                QStatusItem item = new QStatusItem();
                item.setStudentId( student_id );
                item.setName(student_name);
                item.setCheckStatu(CheckStatu);

                ListQStatusItem.add(item);
            }
            adapterQStatus = new AdapterQStatus(QiandaoStatus.this ,
                    R.layout.item_status , ListQStatusItem);
            HandleResponse();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //显示
    private void HandleResponse() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println( "list.size="+ListQStatusItem.size()+"--00000000000000\n");
                for (int i = 0; i < ListQStatusItem.size(); i++) {
                    QStatusItem s = (QStatusItem)ListQStatusItem.get(i);
                    System.out.println(i+"输出："+s.getStudentId()+"  "+s.getCheckStatu()+"  "+s.getName()+"\n");
                }
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager( QiandaoStatus.this );
                recyclerView.setLayoutManager( linearLayoutManager );
                recyclerView.setAdapter(adapterQStatus);

                adapterQStatus.setOnItemClickListener( new AdapterQStatus.OnitemClick(){
                    @Override
                    public void onItemClick(int position) {
                        //........
                    }
                } );
            }
        });
    }
/*    public boolean IsAttendance(String studentId,String postId){
        boolean flag=true;//出勤

        DBUtils dbUtils= new DBUtils();
        String sql = "select sign_id from  sign_in where student_id = "+studentId+" and post_id ="+postId;
        System.out.printf( sql );
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{

            if(resultSet.next()) {
                //查询结果不为空，则出勤
                flag=true;
                System.out.println( resultSet.getString("sign_id") );
                resultSet.getStatement().getConnection().close();
            }else{
                //查询结果为空，表明没有出勤
                System.out.println("学生id= "+studentId+" 未签到" );
                flag=false;
            }

        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }

        return  flag;
    }

    public String  QueryStudentName(String studentId){
        String studentName=" ";

        DBUtils dbUtils= new DBUtils();
        String sql = "select student_name from  student  where student_id ="+studentId;
        System.out.printf( sql );
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{
            if(resultSet.next()) {
                //查询结果不为空
                System.out.println( resultSet.getString("student_name") );
                resultSet.getStatement().getConnection().close();
            }else{
                //查询结果为空
                System.out.println( "未知错误！" );
            }

        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }

        return  studentName;
    }*/
}
/*
        new Thread( new Runnable() {
            @Override
            public void run() {

                //查找该课程所有的学生的学号  姓名
                //查看该次签到该学生是否签到了

                DBUtils dbUtils= new DBUtils();

                String sql = "select student_id  from  student_course where course_id ="+courseCode;
                System.out.printf(sql );
                ResultSet resultSet = dbUtils.excuteSQL( sql );

                try{

                    while(resultSet.next()){
                        QStatusItem item = new QStatusItem();
                        item.setStudentId( resultSet.getString("student_id") );
                        System.out.printf(  "student_id= "+ resultSet.getString("student_id") );

                        item.setName(QueryStudentName( resultSet.getString("student_id")));
                        //System.out.printf(  "student_name= "+ resultSet.getString("student_name") );

                        if(IsAttendance(resultSet.getString("student_id"), postId ))
                        {
                            item.setCheckStatu("出勤");
                        }else{
                            item.setCheckStatu("缺勤");
                        }


                        ListQStatusItem.add(item);
                    }

                    for (int i = 0; i < ListQStatusItem.size(); i++) {
                        QStatusItem s = (QStatusItem)ListQStatusItem.get(i);
                        System.out.println(i+"输出："+s.getStudentId()+"  "+s.getCheckStatu()+"  "+s.getName()+"\n");
                    }

                    adapterQStatus = new AdapterQStatus(QiandaoStatus.this ,
                            R.layout.item_status , ListQStatusItem);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println( "list.size="+ListQStatusItem.size()+"--00000000000000\n");
                            for (int i = 0; i < ListQStatusItem.size(); i++) {
                                QStatusItem s = (QStatusItem)ListQStatusItem.get(i);
                                System.out.println(i+"输出："+s.getStudentId()+"  "+s.getCheckStatu()+"  "+s.getName()+"\n");
                            }
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager( QiandaoStatus.this );
                            recyclerView.setLayoutManager( linearLayoutManager );
                            recyclerView.setAdapter(adapterQStatus);                        }
                    });

                    resultSet.getStatement().getConnection().close();
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.printf( e.getMessage() );
                }
            }
        } ).start();
*/
