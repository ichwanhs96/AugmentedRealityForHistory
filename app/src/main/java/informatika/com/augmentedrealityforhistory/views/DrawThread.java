package informatika.com.augmentedrealityforhistory.views;

/**
 * Created by Ichwan Haryo Sembodo on 29/06/2016.
 */
import android.graphics.Canvas;

import informatika.com.augmentedrealityforhistory.activities.OverlayActivity;

public class DrawThread extends Thread {
    private String id;
    private DrawView drawView;
    private OverlayActivity overlayActivity;
    private boolean running = false;

    public DrawThread(String id, DrawView view, OverlayActivity overlayActivity) {
        this.id = id;
        drawView = view;
        this.overlayActivity = overlayActivity;
    }

    public void setRunning(boolean run) {
        running = run;
    }

    @Override
    public void run() {
        while(running){
            Canvas canvas = drawView.getHolder().lockCanvas();
            if(canvas != null){
                synchronized (drawView.getHolder()) {
                    drawView.drawSomething(canvas, id);
                }
                drawView.getHolder().unlockCanvasAndPost(canvas);
            }
            try {
                sleep(30);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}