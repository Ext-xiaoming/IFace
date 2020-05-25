package dc.iface.TakePhotos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import dc.iface.R;
import dc.iface.student.NumqiandaoActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static dc.iface.teacher.FaqianActivity.getSmallLetter;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE_CAMERA=1001;
    private static final int REQUEST_CAMERA=2001;

    private static final int REQUEST_CODE_PHOTOSHOP=1002;
    private static final int REQUEST_PHOTOSHOP=2002;


    private Button TakePhoto;
    private Button SelectPhoto;
    private Button UploadPhoto;
    private TextView Path;
    private TextView Uri;
    //private LQRPhotoSelectUtils mLqrPhotoSelectUtils;
    private ImageView Pic;
    private Context context = PhotoActivity.this;
    private Bitmap bitmap ;
    private Uri mUri;
    private String imagePath;
    private String studentId;
    private String teacherId;
    int flag=1;//1 教师

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);
        TakePhoto =  findViewById( R.id.TakePhoto);
        SelectPhoto = findViewById(R.id.SelectPhoto);
        UploadPhoto= findViewById(R.id.UploadPhoto);
        Path =  findViewById(R.id.TPath);
        Uri = findViewById(R.id.TUri);
        Pic =  findViewById(R.id.Pic);
        TakePhoto.setOnClickListener( this );
        SelectPhoto.setOnClickListener( this);
        UploadPhoto.setOnClickListener( this );

        Intent intent = getIntent();
        flag = Integer.parseInt(intent.getStringExtra("flag") );

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.TakePhoto:
                if(ContextCompat.checkSelfPermission( PhotoActivity.this, Manifest.permission.CAMERA )!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions( PhotoActivity.this,
                            new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA );
                }else{
                    TPhoto();
                }
                break;

            case R.id.SelectPhoto:
                if(ContextCompat.checkSelfPermission( PhotoActivity.this, Manifest.permission.CAMERA )!=
                        PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions( PhotoActivity.this,new String[]{
                             Manifest.permission.WRITE_EXTERNAL_STORAGE,
                             Manifest.permission.READ_EXTERNAL_STORAGE,
                             Manifest.permission.CAMERA}, REQUEST_PHOTOSHOP);
                }else{
                    SPhotos();
                }
                break;

            case R.id.UploadPhoto:
                if(flag==1)//教师
                {
                    TeacherUPhoto();
                }else{
                    Intent intent = getIntent();
                    studentId = intent.getStringExtra("studentId");
                    String file_save_path_InServer="J:\\";
                    StudengUPhoto(file_save_path_InServer);
                }
                break;
            default:
                break;
        }
    }

    public  String getLetter(int size){//随机生成size位数字字符串
        StringBuffer buffer = new StringBuffer();
        Random random = new Random();
        for(int i=0; i<size;i++){
            buffer.append( (random.nextInt(10) ) );
        }
        return buffer.toString();
    }

    //回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            Bitmap bm = null;
            try {
                bm = getBitmapFormUri(mUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Pic.setImageBitmap(bm);
            System.out.printf( "成功" );
            //加载到页面上
        }
        else if(requestCode == REQUEST_CODE_PHOTOSHOP && resultCode == RESULT_OK) {
            if (data!=null) {
                Bitmap bm = null;
                Bundle bundle = data.getExtras();
                // 获取相机返回的数据，并转换为Bitmap图片格式，这是缩略图
                bm = (Bitmap) bundle.get("data");

                mUri = data.getData();//这里并不是绝对路径，需要根据Uri获取其绝对路径再上传
                try {
                    bm = getBitmapFormUri(mUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Pic.setImageBitmap(bm);
            }
            System.out.printf( "成功！" );
        }else {
            System.out.printf( "失败" );
        }
    }


    //权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        switch (requestCode){
            case REQUEST_CAMERA :
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    TPhoto();
                }else{
                    Toast.makeText( this,"你拒绝了权限",Toast.LENGTH_SHORT ).show();
                }
            break;

            case REQUEST_PHOTOSHOP:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    SPhotos();
                }else{
                    Toast.makeText( this,"你拒绝了权限",Toast.LENGTH_SHORT ).show();
                }
                break;
            default:
        }

    }

    private void TPhoto() {
        // 步骤一：创建存储照片的文件
        String path = getFilesDir() + File.separator + "images" + File.separator;
        File file = new File(path, "test.jpg");
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //步骤二：Android 7.0及以上获取文件 Uri
            //com.rk.myfeaturesapp是自己App的包名fileprovider是死值
            mUri = FileProvider.getUriForFile( PhotoActivity.this, "com.rk.myfeaturesapp.fileprovider", file);
        } else {
            //步骤三：获取文件Uri
            mUri = android.net.Uri.fromFile(file);
        }
        //步骤四：调取系统拍照
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        startActivityForResult(intent,REQUEST_CODE_CAMERA);
    }

    private void SPhotos() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PHOTOSHOP);
    }

    //单纯上传照片用
    private  void StudengUPhoto(final String file_save_path_InServer ){
        /**
         * 需求:
         * 1、指定图片在服务器的保存地址 pic_server_path
         * 2、图片的类型（学生个人 or 班级照片）pic_tors
         * 3、图片命名规范--单独指定字段 pic_name
         *
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                UriPathUtils uriPathUtils = new UriPathUtils();
                imagePath= uriPathUtils.getRealPathFromUri(context,mUri);
                System.out.printf(imagePath );
                File file = new File( imagePath );
                System.out.printf(file.getName().toString());
                RequestBody image = RequestBody.create( MediaType.parse("image/jpg"), file);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType( MultipartBody.FORM)
                        .addFormDataPart("file", imagePath, image)
                        .addFormDataPart( "studentId",studentId )
                        .addFormDataPart( "file_save_path_InServer",file_save_path_InServer )
                        //.addFormDataPart( "file_name_InSFolder",file_name_InSFolder )
                        .build();

                System.out.printf( "111111111111111111" );
                Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/savePictures/")
                        .post(requestBody)
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
                            try{
                                JSONObject jsonObject= new JSONObject( result );
                                int res =jsonObject.getInt( "RESULT" );
                                if(res==1){
                                    //上传成功
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText( PhotoActivity.this,"上传成功",Toast.LENGTH_SHORT ).show();
                                        }
                                    });
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText( PhotoActivity.this,"上传失败",Toast.LENGTH_SHORT ).show();
                                        }
                                    });
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            Log.d( "PhotoActivity", result );
                        }
                    }
                });

            }
        }).start();

    }


    //教师端发布人脸敲到 上传照片
    private void TeacherUPhoto() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        Date curDate = new Date(System.currentTimeMillis());
        final String curTime = formatter.format(curDate);

        //路径和名称
        final String file_name_InSFolder="123.jpg";
        final String file_save_path_InServer="J:\\";


        final String postId = getLetter (6);//生成8位随机字符串
        Intent intent = getIntent();
        teacherId = intent.getStringExtra("teacherId");
        final String  courseId = intent.getStringExtra("courseId");

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                UriPathUtils uriPathUtils = new UriPathUtils();
                imagePath= uriPathUtils.getRealPathFromUri(context,mUri);
                System.out.printf(imagePath );
                File file = new File( imagePath );
                System.out.printf(file.getName().toString());
                RequestBody image = RequestBody.create( MediaType.parse("image/jpg"), file);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType( MultipartBody.FORM)
                        .addFormDataPart("file", imagePath, image)
                        .addFormDataPart( "postId",postId )
                        .addFormDataPart( "teacherId",teacherId )
                        .addFormDataPart( "class_index",courseId )
                        .addFormDataPart( "postData",curTime )
                        .addFormDataPart( "file_name_InSFolder",file_name_InSFolder )
                        .addFormDataPart( "file_save_path_InServer",file_save_path_InServer )
                        .build();

                System.out.printf( "111111111111111111" );
                Request request = new Request.Builder()
                        .url("http://10.34.15.176:8000/savePictures/")
                        .post(requestBody)
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

                                }
                            });
                        }
                    }
                });

            }
        }).start();

    }



    public Bitmap getBitmapFormUri(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = getContentResolver().openInputStream(uri);

        //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;//不加载到内存
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;

        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }
    //进行二次压缩

    public Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            if (options<=0)
                break;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


}
