package easydarwin.android.videostreaming;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import openfire.chat.activity.LoginActivity;
import openfire.chat.adapter.FriendsAdapter;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.videolan.vlc.R;
import org.videolan.vlc.VLCCallbackTask;
import org.videolan.vlc.audio.AudioServiceController;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
public class VideoStreamingFragment extends Fragment implements Callback,
		RtspClient.Callback, android.view.SurfaceHolder.Callback,
		OnClickListener {
	
	private static final int REQUEST_SETTING = 1000;
	// current system info msg
	private static final int msgKey1 = 1;

	private BroadcastReceiver mReceiver;
	private String mAddress;
	private String mPort;
	private String mVideoName;
	protected Session mSession;
	protected RtspClient mClient;

	/** Default quality of video streams. */
	public VideoQuality videoQuality;
	/** By default AMRNB is the audio encoder. */
	public int audioEncoder = SessionBuilder.AUDIO_AMRNB;
	/** By default H.264 is the video encoder. */
	public int videoEncoder = SessionBuilder.VIDEO_H264;
	private static final int mOrientation = 0;
	private Button btnOption;
	private Button btnSelectContact;
	private Button btnStop;
	private Button btnSendMessage;
	private TextView ipView;
	private TextView mTime;
	private boolean alive = false;
	private SurfaceView mSurfaceView;
	private static SurfaceHolder surfaceHolder;
	private SharedPreferences preferences;

	private Pattern pattern = Pattern.compile("([0-9]+)x([0-9]+)");
	public static String username;
	public static String password;
	//private String entries;
	private List<Map<String, String>> friendList;
	
	public static XMPPConnection connection;
	private String streaminglinkTag = "rtsp://129.128.184.46:8554/";
	private static String streaminglink="";
	private String curDateTime;

	/** draw a circle when touch the screen */
	private PaintView paintView;
	 
	private static FragmentActivity faActivity;
	
	public static MultiRoom mRoom;
	private String room = null; // "room3" 


	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		faActivity = (FragmentActivity) super.getActivity();
		
		View v = inflater.inflate(R.layout.streaming_main, container, false);
		// set provider
		configureProviderManager(ProviderManager.getInstance());
		faActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		preferences = PreferenceManager.getDefaultSharedPreferences(faActivity);

		initView(v);
//		curDateTime = new SimpleDateFormat(
//				"yyyy_MMdd_HHmmss").format(Calendar.getInstance().getTime());
//		streaminglink = streaminglinkTag + getDefaultDeviceId()+ curDateTime + ".sdp";
		streaminglink = "rtsp://129.128.184.46:8554/live.sdp";
		
		boolean bParamInvalid = (TextUtils.isEmpty(mAddress)
				|| TextUtils.isEmpty(mPort) || TextUtils.isEmpty(mVideoName));
		if (EasyCameraApp.sState != EasyCameraApp.STATE_DISCONNECTED) {
			setStateDescription(EasyCameraApp.sState);
		}
		if (bParamInvalid) {
			startActivityForResult(new Intent(faActivity, SettingsActivity.class),
					REQUEST_SETTING);
		} else {
//			streaminglink = String.format("rtsp://%s:%d/%s.sdp", mAddress,
//					Integer.parseInt(mPort), mVideoName);
			ipView.setText(String.format("rtsp://%s:%d/%s.sdp", mAddress,
					Integer.parseInt(mPort), mVideoName));
			
		}

		// get message listener
//		ReceiveMsgListenerConnection(connection);
		/** TODO*/
		/** Invitation Listener */
		mRoom.InvitationListener(connection);	
		mRoom.RoomMsgListenerConnection(connection, mRoom.getChatRoom());
//		Log.i("aaaaaaaaaaaaaaaaaa", mRoom.getChatRoom());
		
		
		btnSelectContact.setOnClickListener(this);
		btnOption.setOnClickListener(this);
		btnStop.setOnClickListener(this);
		btnSendMessage.setOnClickListener(this);
		// EditText: set android keyboard enter button as send button
		textMessage.setOnEditorActionListener(new OnEditorActionListener() {	    
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	mRoom.SendMessage(connection, room, textMessage);
		    	return true;
		    }
		});
		
		return v;
	}

	@SuppressWarnings("deprecation")
	public void initView(View v) {
		
		mRoom = new MultiRoom(faActivity);
		
		mAddress = preferences.getString("key_server_address", null);
		mPort = preferences.getString("key_server_port", null);
		mVideoName = preferences.getString("key_device_id",null/*getDefaultDeviceId()*/);
		ipView = (TextView) v.findViewById(R.id.main_text_description);
		mTime = (TextView) v.findViewById(R.id.timeDisplay);
		// draw paint View
		paintView = (PaintView)v.findViewById(R.id.drawView);		
		
		mSurfaceView = (net.majorkernelpanic.streaming.gl.SurfaceView) v.findViewById(R.id.surface);
		mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);
		surfaceHolder = mSurfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// needed
																		// for
																		// sdk<11
		btnSelectContact = (Button) v.findViewById(R.id.btnPlay);
		btnOption = (Button) v.findViewById(R.id.btnOptions);
		btnStop = (Button) v.findViewById(R.id.btnStop);

		//get username & password for [VideoPlayerActivity] reconnect to the  XMPP server
		username = faActivity.getIntent().getStringExtra("username");
		password = faActivity.getIntent().getStringExtra("password");
//		entries = faActivity.getIntent().getStringExtra("entries");

		textMessage = (EditText) v.findViewById(R.id.edit_say_something);
		btnSendMessage = (Button) v.findViewById(R.id.btn_send_message);
		
		// get XMPPConnection if login success
		connection = LoginActivity.connection;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPlay:
			// get all the friends
			friendList = getAllFriendsUser(LoginActivity.connection);

			streaminglink = "rtsp://129.128.184.46:8554/live.sdp";
			
			if (!alive) {
				// popupContactList();
				popupContactList(/*entries*/);

			} else {
				alive = false;
				stopStream();
				String msg = connection.getUser()+"-owner disconnected the connection and left the room!";
				mRoom.SendNotification(connection, room, msg);
				mRoom.departChatRoom(connection, room);
				
				btnSelectContact.setBackgroundResource(R.drawable.play);
				ipView.setText(String.format("rtsp://%s:%d/%s.sdp", mAddress,
						Integer.parseInt(mPort), mVideoName));
			}

			break;
		case R.id.btnOptions:
			Intent intent = new Intent();
			intent.setClass(faActivity, SettingsActivity.class);
			startActivityForResult(intent, REQUEST_SETTING);

			break;
		case R.id.btnStop:
			if (alive) {
				alive = false;
				stopStream();
				String msg = connection.getUser()+"-owner disconnected the connection!";
				mRoom.SendNotification(connection, room, msg);
				btnSelectContact.setBackgroundResource(R.drawable.play);
				ipView.setText(String.format("rtsp://%s:%d/%s.sdp", mAddress,
						Integer.parseInt(mPort), mVideoName));
				mRoom.stopConnection(connection);
			}
			faActivity.finish();

			break;
		case R.id.btn_send_message:
			mRoom.SendMessage(connection, room, textMessage);
			break;
		}
	}


	/**
	 * start video streaming function
	 */
	private void PLAYVideoStreaming() {
		preferences = PreferenceManager.getDefaultSharedPreferences(faActivity);
		
		/**draw a circle when user touch the screen*/
		paintView.setVisibility(View.VISIBLE);
		paintView.setFocusable(true);
		paintView.setOnTouchListener(paintView);
		
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
				// start room message listener in back-end
				mRoom.RoomMsgListenerConnection(connection, mRoom.getChatRoom());
				Log.i("aaaaaaa",mRoom.getChatRoom());
				
				
				if (mSession == null) {// try to load video info directly...
					boolean audioEnable = preferences.getBoolean(
							"p_stream_audio", true);
					boolean videoEnable = preferences.getBoolean(
							"p_stream_video", true);
					audioEncoder = Integer.parseInt(preferences.getString(
							"p_audio_encoder", String.valueOf(audioEncoder)));
					videoEncoder = Integer.parseInt(preferences.getString(
							"p_video_encoder", String.valueOf(videoEncoder)));

					Matcher matcher = pattern.matcher(preferences.getString(
							"video_resolution", "640x480"));
					matcher.find();

					videoQuality = new VideoQuality(Integer.parseInt(matcher
							.group(1)), Integer.parseInt(matcher.group(2)),
							Integer.parseInt(preferences.getString(
									"video_framerate", "24")),
							Integer.parseInt(preferences.getString(
									"video_bitrate", "300")) * 1000);
					mSession = SessionBuilder.getInstance()
							.setContext(faActivity.getApplicationContext())
							.setAudioEncoder(audioEnable ? audioEncoder : 0)
							.setVideoQuality(videoQuality)
							.setAudioQuality(new AudioQuality(8000, 32000))
							.setVideoEncoder(videoEnable ? videoEncoder : 0)
							.setOrigin("127.0.0.0").setDestination(mAddress)
							.setSurfaceView(mSurfaceView)
							.setPreviewOrientation(mOrientation)
							.setCallback(VideoStreamingFragment.this).build();
				}

				if (mClient == null) {
					// Configures the RTSP client
					mClient = new RtspClient();

					String tranport = preferences.getString(
							EasyCameraApp.KEY_TRANPORT, "0");
					if (tranport.equals("0")) {
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
//				mClient.setStreamPath(String.format("/%s.sdp",preferences.getString("key_device_id", Build.MODEL)));
				mClient.setStreamPath(String.format("/%s.sdp",mVideoName));
				
				/**
				 * IMPORTANT, start push stream.*/
				mClient.startStream();
				return 0;
			}

		}.execute();

	}

	/** Add inviation listener in specific class. */
	
	
	
	
	private void stopStream() {
		if (mClient != null) {
			mClient.release();
			mClient.stopStream();
			mClient = null;
		}

		if (mSession != null) {
			mSession.release();
			mSession = null;
		}
		
		paintView.setVisibility(View.GONE);

	}

	/** Time Thread*/
	private class CurrentTimeThread extends Thread {
		@Override
		public void run() {
			do {
				try {
					Thread.sleep(1000);
					android.os.Message msg = new android.os.Message();
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
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case msgKey1:
					String curDateTime = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
					mTime.setText(curDateTime);
					break;

				default:
					break;
				}
			}
		};
	}


	private ArrayList<String> messages = new ArrayList<String>();
	private Handler mHandler = new Handler();
	private ListView listview;
	private EditText textMessage;
	private static Button btn_Send;
	// private Button btn_Cancel;
	private ListView friendlistView;
	private PopupWindow popFriends;
	private static PopupWindow popStreamingLink;
	private FriendsAdapter friendsAdapter;

	private ArrayList<String> selectedListMap = new ArrayList<String>();

	//
	private void MessageAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(faActivity,
				R.layout.listitem, messages);
		listview.setAdapter(adapter);
	}

	// Select Fiends to Share the video
	@SuppressWarnings("deprecation")
	private void popupContactList(/*String entries*/) {

		final View v = faActivity.getLayoutInflater().inflate(R.layout.friendlist, null,
				false);
		int h = faActivity.getWindowManager().getDefaultDisplay().getHeight();
		int w = faActivity.getWindowManager().getDefaultDisplay().getWidth();

		//friendList = getFriendsList(entries);

		popFriends = new PopupWindow(v, w - 10, (int) (((2.8) * h) / 4));
		popFriends.setAnimationStyle(R.style.MyDialogStyleBottom);
		popFriends.setFocusable(true);
		popFriends.setBackgroundDrawable(new BitmapDrawable());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				popFriends.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			}
		}, 1000L);

		friendlistView = (ListView) v.findViewById(R.id.friendlist);
		friendlistView.setItemsCanFocus(true);
		friendsAdapter = new FriendsAdapter(faActivity, friendList);
		
		friendlistView.setAdapter(friendsAdapter);
		friendlistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				// TODO Auto-generated method stub
				// TextView name = (TextView)
				// v.findViewById(R.id.friend_username);
				CheckBox checkbox = (CheckBox) v.findViewById(R.id.check_box);
				checkbox.toggle();

				friendsAdapter.getIsSelected().put(position,
						checkbox.isChecked());

				if (checkbox.isChecked()) {
					selectedListMap.add(friendList.get(position)
							.get("username"));

				}
			}
		});

		btn_Send = (Button) v.findViewById(R.id.btn_play);
		btn_Send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				curDateTime = new SimpleDateFormat(
						"yyyy_MMdd_HHmmss").format(Calendar.getInstance().getTime());
				room = "room" + curDateTime;
				mRoom.setChatRoom(room);
				Log.i("MULTIROOM-ROOM",room);
				
				// START TO PUSH VIDEO
				PLAYVideoStreaming();
				Log.i("PLAY", "following should be streainglink====");
				if (popFriends != null)
					popFriends.dismiss();
				// SEND VIDOE NOTIFICATION TO SELECTED FRIENDS
				mHandler.post(new Runnable() {
					public void run() {
						Message msg = new Message(room + "@conference.myria",
								Message.Type.groupchat);
						msg.setBody(streaminglink);
						if (connection != null ) 
							connection.sendPacket(msg);
					}
				});			
				// CREATE CHAT ROOM AND INVITE SELECTED FRIENDS TO JOIN
				if(selectedListMap.size() > 0){		
					try {
						if(mRoom.createMultiUserRoom(connection, room))
							mRoom.inviteToChatRoom(connection, room, selectedListMap);
						Log.i("INVITEROOM","success!");
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}

				}
		});
		Button btn_send_cancel = (Button) v.findViewById(R.id.btn_send_cancel);
		btn_send_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				popFriends.dismiss();
			}
		});
	}


	@SuppressWarnings("deprecation")
	public static void popupReceiveStreamingLinkMessage(String message) {

		final View v = faActivity.getLayoutInflater().inflate(R.layout.streaminglink,
				null, false);

		int h = faActivity.getWindowManager().getDefaultDisplay().getHeight();
		int w = faActivity.getWindowManager().getDefaultDisplay().getWidth();

		popStreamingLink = new PopupWindow(v, w - 10, 3 * h / 4);
		popStreamingLink.setAnimationStyle(R.style.MyDialogStyleBottom);
		popStreamingLink.setFocusable(true);
		popStreamingLink.setBackgroundDrawable(new BitmapDrawable());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				popStreamingLink.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			}
		}, 1000L);

		TextView stramingLink = (TextView) v.findViewById(R.id.streaming_link);
		stramingLink.setText(message);
		btn_Send = (Button) v.findViewById(R.id.btn_play_streaming);
		btn_Send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				/** DO PLAYING */
                /* Start this in a new thread as to not block the UI thread */
                VLCCallbackTask task = new VLCCallbackTask(faActivity){
                    @Override
                    public void run() {
                      AudioServiceController audioServiceController = AudioServiceController.getInstance();
                      // use audio as default player...
                      audioServiceController.load(streaminglink, false);
                    }
                };
                task.execute();
            
				popStreamingLink.dismiss();
			}
		});
		Button btn_cancel = (Button) v.findViewById(R.id.btn_cancle);
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				popStreamingLink.dismiss();
			}
		});
	}


	@Override
	public void onDestroy() {
		super.onDestroy();	
		stopStream();
		mRoom.stopConnection(connection);
	}
	@Override
	public void onPause() {
		super.onPause();
		stopStream();
		mRoom.stopConnection(connection);
	}


	private void setStateDescription(byte state) {

		switch (state) {
		case EasyCameraApp.STATE_DISCONNECTED:
			ipView.setText(null);
			break;
		case EasyCameraApp.STATE_CONNECTED:
			ipView.setText(String.format(
					"Input this URL in VLC player:\nrtsp://%s:%d/%s.sdp",
					mAddress, mPort, mVideoName));
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
			if (bitrate / 1000 < 250)
//				ipView.setText("	" + bitrate / 1000 + " kbps");
				ipView.setText(" The current network is not stable !");
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
			mClient.stopStream();
			mClient = null;
		}

		if (mSession != null) {
			mSession.release();
			mSession.stop();
			mSession = null;
		}
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {// Configures the
		// SessionBuilder

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
								mAddress, Integer.parseInt(mPort), mVideoName));
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
						ConnectivityManager connManager = (ConnectivityManager) faActivity.getSystemService(faActivity.CONNECTIVITY_SERVICE);
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
									mVideoName));
						}
					}
				}
			}

		};

		ConnectivityManager cm = (ConnectivityManager) faActivity.getSystemService(faActivity.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(faActivity);

			mAddress = pref.getString("key_server_address", null);
			mPort = pref.getString("key_server_port", null);
			mVideoName = pref.getString("key_device_id", null);
			boolean bParamInvalid = (TextUtils.isEmpty(mAddress)
					|| TextUtils.isEmpty(mPort) || TextUtils
					.isEmpty(mVideoName));
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

	public String getDefaultDeviceId() {
		return Build.MODEL.replaceAll(" ", "_");
	}
	

	/**
	 * Configure the provider manager
	 * @param pm
	 */
	public void configureProviderManager(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

//		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
//				new IBBProviders.Open());
//
//		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
//				new IBBProviders.Close());
//
//		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
//				new IBBProviders.Data());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
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

	
	private List<Map<String, String>> getAllFriendsUser(XMPPConnection connection){
		if(connection ==null)
			try {
				connection =this.connection;
				connection.connect();
			} catch (XMPPException e) {
				Log.e("SECOND_GETUSERS","connection=null");
				e.printStackTrace();
			}
		friendList = new ArrayList<Map<String, String>>();
		Roster roster11 = connection.getRoster();
		Collection<RosterEntry> entries11 = roster11.getEntries();
		
		for (RosterEntry entry : entries11) {
			Presence presence = roster11.getPresence(entry.getUser()); 
			Map<String, String> map = new HashMap<String, String>();
			if(presence.isAvailable()){
				map.put("status", "online");
				Log.i("VideoStreaming",entry.getUser() + "--online");
	        }else{
	        	map.put("status", "offline");
	        	Log.i("VideoStreaming",entry.getUser() + "--offline");
	        }

			map.put("name", entry.getName());
			map.put("username", entry.getUser());
//			Log.i("status",presence.getStatus());
			
			friendList.add(map);
		}
		
		return friendList;
	}

}

