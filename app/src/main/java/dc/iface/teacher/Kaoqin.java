package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
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
import dc.iface.TakePhotos.FaceActivity;
import dc.iface.TakePhotos.PhotoActivity;
import dc.iface.TakePhotos.TakePhotoActivity;
import dc.iface.object.AdapterKaoqin;
import dc.iface.object.ListItemKaoqin;
import dc.iface.student.KaoqinActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;


/**
 *教师端 考勤页面
 */

public class Kaoqin extends BaseActivity {
    private ImageButton back;
    private Button kaoqinBtn;
    private Button faceBtn;
    private RecyclerView recyclerView;

    private TextView biaoti;

    private String courseName;
    private String teacherName;
    private String courseCode;//从主界面来的课程码
    private String teacherId;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String TAG = "Kaoqin";
    private AdapterKaoqin adapterKaoqin;
    private List<ListItemKaoqin> listItemKaoqin ;//ListItem课程集

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }
        setContentView( R.layout.kaoqin);

        //下拉刷新
        swipeRefreshLayout=findViewById( R.id.swipeRefreshlayout1);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue);



        recyclerView = findViewById(R.id.kaoqin_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        Intent mainToKQ = getIntent();
        courseCode = mainToKQ.getStringExtra("courseId");//课程码匹配
        teacherId=mainToKQ.getStringExtra("teacherId");
        courseName=mainToKQ.getStringExtra("courseName");
        teacherName=mainToKQ.getStringExtra("teacherName");

        Log.i(TAG,"1."+courseCode);

        //将toolbar转为 “考勤”  字样
        biaoti = findViewById(R.id.title_sec);
        biaoti.setText("考勤");
        //back键
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //发布签到按钮 --> 点击 转到 发布数字码签到
        kaoqinBtn=findViewById(R.id.fabuqiandaoBtn);
        kaoqinBtn.setOnClickListener(new View.OnClickListener() {//进入考勤的按钮
            @Override
            public void onClick(View view) {
                Log.i(TAG , "1"+ "进入跳转" );
                Intent intent2 = new Intent(Kaoqin.this, FaqianActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("teacherId",teacherId);
                intent2.putExtra("flag","1");
                intent2.putExtra("teacherName", teacherName);
                intent2.putExtra("courseName",courseName);
                startActivity(intent2);

            }
        });

        //拍照人脸签到按钮 --> 点击 转到 拍照功能
        faceBtn=findViewById(R.id.facebtn);
        faceBtn.setOnClickListener(new View.OnClickListener() {//进入考勤的按钮
            @Override
            public void onClick(View view) {
                Log.i(TAG , "1"+ "进入跳转" );
                Intent intent2 = new Intent(Kaoqin.this, PhotoActivity.class);
                intent2.putExtra("courseId",courseCode);
                intent2.putExtra("teacherId",teacherId);
                intent2.putExtra("flag","1");
                intent2.putExtra("teacherName", teacherName);
                intent2.putExtra("courseName",courseName);

                startActivity(intent2);
            }
        });

        LodeListView();
        Log.i(TAG , "2"+ "执行" );
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
        /**
         * 传递
         * 获取
         * */
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("courseId",courseCode)
                        .build();

                final Request request = new Request.Builder()
                        .url(server+"teaCourseFabu/")
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
                            Log.d( "StuMainFragmentClass", "http返回："+result );
                        }
                    }
                });

            }
        }).start();
    }
    //json解析  + 适配器数据分发
    private void parseJSONWithJSONObjectArray(String jsonData){

        listItemKaoqin=new ArrayList<>(  );
        try {
            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i=0 ;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject( i );
                ListItemKaoqin item = new ListItemKaoqin();
                int num=jsonObject.getInt( "num" );
                int num_all=jsonObject.getInt( "num_all" );
                String post_num =jsonObject.getString( "post_num" );
                String post_date =jsonObject.getString( "post_date" );
                String post_id =jsonObject.getString( "post_id" );
                int post_type =jsonObject.getInt( "post_type" );


                if(post_type==1){
                    item.setPostType( "人脸识别" );//签到方式
                }else{
                    item.setPostType( "数字签到" );//签到方式
                }

                item.setPostId( post_id);
                item.setCheckNumber(post_num);
                item.setTime(post_date);
                item.setQiandaoNumber(String.valueOf(num)+" / "+String.valueOf(num_all));//签到人数  ！由服务器返回
                listItemKaoqin.add(item);
            }

            adapterKaoqin = new AdapterKaoqin(Kaoqin.this,
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
                System.out.println( "list.size="+listItemKaoqin.size()+"--00000000000000\n");
                for (int i = 0; i < listItemKaoqin.size(); i++) {
                    ListItemKaoqin s = (ListItemKaoqin)listItemKaoqin.get(i);
                    System.out.println(i+"输出："+s.getCheckNumber()+"  "+s.getPostId()+"  "+s.getQiandaoNumber()+"\n");
                }

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager( Kaoqin.this );
                recyclerView.setLayoutManager( linearLayoutManager );
                recyclerView.setAdapter(adapterKaoqin);

                adapterKaoqin.setOnItemClickListener( new AdapterKaoqin.OnitemClick(){
                    @Override
                    public void onItemClick(int position) {
                        Intent intent = new Intent(Kaoqin.this , QiandaoStatus.class);//考勤界面,传入第几次考勤
                        intent.putExtra("checkNum", listItemKaoqin.get(position).getCheckNumber());//第几次考勤
                        intent.putExtra("flag",listItemKaoqin.get(position).getPostType());
                        intent.putExtra("courseId",courseCode);
                        intent.putExtra("postId",listItemKaoqin.get(position).getPostId());
                        startActivity(intent);
                    }
                } );
            }
        });
    }

}

