package dc.iface.student;

import android.app.Activity;
import android.content.Intent;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.AdapterKaoqin;
import dc.iface.object.CourseListItem;
import dc.iface.object.ListItemKaoqin;
import dc.iface.teacher.CoursesAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;


//***********************************************
//学生考勤页面 （签到  /  人脸识别签到）
//************************************************
public class KaoqinActivity extends BaseActivity {
    private ImageButton back;
    private Button kaoqinBtn;
    private Button facebtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView biaoti;
    ListView lvListView ;
    private String courseCode;//从主界面来的课程码
    private String studentId;//从主界面来的当前学生学号
    private RecyclerView recyclerView;

    private String TAG = "KaoqinActivity";
    private AdapterKaoqin adapterKaoqin;
    private List<ListItemKaoqin> listItemKaoqin;//ListItem课程集

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.stukaoqin);

        //下拉刷新
        swipeRefreshLayout=findViewById( R.id.swipeRefreshlayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue);


        recyclerView = findViewById(R.id.stukaoqin_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        kaoqinBtn = findViewById(R.id.stufabuqiandaoBtn);

        kaoqinBtn.setText("签到");

        Intent mainToKQ = getIntent();
        courseCode = mainToKQ.getStringExtra("courseId");//课程码匹配
        studentId = mainToKQ.getStringExtra("studentId");

        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("考勤");
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //考勤按键-->进去签到页面
        kaoqinBtn.setOnClickListener(new View.OnClickListener() {//进入签到的按钮
            @Override
            public void onClick(View view) {
                Log.i(TAG , "1"+ "进入签到" );
                Intent intent2 = new Intent(KaoqinActivity.this, NumqiandaoActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("studentId",studentId);
                startActivity(intent2);

            }
        });
        LodeListView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //这里获取数据的逻辑
                LodeListView();
                Log.i(TAG , "1"+ "执行刷新" );
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public void LodeListView(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("courseId",courseCode)
                        .add("studentId",studentId)
                        .build();

                final Request request = new Request.Builder()
                        .url(server+"stuKaoQinList/")
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
                            Log.d( "KaoqinActivity","http返回："+ result );
                        }
                    }
                });

            }
        }).start();
    }
    //json解析  + 适配器数据分发
    private void parseJSONWithJSONObjectArray(String jsonData){

        try {
            listItemKaoqin = new ArrayList<>();//ListItem课程集
            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i= 0;i<jsonArray.length();i++){
                ListItemKaoqin item = new ListItemKaoqin();
                JSONObject jsonObject = jsonArray.getJSONObject( i );
                String Check=jsonObject.getString( "IsCheck" );
                String post_num =jsonObject.getString( "post_num" );
                String post_date =jsonObject.getString( "post_date" );
                int post_type =jsonObject.getInt( "post_type" );

                Log.d( "KaoqinActivity","post_num is "+post_num );
                Log.d( "KaoqinActivity","post_date is "+post_date );
                Log.d( "KaoqinActivity","IsCheck is "+Check );

                item.setCheckNumber(post_num );
                //item.setPostId( resultSet.getString("post_id") );
                item.setTime(post_date);
                item.setQiandaoNumber(Check);//其实这是出勤状况 默认出勤
                if(post_type==1){
                    item.setPostType( "人脸识别" );//签到方式
                }else{
                    item.setPostType( "数字签到" );//签到方式
                }

                listItemKaoqin.add(item);

            }

            adapterKaoqin = new AdapterKaoqin(KaoqinActivity.this,
                    R.layout.item_kaoqian, listItemKaoqin);
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
                for (int i = 0; i < listItemKaoqin.size(); i++) {
                    ListItemKaoqin s = (ListItemKaoqin)listItemKaoqin.get(i);
                    System.out.println(i+"输1出："+s.getCheckNumber()+"  "+s.getPostType()+"  "+s.getQiandaoNumber()+"\n");
                }

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(KaoqinActivity.this  );
                recyclerView.setLayoutManager( linearLayoutManager );
                recyclerView.setAdapter(adapterKaoqin);

                adapterKaoqin.setOnItemClickListener( new AdapterKaoqin.OnitemClick(){
                    @Override
                    public void onItemClick(int position) {
                        //向老师提交修改签到结果功能
                        //.......
                    }
                } );
            }
        });
    }
}
