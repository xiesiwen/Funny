package com.qingqing.base.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qingqing.api.proto.scoresvc.ScoreProto;
import com.qingqing.api.proto.v1.CityDistrictProto;
import com.qingqing.api.proto.v1.GradeCourseProto;
import com.qingqing.base.bean.City;
import com.qingqing.base.utils.GB2Alpha;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.SparseArray;

/**
 * Created by Wangxiaxin on 2015/12/21.
 * <p/>
 * 基础数据存储
 */
public class DefaultDBDao extends AbsDBDao {
    
    public DefaultDBDao(Context context, String name) {
        super(context, name);
    }
    
    @Override
    public void initDB(Context context) {
        try {
            mDB = new DefaultDBHelper(context).getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }
    
    // ****************** 城市信息 ******************** //
    
    /**
     * 添加开放城市
     */
    private void addCity(final City city) {
        
        execTask(new DBProcessTask<Boolean>(null) {
            
            @Override
            protected Boolean doJob() {
                String alpha = GB2Alpha.getAlpha(city.name);
                boolean exists = isContentExistsInTable(DefaultDBHelper.DB_TABLE_CITY,
                        DefaultDBHelper.DB_TABLE_KEY_ID, String.valueOf(city.id));
                ContentValues cv = new ContentValues();
                cv.put(DefaultDBHelper.DB_TABLE_KEY_ID, city.id);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_NAME, city.name);
                cv.put(DefaultDBHelper.DB_TABLE_CITY_KEY_CODE, city.code);
                cv.put(DefaultDBHelper.DB_TABLE_CITY_KEY_ISOPEN, city.isOpen);
                cv.put(DefaultDBHelper.DB_TABLE_CITY_KEY_ISVISIBLE, city.isVisible);
                cv.put(DefaultDBHelper.DB_TABLE_CITY_KEY_ISSTATIONED, city.isStationed);
                cv.put(DefaultDBHelper.DB_TABLE_CITY_KEY_IS_TEST, city.isTest);
                cv.put(DefaultDBHelper.DB_TABLE_CITY_KEY_ALPHA, alpha);
                if (exists) {
                    mDB.update(DefaultDBHelper.DB_TABLE_CITY, cv,
                            DefaultDBHelper.DB_TABLE_KEY_ID + "=?",
                            new String[] { String.valueOf(city.id) });
                }
                else {
                    mDB.insert(DefaultDBHelper.DB_TABLE_CITY,
                            DefaultDBHelper.DB_TABLE_KEY_NAME, cv);
                }
                return true;
            }
        });
    }
    
    /**
     * 添加城市列表
     */
    public void addCity(List<City> cityList) {
        for (City city : cityList) {
            addCity(city);
        }
    }
    
    public void clearCityList() {
        mDB.execSQL(String.format("DELETE FROM %s", DefaultDBHelper.DB_TABLE_CITY));
    }
    
    /**
     * 获取城市列表
     */
    public ArrayList<City> getCityList() {
        ArrayList<City> cityList = new ArrayList<>();
        Cursor cursor = null;
        try {
            // // TODO: 2017/6/12 add hot params into DB_TABLE_CITY 
            cursor = mDB.query(DefaultDBHelper.DB_TABLE_CITY, null, null, null, null,
                    null, null);
            
            while (cursor.moveToNext()) {
                City city = City.valueOf(
                        cursor.getInt(
                                cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_ID)),
                        cursor.getString(
                                cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_NAME)),
                        cursor.getInt(cursor
                                .getColumnIndex(DefaultDBHelper.DB_TABLE_CITY_KEY_CODE)),
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_ISOPEN)) == 1,
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_ISVISIBLE)) == 1,
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_ISSTATIONED)) == 1,
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_IS_TEST)) == 1);
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return cityList;
    }
    
    /**
     * 获取开放的城市列表
     */
    public ArrayList<City> getCityList(String alpha) {
        ArrayList<City> cityList = new ArrayList<>();
        Cursor cursor = null;
        try {
            
            if (alpha != null && alpha.length() > 0) {
                cursor = mDB.query(DefaultDBHelper.DB_TABLE_CITY, null,
                        DefaultDBHelper.DB_TABLE_CITY_KEY_ISVISIBLE + "=? and "
                                + DefaultDBHelper.DB_TABLE_CITY_KEY_ISSTATIONED
                                + "=? and city_alpha like ?",
                        new String[] { "1", "1", "%" + alpha.toLowerCase() + "%" }, null,
                        null, null);
            }
            else {
                cursor = mDB.query(DefaultDBHelper.DB_TABLE_CITY, null,
                        DefaultDBHelper.DB_TABLE_CITY_KEY_ISVISIBLE + "=? and "
                                + DefaultDBHelper.DB_TABLE_CITY_KEY_ISSTATIONED + "=?",
                        new String[] { "1", "1" }, null, null, null);
            }
            
            while (cursor.moveToNext()) {
                City city = City.valueOf(
                        cursor.getInt(
                                cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_ID)),
                        cursor.getString(
                                cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_NAME)),
                        cursor.getInt(cursor
                                .getColumnIndex(DefaultDBHelper.DB_TABLE_CITY_KEY_CODE)),
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_ISOPEN)) == 1,
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_ISVISIBLE)) == 1,
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_ISSTATIONED)) == 1,
                        cursor.getInt(cursor.getColumnIndex(
                                DefaultDBHelper.DB_TABLE_CITY_KEY_IS_TEST)) == 1);
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return cityList;
    }
    
    // ****************** 二级课程 ******************** //
    
    /**
     * 添加二级科目
     */
    public void addSubCourse(List<GradeCourseProto.SecondaryCourse> secondaryCourseList) {
        for (GradeCourseProto.SecondaryCourse secondaryCourse : secondaryCourseList) {
            addSubCourse(secondaryCourse);
        }
    }
    
    /**
     * 添加二级科目
     */
    private void addSubCourse(final GradeCourseProto.SecondaryCourse secondaryCourse) {
        execTask(new DBProcessTask<Boolean>(null) {
            
            @Override
            protected Boolean doJob() {
                boolean exists = isContentExistsInTable(
                        DefaultDBHelper.DB_TABLE_SUB_COURSE,
                        DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_ID,
                        String.valueOf(secondaryCourse.secondaryCourseId));
                
                ContentValues cv = new ContentValues();
                cv.put(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_ID,
                        secondaryCourse.secondaryCourseId);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_NAME,
                        secondaryCourse.secondaryCourseName);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_IS_OPEN,
                        secondaryCourse.isOpen ? 1 : 0);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_COURSE_ID,
                        secondaryCourse.courseId);
                if (exists) {
                    mDB.update(DefaultDBHelper.DB_TABLE_SUB_COURSE, cv,
                            DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_ID + "=?",
                            new String[] { String
                                    .valueOf(secondaryCourse.secondaryCourseId) });
                }
                else {
                    mDB.insert(DefaultDBHelper.DB_TABLE_SUB_COURSE,
                            DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_NAME, cv);
                }
                return true;
            }
        });
    }
    
    public void clearSubCourseList() {
        mDB.execSQL(String.format("DELETE FROM %s", DefaultDBHelper.DB_TABLE_SUB_COURSE));
    }
    
    /**
     * 获取所有二级课程
     */
    public ArrayList<GradeCourseProto.SecondaryCourse> getSubCourseList() {
        ArrayList<GradeCourseProto.SecondaryCourse> subCourseList = new ArrayList<>();
        Cursor cursor = mDB.query(DefaultDBHelper.DB_TABLE_SUB_COURSE, null, null, null,
                null, null, null);
        
        try {
            while (cursor.moveToNext()) {
                GradeCourseProto.SecondaryCourse secondaryCourse = new GradeCourseProto.SecondaryCourse();
                secondaryCourse.secondaryCourseId = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_ID));
                secondaryCourse.secondaryCourseName = cursor.getString(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_NAME));
                secondaryCourse.isOpen = (cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_IS_OPEN)) == 1);
                secondaryCourse.courseId = cursor
                        .getInt(cursor
                                .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SUB_COURSE_COURSE_ID));
                subCourseList.add(secondaryCourse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return subCourseList;
    }

    // ****************** 二级课程 ******************** //

    // ****************** 年级类型 ******************** //
    /**
     * 添加年级类型
     */
    public void addGradeGroupType(List<GradeCourseProto.GradeGroup> gradeGroupList) {
        for (GradeCourseProto.GradeGroup gradeGroup : gradeGroupList) {
            addGradeGroupType(gradeGroup);
        }
    }

    /**
     * 添加年级类型
     */
    private void addGradeGroupType(final GradeCourseProto.GradeGroup gradeGroup) {
        execTask(new DBProcessTask<Boolean>(null) {

            @Override
            protected Boolean doJob() {
                boolean exists = isContentExistsInTable(
                        DefaultDBHelper.DB_TABLE_GRADE_GROUP,
                        DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE,
                        String.valueOf(gradeGroup.gradeGroupType));

                ContentValues cv = new ContentValues();
                cv.put(DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE,
                        gradeGroup.gradeGroupType);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_NAME,
                        gradeGroup.gradeGroupName);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_IS_OPEN,
                        gradeGroup.isOpen ? 1 : 0);
                if (exists) {
                    mDB.update(DefaultDBHelper.DB_TABLE_GRADE_GROUP, cv,
                            DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE + "=?",
                            new String[] { String
                                    .valueOf(gradeGroup.gradeGroupType) });
                }
                else {
                    mDB.insert(DefaultDBHelper.DB_TABLE_GRADE_GROUP,
                            DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE, cv);
                }
                return true;
            }
        });
    }

    public void clearGradeGroupTypeList() {
        mDB.execSQL(String.format("DELETE FROM %s", DefaultDBHelper.DB_TABLE_GRADE_GROUP));
    }

    /**
     * 获取所有年级类型
     */
    public ArrayList<GradeCourseProto.GradeGroup> getGradeGroupTypeList() {
        ArrayList<GradeCourseProto.GradeGroup> gradeGroupList = new ArrayList<>();
        Cursor cursor = mDB.query(DefaultDBHelper.DB_TABLE_GRADE_GROUP, null, null, null,
                null, null, null);

        try {
            while (cursor.moveToNext()) {
                GradeCourseProto.GradeGroup gradeGroup = new GradeCourseProto.GradeGroup();
                gradeGroup.gradeGroupType = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_TYPE));
                gradeGroup.gradeGroupName = cursor.getString(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_NAME));
                gradeGroup.isOpen = (cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_GRADE_GROUP_GROUP_IS_OPEN)) == 1);
                gradeGroupList.add(gradeGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return gradeGroupList;
    }

    public Map<Integer, Set<Integer>> getGradeCourseInfo() {
        Map<Integer, Set<Integer>> result = new HashMap<>();
        
        Cursor cursor = null;
        try {
            cursor = mDB.query(DefaultDBHelper.DB_TABLE_GRADE_COURSE, null, null, null,
                    null, null, DefaultDBHelper.DB_TABLE_KEY_COURSE_ID);
            int gradeid, courseid;
            while (cursor.moveToNext()) {
                courseid = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_COURSE_ID));
                
                Set<Integer> gradeSet = result.get(courseid);
                if (gradeSet == null) {
                    gradeSet = new HashSet<>();
                    result.put(courseid, gradeSet);
                }
                gradeid = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_GRADE_ID));
                
                if (!gradeSet.contains(gradeid))
                    gradeSet.add(gradeid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
    
    // ****************** 地区districts ******************** //
    public void clearDistricts() {
        mDB.execSQL(String.format("DELETE FROM %s",
                DefaultDBHelper.DB_TABLE_CITY_DISTRICT));
    }
    
    public void addDistrict(final CityDistrictProto.CityDistrict district) {
        
        execTask(new DBProcessTask<Boolean>(null) {
            
            @Override
            protected Boolean doJob() {
                ContentValues cv = new ContentValues();
                cv.put(DefaultDBHelper.DB_TABLE_KEY_DISTRICT_ID, district.cityDistrictId);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_DISTRICT_NANE, district.districtName);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_DISTRICT_ISOPEN, district.isOpen ? 1
                        : 0);
                cv.put(DefaultDBHelper.DB_TABLE_KEY_CITY_ID, district.cityId);
                mDB.insert(DefaultDBHelper.DB_TABLE_CITY_DISTRICT,
                        DefaultDBHelper.DB_TABLE_KEY_CITY_ID, cv);
                return true;
            }
        });
    }
    
    /**
     * 获取年级列表
     */
    public ArrayList<CityDistrictProto.CityDistrict> getDistrictList() {
        ArrayList<CityDistrictProto.CityDistrict> districtList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mDB.query(DefaultDBHelper.DB_TABLE_CITY_DISTRICT, null, null, null,
                    null, null, DefaultDBHelper.DB_TABLE_KEY_CITY_ID);
            while (cursor.moveToNext()) {
                CityDistrictProto.CityDistrict districtBuilder = new CityDistrictProto.CityDistrict();
                
                districtBuilder.cityDistrictId = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_DISTRICT_ID));
                districtBuilder.districtName = cursor.getString(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_DISTRICT_NANE));
                districtBuilder.isOpen = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_DISTRICT_ISOPEN)) == 1;
                districtBuilder.cityId = cursor.getInt(cursor
                        .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_CITY_ID));
                
                districtList.add(districtBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return districtList;
    }

    /**
     * 更新小贴士为已读
     */
    public void setTipRead(final String bid) {
        ContentValues cv = new ContentValues();
        cv.put(DefaultDBHelper.DB_TABLE_KEY_TIP_UNREAD, 0);
        mDB.update(DefaultDBHelper.DB_TABLE_TIPS, cv,
                DefaultDBHelper.DB_TABLE_KEY_TIP_BID + "=?", new String[] { bid });
    }
    
    // ****************** 消息缓存（过期删除） ******************** //
    

    /** 是否存在指定id的缓存消息 */
    public boolean isPushMsgExists(String id) {
        return isContentExistsInTable(DefaultDBHelper.DB_TABLE_MQ_MSG,
                DefaultDBHelper.DB_TABLE_KEY_MSG_ID, id);
    }
    
    /**
     * 批量推送是否存在
     */
    public boolean isBatchTipExists(String bid) {
        
        Cursor cursor = mDB.query(DefaultDBHelper.DB_TABLE_BATCH_TIPS, null,
                DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_USER_ID + "=? and "
                        + DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_BID + "=?",
                new String[] { BaseData.qingqingUserId(), bid }, null, null, null);
        
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToNext();
            cursor.close();
        }
        
        return exists;
    }

    /**
     * 批量推送是否已读状态
     */
    public boolean isBatchTipRead(final String bid){
        Cursor cursor = mDB.query(DefaultDBHelper.DB_TABLE_BATCH_TIPS, null,
                DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_USER_ID + "=? and "
                        + DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_BID + "=?",
                new String[] { BaseData.qingqingUserId(), bid }, null, null, null);

        boolean read = false;
        if (cursor != null && cursor.moveToNext()) {
            read = cursor.getInt(cursor
                    .getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_UNREAD)) > 0;
            cursor.close();
        }

        return read;
    }
    /**
     * 更新批量消息为已读
     */
    public void setBatchTipRead(final String bid) {
        ContentValues cv = new ContentValues();
        cv.put(DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_UNREAD, 0);
        mDB.update(DefaultDBHelper.DB_TABLE_BATCH_TIPS, cv,
                DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_USER_ID + "=? and "
                        + DefaultDBHelper.DB_TABLE_KEY_BATCH_TIP_BID + "=?",
                new String[] { BaseData.qingqingUserId(), bid });
    }

    /**
     * 读取积分表
     */
    public SparseArray<ScoreProto.SCOREScoreTypeEntry> getScores(){
        SparseArray<ScoreProto.SCOREScoreTypeEntry> array = new SparseArray<ScoreProto.SCOREScoreTypeEntry>();
        Cursor cursor = null;
        try {
            cursor = mDB.query(DefaultDBHelper.DB_TABLE_SCORE,null,null,null,null,null,null);
            while (cursor.moveToNext()) {
                ScoreProto.SCOREScoreTypeEntry entry = new ScoreProto.SCOREScoreTypeEntry();
                entry.scoreType = cursor.getInt(cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SCORE_TYPE));
                entry.scoreAmount = cursor.getInt(cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SCORE_AMOUNT));
                entry.name = cursor.getString(cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SCORE_NAME));
                entry.description = cursor.getString(cursor.getColumnIndex(DefaultDBHelper.DB_TABLE_KEY_SCORE_DESCRIPTION));
                array.put(entry.scoreType,entry);
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return array;
    }

    /**
     * 存积分
     */
    public void addScore(ScoreProto.SCOREScoreTypeEntry entry){
        ContentValues cv = new ContentValues();
        cv.put(DefaultDBHelper.DB_TABLE_KEY_SCORE_TYPE,entry.scoreType);
        cv.put(DefaultDBHelper.DB_TABLE_KEY_SCORE_AMOUNT,entry.scoreAmount);
        cv.put(DefaultDBHelper.DB_TABLE_KEY_SCORE_NAME,entry.name);
        cv.put(DefaultDBHelper.DB_TABLE_KEY_SCORE_DESCRIPTION,entry.description);
        mDB.insert(DefaultDBHelper.DB_TABLE_SCORE,DefaultDBHelper.DB_TABLE_KEY_SCORE_TYPE,cv);
    }

    /**
     * 删除所有积分
     */
    public void deleteAllScore(){
        mDB.delete(DefaultDBHelper.DB_TABLE_SCORE,null,null);
    }

    /**
     * 某类型积分是否存在
     */
    public boolean isScoreExists(int scoreType) {

        Cursor cursor = mDB.query(DefaultDBHelper.DB_TABLE_SCORE, null,
                DefaultDBHelper.DB_TABLE_KEY_SCORE_TYPE + " =?",
                new String[] {String.valueOf(scoreType)}, null, null, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToNext();
            cursor.close();
        }

        return exists;
    }

}
