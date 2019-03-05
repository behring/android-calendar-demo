package com.thoughtworks.calendardemo;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import static android.provider.CalendarContract.CALLER_IS_SYNCADAPTER;

//https://developer.android.com/guide/topics/providers/calendar-provider
public class MainActivity extends AppCompatActivity {
    private static final int CALENDAR_ACCOUNT_NO_EXIST = -1;
    private static final String CALENDARS_NAME = "behring";
    private static final String CALENDARS_ACCOUNT_NAME = "lizhao@thoughtworks.com";
    private static final String CALENDARS_ACCOUNT_TYPE = "com.thoughtworks";
    private static final String CALENDARS_DISPLAY_NAME = "BEHRING账户";

    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.calendar_event_info);
    }

    public void addCalendarEvent(View view) {
        String dateStr = "2019-03-08 09:00:00";
        long reminderTime = Timestamp.valueOf(dateStr).getTime();

        try {
            addCalendarEvent(this, "测试添加日历时间", "日历事件描述信息",
                    reminderTime, 1);
        } catch (Exception e) {
            Toast.makeText(this,
                    "add calender event failure!", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteCalendarEventByTitle(View view) {
    }

    public void updateCalendarEventByTitle(View view) {
    }

    public void queryCalendarEventByAccount(View view) {

    }


    private void addCalendarEvent(Context context, String title, String description,
                                  long reminderTime, int previousDate) throws Exception {
        long calendarAccountId = getAndAddCalendarAccount(context);
        if (calendarAccountId == CALENDAR_ACCOUNT_NO_EXIST) {
            throw new Exception("calendar account not exist.");
        }

        //添加日历事件
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminderTime);//设置开始时间
        long start = calendar.getTime().getTime();
        calendar.setTimeInMillis(start + 10 * 60 * 1000);//设置终止时间，开始时间加10分钟
        long end = calendar.getTime().getTime();

        ContentValues event = new ContentValues();
        //关联事件和日历
        event.put(Events.CALENDAR_ID, calendarAccountId);
        event.put(Events.TITLE, title);
        event.put(Events.DESCRIPTION, description);
        event.put(Events.DTSTART, start);
        event.put(Events.DTEND, end);
        //设置有闹钟提醒
        event.put(Events.HAS_ALARM, 1);
        //时区必须设置，也可通过字符串指定时区"Asia/Beijing"
        event.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        Uri newEvent = context.getContentResolver().insert(Events.CONTENT_URI, event);
        if (newEvent == null) {
            throw new Exception("add calendar event failure.");
        }

        //设置事件提醒
        ContentValues values = new ContentValues();
        values.put(Reminders.EVENT_ID, ContentUris.parseId(newEvent));
        // 提前previousDate天有提醒
        values.put(Reminders.MINUTES, previousDate * 24 * 60);
        values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
        Uri uri = context.getContentResolver().insert(Reminders.CONTENT_URI, values);
        if (uri == null) {
            throw new Exception("add calendar reminder failure.");
        }
    }


    public static void deleteCalendarEvent(Context context, String title) {
        Cursor eventCursor = context.getContentResolver().query(Events.CONTENT_URI, null,
                null, null, null);
        try {
            if (eventCursor == null) { //查询返回空值
                return;
            }
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) { //事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

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

    private long getCalendarAccount(Context context) {
        long calendarAccountId = CALENDAR_ACCOUNT_NO_EXIST;

        final String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";

        final String[] selectionArgs = new String[]{CALENDARS_ACCOUNT_NAME, CALENDARS_ACCOUNT_TYPE,
                CALENDARS_ACCOUNT_NAME};

        Cursor cur = context.getContentResolver().query(Calendars.CONTENT_URI,
                new String[]{Calendars._ID},
                selection, selectionArgs, null);

        if (cur == null || cur.getCount() == 0) {
            return calendarAccountId;
        }

        cur.moveToFirst();
        calendarAccountId = cur.getInt(cur.getColumnIndex(Calendars._ID));
        cur.close();

        return calendarAccountId;
    }

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
