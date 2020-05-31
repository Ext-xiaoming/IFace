package dc.iface.teacher;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.TakePhotos.PhotoActivity;
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
    private String TAG = "QiandaoStatus";
    private String postId;
    private List<QStatusItem> ListQStatusItem;
    private AdapterQStatus adapterQStatus;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView ser_res_pic;
    private String flag;//第几次签到

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView( R.layout.qiandao_status );


        //下拉刷新
        swipeRefreshLayout = findViewById( R.id.swipeRefreshlayout2 );
        swipeRefreshLayout.setColorSchemeResources( R.color.blue );

        Intent KQToStatus = getIntent();
        checkNum = KQToStatus.getStringExtra( "checkNum" );//第几次签到
        courseCode = KQToStatus.getStringExtra( "courseId" );//课程码匹配
        postId = KQToStatus.getStringExtra( "postId" );
        flag = KQToStatus.getStringExtra( "flag" );//是否是数字签到


        ser_res_pic = findViewById( R.id.ser_res_pic );
        recyclerView = findViewById( R.id.statusItem );
        recyclerView.addItemDecoration( new DividerItemDecoration( this, DividerItemDecoration.VERTICAL ) );
        biaoti = findViewById( R.id.title_sec );
        biaoti.setText( "第" + checkNum + "次签到" );

        back = findViewById( R.id.btn_back );
        back.setOnClickListener( new View.OnClickListener() {//返回按钮
            @Override
            public void onClick(View view) {
                //Intent intent=new Intent(QiandaoStatus.this, Kaoqin.class);
                //intent.putExtra("courseId",courseCode);
                //startActivity(intent);
                finish();
            }
        } );

        //约定： 识别结果使用签到的  courseId_checkNum.jpg
        if (flag.equals( "人脸识别" )) {

            LinearLayout.LayoutParams params13 = (LinearLayout.LayoutParams) ser_res_pic.getLayoutParams();
            params13.width = 1000;
            params13.height = 750;
            ser_res_pic.setLayoutParams( params13 );
            ser_res_pic.setBackgroundColor( Color.rgb( 240, 248, 255 ) );

            //String url = "http://47.115.6.199/statics/images/"+courseCode+"_"+checkNum+".jpg";
            String url =server+ "statics/images/" + courseCode + "_" + checkNum + ".jpg";

            //String url = "http://47.115.6.199/statics/images/2030_7.jpg";
            String updateTime = String.valueOf( System.currentTimeMillis() );
            Glide.with( QiandaoStatus.this ).load( url )
                    .signature( new StringSignature( updateTime ) )
                    .override( 800, 600 )
                    .into( ser_res_pic );
        } else {
            ser_res_pic.setMaxHeight( 1 );
        }
        LodeListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //这里获取数据的逻辑
                LodeListView();
                Log.i( TAG, "1" + "执行刷新" );
                swipeRefreshLayout.setRefreshing( false );
            }
        } );

    }


    public void LodeListView() {
        /**
         * 传递  courseId
         * 获取  student_id student_name CheckStatu
         * */
        new Thread( new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add( "courseId", courseCode )
                        .add( "postId", postId )

                        .build();

                final Request request = new Request.Builder()
                        .url( server + "whoQiandao/" )
                        .post( body )
                        .build();
                Call call = client.newCall( request );
                call.enqueue( new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.printf( "失败！" );
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                System.out.printf( "网络请求是失败" );
                            }
                        } );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String result = response.body().string();
                            parseJSONWithJSONObjectArray( result );
                            Log.d( "QiandaoStatus", result );
                        }
                    }
                } );

            }
        } ).start();
    }

    //json解析  + 适配器数据分发
    private void parseJSONWithJSONObjectArray(String jsonData) {
        ListQStatusItem = new ArrayList<>();
        try {

            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject( i );

                String student_id = jsonObject.getString( "student_id" );
                String student_name = jsonObject.getString( "student_name" );
                String CheckStatu = jsonObject.getString( "CheckStatu" );
                QStatusItem item = new QStatusItem();
                item.setStudentId( student_id );
                item.setName( student_name );
                item.setCheckStatu( CheckStatu );

                ListQStatusItem.add( item );
            }
            adapterQStatus = new AdapterQStatus( QiandaoStatus.this,
                    R.layout.item_status, ListQStatusItem );
            HandleResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示
    private void HandleResponse() {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                System.out.println( "list.size=" + ListQStatusItem.size() + "--00000000000000\n" );
                for (int i = 0; i < ListQStatusItem.size(); i++) {
                    QStatusItem s = (QStatusItem) ListQStatusItem.get( i );
                    System.out.println( i + "输出：" + s.getStudentId() + "  " + s.getCheckStatu() + "  " + s.getName() + "\n" );
                }
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager( QiandaoStatus.this );
                recyclerView.setLayoutManager( linearLayoutManager );
                recyclerView.setAdapter( adapterQStatus );

                adapterQStatus.setOnItemClickListener( new AdapterQStatus.OnitemClick() {
                    @Override
                    public void onItemClick(int position) {
                        //........
                    }
                } );
            }
        } );
    }
}