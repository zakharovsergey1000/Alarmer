package biz.advancedcalendar.greendao;

import biz.advancedcalendar.greendao.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table MESSAGE.
 */
public class Message {

    private Long id;
    private long LocalWorkgroupId;
    private long LocalCreateDateTime;
    private long LocalChangeDateTime;
    private Byte SyncStatus;
    private Long ServerId;
    private boolean Deleted;
    private Long WorkgroupId;
    private long FromUser;
    private Long ToUser;
    private short Type;
    private long Date;
    private short Status;
    private String Text;
    private Long DiscussionId;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient MessageDao myDao;

    private WorkGroup workGroup;
    private Long workGroup__resolvedKey;


    public Message() {
    }

    public Message(Long id) {
        this.id = id;
    }

    public Message(Long id, long LocalWorkgroupId, long LocalCreateDateTime, long LocalChangeDateTime, Byte SyncStatus, Long ServerId, boolean Deleted, Long WorkgroupId, long FromUser, Long ToUser, short Type, long Date, short Status, String Text, Long DiscussionId) {
        this.id = id;
        this.LocalWorkgroupId = LocalWorkgroupId;
        this.LocalCreateDateTime = LocalCreateDateTime;
        this.LocalChangeDateTime = LocalChangeDateTime;
        this.SyncStatus = SyncStatus;
        this.ServerId = ServerId;
        this.Deleted = Deleted;
        this.WorkgroupId = WorkgroupId;
        this.FromUser = FromUser;
        this.ToUser = ToUser;
        this.Type = Type;
        this.Date = Date;
        this.Status = Status;
        this.Text = Text;
        this.DiscussionId = DiscussionId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMessageDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getLocalWorkgroupId() {
        return LocalWorkgroupId;
    }

    public void setLocalWorkgroupId(long LocalWorkgroupId) {
        this.LocalWorkgroupId = LocalWorkgroupId;
    }

    public long getLocalCreateDateTime() {
        return LocalCreateDateTime;
    }

    public void setLocalCreateDateTime(long LocalCreateDateTime) {
        this.LocalCreateDateTime = LocalCreateDateTime;
    }

    public long getLocalChangeDateTime() {
        return LocalChangeDateTime;
    }

    public void setLocalChangeDateTime(long LocalChangeDateTime) {
        this.LocalChangeDateTime = LocalChangeDateTime;
    }

    public Byte getSyncStatus() {
        return SyncStatus;
    }

    public void setSyncStatus(Byte SyncStatus) {
        this.SyncStatus = SyncStatus;
    }

    public Long getServerId() {
        return ServerId;
    }

    public void setServerId(Long ServerId) {
        this.ServerId = ServerId;
    }

    public boolean getDeleted() {
        return Deleted;
    }

    public void setDeleted(boolean Deleted) {
        this.Deleted = Deleted;
    }

    public Long getWorkgroupId() {
        return WorkgroupId;
    }

    public void setWorkgroupId(Long WorkgroupId) {
        this.WorkgroupId = WorkgroupId;
    }

    public long getFromUser() {
        return FromUser;
    }

    public void setFromUser(long FromUser) {
        this.FromUser = FromUser;
    }

    public Long getToUser() {
        return ToUser;
    }

    public void setToUser(Long ToUser) {
        this.ToUser = ToUser;
    }

    public short getType() {
        return Type;
    }

    public void setType(short Type) {
        this.Type = Type;
    }

    public long getDate() {
        return Date;
    }

    public void setDate(long Date) {
        this.Date = Date;
    }

    public short getStatus() {
        return Status;
    }

    public void setStatus(short Status) {
        this.Status = Status;
    }

    public String getText() {
        return Text;
    }

    public void setText(String Text) {
        this.Text = Text;
    }

    public Long getDiscussionId() {
        return DiscussionId;
    }

    public void setDiscussionId(Long DiscussionId) {
        this.DiscussionId = DiscussionId;
    }

    /** To-one relationship, resolved on first access. */
    public WorkGroup getWorkGroup() {
        long __key = this.LocalWorkgroupId;
        if (workGroup__resolvedKey == null || !workGroup__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            WorkGroupDao targetDao = daoSession.getWorkGroupDao();
            WorkGroup workGroupNew = targetDao.load(__key);
            synchronized (this) {
                workGroup = workGroupNew;
            	workGroup__resolvedKey = __key;
            }
        }
        return workGroup;
    }

    public void setWorkGroup(WorkGroup workGroup) {
        if (workGroup == null) {
            throw new DaoException("To-one property 'LocalWorkgroupId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.workGroup = workGroup;
            LocalWorkgroupId = workGroup.getId();
            workGroup__resolvedKey = LocalWorkgroupId;
        }
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}
