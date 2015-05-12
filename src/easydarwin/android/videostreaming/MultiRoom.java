package easydarwin.android.videostreaming;

import java.util.Iterator;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

public class MultiRoom {

	public static XMPPConnection connection;
	static String room = "room2";
	public static void joinRoom() throws XMPPException{  
        MultiUserChat multiUserChat = new MultiUserChat(connection, room + "@conference.myria");  
        multiUserChat.join("tiger", "");  
        multiUserChat.invite("admin@myria", room);
        multiUserChat.sendMessage("send message successful");// 发送消息  
          
        System.out.println(multiUserChat.getOccupantsCount());// 聊天室人数  
          
        Iterator<String> it = multiUserChat.getOccupants();  
        while(it.hasNext()){  
            // minzujy@conference.127.0.0.1/sushuo1  
            // minzujy@conference.127.0.0.1/guohai  
            System.out.println(StringUtils.parseResource(it.next()));// 聊天室成员名字  
        }  

        multiUserChat.addMessageListener(new PacketListener() {  
            @Override  
            public void processPacket(Packet packet) {  
                Message message = (Message)packet;  
                //接收来自聊天室的聊天信息  
                System.out.println("收到聊天室消息=>" + StringUtils.parseResource(message.getFrom()) + ": "+message.getBody());  
            }  
        });  
          
        multiUserChat.addParticipantStatusListener(new ParticipantStatusListener() {  
              
            @Override  
            public void voiceRevoked(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void voiceGranted(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void ownershipRevoked(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void ownershipGranted(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void nicknameChanged(String participant, String newNickname) {  
                // TODO Auto-generated method stub  
                System.out.println(StringUtils.parseResource(participant) + " is now known as "+ newNickname +".");  
            }  
              
            @Override  
            public void moderatorRevoked(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void moderatorGranted(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void membershipRevoked(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void membershipGranted(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void left(String participant) {  
                // TODO Auto-generated method stub  
                System.out.println(StringUtils.parseResource(participant) + " has left the room.");  
            }  
              
            @Override  
            public void kicked(String participant, String actor, String reason) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void joined(String participant) {  
                // TODO Auto-generated method stub  
                System.out.println(StringUtils.parseResource(participant) + " has joined the room.");  
            }  
              
            @Override  
            public void banned(String participant, String actor, String reason) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void adminRevoked(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
              
            @Override  
            public void adminGranted(String participant) {  
                // TODO Auto-generated method stub  
                  
            }  
        });  
          
        MultiUserChat.addInvitationListener(connection, new InvitationListener() {  
            
            @Override  
            public void invitationReceived(org.jivesoftware.smack.Connection conn, String room,  
                    String inviter, String reason, String password, Message message) {  
                MultiUserChat multiUserChat = new MultiUserChat(conn, room);  
                System.out.println("收到来自 "+inviter+" 的聊天室邀请。邀请附带内容："+reason);  
                try {  
                    multiUserChat.join("HMM", password);  
                } catch (XMPPException e) {  
                    System.out.println("加入聊天室失败");  
                    e.printStackTrace();  
                }  
                System.out.println("成功加入聊天室");  
                multiUserChat.addMessageListener(new PacketListener() {  
                    @Override  
                    public void processPacket(Packet packet) {  
                        Message message = (Message)packet;  
                        //接收来自聊天室的聊天信息  
                        System.out.println(message.getFrom() + ":" + message.getBody());  
                    }  
                });  
            }  
        });  
    } 

}
