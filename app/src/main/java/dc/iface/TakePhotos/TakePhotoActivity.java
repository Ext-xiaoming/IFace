package dc.iface.TakePhotos;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dc.iface.BaseActivity.BaseActivity;
import dc.iface.R;
import dc.iface.SQL.DBUtils;
import dc.iface.teacher.AddcourseActivity;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static dc.iface.TakePhotos.LQRPhotoSelectUtils.REQ_TAKE_PHOTO;
import static dc.iface.TakePhotos.LQRPhotoSelectUtils.REQ_UPLODE_PHOTO;


public class TakePhotoActivity extends BaseActivity {
    //private PhotoHttpUtils photoHttpUtils= new PhotoHttpUtils();
    private Button mBtnTakePhoto;
    private Button mBtnSelectPhoto;
    private Button btnUploadPhoto;
    private TextView mTvPath;
    private TextView mTvUri;
    private LQRPhotoSelectUtils mLqrPhotoSelectUtils;
    private ImageView mIvPic;
    private Context context = TakePhotoActivity.this;
    private  Bitmap  bitmap ;
    private Uri url;
    private String studentId="";
    private String coursecode="";

    private int postNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_takephoto);
        mBtnTakePhoto =  findViewById(R.id.btnTakePhoto2);
        mBtnSelectPhoto = findViewById(R.id.btnSelectPhoto2);
        btnUploadPhoto= findViewById(R.id.btnUploadPhoto2);

        mTvPath =  findViewById(R.id.tvPath2);
        mTvUri = findViewById(R.id.tvUri2);
        mIvPic =  findViewById(R.id.ivPic2);


        Intent intent = getIntent();
        coursecode = intent.getStringExtra("studentId");
        studentId = intent.getStringExtra("studentId");
        studentId = intent.getStringExtra("studentId");
        studentId = intent.getStringExtra("studentId");
        studentId = intent.getStringExtra("studentId");
        postNum  = QueryPostNum(Integer.parseInt( coursecode ))+1;

        init();
        initListener();
        /*Glide.with(context)
                .load(url)
                .into( (ImageView) findViewById(R.id.ivPic2) );*/

    }

    private void init() {
        // 1、创建LQRPhotoSelectUtils（一个Activity对应一个LQRPhotoSelectUtils）
        mLqrPhotoSelectUtils = new LQRPhotoSelectUtils(TakePhotoActivity.this, new LQRPhotoSelectUtils.PhotoSelectListener() {
            @Override
            public void onFinish(File outputFile, Uri outputUri) {
                // 4、当拍照或从图库选取图片成功后回调
                mTvPath.setText(outputFile.getAbsolutePath());
                mTvUri.setText(outputUri.toString());
                url= outputUri;
                System.out.printf( "当拍照或从图库选取图片成功后回调"+url.toString() );
                //Glide.with(TakePhotoActivity.this).load(outputUri).into(mIvPic);
                Glide.with(context)
                        .load(url)
                        .into( (ImageView) findViewById(R.id.ivPic2) );
                System.out.printf( "当拍照或从图库选取图片成功后回调-----------" );
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
                PermissionGen.with(TakePhotoActivity.this)
                        //.addRequestCode(LQRPhotoSelectUtils.REQ_TAKE_PHOTO)
                        .addRequestCode(REQ_TAKE_PHOTO)

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
                PermissionGen.needPermission(TakePhotoActivity.this,
                        LQRPhotoSelectUtils.REQ_SELECT_PHOTO,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE}
                );
            }
        });

        btnUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*PermissionGen.needPermission(TakePhotoActivity.this,
                        LQRPhotoSelectUtils.REQ_UPLODE_PHOTO,
                        new String[]{Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,

                        }
                );
                */




                ShangchuanImage();
               }
            });
        }

    private void ShangchuanImage(){

        //学生端的上传只需要将 学号 + 照片 传输到数据库即可
        new Thread( new Runnable() {
            @Override
            public void run() {
                PreparedStatement ps = null;
                String imgStr = "";
                try{
                    System.out.printf( "!!!!!!!!!!!!!!!!!!!!!" );
                    File f = new File( String.valueOf( "/storage/Pictures/123.jpg" ) );
                    mIvPic.setImageURI(Uri.fromFile(new File("/storage/Pictures/123.jpg")));
                    FileInputStream fis = new FileInputStream( f );

                    DBUtils dbUtils= new DBUtils();
                    String sql = "insert into stu_pic (stu_id,stu_picture)values(?,?)";
                    System.out.printf( sql );
                    Connection conn = dbUtils.getCoon();
                    ps = (PreparedStatement) conn.prepareStatement(sql);
                    ps.setInt(1, Integer.parseInt( studentId ) );
                    ps.setBinaryStream(2, fis, fis.available());
                    int count = ps.executeUpdate();
                    System.out.printf( "!!!!222222222222222222!!!" );
                    try{

                        if(count!=0) {
                            System.out.printf( "成功" );
                            Looper.prepare();
                            Toast.makeText( TakePhotoActivity.this, "照片上传成功", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            conn.close();
                        }
                        else{
                            System.out.printf( "失败" );
                            Looper.prepare();
                            Toast.makeText( TakePhotoActivity.this, "照片上传失败", Toast.LENGTH_LONG).show();
                            Looper.loop();
                            conn.close();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.printf( e.getMessage() );
                    }


                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    System.out.printf( "OK" );
                }
            }
        } ).start();

    }

    private void ShangchuanImage2(){





    }


    // post_num(通过查询post_check_in 中同一个course_id 下的最大 post_num)
    public int QueryPostNum(int course_id)
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
        return postNum;
    }









    @PermissionSuccess(requestCode = REQ_TAKE_PHOTO)
    private void takePhoto() {
        mLqrPhotoSelectUtils.takePhoto();
    }

    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_SELECT_PHOTO)
    private void selectPhoto() {
        mLqrPhotoSelectUtils.selectPhoto();
    }

    @PermissionSuccess(requestCode = LQRPhotoSelectUtils.REQ_UPLODE_PHOTO)
    private void uplodePhoto() {
        ShangchuanImage();
    }

    @PermissionFail(requestCode = LQRPhotoSelectUtils.REQ_UPLODE_PHOTO)
    private void showTip3() {
        showDialog();
    }

    @PermissionFail(requestCode = REQ_TAKE_PHOTO)
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
                intent.setData(Uri.parse("package:" + TakePhotoActivity.this.getPackageName()));
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
  /*public Uri getUriFromDrawableRes(Context context, int id) {
        Resources resources = context.getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(id) + "/"
                + resources.getResourceTypeName(id) + "/"
                + resources.getResourceEntryName(id);
        return Uri.parse(path);
    }*/

   /* //把图片转换为字节
    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }*/