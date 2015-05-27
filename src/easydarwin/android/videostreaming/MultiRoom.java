package easydarwin.android.videostreaming;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class MultiRoom {
	
	private Activity context;
	private String room1 = null;
	
	public String rooom;
	
	public void setChatRoom(String rooom){
//		String curDateTime = new SimpleDateFormat(
//				"yyyy_MMdd_HHmmss").format(Calendar.getInstance().getTime());
//		rooom = "room" + curDateTime;
		this.rooom = rooom;
	}
	public String getChatRoom(){
		return rooom;
	}
	
	public MultiRoom(Activity context){
		this.context = context;
	}
	
	public boolean createMultiUserRoom(XMPPConnection connection,
			String roomName/*, ArrayList<String> friendlist*/) throws XMPPException {
		if(connection==null){
			connection.connect();
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
        //submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);  	        
        submitForm.setAnswer("muc#roomconfig_enablelogging", true);  
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);  
        submitForm.setAnswer("x-muc#roomconfig_registration", true);  
        
		muc.sendConfigurationForm(submitForm);

		Log.i("CREATE_ROOM", roomName);
		return true;

	}
	
	/** Join a chat room
	 * @throws XMPPException */
	public boolean joinChatRoom(XMPPConnection connection, String roomName) throws XMPPException {
		if(connection!=null){
			// Get the MultiUserChatManager
			// Create a MultiUserChat using an XMPPConnection for a room
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@conference.myria");
	
			muc.join(connection.getUser());
			Log.i("JOIN-USER-NAME",connection.getUser());
			
//			InvitationListener(connection);
			// receive chat room message
			muc.addMessageListener(new PacketListener() {  
                @Override  
                public void processPacket(Packet packet) {
					Message message = (Message) packet;
					 Log.i("MULTI-ROOM-CHAT RECEIVE-MESSAGE: ", message.getFrom() + ":" + message.getBody());
					 
					if (message.getBody() != null) {
						final String[] fromName = StringUtils.parseBareAddress(
								message.getFrom()).split("@");
						Log.i("XMPPChatDemoActivity", "Text Recieved "
								+ message.getBody() + " from " + fromName[0]);

						final String msg = message.getBody().toString();
						mHandler.post(new Runnable() {
							@SuppressLint("NewApi")
							public void run() {
								// notification or chat...
								if (msg.contains("rtsp://129.128.184.46:8554/")/*equals(streaminglink)*/)	
									VideoStreamingFragment.popupReceiveStreamingLinkMessage(msg);
								else  if(msg.contains("drawView")){

								}else
									Toast.makeText(context, fromName[0] + ": " + msg,
											Toast.LENGTH_SHORT).show();
							}
						});
					}
				}  
	        });  
			return true;
		}
		else
			return false;

	}
	
	/** Invite users to a chat room
	 * @throws XMPPException */
	public boolean inviteToChatRoom(XMPPConnection connection, String roomName, ArrayList<String> friendsList) throws XMPPException {

		if(connection != null){
			// Get the MultiUserChatManager
			// Create a MultiUserChat using an XMPPConnection for a room
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@conference.myria");
			muc.join(connection.getUser()+"-owner");
			muc.addInvitationRejectionListener(new InvitationRejectionListener(){
	
				@Override
				public void invitationDeclined(String invitee, String reason) {
					// TODO Auto-generated method stub
					Log.i("muc reject", "invitee:"+invitee+"=="+"reason"+reason);
				}
				
			});
			// invite another users
			for(String friend: friendsList){
				Log.i("INVITATION-FRIENDS",friend);
				muc.invite(friend, "Join us "+friend);
			}
		
			return true;
		}else
			return false;
	}
	
	
	private Handler mHandler = new Handler();
	public String InvitationListener(final XMPPConnection connection){
		
		MultiUserChat.addInvitationListener(connection, new InvitationListener(){		
			@Override
			public void invitationReceived(Connection conn, String room,  
                    String inviter, String reason, String password, Message message) {
				room1 = room.split("@")[0];
				rooom = room.split("@")[0];
				setChatRoom(rooom);
				Log.i("RECEIVE_INVITATION", "chat-room:"+room+"/"+"inviter:"+inviter+"/"+"reason:"+reason);
				conn = connection;
				//accepted by default//must be room name without "@conference.myria"
				MultiUserChat multiUserChat = new MultiUserChat(conn, room);    
                try {  
                    multiUserChat.join(connection.getUser()); 
                    Log.i("INVITATION","invite to join success!");  
                } catch (XMPPException e) {  
                    e.printStackTrace();  
                }  
               
                multiUserChat.addMessageListener(new PacketListener() {  
                    @Override  
                    public void processPacket(Packet packet) {
    					Message message = (Message) packet;
    					 Log.i("MULTI-ROOM-CHAT RECEIVE-MESSAGE: ", message.getFrom() + ":" + message.getBody());
    					 
    					if (message.getBody() != null) {
    						final String[] fromName = StringUtils.parseBareAddress(
    								message.getFrom()).split("@");
    						Log.i("XMPPChatDemoActivity", "Text Recieved "
    								+ message.getBody() + " from " + fromName[0]);

    						final String msg = message.getBody().toString();
    						mHandler.post(new Runnable() {
    							@SuppressLint("NewApi")
    							public void run() {
    								// notification or chat...
    								if (msg.contains("rtsp://129.128.184.46:8554/")/*equals(streaminglink)*/)	
    									VideoStreamingFragment.popupReceiveStreamingLinkMessage(msg);
    								else  if(msg.contains("drawView")){
    									/***************
    									Bitmap b = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888);
    									Canvas c = new Canvas(b);
    									paintView.draw(c);
    									paintView.invalidate();
    									 **/	
    									//Toast.makeText(getApplicationContext(),"redraw", Toast.LENGTH_SHORT).show();
    								}else
    									Toast.makeText(context, fromName[0] + ": " + msg,
    											Toast.LENGTH_SHORT).show();
    							}
    						});
    					}
    				}  
                }); 
                
                
              //Receive message listener
    			PacketFilter filter = new MessageTypeFilter(org.jivesoftware.smack.packet.Message.Type.groupchat);
    			connection.addPacketListener(new PacketListener() {
    				@Override
    				public void processPacket(Packet packet) {
    					org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
    					if (message.getBody() != null) {
    						final String[] fromName = StringUtils.parseBareAddress(
    								message.getFrom()).split("@");
    						Log.i("XMPPChatDemoActivity", "VideoPlayer Text Recieved "
    								+ message.getBody() + " from " + fromName[0]);

    						final String msg = message.getBody().toString();
    						// Add the incoming message to the list view
    						mHandler.post(new Runnable() {
    							@SuppressLint("NewApi")
    							public void run() {
    								// notification or chat...						
    								if(msg.contains("drawView")){
    									//Toast.makeText(context,"redraw", Toast.LENGTH_SHORT).show();
    								}else
    									Toast.makeText(context,
    											fromName[0] + ": " + msg, Toast.LENGTH_SHORT).show();
    							}
    						});
    					}
    				}
    			}, filter); 
			}
			
		});
		return room1;
	}
	
	public void ReceiveMsgListenerConnection(XMPPConnection connection) {
		if (connection != null) {
			// Add a packet listener to get messages sent to us
			PacketFilter filter = new MessageTypeFilter(Message.Type.groupchat/*chat*/);
			connection.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						final String[] fromName = StringUtils.parseBareAddress(
								message.getFrom()).split("@");
						Log.i("MultiRoom-PlayerSIDE", "Text Recieved "
								+ message.getBody() + " from " + fromName[0]);

						final String msg = message.getBody().toString();
						mHandler.post(new Runnable() {
							@SuppressLint("NewApi")
							public void run() {
								// notification or chat...
								if(msg.contains("drawView")){

								}else 
									Toast.makeText(context,	fromName[0] + ": " + msg,
											Toast.LENGTH_SHORT).show();
							}
						});
					}
				}
			}, filter);
		}
	}
	public void SendMessage(XMPPConnection connection, String room, EditText textMessage){
		if(room!=null){
		MultiUserChat muc = new MultiUserChat(connection, room);  
		
		String text = textMessage.getText().toString();
		if(!text.equals("")&&text!=null){
			
			Message message = new Message(muc.getRoom()+"@conference.myria",Message.Type.groupchat);  
            message.setBody(text);  
			 
			if (muc != null) {
				try {
					muc.sendMessage(message);
					Log.i("SEND-MSG-TO-ROOM", "Sending text " + text + " to " + muc.getRoom());
				} catch (XMPPException e) {
					e.printStackTrace();
				} 
//				Toast.makeText(faActivity.getApplicationContext(), text,
//						Toast.LENGTH_SHORT).show();
			}
			textMessage.setText("");
		}else{
			Toast.makeText(context, "The input cannot be null!",
					Toast.LENGTH_SHORT).show();
		} 
		}
	}
	
	
	// send notification
	public void SendNotification(XMPPConnection connection, String room, String content){

		MultiUserChat muc = new MultiUserChat(connection, room);  
		Message message = new Message(muc.getRoom()+"@conference.myria", Message.Type.groupchat);  
        message.setBody(content);  
		 
		if (muc != null) {
			try {
				muc.sendMessage(message);
				Log.i("ROOM-NOTIFICATION", "Sending text " + content + " to " + muc.getRoom());
			} catch (XMPPException e) {
				e.printStackTrace();
			} 
//			Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	// for both side call: stop the connection
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
	
	/** Destroy leave the chat room*/
	public boolean departChatRoom(XMPPConnection connection,String room){  
	    boolean result = false;  
	    MultiUserChat multiUserChat = new MultiUserChat(connection, room + "@conference.myria");  
	    if(multiUserChat!=null){  
	    	try {
				multiUserChat.destroy("destroy reason", room + "@conference.myria");
				Log.i("LEAVE_ROOM",connection.getUser()+" Destroy the room");
			} catch (XMPPException e) {
				e.printStackTrace();
			}
	        result = true;  
	    }  
	    
	    return result;    
	}

}
