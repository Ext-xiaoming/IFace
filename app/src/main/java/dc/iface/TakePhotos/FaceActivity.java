package dc.iface.TakePhotos;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;


import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mysql.jdbc.Connection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.Server.NetWork;

import dc.iface.teacher.AddcourseActivity;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static dc.iface.teacher.FaqianActivity.getSmallLetter;


//教师端的人脸考勤选项
public class FaceActivity extends BaseActivity {
    //private PhotoHttpUtils photoHttpUtils= new PhotoHttpUtils();
    private Button mBtnTakePhoto;
    private Button mBtnSelectPhoto;
    private Button btnUploadPhoto;
    private TextView mTvPath;
    private TextView mTvUri;
    private LQRPhotoSelectUtils mLqrPhotoSelectUtils;
    private ImageView mIvPic;
    private Context context = FaceActivity.this;
    private  Bitmap  bitmap ;
    private String studentId="";
    private String pictureId="111";
    private String postId="222";
    private String postNum="333";
    private String courseId;
    private String teacherId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_take_photo);
        mBtnTakePhoto =  findViewById(R.id.btnTakePhoto);
        mBtnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnUploadPhoto= findViewById(R.id.btnUploadPhoto);

        mTvPath =  findViewById(R.id.tvPath);
        mTvUri = findViewById(R.id.tvUri);
        mIvPic =  findViewById(R.id.ivPic);



        //createQRImage(RandNum);
        Intent intent = getIntent();
        courseId = intent.getStringExtra("courseId");
        postNum  = String.valueOf( QueryPostNum(courseId) );
        teacherId= intent.getStringExtra("teacherId");
        pictureId = String.valueOf(System.currentTimeMillis()) ;//获取当前的毫秒数作为文件名

        postId = getSmallLetter (6);//生成8位随机字符串

        init();
        initListener();

    }

    // post_num(通过查询post_check_in 中同一个course_id 下的最大 post_num)
    public int QueryPostNum(String course_id)
    {
        int postNum=0;

        DBUtils dbUtils= new DBUtils();
        String sql = "select max(post_num) from post_check_in where course_id= "+course_id;
        ResultSet resultSet = dbUtils.excuteSQL( sql );

        try{

            if(resultSet.next()) {
                postNum=resultSet.getInt( "max(post_num)" );
                resultSet.getStatement().getConnection().close();
            }
            else{
                postNum=0;
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.printf( e.getMessage() );
        }
        return postNum+1;
    }

    private void init() {
        // 1、创建LQRPhotoSelectUtils（一个Activity对应一个LQRPhotoSelectUtils）
        mLqrPhotoSelectUtils = new LQRPhotoSelectUtils(this, new LQRPhotoSelectUtils.PhotoSelectListener() {
            @Override
            public void onFinish(File outputFile, Uri outputUri) {
                // 4、当拍照或从图库选取图片成功后回调
                mTvPath.setText(outputFile.getAbsolutePath());
                mTvUri.setText(outputUri.toString());
                Glide.with(FaceActivity.this).load(outputUri).into(mIvPic);
            }
        }, true);//true裁剪，false不裁剪

        //        mLqrPhotoSelectUtils.setAuthorities("com.lqr.lqrnativepicselect.fileprovider");
        //        mLqrPhotoSelectUtils.setImgPath(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + String.valueOf(System.currentTimeMillis()) + ".jpg");
    }

    private void initListener() {
        mBtnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3、调用拍照方法
                PermissionGen.with(FaceActivity.this)
                        .addRequestCode(LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        ).request();
            }
        });

        mBtnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3、调用从图库选取图片方法
                PermissionGen.needPermission(FaceActivity.this,
                        LQRPhotoSelectUtils.REQ_SELECT_PHOTO,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                );
            }
        });


        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //教师端  上传照片 + 发布签到信息
                System.out.printf( " //教师端  上传照片 + 发布签到信息" );
                //上传图片
                //PostImage();

                //
                //发布一次签到 + 服务器发送字段识别学生
                PostRequest();

            }
        });

    }


    private void PostImage(){

    }

    private void PostRequest(){
        //教师端  上传照片 + 发布签到信息
        System.out.printf( " PostRequest()" );

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                FormBody body = new FormBody.Builder()
                        .add("pictureId",pictureId)
                        .add("postId",postId)
                        .add("postNum",postNum)
                        .add("teacherId",teacherId)
                        .add("courseId",courseId)

                        .build();
                Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/postIfaceCheck/")
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //...
                        System.out.printf( "!!!!!!!!!!!!!!!!!!!失败" );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.printf( result );
                                }
                            });
                        }
                    }
                });

            }
        }).start();
    }


    public Uri getUriFromDrawableRes(Context context, int id) {
        Resources resources = context.getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(id) + "/"
                + resources.getResourceTypeName(id) + "/"
                + resources.getResourceEntryName(id);
        return Uri.parse(path);
    }

    //把图片转换为字节
    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
    private void takePhoto() {
        mLqrPhotoSelectUtils.takePhoto();
    }

    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_SELECT_PHOTO)
    private void selectPhoto() {
        mLqrPhotoSelectUtils.selectPhoto();
    }

    @PermissionFail(requestCode = LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
    private void showTip1() {
        showDialog();
    }

    @PermissionFail(requestCode = LQRPhotoSelectUtils.REQ_SELECT_PHOTO)
    private void showTip2() {
        showDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 2、在Activity中的onActivityResult()方法里与LQRPhotoSelectUtils关联
        mLqrPhotoSelectUtils.attachToActivityForResult(requestCode, resultCode, data);
    }

    public void showDialog() {
        //创建对话框创建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置对话框显示小图标
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        //设置标题
        builder.setTitle("权限申请");
        //设置正文
        builder.setMessage("在设置-应用-虎嗅-权限 中开启相机、存储权限，才能正常使用拍照或图片选择功能");

        //添加确定按钮点击事件
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {//点击完确定后，触发这个事件

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //这里用来跳到手机设置页，方便用户开启权限
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + FaceActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //添加取消按钮点击事件
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        //使用构建器创建出对话框对象
        AlertDialog dialog = builder.create();
        dialog.show();//显示对话框
    }
}

 /*PreparedStatement ps = null;
                        String imgStr = "";
                        try{

                            // ½«Í¼Æ¬×ª»»³É×Ö·û´®
                            File f = new File("c:\\Users\\Administrator\\Desktop\\111.png");
                            FileInputStream fis = new FileInputStream( f );


                            DBUtils dbUtils= new DBUtils();
                            String sql = "insert into pic_test (pictest_id,pic)values(?,?)";
                            System.out.printf( sql );
                            Connection conn = dbUtils.getCoon();
                            ps = (PreparedStatement) conn.prepareStatement(sql);
                            ps.setInt(1, 1);
                            ps.setBinaryStream(2, fis, fis.available());
                            int count = ps.executeUpdate();

                            try{

                                if(count!=0) {
                                    System.out.printf( "³É¹¦£¡" );
                                    conn.close();
                                }
                                else{
                                    System.out.printf( "Ê§°Ü£¡" );
                                    conn.close();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                                System.out.printf( e.getMessage() );
                            }



                            //²éÑ¯
                            sql="select pic from pic_test where pictest_id=1  ";
                            ResultSet resultSet = dbUtils.excuteSQL( sql );
                            try {
                                if(resultSet.next()){
                                    InputStream in = resultSet.getBinaryStream("pic");
                                    //imgStr = byte2hex( resultSet.getBytes("pic") );

                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(new File("J:/1.png"));

                                        int nRead = 0;
                                        byte[] buf  = new byte[1024];
                                        while( ( nRead = in.read(buf ) ) != -1 ){
                                            fos.write( buf , 0, nRead );
                                        }

                                        fos.flush();
                                        fos.close();
                                        in.close();


                                    }catch(Exception e) {
                                        e.getStackTrace();
                                    }

                                }

                                resultSet.getStatement().getConnection().close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }finally{
                            System.out.printf( "ok" );
                        }
*/