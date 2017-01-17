package com.cviac.com.cviac.app.datamodels;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.Date;
import java.util.List;


@Table(name = "ConvMessages")
public class ConvMessage extends Model {

    @Column(name = "msg")
    private String msg;

    @Column(name = "isIn")
    private boolean isIn;

    @Column(name = "ctime")
    private Date ctime;

    @Column(name = "sender")
    private String from;

    @Column(name = "sendername")
    private String name;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "msgid")
    private String msgid;


    public ConvMessage() {
        super();
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setIn(boolean isIn) {
        this.isIn = isIn;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isIn() {
        return isIn;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public static List<ConvMessage> getAll(String from) {
        return new Select()
                .from(ConvMessage.class)
                .where("sender = ?", from)
                //.orderBy("Name ASC")
                .execute();
    }

    public static List<ConvMessage> getConversations() {
        List<ConvMessage> conversations = SQLiteUtils.rawQuery(ConvMessage.class, "select * from (select * from ChatMessages ORDER BY ctime asc) AS x GROUP BY sender ORDER BY ctime DESC",
                new String[]{});
        return conversations;
    }

//	public static List<ConvMessage> getMessagesFromConversation(int userId, int teamId, String conversationId, boolean isGroupConversation) {
//		List<ConvMessage> messages = new Select().from(ConvMessage.class).where("userId=? AND teamId=? AND conversation_id=? AND is_group_conversation=?", userId, teamId, conversationId, isGroupConversation).orderBy("created_time DESC").execute();
//		return messages;
//	}
//
//	public static void deleteMessages(int teamId) {
//		new Delete().from(ConvMessage.class).where("teamId=?", teamId).execute();
//	}


}