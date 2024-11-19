package com.example.phogoviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class myPictureView extends View {
    private static final String IMAGE_DIRECTORY_PATH = "/storage/emulated/0/Pictures/PhotosEditor/";
    private static final String IMAGE_NAME = "image.jpeg";
    private String imagePath;

    public myPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setImagePath();
    }

    public void setImagePath() {
        this.imagePath = IMAGE_DIRECTORY_PATH + IMAGE_NAME;
        Log.d("MyPictureView", "이미지 경로 설정됨: " + imagePath);
        invalidate();
    }
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();
            } else {
                Log.e("myPictureView", "Bitmap 로드 실패: " + imagePath);
            }
        } else {
            Log.e("myPictureView", "이미지 경로가 null입니다.");
        }
    }
}