package easydarwin.android.videostreaming;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint("ClickableViewAccessibility")
public class PaintView extends View implements OnTouchListener {

	public static final int SERVER_PORT = 5220; // server port
	public static String SERVER_HOST = "129.128.184.46";// server ip
	public static String SERVER_NAME = "myria";// server name
	private static XMPPConnection connection = null;

	Paint mPaint;
	float mX;
	float mY;

	public PaintView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		/** Initializing the variables */
		mPaint = new Paint();
		mX = mY = -100;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Setting the color of the circle
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Style.STROKE);
		// RectF oval = new RectF(mX, mY, 60, 80);
		// canvas.drawOval(oval, mPaint);
		// Draw the circle at (x,y) with radius 60
		canvas.drawCircle(mX, mY, 60, mPaint);

		// Redraw the canvas
		invalidate();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		// When user touches the screen
		case MotionEvent.ACTION_DOWN:
			// Getting X,Y coordinate
			mX = event.getX();
			mY = event.getY();

			//SendMessage(VideoStreamingFragment.connection);
			//ReceiveMsgListenerConnection(VideoStreamingFragment.connection);
			break;
		}
		return true;
	}
//
//	private void SendMessage(XMPPConnection connection) {
//
//		if (mX > 0) {
//
//			Message msg = new Message(VideoStreamingFragment.to,
//					Message.Type.chat);
//			msg.setBody("drawView" + Float.toString(mX));
//			if (connection != null&& connection.isConnected()) {
//				connection.sendPacket(msg);
//				Log.i("PaintView", msg.getBody());
//			}else{
//				try {
//					connection.connect();
//					connection.sendPacket(msg);
//				} catch (XMPPException e) {
//					e.printStackTrace();
//				}
//
//			}
//		}
//	}

	private XMPPConnection GetConnection() {
		try {
			if (null == connection || !connection.isAuthenticated()) {
				XMPPConnection.DEBUG_ENABLED = true;

				ConnectionConfiguration config = new ConnectionConfiguration(
						SERVER_HOST, SERVER_PORT, SERVER_NAME);
				config.setReconnectionAllowed(true);
				config.setSendPresence(true);
				config.setSASLAuthenticationEnabled(true);
				connection = new XMPPConnection(config);
				connection.connect();

				return connection;
			}
		} catch (XMPPException xe) {
			Log.e("XMPPChatDemoActivity", xe.toString());
		}
		return null;
	}

	private Handler mHandler2 = new Handler();

	private void ReceiveMsgListenerConnection(XMPPConnection connection) {
		this.connection = connection;
		if (connection != null) {
			// Add a packet listener to get messages sent to us
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						final String[] fromName = StringUtils.parseBareAddress(
								message.getFrom()).split("@");
						Log.i("PaintView",
								"PaintView Text Recieved "
										+ message.getBody() + " from "
										+ fromName[0]);

						final String msg = message.getBody().toString();
						// draw the circle on screen
						mHandler2.post(new Runnable() {
							@SuppressLint("NewApi")
							public void run() {
								// notification or chat...
								if (msg.contains("drawView")) {
//									PaintView.this.getOverlay();
								}
							}
						});
					}
				}
			}, filter);
		}
	}
}