package com.qingqing.base.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by Wangxiaxin on 2015/12/21.
 *
 * 基础数据
 * 
 * 城市数据，年级，科目 的统一保存
 *
 *
 * ---------------版本变更记录--------------------
 * 
 * 1 ==> 创建db，保存城市列表，年级和科目信息
 *
 * 2 ==> 增加 CityDistrict since v4.9.0
 *
 * 3 ==> 增加 小贴士 数据结构使用MessageInfo的几个字段, 消息（DB_TABLE_MQ_MSG）存储移到base since v5.0
 *
 * 4 ==> 增加 批量推送 数据结构使用MessageInfo的字段，展示和小贴士一样(主要区别是针对不同用户推送)
 *
 * 5 ==> 增加 统计相关的表 DB_TABLE_USER_BEHAVIOR_LOG
 *
 * 6 ==> 增加 二级科目的表
 *
 * 7 ==> 增加年级的simple name
 *
 * 8 ==> 增加年级分组的表 DB_TABLE_GRADE_GROUP
 *
 * 9 ==> 城市表DB_TABLE_CITY增加字段 city_is_test 表示是否是测试城市
 *
 * 10 ==> 年级列表DB_TABLE_GRADE增加字段 group_type
 *
 * 11 ==> DB_TABLE_USER_BEHAVIOR_LOG独立出来
 *
 * 12 ==> 增加新的记录首页评价弹窗出现过的老师的表
 *
 * 13 ==> 新增积分表
 *
 * 14 == > 城市表DB_TABLE_CITY增加字段 city_is_visible 和  city_is_stationed 表示是否是驻点城市
 *
 * 15 == > 新增老师指标表
 */
public class DefaultDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "default.db";
    private static final int DATABASE_VERSION = 15;

    public static final String DB_TABLE_CITY = "t_city_list";
    public static final String DB_TABLE_CITY_KEY_CODE = "city_code";
    public static final String DB_TABLE_CITY_KEY_ALPHA = "city_alpha";
    @Deprecated
    public static final String DB_TABLE_CITY_KEY_ISOPEN = "city_isopen";
    public static final String DB_TABLE_CITY_KEY_ISVISIBLE = "city_isvisible";
    public static final String DB_TABLE_CITY_KEY_ISSTATIONED = "city_isstationed";
    public static final String DB_TABLE_CITY_KEY_IS_TEST = "city_is_test";

    public static final String DB_TABLE_GRADE = "t_grade_list";
    public static final String DB_TABLE_COURSE = "t_course_list";
    public static final String DB_TABLE_KEY_ID = "id";
    public static final String DB_TABLE_KEY_NAME = "name";
    public static final String DB_TABLE_KEY_SIMPLE_NAME = "simple_name";
    public static final String DB_TABLE_KEY_GRADE_GROUP_NAME = "group_name";
    public static final String DB_TABLE_KEY_GRADE_GROUP_TYPE = "group_type";

    public static final String DB_TABLE_GRADE_COURSE = "t_grade_course_list";
    public static final String DB_TABLE_KEY_GRADE_ID = "grade_id";
    public static final String DB_TABLE_KEY_COURSE_ID = "course_id";

    public static final String DB_TABLE_CITY_DISTRICT = "t_city_district";
    public static final String DB_TABLE_KEY_DISTRICT_ID = "district_id";
    public static final String DB_TABLE_KEY_DISTRICT_NANE = "district_name";
    public static final String DB_TABLE_KEY_DISTRICT_ISOPEN = "is_open";
    public static final String DB_TABLE_KEY_CITY_ID = "city_id";

    public static final String DB_TABLE_TIPS = "t_tips";
    public static final String DB_TABLE_KEY_TIP_ID = "tip_id";
    public static final String DB_TABLE_KEY_TIP_TYPE = "tip_type";
    public static final String DB_TABLE_KEY_TIP_BID = "tip_bid";
    public static final String DB_TABLE_KEY_TIP_TITLE = "tip_title";
    public static final String DB_TABLE_KEY_TIP_CONTENT = "tip_content";
    public static final String DB_TABLE_KEY_TIP_TIME = "tip_time";
    public static final String DB_TABLE_KEY_TIP_UNREAD = "tip_unread";
    public static final String DB_TABLE_KEY_TIP_LINK_URL = "tip_link_url";

    public static final String DB_TABLE_MQ_MSG = "t_mq_msg";
    public static final String DB_TABLE_KEY_MSG_ID = "msg_id";
    public static final String DB_TABLE_KEY_MSG_BODY = "msg_body";
    public static final String DB_TABLE_KEY_MSG_RECEIVE_TIME = "receive_time";
    public static final String DB_TABLE_KEY_MSG_USER_ID = "user_id";
    public static final String DB_TABLE_KEY_MSG_CLIENT_ID = "client_id";
    public static final String DB_TABLE_KEY_MSG_USER_NAME = "user_name";

    public static final String DB_TABLE_BATCH_TIPS = "t_batch_tips";
    public static final String DB_TABLE_KEY_BATCH_TIP_USER_ID = "batch_tip_user_id";
    public static final String DB_TABLE_KEY_BATCH_TIP_ID = "batch_tip_id";
    public static final String DB_TABLE_KEY_BATCH_TIP_TYPE = "batch_tip_type";
    public static final String DB_TABLE_KEY_BATCH_TIP_BID = "batch_tip_bid";
    public static final String DB_TABLE_KEY_BATCH_TIP_TITLE = "batch_tip_title";
    public static final String DB_TABLE_KEY_BATCH_TIP_CONTENT = "batch_tip_content";
    public static final String DB_TABLE_KEY_BATCH_TIP_TIME = "batch_tip_time";
    public static final String DB_TABLE_KEY_BATCH_TIP_UNREAD = "batch_tip_unread";
    public static final String DB_TABLE_KEY_BATCH_TIP_LINK_URL = "batch_tip_link_url";

    public static final String DB_TABLE_SCORE = "t_score";
    public static final String DB_TABLE_KEY_SCORE_TYPE = "score_type";
    public static final String DB_TABLE_KEY_SCORE_DESCRIPTION = "score_description";
    public static final String DB_TABLE_KEY_SCORE_AMOUNT = "score_amount";
    public static final String DB_TABLE_KEY_SCORE_NAME = "score_name";


    @Deprecated
    public static final String DB_TABLE_USER_BEHAVIOR_LOG = "qq_teacher_user_behavior_log";

    public static final String DB_TABLE_SUB_COURSE = "t_sub_course";
    public static final String DB_TABLE_KEY_SUB_COURSE_ID = "secondary_course_id";
    public static final String DB_TABLE_KEY_SUB_COURSE_NAME = "secondary_course_name";
    public static final String DB_TABLE_KEY_SUB_COURSE_IS_OPEN = "is_open";
    public static final String DB_TABLE_KEY_SUB_COURSE_COURSE_ID = "course_id";

    public static final String DB_TABLE_GRADE_GROUP = "t_grade_group";
    public static final String DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE = "grade_group_type";
    public static final String DB_TABLE_KEY_GRADE_GROUP_GROUP_NAME = "grade_group_name";
    public static final String DB_TABLE_KEY_GRADE_GROUP_GROUP_IS_OPEN = "is_open";

    public static final String DB_TABLE_APPRAISE_TEACHER = "appraise_teacher";
    
    public static final String DB_TABLE_TEACHER_INDEX = "t_teacher_index";
    public static final String DB_TABLE_KEY_QQ_USER_ID = "qq_user_id";
    public static final String DB_TABLE_KEY_SCHOOL_AGE = "school_age";
    public static final String DB_TABLE_KEY_HAS_SCHOOL_AGE = "has_school_age";
    public static final String DB_TABLE_KEY_RETENTION_RATE = "retention_rate";
    public static final String DB_TABLE_KEY_HAS_RETENTION_RATE = "has_retention_rate";
    public static final String DB_TABLE_KEY_AVG_COURSE_TIME = "avg_course_time";
    public static final String DB_TABLE_KEY_HAS_AVG_COURSE_TIME = "has_avg_course_time";
    public static final String DB_TABLE_KEY_UPDATE_TIME = "update_time";
    
    public DefaultDBHelper(Context context) {
        super(context,
                (new File(context.getFilesDir(), DATABASE_NAME)).getAbsolutePath(), null,
                DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        createTableCity(db);
        createTableCourse(db);
        createTableGrade(db);
        createTableGradeCourse(db);

        createTableCityDistrict(db);

        createTableTips(db);
        createTableMsg(db);
        createTableBatchTips(db);
        createTableSubCourse(db);
        createTableGradeGroup(db);
        createTableAppraiseTeacher(db);
        createTableScore(db);
        createTableTeacherIndex(db);
    }

    private void createTableCity(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_ID
                + " INTEGER NOT NULL DEFAULT 0," + DB_TABLE_KEY_NAME
                + " VARCHAR NOT NULL," + DB_TABLE_CITY_KEY_CODE
                + " INTEGER NOT NULL DEFAULT 0," + DB_TABLE_CITY_KEY_ISOPEN
                + " INTEGER NOT NULL DEFAULT 0," + DB_TABLE_CITY_KEY_ISVISIBLE
                + " INTEGER NOT NULL DEFAULT 0," + DB_TABLE_CITY_KEY_ISSTATIONED
                + " INTEGER NOT NULL DEFAULT 0," + DB_TABLE_CITY_KEY_IS_TEST
                + " INTEGER NOT NULL DEFAULT 0," + DB_TABLE_CITY_KEY_ALPHA
                + " VARCHAR NULL)", DB_TABLE_CITY));
    }

    private void createTableCourse(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + DB_TABLE_KEY_ID
                + " INTEGER, " + DB_TABLE_KEY_NAME + " VARCHAR)", DB_TABLE_COURSE));
    }

    private void createTableGrade(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_ID
                + " INTEGER," + DB_TABLE_KEY_NAME + " VARCHAR,"
                + DB_TABLE_KEY_SIMPLE_NAME + " VARCHAR," + DB_TABLE_KEY_GRADE_GROUP_NAME
                + " VARCHAR,"
                + DB_TABLE_KEY_GRADE_GROUP_TYPE + " INTEGER)", DB_TABLE_GRADE));
    }

    private void createTableGradeCourse(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_GRADE_ID
                + " INTEGER," + DB_TABLE_KEY_COURSE_ID + " INTEGER)",
                DB_TABLE_GRADE_COURSE));
    }

    private void createTableCityDistrict(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_TABLE_CITY_DISTRICT
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_DISTRICT_ID
                + " INTEGER," + DB_TABLE_KEY_DISTRICT_NANE + " VARCHAR NOT NULL,"
                + DB_TABLE_KEY_DISTRICT_ISOPEN + " INTEGER NOT NULL DEFAULT 0,"
                + DB_TABLE_KEY_CITY_ID + " INTEGER NOT NULL DEFAULT 0)");
    }

    private void createTableTips(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_TIP_ID
                + " VARCHAR," + DB_TABLE_KEY_TIP_TYPE + " INTEGER,"
                + DB_TABLE_KEY_TIP_BID + " VARCHAR," + DB_TABLE_KEY_TIP_TITLE
                + " VARCHAR," + DB_TABLE_KEY_TIP_CONTENT + " VARCHAR,"
                + DB_TABLE_KEY_TIP_TIME + " INTEGER," + DB_TABLE_KEY_TIP_UNREAD
                + " INTEGER," + DB_TABLE_KEY_TIP_LINK_URL + " VARCHAR)", DB_TABLE_TIPS));
    }

    private void createTableMsg(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_MSG_ID
                + " VARCHAR," + DB_TABLE_KEY_MSG_BODY + " VARCHAR,"
                + DB_TABLE_KEY_MSG_RECEIVE_TIME + " INTEGER," + DB_TABLE_KEY_MSG_USER_ID
                + " VARCHAR," + DB_TABLE_KEY_MSG_CLIENT_ID + " VARCHAR,"
                + DB_TABLE_KEY_MSG_USER_NAME + " VARCHAR)", DB_TABLE_MQ_MSG));
    }

    private void createTableBatchTips(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DB_TABLE_KEY_BATCH_TIP_USER_ID + " VARCHAR,"
                + DB_TABLE_KEY_BATCH_TIP_ID + " VARCHAR," + DB_TABLE_KEY_BATCH_TIP_TYPE
                + " INTEGER," + DB_TABLE_KEY_BATCH_TIP_BID + " VARCHAR,"
                + DB_TABLE_KEY_BATCH_TIP_TITLE + " VARCHAR,"
                + DB_TABLE_KEY_BATCH_TIP_CONTENT + " VARCHAR,"
                + DB_TABLE_KEY_BATCH_TIP_TIME + " INTEGER,"
                + DB_TABLE_KEY_BATCH_TIP_UNREAD + " INTEGER,"
                + DB_TABLE_KEY_BATCH_TIP_LINK_URL + " VARCHAR)", DB_TABLE_BATCH_TIPS));
    }

    private void createTableSubCourse(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + DB_TABLE_KEY_SUB_COURSE_ID
                + " INTEGER," + DB_TABLE_KEY_SUB_COURSE_NAME + " TEXT,"
                + DB_TABLE_KEY_SUB_COURSE_IS_OPEN + " INTEGER,"
                + DB_TABLE_KEY_SUB_COURSE_COURSE_ID + " INTEGER)", DB_TABLE_SUB_COURSE));
    }

    private void createTableGradeGroup(SQLiteDatabase db) {
        db.execSQL(String.format(
                "CREATE TABLE IF NOT EXISTS %s "
                        + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE + " INTEGER,"
                        + DB_TABLE_KEY_GRADE_GROUP_GROUP_NAME + " TEXT,"
                        + DB_TABLE_KEY_GRADE_GROUP_GROUP_IS_OPEN + " INTEGER)",
                DB_TABLE_GRADE_GROUP));
    }

    private void createTableAppraiseTeacher(SQLiteDatabase db){
        db.execSQL(String.format(
                "CREATE TABLE IF NOT EXISTS %s "
                        + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + DB_TABLE_KEY_ID + " VARCHAR)",
                DB_TABLE_APPRAISE_TEACHER));
    }

    private void createTableScore(SQLiteDatabase db){
        db.execSQL(String.format(
                "CREATE TABLE IF NOT EXISTS %s "
                        + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                +DB_TABLE_KEY_SCORE_TYPE + " INTEGER,"
                +DB_TABLE_KEY_SCORE_AMOUNT +" INTEGER,"
                +DB_TABLE_KEY_SCORE_NAME + " VARCHAR,"
                +DB_TABLE_KEY_SCORE_DESCRIPTION + " VARCHAR)",
                DB_TABLE_SCORE
        ));
    }
    
    private void createTableTeacherIndex(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s "
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                
                + DB_TABLE_KEY_QQ_USER_ID + " VARCHAR,"
                
                + DB_TABLE_KEY_SCHOOL_AGE + " INTEGER,"
                
                + DB_TABLE_KEY_HAS_SCHOOL_AGE + " INTEGER NOT NULL DEFAULT 0,"
                
                + DB_TABLE_KEY_RETENTION_RATE + " VARCHAR,"
                
                + DB_TABLE_KEY_HAS_RETENTION_RATE + " INTEGER NOT NULL DEFAULT 0,"
                
                + DB_TABLE_KEY_AVG_COURSE_TIME + " VARCHAR,"
                
                + DB_TABLE_KEY_HAS_AVG_COURSE_TIME + " INTEGER NOT NULL DEFAULT 0,"
                
                + DB_TABLE_KEY_UPDATE_TIME + " INTEGER)",
                
                DB_TABLE_TEACHER_INDEX));
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion >= 2 && oldVersion < 2) {
            createTableCityDistrict(db);
        }

        if (newVersion >= 3 && oldVersion < 3) {
            createTableTips(db);
            createTableMsg(db);
        }

        if (newVersion >= 4 && oldVersion < 4) {
            createTableBatchTips(db);
        }

        if (newVersion >= 6 && oldVersion < 6) {
            createTableSubCourse(db);
        }

        if (newVersion >= 7 && oldVersion < 7) {
            try {
                db.execSQL(String.format("ALTER TABLE " + DB_TABLE_GRADE
                        + " ADD %s VARCHAR", DB_TABLE_KEY_SIMPLE_NAME));
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GRADE);
                createTableGrade(db);
            }
        }

        if (newVersion >= 8 && oldVersion < 8) {
            createTableGradeGroup(db);
        }

        if(oldVersion < 9){
            try {
                db.execSQL(String.format("ALTER TABLE " + DB_TABLE_CITY
                        + " ADD %s INTEGER NOT NULL DEFAULT 0", DB_TABLE_CITY_KEY_IS_TEST));
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CITY);
                createTableCity(db);
            }
        }

        if(oldVersion < 10){
            try {
                db.execSQL(String.format("ALTER TABLE " + DB_TABLE_GRADE
                        + " ADD %s INTEGER NOT NULL DEFAULT 0", DB_TABLE_KEY_GRADE_GROUP_TYPE));
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_GRADE);
                createTableCity(db);
            }
        }

        if (oldVersion < 11) {
            db.execSQL(
                    String.format("drop table if exists %s", DB_TABLE_USER_BEHAVIOR_LOG));
        }

        if (oldVersion<12){
            createTableAppraiseTeacher(db);
        }

        if (oldVersion < 13) {
            createTableScore(db);
        }
        
        if (oldVersion < 14) {
            try {
                db.execSQL(String.format(
                        "ALTER TABLE " + DB_TABLE_CITY
                                + " ADD %s INTEGER NOT NULL DEFAULT 0",
                        DB_TABLE_CITY_KEY_ISVISIBLE));
                db.execSQL(String.format(
                        "ALTER TABLE " + DB_TABLE_CITY
                                + " ADD %s INTEGER NOT NULL DEFAULT 0",
                        DB_TABLE_CITY_KEY_ISSTATIONED));
            } catch (Exception e) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_CITY);
                createTableCity(db);
            }
        }
        
        if (oldVersion < 15) {
            createTableTeacherIndex(db);
        }
    }
}
