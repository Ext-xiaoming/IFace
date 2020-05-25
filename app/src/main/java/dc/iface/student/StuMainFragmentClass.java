package dc.iface.student;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.CourseListItem;
import dc.iface.teacher.CoursesAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class StuMainFragmentClass extends Fragment {
    private static StuMainFragmentClass mf;
    private static String TAG = "StuMainFragmentClass";
    private String studentId;
    private int i=0;
    private ListView lvListView ;
    public static StuMainFragmentClass getMainFragment(String studentId){
        if(mf == null){
            mf = new StuMainFragmentClass();
        }
        mf.studentId= studentId;
        return mf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG,"进入onCreateView :");
        final View view = inflater.inflate( R.layout.main_fragment_class ,container, false);//fragment_message为底部栏的界面
        lvListView = view.findViewById(R.id.course_list);
        System.out.printf("234324studentId= " +studentId );
        Log.i(TAG,"123."+"进入:");
        //从 student_course表中查找并以列表的形式显示出来

        //教师名称 + 课程名称 + 加课码
        //从course中找到 教师名称 + 课程名称 ，从student_course中找到 加课码
        //*************************************************************************************************************
        Log.d( "StuMainFragmentClass", String.valueOf( i++ ) );
        final List<CourseListItem> listItemCourses = new ArrayList<>();//ListItem课程集

        LodeListView(listItemCourses);

        /////////////////////////////////////////////////////////////////////////////////////////////

        //*************************************************************************************************************

        lvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                //点击课程后进入考勤界面
                Activity activity = getActivity();
                Intent intent = new Intent( activity , KaoqinActivity.class);//考勤界面
                intent.putExtra("courseId", listItemCourses.get(position).getCourseId());
                intent.putExtra("studentId", studentId );
                startActivity(intent);
            }
        });

        return view;
    }

    public String QueryTeacherName(String teacher_id){

            String TeacherName="";

            DBUtils dbUtils= new DBUtils();
            String sql = "select teacher_name from teacher where teacher_id ="+teacher_id;
            System.out.printf( sql );
            ResultSet resultSet = dbUtils.excuteSQL( sql );

            try{

                if( resultSet.next()) {
                    TeacherName=resultSet.getString( "teacher_name" );
                    resultSet.getStatement().getConnection().close();
                }
                else{
                    //TeacherName=0;
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.printf( e.getMessage() );
            }
            return TeacherName;
    }

    //加载ListView
    private void LodeListView(final List<CourseListItem> listItemCourses){
        /**
         * 传递 teacherId 、
         * 获取 course_name、course_id、teacher_name
         * */
        new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("studentId",studentId)
                        .build();

                final Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/lodeStuCourseList/")
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
                            parseJSONWithJSONObjectArray(result,listItemCourses);
                            Log.d( "StuMainFragmentClass", result );
                        }
                    }
                });

            }
        }).start();

    }

    //json解析  + 适配器数据分发       * 获取 course_name、course_id、teacher_name
    private void parseJSONWithJSONObjectArray(String jsonData,List<CourseListItem> listItemCourses){
        try {
            JSONArray jsonArray = new JSONArray( jsonData );
            for (int i=0 ;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject( i );
                String course_name=jsonObject.getString( "course_id" );
                String course_id =jsonObject.getString( "course_name" );
                String teacher_name =jsonObject.getString( "teacher_name" );

                Log.d( "StuMainFragmentClass","course_name is "+course_name );
                Log.d( "StuMainFragmentClass","course_id is "+course_id );
                Log.d( "StuMainFragmentClass","teacher_name is "+teacher_name );

                CourseListItem item = new CourseListItem();
                item.setCourseId( course_id );
                item.setCourseName(course_name);
                item.setTeacherName(teacher_name);
                listItemCourses.add(item);
            }

            CoursesAdapter coursesAdapter = new CoursesAdapter(getActivity() ,
                    R.layout.course_item , listItemCourses);//MainActivity.this = getActivity ()
            HandleResponse(coursesAdapter,listItemCourses);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //显示
    private void HandleResponse(final CoursesAdapter coursesAdapter, final List<CourseListItem> listItemCourses) {
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
    }
}



