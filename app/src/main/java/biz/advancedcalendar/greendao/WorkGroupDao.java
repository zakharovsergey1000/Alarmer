package biz.advancedcalendar.greendao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.SqlUtils;
import de.greenrobot.dao.internal.DaoConfig;

import biz.advancedcalendar.greendao.WorkGroup;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table WORK_GROUP.
*/
public class WorkGroupDao extends AbstractDao<WorkGroup, Long> {

    public static final String TABLENAME = "WORK_GROUP";

    /**
     * Properties of entity WorkGroup.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property LocalTaskId = new Property(1, Long.class, "LocalTaskId", false, "LOCAL_TASK_ID");
        public final static Property LocalCreateDateTime = new Property(2, long.class, "LocalCreateDateTime", false, "LOCAL_CREATE_DATE_TIME");
        public final static Property LocalChangeDateTime = new Property(3, long.class, "LocalChangeDateTime", false, "LOCAL_CHANGE_DATE_TIME");
        public final static Property ServerId = new Property(4, Long.class, "ServerId", false, "SERVER_ID");
        public final static Property TaskId = new Property(5, Long.class, "TaskId", false, "TASK_ID");
        public final static Property OwnerId = new Property(6, Long.class, "OwnerId", false, "OWNER_ID");
        public final static Property State = new Property(7, byte.class, "State", false, "STATE");
        public final static Property Name = new Property(8, String.class, "Name", false, "NAME");
    };

    private DaoSession daoSession;


    public WorkGroupDao(DaoConfig config) {
        super(config);
    }
    
    public WorkGroupDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'WORK_GROUP' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'LOCAL_TASK_ID' INTEGER UNIQUE ," + // 1: LocalTaskId
                "'LOCAL_CREATE_DATE_TIME' INTEGER NOT NULL ," + // 2: LocalCreateDateTime
                "'LOCAL_CHANGE_DATE_TIME' INTEGER NOT NULL ," + // 3: LocalChangeDateTime
                "'SERVER_ID' INTEGER," + // 4: ServerId
                "'TASK_ID' INTEGER UNIQUE ," + // 5: TaskId
                "'OWNER_ID' INTEGER," + // 6: OwnerId
                "'STATE' INTEGER NOT NULL ," + // 7: State
                "'NAME' TEXT);"); // 8: Name
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'WORK_GROUP'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, WorkGroup entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long LocalTaskId = entity.getLocalTaskId();
        if (LocalTaskId != null) {
            stmt.bindLong(2, LocalTaskId);
        }
        stmt.bindLong(3, entity.getLocalCreateDateTime());
        stmt.bindLong(4, entity.getLocalChangeDateTime());
 
        Long ServerId = entity.getServerId();
        if (ServerId != null) {
            stmt.bindLong(5, ServerId);
        }
 
        Long TaskId = entity.getTaskId();
        if (TaskId != null) {
            stmt.bindLong(6, TaskId);
        }
 
        Long OwnerId = entity.getOwnerId();
        if (OwnerId != null) {
            stmt.bindLong(7, OwnerId);
        }
        stmt.bindLong(8, entity.getState());
 
        String Name = entity.getName();
        if (Name != null) {
            stmt.bindString(9, Name);
        }
    }

    @Override
    protected void attachEntity(WorkGroup entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public WorkGroup readEntity(Cursor cursor, int offset) {
        WorkGroup entity = new WorkGroup( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // LocalTaskId
            cursor.getLong(offset + 2), // LocalCreateDateTime
            cursor.getLong(offset + 3), // LocalChangeDateTime
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4), // ServerId
            cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5), // TaskId
            cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6), // OwnerId
            (byte) cursor.getShort(offset + 7), // State
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // Name
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, WorkGroup entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setLocalTaskId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setLocalCreateDateTime(cursor.getLong(offset + 2));
        entity.setLocalChangeDateTime(cursor.getLong(offset + 3));
        entity.setServerId(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
        entity.setTaskId(cursor.isNull(offset + 5) ? null : cursor.getLong(offset + 5));
        entity.setOwnerId(cursor.isNull(offset + 6) ? null : cursor.getLong(offset + 6));
        entity.setState((byte) cursor.getShort(offset + 7));
        entity.setName(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(WorkGroup entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(WorkGroup entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getTaskDao().getAllColumns());
            builder.append(" FROM WORK_GROUP T");
            builder.append(" LEFT JOIN TASK T0 ON T.'LOCAL_TASK_ID'=T0.'_id'");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected WorkGroup loadCurrentDeep(Cursor cursor, boolean lock) {
        WorkGroup entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        Task task = loadCurrentOther(daoSession.getTaskDao(), cursor, offset);
        entity.setTask(task);

        return entity;    
    }

    public WorkGroup loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<WorkGroup> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<WorkGroup> list = new ArrayList<WorkGroup>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<WorkGroup> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<WorkGroup> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
