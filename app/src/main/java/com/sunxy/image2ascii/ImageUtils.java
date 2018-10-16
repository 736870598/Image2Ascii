package com.sunxy.image2ascii;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import com.sunxiaoyu.utils.core.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * --
 * <p>
 * Created by sunxy on 2018/10/16 0016.
 */
public class ImageUtils {

    private static final String base = "#8XOHLTI)i=+;:,.";

    public static Bitmap createAsciiPic(String path, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width0 = options.outWidth;
        int height0 = options.outHeight;
        //这里为什么要取7？
        float scale = width / 7f / width0;
        Bitmap image = scaleBitmap(path, (int)(width0*scale),  (int)(height0*scale));
//        Bitmap image = scaleBitmap(path, 300,300);

        //输出到指定文件中
        StringBuilder text = new StringBuilder();
        for (int y = 0; y < image.getHeight(); y+=2) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixel(x, y);
                int r =  (pixel & 0xff0000) >> 16;
                int g =  (pixel & 0xff00) >> 8;
                int b =  (pixel & 0xff);
                final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                int index = Math.round(gray*(base.length() + 1) / 255);
                String s = index >= base.length() ? " " : String.valueOf(base.charAt(index));
                text.append(s);
            }
            text.append("\n");
        }

        return textAsBitmap(text, width);

    }

    private static Bitmap scaleBitmap(String src, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(src), newWidth, newHeight, true);
    }


    private static Bitmap textAsBitmap(StringBuilder text, int width) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.RED);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSize(12);

        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_CENTER, 1f, 0.0f, true);

        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,
                layout.getHeight() + 20, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);
        layout.draw(canvas);

        return bitmap;
    }


    public static void saveBitmap2file(Context context, Bitmap bmp) {
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/asciiImage/";
        String saveName = System.currentTimeMillis() + ".jpg";
        FileUtils.createFile(savePath, saveName);
        File filePic = new File(savePath, saveName);
        try {
            FileOutputStream fos = new FileOutputStream(filePic);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(context, "保存成功,位置:" + filePic.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePic.getAbsolutePath())));

    }

}
