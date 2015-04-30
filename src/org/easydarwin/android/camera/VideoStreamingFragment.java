package org.easydarwin.android.camera;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.Session.Callback;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtp.RtpThread;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;

import org.videolan.vlc.R;
import org.videolan.vlc.gui.VLCMainActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class VideoStreamingFragment extends Activity implements Callback,
		RtspClient.Callback, android.view.SurfaceHolder.Callback {

	private static final int REQUEST_SETTING = 1000;
	// current system info msg
	private static final int msgKey1 = 1;
	private BroadcastReceiver mReceiver;
	private String mAddress;
	private String mPort;
	private String mDeviceId;
	protected Session mSession;
	protected RtspClient mClient;

	/** Default quality of video streams. */
	public VideoQuality videoQuality;
	/** By default AMRNB is the audio encoder. */
	public int audioEncoder = SessionBuilder.AUDIO_AMRNB;
	/** By default H.264 is the video encoder. */
	public int videoEncoder = SessionBuilder.VIDEO_H264;

	private Button btnOption;
	private Button btnSelectContact;
	private Button btnStop;
	private TextView ipView;
	private TextView mTime;
	private boolean alive = false;
	private SurfaceView mSurfaceView;
	private static SurfaceHolder surfaceHolder;
	private SharedPreferences preferences;
	private WebView myWebView;

	private ImageButton openUrlStreaming;
	
	Pattern pattern = Pattern.compile("([0-9]+)x([0-9]+)");

	VideoStreamingFragment faActivity;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.streaming_main);
		faActivity = VideoStreamingFragment.this;
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//View v = inflater.inflate(R.layout.streaming_main, container, false);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(faActivity);

		// final SharedPreferences preferences =
		// PreferenceManager.getDefaultSharedPreferences(faActivity);

		preferences = PreferenceManager.getDefaultSharedPreferences(faActivity);

		ConnectivityManager cm = (ConnectivityManager) faActivity
				.getSystemService(faActivity.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				pref.edit().putString("bit_rate", "4").commit();
			} else {
				pref.edit().putString("bit_rate", "2").commit();
			}
		}

		mAddress = preferences.getString("key_server_address", null);
		mPort = preferences.getString("key_server_port", null);
		mDeviceId = preferences.getString("key_device_id", null);

		ipView = (TextView) findViewById(R.id.main_text_description);
		mTime = (TextView) findViewById(R.id.timeDisplay);
		EditText editComment1 = (EditText) findViewById(R.id.edit_comment1);
		Button btnComment = (Button) findViewById(R.id.btnComment);

		editComment1.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					// sendMessage();
					System.out.println("senddddddd");
					handled = true;

				}
				return handled;
			}
		});
		// btnComment.setFocusable(true);
		btnComment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// popup comment window
				popupComment();
			}

		});

		/** TODO================================================================ */

		boolean bParamInvalid = (TextUtils.isEmpty(mAddress)
				|| TextUtils.isEmpty(mPort) || TextUtils.isEmpty(mDeviceId));

		if (EasyCameraApp.sState != EasyCameraApp.STATE_DISCONNECTED) {
			setStateDescription(EasyCameraApp.sState);
		}
		if (bParamInvalid) {
			startActivityForResult(new Intent(faActivity,
					SettingsActivity.class), REQUEST_SETTING);
		} else {
			ipView.setText(String.format("rtsp://%s:%d/%s.sdp", mAddress,
					Integer.parseInt(mPort), mDeviceId));
		}

		mSurfaceView = (net.majorkernelpanic.streaming.gl.SurfaceView) findViewById(R.id.surface);

		mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);
		surfaceHolder = mSurfaceView.getHolder();
		surfaceHolder.addCallback(this);
		// needed for sdk<11
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		btnSelectContact = (Button) findViewById(R.id.btnPlay);
		btnSelectContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!alive) {
					popupContactList();
				} else {
					alive = false;
					stopStream();
					btnSelectContact.setBackgroundResource(R.drawable.play);
					ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
							mAddress, Integer.parseInt(mPort), mDeviceId));
				}
			}
		});

		btnOption = (Button) findViewById(R.id.btnOptions);
		btnOption.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// toSeting = true;
				Intent intent = new Intent();
				intent.setClass(faActivity, SettingsActivity.class);
				startActivityForResult(intent, REQUEST_SETTING);
			}
		});

		btnStop = (Button) findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// toSeting = true;
				if (alive) {
					alive = false;
					stopStream();
					btnSelectContact.setBackgroundResource(R.drawable.play);
					ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
							mAddress, Integer.parseInt(mPort), mDeviceId));
				}
				 finish();
			}
		});
//		return v;

		openUrlStreaming = (ImageButton) findViewById(R.id.ml_menu_open_mrl);
		openUrlStreaming.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
	
	// Select contact function
	private Button btn_OK_PLAY;
	private Button btn_Cancel;
	private ListView conctacListView;
	private PopupWindow popContact;
	private ContactAdapter contactAdapter;
	private List<ContactInfo> contactlist;
	private List<ContactInfo> selectedContactlist;

	private void popupContactList() {
		// TODO Auto-generated method stub
		final View v =  getLayoutInflater().inflate(
				R.layout.contact_list, null, false);
		int h =  getWindowManager().getDefaultDisplay()
				.getHeight();
		int w =  getWindowManager().getDefaultDisplay().getWidth();

		contactlist = new ArrayList<ContactInfo>();
		selectedContactlist = new ArrayList<ContactInfo>();

		popContact = new PopupWindow(v, w - 10, 3 * h / 4);
		popContact.setAnimationStyle(R.style.MyDialogStyleBottom);
		popContact.setFocusable(true);
		popContact.setBackgroundDrawable(new BitmapDrawable());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				popContact.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			}
		}, 100L);

		btn_OK_PLAY = (Button) v.findViewById(R.id.btn_OK_PLAY);
		btn_Cancel = (Button) v.findViewById(R.id.btn_Cancel);

		conctacListView = (ListView) v.findViewById(R.id.contactList);
		conctacListView.setItemsCanFocus(true);
		contactAdapter = new ContactAdapter(contactlist,
				VideoStreamingFragment.this);

		conctacListView.setAdapter(contactAdapter);
		conctacListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				// TODO Auto-generated method stub
				TextView name = (TextView) v.findViewById(R.id.contactName);
				CheckBox checkbox = (CheckBox) v.findViewById(R.id.check_box);
				checkbox.toggle();

				contactAdapter.getIsSelected().put(position,
						checkbox.isChecked());

				ContactInfo contact = contactlist.get(position);

				if (checkbox.isChecked()) {
					System.out.println(position + "--"
							+ name.getText().toString() + "--"
							+ contact.getUserNumber());
					selectedContactlist.add(contact);
				}				
			}
		});

		btn_OK_PLAY.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Stream Start...
				PLAY();
				popContact.dismiss();
				// start a new thread
				String message = "rtsp://129.128.184.46:8554/live.sdp";
				SendSMS(selectedContactlist, message);
			}

		});
		btn_Cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popContact.dismiss();
			}

		});

	}

	// start video streaming function
	private void PLAY() {
		preferences = PreferenceManager.getDefaultSharedPreferences(faActivity);

		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected void onProgressUpdate(Void... values) {

				super.onProgressUpdate(values);
				alive = true;
				// start time thread
				new CurrentTimeThread().start();
				btnSelectContact.setBackgroundResource(R.drawable.pause);
			}

			@Override
			protected Integer doInBackground(Void... params) {

				publishProgress();

				if (mSession == null) {// 尝试不用session 直接加载video info
					boolean audioEnable = preferences.getBoolean(
							"p_stream_audio", true);
					boolean videoEnable = preferences.getBoolean(
							"p_stream_video", true);
					audioEncoder = Integer.parseInt(preferences.getString(
							"p_audio_encoder", String.valueOf(audioEncoder)));
					videoEncoder = Integer.parseInt(preferences.getString(
							"p_video_encoder", String.valueOf(videoEncoder)));

					Matcher matcher = pattern.matcher(preferences.getString(
							"video_resolution", "176x144"));
					matcher.find();

					videoQuality = new VideoQuality(Integer.parseInt(matcher
							.group(1)), Integer.parseInt(matcher.group(2)),
							Integer.parseInt(preferences.getString(
									"video_framerate", "15")),
							Integer.parseInt(preferences.getString(
									"video_bitrate", "500")) * 1000);
					mSession = SessionBuilder.getInstance()
							.setContext(faActivity.getApplicationContext())
							.setAudioEncoder(audioEnable ? audioEncoder : 0)
							.setVideoQuality(videoQuality)
							.setAudioQuality(new AudioQuality(8000, 32000))
							.setVideoEncoder(videoEnable ? videoEncoder : 0)
							.setOrigin("127.0.0.0").setDestination(mAddress)
							.setSurfaceView(mSurfaceView)
							.setPreviewOrientation(0)
							.setCallback(VideoStreamingFragment.this).build();
				}

				if (mClient == null) {
					// Configures the RTSP client
					mClient = new RtspClient();

					String tranport = preferences.getString(
							EasyCameraApp.KEY_TRANPORT, "0");
					if ("0".equals(tranport)) {
						mClient.setTransportMode(RtspClient.TRANSPORT_TCP);
					} else {
						mClient.setTransportMode(RtspClient.TRANSPORT_UDP);
					}

					// mClient.setTransportMode(RtspClient.TRANSPORT_TCP);
					mClient.setSession(mSession);
					mClient.setCallback(VideoStreamingFragment.this);
				}

				mClient.setCredentials("", "");
				mClient.setServerAddress(mAddress, Integer.parseInt(mPort));
				mClient.setStreamPath(String.format("/%s.sdp",
						preferences.getString("key_device_id", Build.MODEL)));
				/**
				 * IMPORTANT, start push stream.
				 */
				mClient.startStream();
				return 0;
			}

		}.execute();

	}

	private void SendSMS(List<ContactInfo> Contactlist, String message) {
		final Context context =  getApplicationContext();
		String SENT_SMS_ACTION = "SENT_SMS_ACTION";
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(this
				.getApplicationContext(), 0, sentIntent, 0);
		// register the Broadcast Receivers
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context _context, Intent _intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(context, "send sms success!",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(context, "send sms failure!",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					break;
				}
			}
		}, new IntentFilter(SENT_SMS_ACTION));
		//
		// String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
		// // create the deilverIntent parameter
		// Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
		// PendingIntent deliverPI =
		// PendingIntent.getBroadcast( context, 0,
		// deliverIntent, 0);
		// context.registerReceiver(new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context _context, Intent _intent) {
		// Toast.makeText(context,
		// "收信人已经成功接收", Toast.LENGTH_SHORT)
		// .show();
		// }
		// }, new IntentFilter(DELIVERED_SMS_ACTION));
		if (Contactlist.size() > 0) {
			SmsManager smsManager = SmsManager.getDefault();
			List<String> divideContents = smsManager.divideMessage(message);
			for (String text : divideContents) {
				smsManager.sendTextMessage(Contactlist.get(0).getUserNumber(),
						null, text, sentPI, null);
			}
		}
	}

	// Comment system...
	PopupWindow popComment;

	@SuppressLint("SetJavaScriptEnabled")
	protected void popupComment() {
		// TODO Auto-generated method stub
		final View v =  getLayoutInflater().inflate(
				R.layout.pop_comment1, null, false);
		int h =  getWindowManager().getDefaultDisplay()
				.getHeight();
		int w =  getWindowManager().getDefaultDisplay().getWidth();

		popComment = new PopupWindow(v, w - 10, 3 * h / 4);
		popComment.setAnimationStyle(R.style.MyDialogStyleBottom);
		popComment.setFocusable(true);
		popComment.setBackgroundDrawable(new BitmapDrawable());
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				popComment.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			}

		}, 100L);

		myWebView = (WebView) v.findViewById(R.id.webview);
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.setWebChromeClient(new WebChromeClient());
		myWebView.setWebViewClient(new WebViewClient());
		myWebView.loadUrl("http://129.128.184.46:8080/index.html");

		myWebView.requestFocus(View.FOCUS_DOWN);// popup keyboard
		myWebView.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
				}
				return false;
			}
		});
	}

	private void stopStream() {
		if (mClient != null) {
			mClient.release();
			mClient = null;
		}

		if (mSession != null) {
			mSession.release();
			mSession = null;
		}

		// mSurfaceView.getHolder().removeCallback(MainActivity.this);
		// mSurfaceView.setVisibility(View.GONE);
		// mSurfaceView.setVisibility(View.VISIBLE);
		// mSurfaceView.getHolder().addCallback(MainActivity.this);

		// finish();
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// faActivity.getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		// int id = item.getItemId();
		// if (id == R.id.action_settings) {
		// startActivityForResult(new Intent(faActivity,
		// SettingsActivity.class),
		// REQUEST_SETTING);
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	private void setStateDescription(byte state) {

		switch (state) {
		case EasyCameraApp.STATE_DISCONNECTED:
			ipView.setText(null);
			break;
		case EasyCameraApp.STATE_CONNECTED:
			ipView.setText(String.format(
					"Input this URL in VLC player:\nrtsp://%s:%d/%s.sdp",
					mAddress, mPort, mDeviceId));
			break;
		case EasyCameraApp.STATE_CONNECTING:
			ipView.setText(null);
			break;
		default:
			break;
		}
	}

	@Override
	public void onBitrareUpdate(long bitrate) {
		if (mClient != null) {

			ipView.setText("" + bitrate / 1000 + " kbps");
		}
	}

	@Override
	public void onRtspUpdate(int message, Exception exception) {
		if (message == RtpThread.WHAT_THREAD_END_UNEXCEPTION) {
			faActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					btnSelectContact.setBackgroundResource(R.drawable.pause);
					alive = true;
					stopStream();
					ipView.setText("Disconnect with server，stop transfer");

				}
			});
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mReceiver != null) {
			LocalBroadcastManager.getInstance(faActivity).unregisterReceiver(
					mReceiver);
			mReceiver = null;
		}
		if (mClient != null) {
			mClient.release();
			mClient = null;
		}

		if (mSession != null) {
			mSession.release();
			mSession = null;
		}
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {// Configures the
		// SessionBuilder 需要这个在重新设置吗？

		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (EasyCameraApp.ACTION_COMMOND_STATE_CHANGED.equals(intent
						.getAction())) {
					byte state = intent.getByteExtra(EasyCameraApp.KEY_STATE,
							EasyCameraApp.STATE_DISCONNECTED);
					// setStateDescription(state);

					if (state == EasyCameraApp.STATE_CONNECTED) {
						ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
								mAddress, Integer.parseInt(mPort), mDeviceId));
					}

				} else {
					if ("REDIRECT".equals(intent.getAction())) {
						String location = intent.getStringExtra("location");
						if (!TextUtils.isEmpty(location)) {
							// ======================
						}
					} else if ("PAUSE".equals(intent.getAction())) {
						// ==========================
					} else if (ConnectivityManager.CONNECTIVITY_ACTION
							.equals(intent.getAction())) {
						boolean success = false;
						// get the network connection
						ConnectivityManager connManager = (ConnectivityManager) faActivity
								.getSystemService(faActivity.CONNECTIVITY_SERVICE);
						// State state =
						// connManager.getActiveNetworkInfo().getState();
						State state = connManager.getNetworkInfo(
								ConnectivityManager.TYPE_WIFI).getState();
						if (State.CONNECTED == state) {
							success = true;
						}
						state = connManager.getNetworkInfo(
								ConnectivityManager.TYPE_MOBILE).getState();
						if (State.CONNECTED != state) {
							success = true;
						}
						if (success) {
							// startService(new Intent(MainActivity.this,
							// CommandService.class));
							ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
									mAddress, Integer.parseInt(mPort),
									mDeviceId));
						}
					}
				}
			}

		};

		ConnectivityManager cm = (ConnectivityManager) faActivity
				.getSystemService(faActivity.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(faActivity);

			mAddress = pref.getString("key_server_address", null);
			mPort = pref.getString("key_server_port", null);
			mDeviceId = pref.getString("key_device_id", null);
			boolean bParamInvalid = (TextUtils.isEmpty(mAddress)
					|| TextUtils.isEmpty(mPort) || TextUtils.isEmpty(mDeviceId));
			if (!bParamInvalid) {
				// startService(new Intent(this, CommandService.class));
				//
				// IntentFilter inf = new
				// IntentFilter(EasyCameraApp.ACTION_COMMOND_STATE_CHANGED);
				// inf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
				// inf.addAction("REDIRECT");
				// inf.addAction("PAUSE");
				// LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mReceiver,
				// inf);
				// setStateDescription(EasyCameraApp.sState);
			}
		} else {

			ipView.setText("Network is unavailable,please open the network and try again");
		}

	}

	public class CurrentTimeThread extends Thread {

		@Override
		public void run() {
			do {
				try {
					Thread.sleep(1000);
					Message msg = new Message();
					msg.what = msgKey1;
					mHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (alive);
		}

		@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case msgKey1:
					Calendar cal = Calendar.getInstance();
					String curDateTime = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(cal.getTime());
					mTime.setText(curDateTime);
					break;

				default:
					break;
				}
			}
		};
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreviewStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionConfigured() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// nothing to do

		} else {
			// nothing to do
		}
	}

}
