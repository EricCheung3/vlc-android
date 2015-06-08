package easydarwin.android.videostreaming;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
 
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    // Global variables
    private Boolean _run;
//    public boolean isDrawing = true;
    protected DrawThread thread;
    private Bitmap mBitmap;
 
    // Constructor
    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        thread = new DrawThread(getHolder());
    }
 
    class DrawThread extends  Thread {
        private SurfaceHolder mSurfaceHolder;
 
        public DrawThread (SurfaceHolder surfaceHolder){
            mSurfaceHolder = surfaceHolder;
        }
 
        public void setRunning(boolean run) {
            _run = run;
        }
 
        @Override
        public void run() {
            Canvas canvas = null;
 
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(1);
            paint.setTextSize(20);
 
            while (_run){
//                if(isDrawing == true){
                    try{
                        canvas = mSurfaceHolder.lockCanvas(null);
                        if(mBitmap == null){
                            mBitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
                        }
                        final Canvas c = new Canvas (mBitmap);
 
                        c.drawColor(Color.WHITE);
                        c.drawCircle(80,80, 30, paint);
                        c.drawLine(80, 80, 80, 200, paint);
                        c.drawText(""+canvas.getWidth()+", "+canvas.getHeight(), 0, 200,paint);
 
                        canvas.drawBitmap (mBitmap, 0,  0,null);
                    } finally {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
//                }
            }
        }
    }
 
    public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) {
        // Create a Bitmap with the dimensions of the View
        mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);;
    }
 
    public void surfaceCreated(SurfaceHolder holder) {
        // Starts thread execution
        thread.setRunning(true);
        thread.start();
    }
 
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Finish thread execution
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }
 
}