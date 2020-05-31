package dc.iface.TakePhotos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import dc.iface.BaseActivity.ActivityCollectorUtil;
import dc.iface.R;
import dc.iface.student.ChangePswActivity;
import dc.iface.student.NumqiandaoActivity;
import dc.iface.student.StuMainFragmentUser;
import dc.iface.teacher.Kaoqin;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static dc.iface.Server.URI.server;
import static dc.iface.teacher.FaqianActivity.getSmallLetter;
import static mapsdkvi.com.gdi.bgl.android.java.EnvDrawText.bmp;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE_CAMERA=1001;
    private static final int REQUEST_CAMERA=2001;

    private static final int REQUEST_CODE_PHOTOSHOP=1002;
    private static final int REQUEST_PHOTOSHOP=2002;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    private Button TakePhoto;
    private Button SelectPhoto;
    private Button UploadPhoto;
    private TextView Path;
    private TextView viewUri;
    //private LQRPhotoSelectUtils mLqrPhotoSelectUtils;
    private ImageView Pic;
    private Context context = PhotoActivity.this;
    private Bitmap bitmap ;
    private Uri mUri;
    private String imagePath;
    private String studentId;
    private String teacherId;
    int flag=1;//1 教师
    private File f;
    private String currentPhotoPath;
    private Uri photoURI;
    private String filename;
    private int num;
    private String courseName;
    private String teacherName;
    private String  courseId;
    private TextView s_res_num;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);
        TakePhoto =  findViewById( R.id.TakePhoto);
        SelectPhoto = findViewById(R.id.SelectPhoto);
        UploadPhoto= findViewById(R.id.UploadPhoto);
        Path =  findViewById(R.id.TPath);
        viewUri = findViewById(R.id.TUri);
        Pic =  findViewById(R.id.Pic);
        s_res_num =  findViewById(R.id.num);
        TakePhoto.setOnClickListener( this );
        SelectPhoto.setOnClickListener( this);
        UploadPhoto.setOnClickListener( this );
        UploadPhoto.setEnabled(false);

        progressBar=findViewById( R.id.prograss_bar );
        progressBar.setVisibility( View.INVISIBLE );
        Intent intent = getIntent();
        flag = Integer.parseInt(intent.getStringExtra("flag") );
        courseName=intent.getStringExtra("courseName");
        teacherName=intent.getStringExtra("teacherName");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.TakePhoto:
                if(ContextCompat.checkSelfPermission( PhotoActivity.this, Manifest.permission.CAMERA )!=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions( PhotoActivity.this,
                            new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA},REQUEST_CAMERA );
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
                        String file_save_path_InServer="/data/wwwroot/IFace/student";
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

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this,
                    "dc.iface.fileprovider",
                    photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createImageFile2() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void SPhotos() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PHOTOSHOP);
    }

    //回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {

            Bitmap bm = null;

            try {
                bm = getBitmapFormUri(photoURI);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d( "PhotoActivity","wqq3r4" +e.getMessage() );
            } catch (IOException e) {
                e.printStackTrace();
                Log.d( "PhotoActivity","324" +e.getMessage() );
            }
            System.out.printf( bm.toString() );
            if(bm!=null)
                Pic.setImageBitmap(bm);

            UploadPhoto.setEnabled(true);
            imagePath=currentPhotoPath;
            System.out.printf( "成功" );

            try {
                galleryAddPic();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                UploadPhoto.setEnabled(true);
                UriPathUtils uriPathUtils = new UriPathUtils();
                imagePath= uriPathUtils.getRealPathFromUri(context,mUri);

            }
            System.out.printf( "成功！" );
        }else {
            System.out.printf( "失败" );
        }
    }

    private void galleryAddPic() throws IOException {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //单纯上传照片用
    private  void StudengUPhoto(final String file_save_path_InServer ){

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                String fileName = null;
                File file = null;

                try {
                    file  = new File( imagePath );
                    Log.d( "PhotoActivity", "4234244"+file.getAbsolutePath() );
                    Log.d( "PhotoActivity", "4444"+file.getName().toString() );
                }catch (Exception e){
                    Log.d( "PhotoActivity","111" +e.getMessage() );
                }

                RequestBody image = RequestBody.create( MediaType.parse("image/jpg"), file);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType( MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), image)
                        .addFormDataPart( "studentId",studentId )
                        .addFormDataPart( "file_name_InSFolder","1.jpg" )
                        .addFormDataPart( "file_save_path_InServer",file_save_path_InServer )
                        .build();


                Log.d( "PhotoActivity", "111111111111111111" );

                Request request = new Request.Builder()
                        .url(server+"savePictures/")
                        .post(requestBody)
                        .build();
                Call call = client.newCall(request);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility( View.VISIBLE );
                    }
                });

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //...
                        Log.d( "PhotoActivity","222!!!!!!!!!!!!!!!!!!失败222222" );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();
                            try{
                                JSONObject jsonObject= new JSONObject( result );
                                int res =jsonObject.getInt( "RESULT" );
                                Log.d( "PhotoActivity", "000"+result );
                                if(res==1){
                                    //上传成功
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText( PhotoActivity.this,"上传成功",Toast.LENGTH_SHORT ).show();
                                            progressBar.setVisibility( View.INVISIBLE );
                                            finish();
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
                                Log.d( "PhotoActivity", "000"+ e.getMessage() );
                            }
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
        courseId = intent.getStringExtra("courseId");

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();


                String fileName = null;
                File file = null;

                try {
                    file  = new File( imagePath );
                    Log.d( "PhotoActivity", file.getAbsolutePath() );
                }catch (Exception e){
                    Log.d( "PhotoActivity", e.getMessage() );
                }

                RequestBody image = RequestBody.create( MediaType.parse("image/jpg"), file);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType( MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(), image)
                        .addFormDataPart( "postId",postId )
                        .addFormDataPart( "teacherId",teacherId )
                        .addFormDataPart( "class_index",courseId )
                        .addFormDataPart( "postData",curTime )
                        .addFormDataPart( "file_name_InSFolder",file_name_InSFolder )
                        .addFormDataPart( "file_save_path_InServer",file_save_path_InServer )
                        .build();

                Request request = new Request.Builder()
                        .url(server+"postIfaceCheck/")
                        .post(requestBody)
                        .build();
                Call call = client.newCall(request);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility( View.VISIBLE );
                    }
                });


                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        System.out.printf( "!!!!!!!!!!!!!!!!!!!失败" );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if(response.isSuccessful()){
                            final String result = response.body().string();

                            JSONObject jsonObject= null;
                            try {
                                jsonObject = new JSONObject( result );
                                num =jsonObject.getInt( "num" );
                                filename =jsonObject.getString( "filename" );
                                Log.d( "PhotoActivity", "000"+result );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText( PhotoActivity.this,"上传成功",Toast.LENGTH_SHORT ).show();
                                    progressBar.setVisibility( View.INVISIBLE );
                                    //s_res_num.setText( "识别结果人数："+ String.valueOf( num ));
                                    finish();
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
