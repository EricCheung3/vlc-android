package easydarwin.android.videostreaming;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class MultiRoom {
	
	private Activity context;

	private String rooom;
	
//	private Handler mHandler = new Handler();
	
	public MultiRoom(Activity context){
		this.context = context;
	}

	public void setChatRoom(String rooom){
		this.rooom = rooom;
	}
	public String getChatRoom(){
		return rooom;
	}
	
	
	/**Create chat room*/
	public boolean createMultiUserRoom(XMPPConnection connection,
			String roomName) throws XMPPException {
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("createMultiUserRoom-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		// Get the MultiUserChatManager
		// Create a MultiUserChat using an XMPPConnection for a room
		MultiUserChat muc = new MultiUserChat(connection, roomName + "@conference.myria"); 

		// Create the room
		muc.create(roomName);
		// Get the the room's configuration form
		Form form = muc.getConfigurationForm();
		// Create a new form to submit based on the original form
		Form submitForm = form.createAnswerForm();

		// configure the room 
		List<String> roomOwner = new ArrayList<String>();
		roomOwner.add(connection.getUser());
		submitForm.setAnswer("muc#roomconfig_roomowners", roomOwner);
        submitForm.setAnswer("muc#roomconfig_persistentroom", false);   
        submitForm.setAnswer("muc#roomconfig_membersonly", false);  
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);          
        submitForm.setAnswer("muc#roomconfig_enablelogging", true);  
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);  
        submitForm.setAnswer("x-muc#roomconfig_registration", true);  
        
		muc.sendConfigurationForm(submitForm);
		Log.i("CREATE_ROOM", roomName);
		
		return true;
	}
	
	/** Join a chat room by default
	 * @throws XMPPException */
	public boolean joinChatRoom(XMPPConnection connection, String roomName) throws XMPPException {
		if(connection!=null){
			// Get the MultiUserChatManager
			// Create a MultiUserChat using an XMPPConnection for a room
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@conference.myria");
			muc.join(connection.getUser());
			Log.i("JOIN-USER-NAME",connection.getUser());

			return true;
		}
		else
			return false;
	}
	
	/** Invite users to a chat room
	 * @throws XMPPException */
	public boolean inviteToChatRoom(XMPPConnection connection, String roomName, ArrayList<String> friendsList) throws XMPPException {

		if(connection != null){
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@conference.myria");
			muc.join(connection.getUser()+"-owner");

			// invite another users
			for(String friend: friendsList){
				Log.i("INVITATION-FRIENDS",friend);
				muc.invite(friend, "Join us "+friend);
			}
		
			return true;
		}else
			return false;
	}

	
	/** ROOM message Listener*/
	public void RoomMsgListenerConnection(XMPPConnection connection, String roomName) {

		if(!connection.isConnected()) {
			Log.i("SECOND-MULTIROOM-RoomMsgListenerConnection","connection == null! disconnected");
			try {
				connection.connect();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Add a packet listener to get messages sent to us
		MultiUserChat muc = new MultiUserChat(connection, roomName +"@conference.myria");
		muc.addMessageListener(new PacketListener() {  
            @Override  
            public void processPacket(Packet packet) {  
            	Message message = (Message) packet;
                Log.i("ROOM-CHAT RECEIVE-MESSAGE: ", message.getFrom() + ":" + message.getBody());
                //room3@conference.myria/admin@myria/Smack-owner:dggjjk
                final String[] fromName = message.getFrom().split("/");
                final String msg = message.getBody().toString();
                mHandler.post(new Runnable() {
					@SuppressLint("NewApi")
					public void run() {
						
						/** update UI [ne thread]
						 *  use handler to process it: display message
						 */
						String[] coordination = msg.split(",");
						if (msg.equals("PaintView"))	{
							android.os.Message handlerMsg = new android.os.Message();
							handlerMsg.what = 2;
							handlerMsg.obj = fromName[1]+ ": (" + coordination[1]+","+coordination[2]+")";
							mHandler.sendMessage(handlerMsg);
						}else{
							android.os.Message handlerMsg = new android.os.Message();
							handlerMsg.what = 3;
							handlerMsg.obj = fromName[1]+ ": " + msg;
							mHandler.sendMessage(handlerMsg);
						}
						
//						// notification or chat...	
//						if(msg.equals("PaintView")){
//							String[] coordination = msg.split(",");
//							Toast.makeText(context,fromName[1]+ ": (" + coordination[1]+","+coordination[2]+")", Toast.LENGTH_SHORT).show();
//						}else
//							Toast.makeText(context,fromName[1]+ ": " + msg, Toast.LENGTH_SHORT).show();
					}
				}); 
            }  
            
            @SuppressLint("HandlerLeak")
			private Handler mHandler = new Handler(Looper.getMainLooper()) {
        		
        		@Override
        		public void handleMessage(android.os.Message handlerMsg) {
        			super.handleMessage(handlerMsg);
        			switch (handlerMsg.what) {
        			case 2:
        				Log.i("handlerMsg", handlerMsg.obj.toString() );
        				Toast.makeText(context, handlerMsg.obj.toString(), Toast.LENGTH_SHORT).show();
        				break;
        			case 3:
        				Toast.makeText(context, handlerMsg.obj.toString(), Toast.LENGTH_SHORT).show();
        				break;
        			default:
        				break;
        			}
        		}
        	};
        });  
	}
	
	/**Send message function*/
	public void SendMessage(XMPPConnection connection, String room, String textMessage){
		
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("SendMessage-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		if(room!=null){
			MultiUserChat muc = new MultiUserChat(connection, room);  
//			String text = textMessage.getText().toString();
			if(!textMessage.equals("")&&textMessage!=null){
				
				Message message = new Message(room + "@conference.myria",Message.Type.groupchat);  
	            message.setBody(textMessage);  
				try {
					if (muc != null) {
						muc.sendMessage(message);
						Log.i("SEND-MSG-TO-ROOM", "Sending text " + textMessage + " to " + room+"=="+muc.getRoom());
					}
				} catch (XMPPException e) {
					e.printStackTrace();
				} 
	
//				textMessage.setText("");
			}else{
				Toast.makeText(context, "The input cannot be null!",
						Toast.LENGTH_SHORT).show();
			} 
		}else{
//			room = getChatRoom();
			Log.i("MULTIROOM-SENDMESSAGE:", "room Name"+room);
			Toast.makeText(context, "Please join a Room first",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/*** send notification */
	public void SendNotification(XMPPConnection connection, String room, String content){

		MultiUserChat muc = new MultiUserChat(connection, room);  
		Message message = new Message(muc.getRoom()+"@conference.myria", Message.Type.groupchat);  
        message.setBody(content);  
		 
		try {
			if (muc != null)
				muc.sendMessage(message);
			Log.i("ROOM-NOTIFICATION", "Sending text " + content + " to " + muc.getRoom());
		} catch (XMPPException e) {
			e.printStackTrace();
		} 
	
	}
	
	/**stop XMPPconnection
	 * Do not call it:  */ 
	public void stopConnection(XMPPConnection connection){
		try {
			if (connection!=null){
				connection.disconnect();
				Log.i("STOP","stop connection");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		connection = null;
	}
	
	public void userOffline(XMPPConnection connection){
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("userOffline-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		 Presence presence = new Presence(Presence.Type.unavailable);
	     connection.sendPacket(presence);
	     Log.i("off-line","off-line");
	}
	/** Destroy /leave the chat room*/
	public boolean departChatRoom(XMPPConnection connection,String room){  
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("departChatRoom-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	    MultiUserChat muc = new MultiUserChat(connection, room+"@conference.myria"); //Must write room- jid  
	    try {
			muc.destroy("destroy reason", room + "@conference.myria");
			Log.i("LEAVE_ROOM",connection.getUser()+" Destroy the room");
//			room = null;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return true;    
	}

}
