package com.ebnbin.recyclercalendarview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ebnbin.recyclercalendarview.CalendarSelectListener;
import com.ebnbin.recyclercalendarview.RecyclerCalendarView;

public class MainActivity extends Activity implements CalendarSelectListener {
    private RecyclerCalendarView mRecyclerCalendarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerCalendarView = new RecyclerCalendarView(this);

        mRecyclerCalendarView.setCallBackSelectListener(this);

        setContentView(mRecyclerCalendarView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        boolean doubleSelected = mRecyclerCalendarView.isDoubleSelectedMode();
        MenuItem doubleSelectedMenuItem = menu.findItem(R.id.double_selected_mode);
        doubleSelectedMenuItem.setTitle(doubleSelected ? R.string.single_selected_mode
                : R.string.double_selected_mode);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.double_selected_mode: {
                boolean doubleSelectedMode = mRecyclerCalendarView.isDoubleSelectedMode();
                mRecyclerCalendarView.setDoubleSelectedMode(!doubleSelectedMode);
                mRecyclerCalendarView.scrollToSelected();

                item.setTitle(doubleSelectedMode ? R.string.double_selected_mode : R.string.single_selected_mode);

                return true;
            }
            case R.id.reset_selected: {
                mRecyclerCalendarView.resetSelected();

                return true;
            }
            case R.id.scroll_to_today: {
               mRecyclerCalendarView.scrollToToday();
               //mRecyclerCalendarView.scrollToTodayBeforeOneDay();

                return true;
            }
            case R.id.scroll_to_selected: {
                mRecyclerCalendarView.scrollToSelected();

                return true;
            }
            case R.id.scroll_to_someDay: {
                mRecyclerCalendarView.scrollToSomedayDay("2018-09-20",false);

                return true;
            }
            case R.id.scroll_to_yesterday: {
                mRecyclerCalendarView.scrollToYesterday();

                return true;
            }
            case R.id.scroll_to_bottom: {
                mRecyclerCalendarView.scrollToBottom();

                return true;
            }
            case R.id.getSelectData: {
                String selectData= mRecyclerCalendarView.getSelectData();
                Toast.makeText(this,selectData,Toast.LENGTH_SHORT).show();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    @Override
    public void getCalendarString(String date) {
        Toast.makeText(this,date,Toast.LENGTH_SHORT).show();
    }
}
