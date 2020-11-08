package com.android.bean;

import com.android.PEApplication;
import com.android.model.Message;
import com.android.utils.ALog;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.List;

import static com.android.PEApplication.daoConfig;
import static org.xutils.x.getDb;

@Table(name ="T_Contacts")
public class User {
    @Column(name = "id",isId = true,autoGen = false)
    private int id;
    @Column(name = "login")
    private String login;
    @Column(name = "name")
    private String name;
    @Column(name = "sex")
    private String sex;
    @Column(name = "headPortraitsUrl")
    private String headPortraitsUrl;
    @Column(name = "remark")
    private String remark;
    @Column(name = "pinyin")
    private String pinyin;
    @Column(name = "firstLetter")
    private String firstLetter;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getHeadPortraitsUrl() {
        return headPortraitsUrl;
    }

    public void setHeadPortraitsUrl(String headPortraitsUrl) {
        this.headPortraitsUrl = headPortraitsUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public void insert(){
        try {
            getDb(daoConfig).saveBindingId(this);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    public void update() {
        try {
            getDb(daoConfig).saveOrUpdate(this);
        } catch (DbException e) {
            ALog.e(e);
        }
    }
    public void delete(String login) {
        try {
            getDb(daoConfig).delete(User.class, WhereBuilder.b("login", "=", login));

        } catch (DbException e) {
            ALog.e(e);
        }
    }
    public static void deleteAll() {
        try {
            getDb(daoConfig).delete(User.class);

        } catch (DbException e) {
            ALog.e(e);
        }
    }
    /**
     * 获取所有联系人
     * @return
     * @throws DbException
     */
    public static List<User> getAll() throws DbException {
        return x.getDb(daoConfig).selector(User.class).findAll();
    }
    public static User findUser(String fromUser) {
        try {
            return x.getDb(daoConfig).selector(User.class)
                    .where(WhereBuilder.b("login", "=", fromUser)).findFirst();
        } catch (DbException e) {
            e.printStackTrace();
        }
        return null;
    }
}
