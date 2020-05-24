package dc.iface.SQL;


import android.util.Log;
import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import static android.content.ContentValues.TAG;

public class DBUtils {

    private static String driver = "com.mysql.jdbc.Driver";// MySql驱动
    private static String user = "root";// 用户名
    private static String password = "XUAN";// 密码
    private Connection conn;
    private Statement statement;
    private ResultSet res;
    private PreparedStatement ps = null;
    private static Connection getConnection(String dbName) {

        Connection connection = null;
        try {
            Class.forName( driver );// 动态加载类
            connection = (Connection) DriverManager.getConnection( "jdbc:mysql://47.102.200.197:3306/iface" +
                            "?useUnicode=true&characterEncoding=UTF-8",
                    user, password );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    //查询操作--获取
    public ResultSet excuteSQL(String sql) {
        conn = getConnection( "iface" );//获取一个链接到数据库shop的Connection

        try {
            if (!conn.isClosed()) {
                System.out.println( "Succeeded connecting to the Database!" );
            }
            //String commend = sql;
            //String sql = "select *  from user ";
            //创建statement类对象，用来执行SQL语句
            statement = conn.createStatement();
            res = statement.executeQuery(sql);
            if (res == null) {
                Log.d( TAG, "结果为空！" );
                conn.close();
                return null;
            } else {
                //res.close();
                //conn.close();//只有在res不为null时才能关闭，否则会出错！！！
                return res;
            }


        } catch (SQLException e) {
            //数据库连接失败异常处理
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println( "数据库数据成功获取！！" );
        }

        return res;
    }



    //上传图片
    public Connection getCoon(){

        conn = getConnection( "iface" );//获取一个链接到数据库shop的Connection

        try {
            if (!conn.isClosed()) {
                System.out.println( "Succeeded connecting to the Database!" );
                return conn;
            }

        } catch (SQLException e) {
            //数据库连接失败异常处理
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println( "数据库数据成功获取！！" );
        }

        return conn;
    }



    //更新删除操作
    public int excuteSQLToADU(String sql) {
        conn = getConnection( "iface" );//获取一个链接到数据库shop的Connection
        int count=0;
        try {
            if (!conn.isClosed()) {
                System.out.println( "Succeeded connecting to the Database!" );
            }

            //创建statement类对象，用来执行SQL语句
            statement = conn.createStatement();
            count = statement.executeUpdate(sql);//影响的数据库行数

            if (count==0) {
                Log.d( TAG, "操作失败！" );
                conn.close();
                return count;
            } else {
                conn.close();//只有在res不为null时才能关闭，否则会出错！！！
                return count;
            }
        } catch (SQLException e) {
            //数据库连接失败异常处理
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println( "数据库数据成功获取！！" );
        }

        return count;
    }
}








