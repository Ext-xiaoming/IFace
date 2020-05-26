package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.BaseActivity.StatisClass;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.login.LoginActivity;
import dc.iface.object.CourseListItem;
import dc.iface.student.ChangePswActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.Server.URI.server;


public class MainFragmentClass extends Fragment {
    private static MainFragmentClass mf;
    private static String TAG = "MainFragmentClass";
    private String  teacherId;
    private String teacherName;

    private RecyclerView recyclerView ;
    private List<CourseListItem> listItemCourses;
    private CoursesAdapter coursesAdapter;
    //单例模式
    public static MainFragmentClass getMainFragment(String studentId){
        if(mf == null){
            mf = new MainFragmentClass();
        }
        mf.teacherId= studentId;
        return mf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate( R.layout.main_fragment_class ,container, false);//fragment_message为底部栏的界面

        recyclerView = view.findViewById(R.id.course_list);

        Log.i(TAG,"进入onCreateView");
        //从 course表中查找并以列表的形式显示出来

        LodeListView();

        /*recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                Intent intent = new Intent(getActivity () , Kaoqin.class);//考勤界面
                intent.putExtra("teacherId",teacherId);
                intent.putExtra("teacherName", listItemCourses.get(position).getTeacherName());
                intent.putExtra("courseName", listItemCourses.get(position).getCourseName());
                intent.putExtra("courseId", listItemCourses.get(position).getCourseId());
                startActivity(intent);
            }
        });*/

        return view;
    }

    //加载ListView
    private void LodeListView(){
        /**
         * 传递 teacherId 、
         * 获取 course_name、course_id、teacher_name
         * */

        new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("teacherId",teacherId)
                        .build();

                final Request request = new Request.Builder()
                        .url(server+"lodeTeaCourseList/")
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.printf( "失败！" );
                        getActivity().runOnUiThread(new Runnable() {
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
                            Log.d( "MainFragmentClass", result );
                        }
                    }
                });

            }
        }).start();

    }

    //json解析  + 适配器数据分发       * 获取 course_name、course_id、teacher_name
    private void parseJSONWithJSONObjectArray(String jsonData){
        try {
            listItemCourses = new ArrayList<>();//ListItem课程集
            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i=0 ;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject( i );
                String course_name=jsonObject.getString( "course_name" );
                String course_id =jsonObject.getString( "course_id" );
                String teacher_name =jsonObject.getString( "teacher_name" );

                Log.d( "MainFragmentClass","course_name is "+course_name );
                Log.d( "MainFragmentClass","course_id is "+course_id );
                Log.d( "MainFragmentClass","teacher_name is "+teacher_name );

                CourseListItem item = new CourseListItem();
                item.setCourseId( course_id );
                item.setCourseName(course_name);
                item.setTeacherName(teacher_name);
                listItemCourses.add(item);
            }
            coursesAdapter = new CoursesAdapter(getActivity() ,
                    R.layout.course_item , listItemCourses);//MainActivity.this = getActivity ()
            HandleResponse();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //显示
    private void HandleResponse() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < listItemCourses.size(); i++) {
                    CourseListItem s = (CourseListItem)listItemCourses.get(i);
                    System.out.println(i+"输出："+s.getCourseId()+"  "+s.getCourseName()+"  "+s.getTeacherName()+"\n");
                }
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager( getContext() );
                recyclerView.setLayoutManager( linearLayoutManager );
                recyclerView.setAdapter(coursesAdapter);
                coursesAdapter.setOnItemClickListener( new CoursesAdapter.OnitemClick(){
                    @Override
                    public void onItemClick(int position) {
                        Intent intent = new Intent(getActivity () , Kaoqin.class);//考勤界面
                        intent.putExtra("teacherId",teacherId);
                        intent.putExtra("teacherName", listItemCourses.get(position).getTeacherName());
                        intent.putExtra("courseName", listItemCourses.get(position).getCourseName());
                        intent.putExtra("courseId", listItemCourses.get(position).getCourseId());
                        startActivity(intent);
                    }
                } );

            }
        });
    }

}

/*new Thread( new Runnable() {
            @Override
            public void run() {

                //教师名称 + 课程名称 + 加课码
                //首先查询 教师名称

                DBUtils dbUtils= new DBUtils();
                String sql = "select  teacher_name from  teacher where teacher_id ="+teacherId;
                ResultSet resultSet = dbUtils.excuteSQL( sql );

                /////////////////////////////////////////////////////////////////////////////////////////////
                try{
                    if(resultSet.next()) {
                        System.out.println( resultSet.getString("teacher_name") );
                        teacherName=resultSet.getString( "teacher_name" );
                        resultSet.getStatement().getConnection().close();
                    }else {
                        System.out.println("未知错误" );
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.printf( e.getMessage() );
                }
                /////////////////////////////////////////////////////////////////////////////////////////////

                //查询课程列表
                sql = "select course_id , course_name from  course  where teacher_id ="+teacherId;
                resultSet = dbUtils.excuteSQL( sql );
                try{
                    while(resultSet.next()){
                        CourseListItem item = new CourseListItem();//////////////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!需要new 出新的对象
                        System.out.println( resultSet.getString("course_name") );
                        item.setCourseId( resultSet.getString("course_id") );
                        item.setCourseName(resultSet.getString("course_name"));
                        item.setTeacherName(teacherName);
                        listItemCourses.add(item);
                    }

                    final  CoursesAdapter coursesAdapter = new CoursesAdapter(getActivity() ,//***********************************
                            R.layout.course_item , listItemCourses);//MainActivity.this = getActivity ()

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println( "list.size="+listItemCourses.size()+"--00000000000000\n");
                            for (int i = 0; i < listItemCourses.size(); i++) {
                                CourseListItem s = (CourseListItem)listItemCourses.get(i);
                                System.out.println(i+"输出："+s.getCourseId()+"  "+s.getCourseName()+"  "+s.getTeacherName()+"\n");
                            }
                            lvListView.setAdapter(coursesAdapter);
                        }
                    });
                    resultSet.getStatement().getConnection().close();
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.printf( e.getMessage() );
                }
            }
        } ).start();*/

//Toast.makeText(getActivity(), "查询成功：共"+coursesAdapter.getCount()+"条数据。" , Toast.LENGTH_LONG).show();