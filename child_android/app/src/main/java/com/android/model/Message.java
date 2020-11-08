package com.android.model;

import com.android.utils.ALog;
import com.android.PEApplication;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.Serializable;
import java.util.List;

import static com.android.PEApplication.daoConfig;
import static org.xutils.x.getDb;

/**
 * 消息
 */
@Table(name = "T_ChatMessage")
public class Message implements Serializable {

    /**
     * Column 必须要  不要的话就忽略了，不存入数据库
     * name = "id"：数据库表中的一个字段
     * isId = true：是否是主键
     * autoGen = true：是否自动增长
     * property = "NOT NULL"：添加约束
     */
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "sendUserLogin")
    private String sendUserLogin;
    @Column(name = "acceptUserLogin")
    private String acceptUserLogin;
    @Column(name = "sendTime")
    private String sendTime;
    @Column(name = "content")
    private String content;
    @Column(name = "type")
    private int type;
    @Column(name = "isRead")
    private boolean isRead;// 是否已读 接受的消息才有
    @Column(name = "isSend")
    private boolean isSend;// 是否是发送的
    @Column(name = "sendSuccess")
    private boolean sendSuccess;// 是否发送成功 发送的消息才有
    @Column(name = "sendState")
    private int sendState;//状态 0发送中 1发送成功 2发送失败
   // private int sendState;//发送状态 0发送中 1发送成功 2发送失败 3不是好友
    public Message() {
        super();
    }

    public Message(int type, String sendUserLogin, String acceptUserLogin, String content, String sendTime) {
        this.type = type;
        this.sendUserLogin = sendUserLogin;
        this.acceptUserLogin = acceptUserLogin;
        this.sendTime = sendTime;
        this.content = content;
    }

    //get set方法必须写
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSendUserLogin() {
        return sendUserLogin;
    }

    public void setSendUserLogin(String sendUserLogin) {
        this.sendUserLogin = sendUserLogin;
    }

    public String getAcceptUserLogin() {
        return acceptUserLogin;
    }

    public void setAcceptUserLogin(String acceptUserLogin) {
        this.acceptUserLogin = acceptUserLogin;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public boolean isSendSuccess() {
        return sendSuccess;
    }

    public void setSendSuccess(Boolean sendSuccess) {
        this.sendSuccess = sendSuccess;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", sendUserLogin='" + sendUserLogin + '\'' +
                ", acceptUserLogin='" + acceptUserLogin + '\'' +
                ", sendTime='" + sendTime + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                '}';
    }

    public void save() {
        try {
            if (getId() == 0) {
                getDb(daoConfig).saveBindingId(this);
            } else {
                getDb(daoConfig).saveOrUpdate(this);
            }
        } catch (DbException e) {
            ALog.e(e);
        }
    }
    public void delete(){
        try {
            getDb(daoConfig).delete(this);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    public static void delete(String fromUser) {
        try {
            String currentUser = PEApplication.INSTANCE.getUserLogin();
            WhereBuilder whereBuilder=WhereBuilder.b("sendUserLogin", "=", currentUser).and("acceptUserLogin", "=", fromUser)
                    .or("sendUserLogin", "=", fromUser).and("acceptUserLogin", "=", currentUser);
            x.getDb(daoConfig).delete(Message.class,whereBuilder);
        } catch (DbException e) {
            ALog.e(e);
        }
    }
    public static List<Message> findByFromUser(String fromUser) throws DbException {
        String currentUser = PEApplication.INSTANCE.getUserLogin();
        return x.getDb(daoConfig).selector(Message.class)
                .where(WhereBuilder.b("sendUserLogin", "=", currentUser).and("acceptUserLogin", "=", fromUser))
                .or(WhereBuilder.b("sendUserLogin", "=", fromUser).and("acceptUserLogin", "=", currentUser))
                .orderBy("sendTime", false).findAll();
    }

    public static void setMessageReadByUser(String fromUser) throws DbException {
        String currentUser = PEApplication.INSTANCE.getUserLogin();
        SqlInfo sql = new SqlInfo();
        sql.setSql("UPDATE T_ChatMessage SET isRead = 1 WHERE isRead = 0 AND (sendUserLogin = '" + currentUser + "' AND acceptUserLogin = '" + fromUser + "') OR (sendUserLogin = '" + fromUser + "' AND acceptUserLogin = '" + currentUser + "')");
        x.getDb(daoConfig).execNonQuery(sql);
    }
    public static long getUnreadMessageFromUser(String fromUser) throws DbException {
        String currentUser = PEApplication.INSTANCE.getUserLogin();
        return x.getDb(daoConfig).selector(Message.class)
                .where("isRead", "=", false)
                .and(WhereBuilder.b("sendUserLogin", "=", currentUser).and("acceptUserLogin", "=", fromUser))
                .or(WhereBuilder.b("sendUserLogin", "=", fromUser).and("acceptUserLogin", "=", currentUser))
                .count();
    }

    public static long getUnreadMessage() throws DbException {
        String currentUser = PEApplication.INSTANCE.getUserLogin();
        return x.getDb(daoConfig).selector(Message.class).where("isRead", "=", false).and("acceptUserLogin", "=", currentUser).count();
    }

}
