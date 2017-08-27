package com.wzf.album;

/**
 * ===============================
 * 描    述：
 * 作    者：wzf
 * 创建日期：2017/8/10 16:32
 * ===============================
 */
public class Constants {

    public static final int MAX_CHECK_COUNT = 9;

    // 上一个页面存在照片数据
    public static final String KEY_OLD_IMAGE_LIST = "KEY_OLD_IMAGE_LIST";
    // 单选或多选的KEY
    public static final String KEY_IS_SINGLE_MODE = "KEY_IS_SINGLE_MODE";
    // 多选时，返回选中的所有对象列表
    public static final String RESULT_ENITITY_LIST_ALL = "RESULT_ENITITY_LIST_ALL";
    // 多选时，返回选中的差异对象列表(如：原数据ABCD，现数据ACDFG，差异数据则是FG)
    public static final String RESULT_ENITITY_LIST_DIFF = "RESULT_ENITITY_LIST_DIFF";
    // 单选时，返回选中的对象
    public static final String RESULT_SINGLE_ENITITY = "RESULT_SINGLE_ENITITY";
}
