package easydarwin.android.videostreaming;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class PaintSurfaceView2 extends SurfaceView implements
		SurfaceHolder.Callback, OnTouchListener {
	// Surface holder allows to control and monitor the surface
	private SurfaceHolder mHolder;
	// A thread where the painting activities are taking place
	private BubbleThread mThread;

	
	private Context ctx;
	private Handler handler;
	
	private float mX = -100;
	private float mY = -100;
	private Paint mPaint = new Paint();

	// private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public PaintSurfaceView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Getting the holder
		mHolder = getHolder();
		// Initializing the X,Y position

		mHolder.addCallback(this);
		// Setting the color for the paint object
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.STROKE);

		ctx = context;
		setFocusable(true); // make sure we get key events

	}


	@Override
	public boolean onTouch(View v, MotionEvent e) {
		float touchX = e.getX();
		float touchY = e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mThread.setBubble(touchX, touchY);
			Log.i("TOUCH++COORDINATE==PaintView", Float.toString(touchX)+","+Float.toString(touchY));
			break;
		// case MotionEvent.ACTION_MOVE:
		// mThread.setBubble(touchX, touchY);
		// break;
		// case MotionEvent.ACTION_UP:
		// mThread.setBubble(touchX, touchY);
		// break;
		}
		return true;
	}

	class BubbleThread extends Thread {
		
		private boolean run = false;

		private float bubbleX;
		private float bubbleY;

		public BubbleThread(SurfaceHolder surfaceHolder, Context context) {
			mHolder = surfaceHolder;
			ctx = context;
		}

		protected void setBubble(float x, float y) {
			bubbleX = x;
			bubbleY = y;
		}

		public void run() {
			while (run) {
				Canvas c = null;
				try {
					c = mHolder.lockCanvas();
					synchronized (mHolder) {
						doDraw(c);
					}
				} finally {
					if (c != null) {
						mHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		public void setRunning(boolean b) {
			run = b;
		}


		private void doDraw(Canvas canvas) {
			if (run) {
				canvas.restore();
				canvas.drawColor(Color.BLACK);
				canvas.drawCircle(bubbleX, bubbleY, 50, mPaint);
			}
		}
	}

	public BubbleThread getThread() {
		return mThread;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		mThread = new BubbleThread(holder, ctx);
		mThread.setRunning(true);
		mThread.start();

//		// Start editing the surface
//		Canvas canvas = mHolder.lockCanvas();
//		// Draw a background color
//		canvas.drawColor(Color.TRANSPARENT);
//		// Draw a circle at (mX,mY) with radius 5
//		canvas.drawCircle(mX, mY, 50, mPaint);
//		// Finish editing the canvas and show to the user
//		mHolder.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		mThread.setRunning(false);
		while (retry) {
			try {
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

}