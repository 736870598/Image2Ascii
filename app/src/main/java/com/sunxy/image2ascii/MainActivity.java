package com.sunxy.image2ascii;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.sunxiaoyu.utils.UtilsCore;
import com.sunxiaoyu.utils.core.PhotoConfig;
import com.sunxiaoyu.utils.core.model.ActivityResultInfo;
import com.sunxiaoyu.utils.core.utils.DialogUtils;
import com.sunxiaoyu.utils.core.utils.ToastUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView showView;
    private Disposable disposable;
    private Bitmap showBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.take_photo).setOnClickListener(this);
        findViewById(R.id.take_album).setOnClickListener(this);
        findViewById(R.id.save_photo).setOnClickListener(this);
        showView = findViewById(R.id.showView);

        requestPermissions();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.take_photo:
                String savePath = getExternalFilesDir("image").getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
                UtilsCore.manager().takePicture(MainActivity.this, 0, false, savePath)
                        .subscribe(new Consumer<ActivityResultInfo>() {
                            @Override
                            public void accept(ActivityResultInfo activityResultInfo) throws Exception {
                                if (activityResultInfo.success()){
                                    String path = activityResultInfo.getData().getStringExtra(PhotoConfig.RESULT_PHOTO_PATH);
                                    dealImage(path);
                                }
                            }
                        });
                break;
            case R.id.take_album:
                UtilsCore.manager().selectPicture(MainActivity.this, 0, false, null)
                        .subscribe(new Consumer<ActivityResultInfo>() {
                            @Override
                            public void accept(ActivityResultInfo activityResultInfo) throws Exception {
                                if (activityResultInfo.success()){
                                    String path = activityResultInfo.getData().getStringExtra(PhotoConfig.RESULT_PHOTO_PATH);
                                    dealImage(path);
                                }
                            }
                        });
                break;
            case R.id.save_photo:
                if (showBitmap != null && !showBitmap.isRecycled()){
                    ImageUtils.saveBitmap2file(MainActivity.this, showBitmap);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
        if (showBitmap != null && !showBitmap.isRecycled()){
            showBitmap.recycle();
            showBitmap = null;
        }
    }

    private void requestPermissions(){
        disposable = UtilsCore.manager().requestPermissions(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean it) throws Exception {
                        if (!it){
                            ToastUtils.showToast(MainActivity.this, "缺少必要的权限，无法注册运行！");
                        }
                    }
                });
    }


    private void dealImage(final String path){
        DialogUtils.showLoadDialog(this, "加载中...");
        disposable = Observable
                .just(path)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(String path) throws Exception {
                        return ImageUtils.createAsciiPic(path, showView.getWidth(), showView.getHeight());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        if (showBitmap != null && !showBitmap.isRecycled()){
                            showBitmap.recycle();
                            showBitmap = null;
                        }
                        showBitmap = bitmap;
                        showView.setImageBitmap(showBitmap);
                        DialogUtils.dismiss();
                    }
                });

    }
}
