package fs.playground;


import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MucConfigFormManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.jid.util.JidUtil;

import java.time.LocalDateTime;
import java.util.Set;

public class App {

    public static void main(String... args) throws Exception {

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setConnectTimeout(20000)
                .setUsernameAndPassword("admin", "admin")
                .setXmppDomain("changseok-han.com")
                .setHost("localhost")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setPort(3331)
                .setDebuggerEnabled(true)
                .setConnectTimeout(30000)
                .setResource(Resourcepart.from("nickname") + LocalDateTime.now().toString())
                .build();

        XMPPTCPConnection connection = new XMPPTCPConnection(config);
        connection.connect();
        connection.login("admin", "admin");

        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);

        EntityBareJid roomId = JidCreate.entityBareFrom("room@a.changseok-han.com");
        MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(roomId);
        MultiUserChat.MucCreateConfigFormHandle userChat = multiUserChat.create(Resourcepart.from("nick"));

        MucConfigFormManager mucConfigFormManager = userChat.getConfigFormManager();
        mucConfigFormManager.makeMembersOnly();

        Set<Jid> owner = JidUtil.jidSetFrom(
                new String[]{
                        "admin@changseok-han.com"
                }
        );

        mucConfigFormManager.setRoomOwners(owner);
        mucConfigFormManager.submitConfigurationForm();
    }
}
