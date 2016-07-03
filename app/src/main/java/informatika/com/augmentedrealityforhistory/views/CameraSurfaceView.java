package informatika.com.augmentedrealityforhistory.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Ichwan Haryo Sembodo on 27/06/2016.
 */
public class CameraSurfaceView extends SurfaceView {
    private Camera camera;
    private SurfaceHolder surfaceHolder;

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    // open the camera
                    camera = Camera.open();
                } catch (RuntimeException e) {
                    // check for exceptions
                    System.err.println(e);
                    return;
                }
                Camera.Parameters param;
                param = camera.getParameters();

                List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();

                // You need to choose the most appropriate previewSize for your app
                Camera.Size previewSize = getOptimalPreviewSize(previewSizes, 480, 720);

                param.setPreviewSize(previewSize.width, previewSize.height);
                camera.setParameters(param);
                try {
                    // The Surface has been created, now tell the camera where to draw
                    // the preview.
                    camera.setDisplayOrientation(90);
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    // check for exceptions
                    System.err.println(e);
                    return;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                refreshCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }

        });
    }

    protected void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    protected Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
