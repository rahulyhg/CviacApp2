package com.cviac.com.cviac.app.xmpp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cviac.activity.cviacapp.CVIACApplication;
import com.cviac.activity.cviacapp.R;
import com.cviac.activity.cviacapp.XMPPChatActivity;
import com.cviac.com.cviac.app.datamodels.ConvMessage;
import com.cviac.com.cviac.app.datamodels.Conversation;
import com.cviac.com.cviac.app.datamodels.Employee;
import com.cviac.com.cviac.app.fragments.ChatsFragment;
import com.cviac.com.cviac.app.restapis.CVIACApi;
import com.cviac.com.cviac.app.restapis.GeneralResponse;
import com.cviac.com.cviac.app.restapis.UpdateStatusInfo;
import com.google.gson.Gson;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager.AutoReceiptMode;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;
import java.util.Date;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class XMPPClient {

    public boolean connected = false;
    public boolean loggedin = false;
    public boolean isconnecting = false;
    public static boolean isToasted = true;
    private boolean chat_created = false;
    private String serverAddress;
    public static XMPPTCPConnection connection;
    public static String loginUser;
    public static String passwordUser;
    Gson gson;
    XMPPService context;
    public static XMPPClient instance = null;
    public static boolean instanceCreated = false;
    String onlinestatus = "online";
    String offlinestatus = "offline";
    Date onlinestatusdate = new Date();
    //String onlinestatus=onlinestatusdate.toString();
    // String offline=onlinestatusdate.toString();
    String mobile, emp_namelogged;


    public XMPPClient(XMPPService context, String serverAdress, String logiUser,
                      String passwordser) {
        this.serverAddress = serverAdress;
        this.loginUser = logiUser;
        this.passwordUser = passwordser;
        this.context = context;
        init();

    }

    public static XMPPClient getInstance(XMPPService context, String server,
                                         String user, String pass) {

        if (instance == null) {
            instance = new XMPPClient(context, server, user, pass);
            instanceCreated = true;
        }
        return instance;

    }

    public boolean isConnected() {
        return connected;
    }


    public org.jivesoftware.smack.chat.Chat Mychat;

    ChatManagerListenerImpl mChatManagerListener;
    MMessageListener mMessageListener;

    String text = "";
    String mMessage = "", mReceiver = "";

    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");

        } catch (ClassNotFoundException ex) {
            // problem loading reconnection manager
        }
    }

    public void init() {
        gson = new Gson();
        mMessageListener = new MMessageListener(context);
        mChatManagerListener = new ChatManagerListenerImpl();
        initialiseConnection();

    }

    private void initialiseConnection() {

        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                .builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(serverAddress);
        config.setHost(serverAddress);
        config.setPort(5222);
        config.setDebuggerEnabled(true);
        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        connection = new XMPPTCPConnection(config.build());
        XMPPConnectionListener connectionListener = new XMPPConnectionListener();
        connection.addConnectionListener(connectionListener);
    }

    public void disconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connect(final String caller) {

        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected synchronized Boolean doInBackground(Void... arg0) {
                if (connection.isConnected())
                    return false;
                isconnecting = true;
                if (isToasted)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {

//                            Toast.makeText(context,
//                                    caller + "=>connecting....",
//                                    Toast.LENGTH_LONG).show();
                        }
                    });
                Log.d("Connect() Function", caller + "=>connecting....");

                try {
                    connection.connect();
                    DeliveryReceiptManager dm = DeliveryReceiptManager
                            .getInstanceFor(connection);
                    dm.setAutoReceiptMode(AutoReceiptMode.always);
                    dm.addReceiptReceivedListener(new ReceiptReceivedListener() {

                        @Override
                        public void onReceiptReceived(final String fromid,
                                                      final String toid, final String msgid,
                                                      final Stanza packet) {

                        }
                    });
                    connected = true;

                } catch (IOException e) {
                    if (isToasted)
                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "IOException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    Log.e("(" + caller + ")", "IOException: " + e.getMessage());
                } catch (final SmackException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
//                            Toast.makeText(context,
//                                    "(" + caller + ")" + "SMACKException::: " + e.getMessage(),
//                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.e("(" + caller + ")",
                            "SMACKException: " + e.getMessage());
                } catch (XMPPException e) {
                    if (isToasted)

                        new Handler(Looper.getMainLooper())
                                .post(new Runnable() {

                                    @Override
                                    public void run() {

                                        Toast.makeText(
                                                context,
                                                "(" + caller + ")"
                                                        + "XMPPException: ",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    Log.e("connect(" + caller + ")",
                            "XMPPException: " + e.getMessage());

                }
                return isconnecting = false;
            }
        };
        connectionThread.execute();
    }

    public void login() {

        try {
            connection.login(loginUser, passwordUser);
            Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

    }

    private class ChatManagerListenerImpl implements ChatManagerListener {
        @Override
        public void chatCreated(final org.jivesoftware.smack.chat.Chat chat,
                                final boolean createdLocally) {
            if (!createdLocally)
                chat.addMessageListener(mMessageListener);

        }

    }

    public void sendMessage(ChatMessage chatMessage) {
        String body = gson.toJson(chatMessage);
        Mychat = ChatManager.getInstanceFor(connection).createChat(
                chatMessage.receiver + "@"
                        + context.getString(R.string.server),
                mMessageListener);
        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.msgid);
        message.setType(Message.Type.chat);

        try {
            if (connection.isAuthenticated()) {
                Mychat.sendMessage(message);
            } else {
                login();
            }
        } catch (NotConnectedException e) {
            Log.e("xmpp.SendMessage()", "msg Not sent!-Not Connected!");

        } catch (Exception e) {
            Log.e("xmppException",
                    "msg Not sent!" + e.getMessage());
        }

    }

    public void sendAckMessage(ChatMessage chatMessage) {
        chatMessage.ack = 1;
        chatMessage.msg = "";
        String body = gson.toJson(chatMessage);
        Mychat = ChatManager.getInstanceFor(connection).createChat(
                chatMessage.sender + "@"
                        + context.getString(R.string.server),
                mMessageListener);

        final Message message = new Message();
        message.setBody(body);
        message.setStanzaId(chatMessage.msgid);
        message.setType(Message.Type.normal);

        try {
            if (connection.isAuthenticated()) {

                Mychat.sendMessage(message);

            } else {

                login();
            }
        } catch (NotConnectedException e) {
            Log.e("xmpp.SendMessage()", "msg Not sent!-Not Connected!");

        } catch (Exception e) {
            Log.e("xmppException",
                    "msg Not sent!" + e.getMessage());
        }

    }

    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {

            Log.d("xmpp", "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        updateStatus(offlinestatus);
                        //Toast.makeText(context, "ConnectionCLosed!",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.setAction("XMPPConnection");
                        intent.putExtra("status", "DisConnected");
                        context.sendBroadcast(intent);
                    }
                });
            Log.d("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        //Toast.makeText(context, "ConnectionClosedOn Error!!",
                        //        Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setAction("XMPPConnection");
                        intent.putExtra("status", "DisConnected");
                        context.sendBroadcast(intent);

                    }
                });
            Log.d("xmpp", "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {

            Log.d("xmpp", "Reconnectingin " + arg0);

            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {

                        //Toast.makeText(context, "ReconnectionFailed!",
                        //        Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                        //Toast.makeText(context, "REConnected!",
                        //        Toast.LENGTH_SHORT).show();

                    }
                });
            Log.d("xmpp", "ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d("xmpp", "Authenticated!");
            loggedin = true;

            ChatManager.getInstanceFor(connection).addChatListener(
                    mChatManagerListener);

            chat_created = false;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }).start();
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {


                        // TODO Auto-generated method stub
//                        Toast.makeText(context, "Connected!",
//                                Toast.LENGTH_SHORT).show();
                        updateStatus(onlinestatus);

                        Intent intent = new Intent();
                        intent.setAction("XMPPConnection");
                        intent.putExtra("status", "Connected");
                        context.sendBroadcast(intent);
                    }
                });
        }
    }

    private class MMessageListener implements ChatMessageListener {

        public MMessageListener(Context contxt) {
        }

        @Override
        public void processMessage(final org.jivesoftware.smack.chat.Chat chat,
                                   final Message message) {
            Log.i("MyXMPP_MESSAGE_LISTENER", "Xmpp message received: '"
                    + message);

            if (message.getType() == Message.Type.chat
                    && message.getBody() != null) {
                final ChatMessage chatMessage = gson.fromJson(
                        message.getBody(), ChatMessage.class);
                if (chatMessage.ack == 1) {
                    String msgId = chatMessage.msgid;
                    ConvMessage.updateStatus(msgId, 2);
                    updateMessageStatusInUI(msgId, 2);
                } else {
                    processMessage(chatMessage);
                    sendAckMessage(chatMessage);
                }
            }
        }

        private void saveLastConversationMessage(ChatMessage msg) {
            CVIACApplication app = (CVIACApplication) context.getApplication();
            ChatsFragment chatFrag = app.getChatsFragment();
            Conversation cnv = Conversation.getConversation(msg.sender);
            boolean newconv = false;
            if (cnv == null) {
                cnv = new Conversation();
                newconv = true;
            }
            cnv.setEmpid(msg.sender);
            // cnv.setImageurl(conv.getImageurl());
            cnv.setName(msg.senderName);
            cnv.setDatetime(new Date());
            cnv.setLastmsg(msg.msg);
            cnv.save();

        }

        private void updateMessageStatusInUI(final String msgId, final int status) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    CVIACApplication app = (CVIACApplication) context.getApplication();
                    if (app != null) {
                        XMPPChatActivity actv = app.getChatActivty();
                        if (actv != null) {
                            actv.updateMessageStatus(msgId, status);
                        }
                    }
                }
            });
        }

        private void processMessage(final ChatMessage msg) {
            final ConvMessage cmsg = new ConvMessage();
            cmsg.setMsg(msg.msg);
            cmsg.setCtime(new Date());
            //  cmsg.setCtime(msg.ctime);
            cmsg.setMine(false);
            cmsg.setSender(msg.sender);
            cmsg.setSenderName(msg.senderName);
            cmsg.setConverseid(msg.converseid);
            cmsg.setReceiver(msg.receiver);
            cmsg.setMsgid(msg.msgid);
            cmsg.setStatus(-1);

            try {
                cmsg.save();
                saveLastConversationMessage(msg);
            } catch (Exception e) {
            }
            // Chats.chatlist.add(chatMessage);
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    CVIACApplication app = (CVIACApplication) context.getApplication();
                    ChatsFragment chatFrag = app.getChatsFragment();
                    if (chatFrag != null && chatFrag.adapter != null) {
                        chatFrag.reloadConversation();
                    }

                    if (app != null) {
                        XMPPChatActivity actv = app.getChatActivty();
                        if (actv != null) {
                            String convId = actv.getConverseId();
                            if (convId.equalsIgnoreCase(msg.converseid)) {
                                actv.addInMessage(cmsg);
                                return;
                            }
                        }
                    }
                    showMsgNotification(cmsg);

                }
            });
        }

    }

    private void showMsgNotification(ConvMessage cmsg) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.cviac_logo)
                        .setContentTitle(cmsg.getSenderName())
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setContentText(cmsg.getMsg());
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.

        mNotificationManager.notify(0, mBuilder.build());
    }

    public void updateStatus(String status) {
        final String MyPREFERENCES = "MyPrefs";
        SharedPreferences prefs = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        mobile = prefs.getString("mobile", "");
        Employee emplogged = Employee.getemployeeByMobile(mobile);
        emp_namelogged = emplogged.getEmp_code();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apps.cviac.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        CVIACApi api = retrofit.create(CVIACApi.class);
        UpdateStatusInfo statusinfo = new UpdateStatusInfo();
        statusinfo.setEmp_code(emp_namelogged);
        // statusinfo.setStatus(new Date().toString());
        statusinfo.setStatus(status);

        Call<GeneralResponse> call = api.updatestatus(statusinfo);
        call.enqueue(new retrofit.Callback<GeneralResponse>() {
            @Override
            public void onResponse(retrofit.Response<GeneralResponse> response, Retrofit retrofit) {
                GeneralResponse rsp = response.body();
                //Toast.makeText(context, "update status Success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable t) {
                // Toast.makeText(context, "update status failed", Toast.LENGTH_LONG).show();
            }
        });

    }
}