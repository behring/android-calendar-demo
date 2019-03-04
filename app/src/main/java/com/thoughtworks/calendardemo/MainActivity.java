package com.thoughtworks.calendardemo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.TimeZone;

import static android.provider.CalendarContract.CALLER_IS_SYNCADAPTER;

public class MainActivity extends AppCompatActivity {
    //https://www.jianshu.com/p/4820e02b2ee4
    private static final int CALENDAR_ACCOUNT_NO_EXIST = -1;

    private static String CALENDARS_NAME = "hera";
    private static String CALENDARS_ACCOUNT_NAME = "hera@example.com";
    private static String CALENDARS_ACCOUNT_TYPE = "com.example";
    private static String CALENDARS_DISPLAY_NAME = "HERA账户";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addSchedule(View view) {
        Cursor cur = null;
        ContentResolver cr = getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[]{"hera@example.com", "com.example",
                "hera@example.com"};
// Submit the query and get a Cursor object back.
//        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
    }

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
     * 获取账户成功返回账户id，否则返回-1
     */
    private long getAndAddCalendarAccount(Context context) {
        long calendarAccountId = getCalendarAccount(context);
        if (calendarAccountId > CALENDAR_ACCOUNT_NO_EXIST) {
            return calendarAccountId;
        } else {
            calendarAccountId = addCalendarAccount(context);
            return calendarAccountId > CALENDAR_ACCOUNT_NO_EXIST ?
                    calendarAccountId : CALENDAR_ACCOUNT_NO_EXIST;
        }
    }

    /**
     * 检查是否存在现有账户，存在则返回账户id，否则返回-1
     */
    private long getCalendarAccount(Context context) {
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";

        String[] selectionArgs = new String[]{CALENDARS_ACCOUNT_NAME, CALENDARS_ACCOUNT_TYPE,
                CALENDARS_ACCOUNT_NAME};

        Cursor userCursor = context.getContentResolver().query(Calendars.CONTENT_URI,
                new String[]{Calendars._ID},
                selection, selectionArgs, null);
        try {
            if (userCursor == null) { //查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) { //存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
     * 添加日历账户，账户创建成功则返回账户id，否则返回-1
     */
    private long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(Calendars.NAME, CALENDARS_NAME);
        value.put(Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(Calendars.VISIBLE, 1);
        value.put(Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        value.put(Calendars.SYNC_EVENTS, 1);
        value.put(Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
            return result == null ? -1 : ContentUris.parseId(result);
    }
}
