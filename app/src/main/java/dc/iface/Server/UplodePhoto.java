package dc.iface.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class UplodePhoto {

/*
    String photo_path = new String();//照片的安卓本地路径
    String Server_address = new String();//服务器访问地址
    File file =new File(photo_path);


    public UplodePhoto(String photo_path, String Server_address) {
        this.photo_path=photo_path;
        this.Server_address=Server_address;
    }
    //图片数据处理


    //okhttp 上传
    public void FileUpload(){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // .addFormDataPart("",)  //其他信息
                .addFormDataPart("stuphoto", "1.jpg",
                        RequestBody.create(MediaType.parse("image/png"), file))//文件
                .build();
        Request request = new Request.Builder()
                .url(Server_address).post(requestBody)
                .build();

        executeRequest(request);

        System.out.println("正在上传....");

    }


    //服务器回调
    private void executeRequest(Request request) {
        //OkHttpUtils
        //3.将Request封装为Call

        Call call = new OkHttpClient().newCall(request);
        //4.执行call
        call.enqueue( new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                // Toast.makeText(MainActivity.this,"网络连接失败！", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                {

                    final String relsult = response.body().string();//接收服务器返回来的信息
                    try {
                        JSONObject jsonObject = new JSONObject(relsult);
                        String rel = jsonObject.getString("result");
                        if(rel.equals("1")){
                            System.out.println("上传成功");
                        }else {
                            System.out.println("上传失败");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

    }*/

}
