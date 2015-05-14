package easydarwin.android.videostreaming;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

public class MultiRoom {

//	public static XMPPConnection connection;
//	static String room = "room2";
	public MultiRoom(){}
	
	public boolean createMultiUserRoom(XMPPConnection connection,
			String roomName/*, ArrayList<String> friendlist*/) throws XMPPException {

		// Get the MultiUserChatManager
		// Create a MultiUserChat using an XMPPConnection for a room
		MultiUserChat muc = new MultiUserChat(connection, roomName + "@conference.myria");

		// Create the room
		muc.create(roomName);
		// Get the the room's configuration form
		Form form = muc.getConfigurationForm();
		// Create a new form to submit based on the original form
		Form submitForm = form.createAnswerForm();
		// Add default answers to the form to submit
/*			for (Iterator fields = form.getFields(); fields.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// Sets the default value as the answer
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}*/
		// configure the room 
//		List<String> roomOwner = new ArrayList<String>();
//		roomOwner.add(connection.getUser());
//		submitForm.setAnswer("muc#roomconfig_roomowners", roomOwner);
		
        submitForm.setAnswer("muc#roomconfig_persistentroom", false);   
        submitForm.setAnswer("muc#roomconfig_membersonly", false);  
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);  
        //submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);  	        
        submitForm.setAnswer("muc#roomconfig_enablelogging", true);  
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);  
        submitForm.setAnswer("x-muc#roomconfig_registration", false);  
        
		muc.sendConfigurationForm(submitForm);

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
	
			muc.join("newJoinUser");
			// receive chat room message
			muc.addMessageListener(new PacketListener() {  
	            @Override  
	            public void processPacket(Packet packet) {  
	                Message message = (Message)packet;  
	                System.out.println("Receive message from chat room=>" + StringUtils.parseResource(message.getFrom()) + ": "+message.getBody());  
	            }  
	        });  
			return true;
		}
		else
			return false;

	}
	
	/** Invite users to a chat room
	 * @throws XMPPException */
	public boolean inviteToChatRoom(XMPPConnection connection, String roomName) throws XMPPException {

//		if(connection==null)
//			connection.connect();

		// Get the MultiUserChatManager
		// Create a MultiUserChat using an XMPPConnection for a room
		MultiUserChat muc = new MultiUserChat(connection, roomName
				+ "@conference.myria");
		muc.join("join11");
		muc.addInvitationRejectionListener(new InvitationRejectionListener(){

			@Override
			public void invitationDeclined(String invitee, String reason) {
				// TODO Auto-generated method stub
				Log.i("muc reject", "invitee:"+invitee+"=="+"reason"+reason);
			}
			
		});
		// invite another users
		muc.invite(VideoStreamingFragment.to, "test invitation !");
		muc.invite("ali@myria", "test2");
		muc.invite("diego@myria", "test3");
		
		
		return true;
	}
	
	public void InvitationListener(final XMPPConnection connection){
		MultiUserChat.addInvitationListener(connection, new InvitationListener(){

			@Override
			public void invitationReceived(Connection conn, String room,  
                    String inviter, String reason, String password, Message message) {
				/** room = room3@conference.myria
				 *  inviter = tiger@myria
				 *  reason = test invitation !
				 */
				Log.i("RECEIVE_INVITATION", "chat-room:"+room+"/"+"inviter:"+inviter+"/"+"reason:"+reason);
				conn = connection;
				//accepted by default
				MultiUserChat multiUserChat = new MultiUserChat(conn, room);  
                System.out.println("Receive invitation from "+inviter+", and reason："+reason);  
                try {  
                    multiUserChat.join("newJoin"); 
                } catch (XMPPException e) {  
                    Log.e("INVITATION","invite to join failure!");  
                    e.printStackTrace();  
                }  
                Log.i("INVITATION","invite to join success!");  
                multiUserChat.addMessageListener(new PacketListener() {  
                    @Override  
                    public void processPacket(Packet packet) {  
                        Message message = (Message)packet;  
                        Log.i("ROOM-CHAT RECEIVE-MESSAGE: ", message.getFrom() + ":" + message.getBody());
                        //System.out.println(message.getFrom() + ":" + message.getBody());  
                    }  
                });  
			}
			
		});
	}
//	public static void joinRoom() throws XMPPException{  
//        MultiUserChat multiUserChat = new MultiUserChat(connection, room + "@conference.myria");  
//        multiUserChat.join("tiger", "");  
//        multiUserChat.invite("admin@myria", room);
//        multiUserChat.sendMessage("send message successful");// 发送消息  
//          
//        System.out.println(multiUserChat.getOccupantsCount());// 聊天室人数  
//          
//        Iterator<String> it = multiUserChat.getOccupants();  
//        while(it.hasNext()){  
//            // minzujy@conference.127.0.0.1/sushuo1  
//            // minzujy@conference.127.0.0.1/guohai  
//            System.out.println(StringUtils.parseResource(it.next()));// 聊天室成员名字  
//        }  
//
//        multiUserChat.addMessageListener(new PacketListener() {  
//            @Override  
//            public void processPacket(Packet packet) {  
//                Message message = (Message)packet;  
//                //接收来自聊天室的聊天信息  
//                System.out.println("收到聊天室消息=>" + StringUtils.parseResource(message.getFrom()) + ": "+message.getBody());  
//            }  
//        });  
//          
//        multiUserChat.addParticipantStatusListener(new ParticipantStatusListener() {  
//              
//            @Override  
//            public void voiceRevoked(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void voiceGranted(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void ownershipRevoked(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void ownershipGranted(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void nicknameChanged(String participant, String newNickname) {  
//                // TODO Auto-generated method stub  
//                System.out.println(StringUtils.parseResource(participant) + " is now known as "+ newNickname +".");  
//            }  
//              
//            @Override  
//            public void moderatorRevoked(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void moderatorGranted(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void membershipRevoked(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void membershipGranted(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void left(String participant) {  
//                // TODO Auto-generated method stub  
//                System.out.println(StringUtils.parseResource(participant) + " has left the room.");  
//            }  
//              
//            @Override  
//            public void kicked(String participant, String actor, String reason) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void joined(String participant) {  
//                // TODO Auto-generated method stub  
//                System.out.println(StringUtils.parseResource(participant) + " has joined the room.");  
//            }  
//              
//            @Override  
//            public void banned(String participant, String actor, String reason) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void adminRevoked(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//              
//            @Override  
//            public void adminGranted(String participant) {  
//                // TODO Auto-generated method stub  
//                  
//            }  
//        });  
//          
//        MultiUserChat.addInvitationListener(connection, new InvitationListener(){
//
//			@Override
//			public void invitationReceived(Connection conn, String room,  
//                    String inviter, String reason, String password, Message message) {
//
//				Log.i("receive info", "chat-room:"+room+"/"+"inviter:"+inviter+"/"+"reason:"+reason);
//				//accepted by default
//				 MultiUserChat multiUserChat = new MultiUserChat(conn, room);  
//	                System.out.println("Receive invitation from "+inviter+", and reason："+reason);  
//	                try {  
//	                    multiUserChat.join("InvitationDefaultJoin"); 
//	                } catch (XMPPException e) {  
//	                    System.out.println("join failure!");  
//	                    e.printStackTrace();  
//	                }  
//	                System.out.println("join success!");  
//	                multiUserChat.addMessageListener(new PacketListener() {  
//	                    @Override  
//	                    public void processPacket(Packet packet) {  
//	                        Message message = (Message)packet;  
//	                        Log.i("received message from: ", message.getFrom() + ":" + message.getBody());
//	                        System.out.println(message.getFrom() + ":" + message.getBody());  
//	                    }  
//	                });  
//			}
//			
//		});
//    } 
//
//	private void isUserOnline(XMPPConnection connection){
//		
//		Roster roster11 = connection.getRoster();
//		Collection<RosterEntry> entries11 = roster11.getEntries();
//		for (RosterEntry entry : entries11) {
//			Presence presence1 = roster11.getPresence(entry.getUser()); 
//			//user is online or not
//			if(presence1.isAvailable() == true){
////				Log.i("RosterEntry",entry.getUser() + "--online");
//            }else{
////            	Log.i("RosterEntry",entry.getUser() + "--offline");
//            }
//		}	
//	}
//	
//
//	// create a multi-user chat room & invite them to join
//	private boolean createMultiUserRoom(XMPPConnection connection,
//			String roomName, ArrayList<String> friendlist) {
//
//		// Get the MultiUserChatManager
//		// Create a MultiUserChat using an XMPPConnection for a room
//		MultiUserChat muc = new MultiUserChat(connection, roomName
//				+ "@conference.myria");
//
//		try {
//
//			// Create the room
//			muc.create(roomName);
//
//			// Get the the room's configuration form
//			Form form = muc.getConfigurationForm();
//			// Create a new form to submit based on the original form
//			Form submitForm = form.createAnswerForm();
//			// Add default answers to the form to submit
//			for (Iterator fields = form.getFields(); fields.hasNext();) {
//				FormField field = (FormField) fields.next();
//				if (!FormField.TYPE_HIDDEN.equals(field.getType())
//						&& field.getVariable() != null) {
//					// Sets the default value as the answer
//					submitForm.setDefaultAnswer(field.getVariable());
//				}
//			}
//			// Send the completed form (with default values) to the server to
//			// configure the room
//			muc.sendConfigurationForm(submitForm);
//
//			muc.invite("user1@myria", "come baby");
//
//			return true;
//		} catch (XMPPException e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}
//
//	// Add user
//	private boolean addUsers(Roster roster, String userName, String name) {
//		try {
//			roster.createEntry(userName, name, null/*
//													 * roster.getGroup(groupName)
//													 */);
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
//	
//	/**
//	 * Get All the Friends of user
//	 * 
//	 * @param entries
//	 * @return
//	 */
//	private List<Map<String, String>> friendList;
//	private List<Map<String, String>> getFriendsList(String entries) {
//		String[] entryList = entries.split(", ");
//		friendList = new ArrayList<Map<String, String>>();
//		for (String entry : entryList) {
//
//			Map<String, String> map = new HashMap<String, String>();
//			String[] s = entry.toString().split(" ");
//			if (s.length == 2) {
//				map.put("name", s[0].substring(0, s[0].length() - 1));
//				map.put("username", s[1]);
//			} else if (s.length == 3) {
//				map.put("name", s[0].substring(0, s[0].length() - 1));
//				map.put("username", s[1]);
//				map.put("group", s[2].substring(1, s[2].length() - 1));
//			}
//			friendList.add(map);
//		}
//		System.out.println(friendList.toString());
//		return friendList;
//	}
//	/*
//	public class ReceiveMessageThread extends Thread {
//	@Override
//	public void run() {
//		do {
//			// Thread.sleep(1000);
//			android.os.Message msg = new android.os.Message();
//			msg.what = 2;
//			mHandler.sendMessage(msg);
//		} while (messageFlag);
//	}
//
//	@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
//	private Handler mHandler = new Handler() {
//		@Override
//		public void handleMessage(android.os.Message msg) {
//			super.handleMessage(msg);
//			switch (msg.what) {
//			case 2:
//				// get message listener
//				ReceiveMsgListenerConnection(connection);
//				Log.i("ReceiveMessageThread", connection.getHost());
//				break;
//
//			default:
//				break;
//			}
//		}
//	};
//*/
//	
//	/*
//	@SuppressWarnings("rawtypes")
//	private class GetXMPPConnection extends AsyncTask {
//		@Override
//		protected XMPPConnection doInBackground(Object... urls) {
//			try {
//				if (null == connection || !connection.isAuthenticated()) {
//					XMPPConnection.DEBUG_ENABLED = true;
//
//					ConnectionConfiguration config = new ConnectionConfiguration(
//							UserServiceImpl.SERVER_HOST,
//							UserServiceImpl.SERVER_PORT,
//							UserServiceImpl.SERVER_NAME);
//					config.setReconnectionAllowed(true);
//					config.setSendPresence(true);
//					config.setSASLAuthenticationEnabled(true);
//					connection = new XMPPConnection(config);
//					if(!connection.isConnected())
//						connection.connect();
//					//connection.login(username, password);
//					// Set the status to available
//					Presence presence = new Presence(Presence.Type.available);
//					connection.sendPacket(presence);
//					// get all the friends
//					friendList = getAllFriendsUser(connection);
//					// judge user is online or not
//					isUserOnline(connection);
//					// get message listener
//					ReceiveMsgListenerConnection(connection);
//
//				}
//
//				return connection;
//			} catch (XMPPException e) {
//				e.printStackTrace();
//			}
//
//			return null;
//		}
//	}
//*/

}
