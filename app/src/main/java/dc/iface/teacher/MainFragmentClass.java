package dc.iface.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.object.CourseListItem;

public class MainFragmentClass extends Fragment {
    private static MainFragmentClass mf;
    private static String TAG = "MainFragmentClass";
    private List<CourseListItem> listItemCourses = new ArrayList<>();//ListItem课程集
    private String teacherId;
    private String teacherName;
    private CoursesAdapter coursesAdapter;
    private ListView lvListView ;
    //单例模式
    public static MainFragmentClass getMainFragment(String teacherId) {
        return  new MainFragmentClass(teacherId);
    }

    public MainFragmentClass(String teacherId ){
        this.teacherId=teacherId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate( R.layout.main_fragment_class ,container, false);//fragment_message为底部栏的界面

        lvListView = view.findViewById(R.id.course_list);

        Log.i(TAG,"进入onCreateView");
        //从 course表中查找并以列表的形式显示出来

        new Thread( new Runnable() {
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

                    coursesAdapter = new CoursesAdapter(getActivity() ,//***********************************
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
        } ).start();

        //Toast.makeText(getActivity(), "查询成功：共"+coursesAdapter.getCount()+"条数据。" , Toast.LENGTH_LONG).show();
        lvListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });

        return view;
    }

}