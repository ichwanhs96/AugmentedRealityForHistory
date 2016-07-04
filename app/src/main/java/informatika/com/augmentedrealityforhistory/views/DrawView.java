package informatika.com.augmentedrealityforhistory.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.activities.OverlayActivity;
import informatika.com.augmentedrealityforhistory.models.CustomRect;
import informatika.com.augmentedrealityforhistory.models.Response;

/**
 * Created by Ichwan Haryo Sembodo on 27/06/2016.
 */
public class DrawView extends SurfaceView {
    private Bitmap bmpIcon;
    private SurfaceHolder surfaceHolder;
    private OverlayActivity overlayActivity;

    public DrawView(Context context) {
        super(context);
        if(!isInEditMode()){
            overlayActivity = (OverlayActivity) context;
            init();
        }
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            overlayActivity = (OverlayActivity) context;
            init();
        }
    }

    private void init(){
        setZOrderOnTop(true);
        for(Response response :overlayActivity.responseList){
            DrawThread drawThread = new DrawThread(response.getId(), this, overlayActivity);
            overlayActivity.drawThreads.put(response.getId(), drawThread);
        }
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                bmpIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                for(Map.Entry<String, DrawThread> entry : overlayActivity.drawThreads.entrySet()){
                    overlayActivity.customRects.get(entry.getKey()).setObjHeight(bmpIcon.getHeight());
                    overlayActivity.customRects.get(entry.getKey()).setObjWidth(bmpIcon.getWidth());
                    overlayActivity.customRects.get(entry.getKey()).setRect();
                    entry.getValue().setRunning(true);
                    entry.getValue().start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                for(Map.Entry<String, DrawThread> entry : overlayActivity.drawThreads.entrySet()){
                    entry.getValue().setRunning(false);
                }
                while (retry) {
                    try {
                        for(Map.Entry<String, DrawThread> entry : overlayActivity.drawThreads.entrySet()){
                            entry.getValue().join();
                        }
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
    }

    protected void drawSomething(Canvas canvas, String id) {
        int x = overlayActivity.customRects.get(id).getX();
        int y = overlayActivity.customRects.get(id).getY();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if(x >= 0 && y >= 0){
            canvas.drawBitmap(bmpIcon,
                    null, overlayActivity.customRects.get(id).getRect(), null);
        }
    }
}
