package easydarwin.android.videostreaming;

import java.util.ArrayList;
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
	private String rooom;
	private Handler mHandler = new Handler();
	
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
			String roomName/*, ArrayList<String> friendlist*/) throws XMPPException {
		if(connection!=null){
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
		}
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
			/*
			// receive chat room message
			muc.addMessageListener(new PacketListener() {  
                @Override  
                public void processPacket(Packet packet) {
					Message message = (Message) packet;		 
					if (message.getBody() != null) {
						
						Log.i("JOIN-MULTI-ROOM-RECEIVE-MESSAGE: ", "Text Recieved "
								+ message.getBody() + " from " + message.getFrom());
						final String[] fromName =message.getFrom().split("@");
						final String msg = message.getBody().toString();
						mHandler.post(new Runnable() {
							@SuppressLint("NewApi")
							public void run() {
								// notification or chat...
								if (msg.contains("rtsp://129.128.184.46:8554/")//equals(streaminglink))	
									VideoStreamingFragment.popupReceiveStreamingLinkMessage(msg);
								else  if(msg.contains("drawView")){
									// to do something
								}else
									Toast.makeText(context, fromName[0] + ": " + msg,
											Toast.LENGTH_SHORT).show();
							}
						});
					}
				}  
	        });  */
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

			// invite another users
			for(String friend: friendsList){
				Log.i("INVITATION-FRIENDS",friend);
				muc.invite(friend, "Join us "+friend);
			}
		
			return true;
		}else
			return false;
	}
	
	/**Invitation Listener */
	public void InvitationListener(final XMPPConnection connection){
		
		MultiUserChat.addInvitationListener(connection, new InvitationListener(){		
			@Override
			public void invitationReceived(Connection conn, String Aroom,  
                    String inviter, String reason, String password, Message message) {
				room1 = Aroom.split("@")[0];
				setChatRoom(room1); // userB.Room = userA.Room if userA.Room!=null
				Log.i("RECEIVE_INVITATION", "chat-room:"+room1+"/"+"inviter:"+inviter);
				conn = connection;
				//accepted by default//must be room name without "@conference.myria"
				MultiUserChat multiUserChat = new MultiUserChat(conn, Aroom);    
                try {  
                    multiUserChat.join(connection.getUser()); 
                    Log.i("INVITATION","invite to join success!");  
                } catch (XMPPException e) {  
                    e.printStackTrace();  
                }  
               // streaming link listener
                multiUserChat.addMessageListener(new PacketListener() {  
                    @Override  
                    public void processPacket(Packet packet) {
    					Message message = (Message) packet;
    					if (message.getBody() != null) {
    						Log.i("INVITATION-MULTI-ROOM RECEIVE MESSAGE:", "Text Recieved "
    								+ message.getBody() + " from " + message.getFrom());
    						final String[] fromName = message.getFrom().split("@");
    						final String msg = message.getBody().toString();
    						mHandler.post(new Runnable() {
    							@SuppressLint("NewApi")
    							public void run() {
    								// notification or chat...
    								if (msg.contains("rtsp://129.128.184.46:8554/")/*equals(streaminglink)*/)	
    									VideoStreamingFragment.popupReceiveStreamingLinkMessage(msg);
    								else
    									Toast.makeText(context,"Invitation ELSE: " + msg,
    											Toast.LENGTH_SHORT).show();
    							}
    						});
    					}
    				}  
                }); 
			}			
		});
	}
	
	/** ROOM message Listener*/
	public void RoomMsgListenerConnection(XMPPConnection connection, String roomName) {

		if (connection != null) {
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
							// notification or chat...	
							Toast.makeText(context,fromName[1]+ ": " + msg, Toast.LENGTH_SHORT).show();
						}
					}); 
                }  
            });  

		}
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
						Log.i("RECEIVE-MESSAGE-LISTENER", "Text Recieved "
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
	/**Send message function*/
	public void SendMessage(XMPPConnection connection, String room, EditText textMessage){
		if(room!=null){
			MultiUserChat muc = new MultiUserChat(connection, room);  
			
			String text = textMessage.getText().toString();
			if(!text.equals("")&&text!=null){
				
				Message message = new Message(room + "@conference.myria",Message.Type.groupchat);  
	            message.setBody(text);  
				if (muc != null) {
					try {
						muc.sendMessage(message);
						Log.i("SEND-MSG-TO-ROOM", "Sending text " + text + " to " + room+"=="+muc.getRoom());
					} catch (XMPPException e) {
						e.printStackTrace();
					} 
				}
				textMessage.setText("");
			}else{
				Toast.makeText(context, "The input cannot be null!",
						Toast.LENGTH_SHORT).show();
			} 
		}else{
			room = getChatRoom();
			Log.i("MULTIROOM-SENDMESSAGE:", "room Name"+room);
			Toast.makeText(context, "Room is null",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/*** send notification */
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
		}	
	}
	
	/*** for both side call: stop the connection */
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
	
	/** Destroy /leave the chat room*/
	public boolean departChatRoom(XMPPConnection connection,String room){  
	    MultiUserChat muc = new MultiUserChat(connection, room+"@conference.myria"); //Must write room- jid  
	    try {
			muc.destroy("destroy reason", room + "@conference.myria");
			Log.i("LEAVE_ROOM",connection.getUser()+" Destroy the room");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return true;    
	}

}
