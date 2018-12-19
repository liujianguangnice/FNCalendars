# Android-RecyclerCalendarView

仿 猫眼专业版 日历 view.

#### 添加依赖和配置

* 工程添加依赖仓库，Add the JitPack repository to your build file

```Java
allprojects {
   repositories {
   		...
   	    maven { url 'https://jitpack.io' }
   }
}
```

* APP目录build.gradle文件添加如下配置：

```Java
dependencies {
   implementation 'com.github.liujianguangnice:FNCalendar:1.0.1'
}
```

#### 2、效果展示


![show.gif](gif/show.gif)


![a.png](png/a.png)


<!--
![点我查看效果图](https://github.com/liujianguangnice/RatingBar/blob/master/screenshot/tste_20181206175419.png?raw=true)
-->


#### 3、核心代码

```Java
/**
 * 列表日历 view.
 */
public class RecyclerCalendarView extends FrameLayout {
    private final int[] mTodayDateBeforeOneDay;
    /**
     * 今天日期.
     */
    private int[] mTodayDate;

    private PinnedHeaderRecyclerView mCalendarRecyclerView;

    private GridLayoutManager mCalendarLayoutManager;

    private CalendarAdapter mCalendarAdapter;

    private String selectDataStar;

    private String selectDataEnd;

    private int dataCount;

    public RecyclerCalendarView(@NonNull Context context) {
        this(context, null);
    }

    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerCalendarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Util.init(getContext());

        mTodayDate = Util.getTodayDate();
        mTodayDateBeforeOneDay = Util.getTodayDateBeforeOneDay();

        inflate(getContext(), R.layout.view_recycler_calendar, this);

        mCalendarRecyclerView = (PinnedHeaderRecyclerView) findViewById(R.id.calendar);

        mCalendarLayoutManager = new GridLayoutManager(getContext(), 7);
        mCalendarRecyclerView.setLayoutManager(mCalendarLayoutManager);

        mCalendarAdapter = new CalendarAdapter(getContext());

        mCalendarAdapter.setOnDayClickListener(new CalendarAdapter.OnDayClickListener() {
            @Override
            void onDayClick(int position) {
                super.onDayClick(position);

                if (dayCanClick(position)) {
                    clickPosition(position, true, true);
                } else {
                    //Toast.makeText(getContext(), "不可点击", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mCalendarRecyclerView.setAdapter(mCalendarAdapter);

        mCalendarRecyclerView.setPinnedHeaderView(R.layout.item_month);

        setDoubleSelectedMode(false);
        scrollToSelected();
    }

    //*****************************************************************************************************************
    // 选中模式.

    /**
     * 如果为 true 则为双选模式, 否则为单选模式.
     */
    private boolean mDoubleSelectedMode;

    /**
     * 当前选中的第一个位置.
     */
    private int mSelectedPositionA = -1;
    /**
     * 当前选中的第二个位置.
     */
    private int mSelectedPositionB = -1;

    /**
     * 返回是否为双选模式.
     */
    public boolean isDoubleSelectedMode() {
        return mDoubleSelectedMode;
    }

    /**
     * 设置是否为双选模式, 并重置选中日期.
     */
    public void setDoubleSelectedMode(boolean doubleSelectedMode) {
        setDoubleSelectedMode(doubleSelectedMode, true);
        mCalendarAdapter.setDoubleSelectedMode(doubleSelectedMode);

    }

    /**
     * 设置单选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] date) {
        setDoubleSelectedMode(false, false);

        clickPosition(getPosition(date), true, false);
    }

    /**
     * 设置双选模式, 并指定选中的日期.
     */
    public void setDoubleSelectedMode(int[] dateFrom, int[] dateTo) {
        setDoubleSelectedMode(true, false);

        clickPosition(getPosition(dateFrom), false, false);
        clickPosition(getPosition(dateTo), true, false);
    }

    private void setDoubleSelectedMode(boolean doubleSelectedMode, boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode != doubleSelectedMode) {
            mDoubleSelectedMode = doubleSelectedMode;

            mCalendarAdapter.setCalendarData(null);
        }

        if (mCalendarAdapter.getCalendarData().isEmpty()) {
            mCalendarAdapter.setCalendarData(CalendarEntity.newCalendarData(mDoubleSelectedMode, mTodayDate));
        }

        resetSelected(notifyDataSetChanged);
    }

    /**
     * 重置选中日期.
     */
    public void resetSelected() {
        resetSelected(true);
    }


    private void resetSelected(boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode) {
            unselectPositionAB();
        } else {
            selectPositionB(-1);
            selectPositionA(getPosition(mTodayDate));
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    private void resetSelectedTodayBeforeOne(boolean notifyDataSetChanged) {
        if (mDoubleSelectedMode) {
            unselectPositionAB();
        } else {
            selectPositionB(-1);
            selectPositionA(getPosition(mTodayDateBeforeOneDay));
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 点击某位置.
     */
    private void clickPosition(int position, boolean notifyDataSetChanged, boolean callback) {
        if (mDoubleSelectedMode) {
            // 双选.
            if (mSelectedPositionA == -1) {
                // 两个都未选中.
                selectPositionB(-1);
                selectPositionA(position);
                if (callback) {
                    onDoubleFirstSelected(mSelectedPositionA);
                }
            } else if (mSelectedPositionB == -1) {
                // 已选中第一个.
                if (position == mSelectedPositionA) {
                    // 要取消选中第一个.
                    selectPositionA(-1);
                    if (callback) {
                        onDoubleFirstUnselected(position);
                    }
                } else {
                    // 要选中第二个.
                    int selectedCount = getPositionABSelectedCount(mSelectedPositionA, position);
                    if (selectedCount <= Util.getInstance().max_double_selected_count) {
                        selectPositionAB(mSelectedPositionA, position);
                        if (callback) {
                            onDoubleSelected(mSelectedPositionA, mSelectedPositionB, selectedCount);
                        }
                    } else {
                        if (callback) {
                            onExceedMaxDoubleSelectedCount(selectedCount);
                        }
                    }
                }
            } else {
                // 两个都已选中.
                unselectPositionAB();
                selectPositionA(position);
                if (callback) {
                    onDoubleFirstSelected(mSelectedPositionA);
                }
            }
        } else {
            selectPositionA(position);
            if (callback) {
                onSingleSelected(mSelectedPositionA);
            }
        }

        if (notifyDataSetChanged) {
            mCalendarAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置第一个位置.
     */
    private void selectPositionA(int position) {
        if (mSelectedPositionA == position) {
            return;
        }

        if (mSelectedPositionA != -1) {
            setPositionSelected(mSelectedPositionA, CalendarEntity.SELECTED_TYPE_UNSELECTED);
            mSelectedPositionA = -1;
        }

        if (position == -1) {
            return;
        }

        setPositionSelected(position, SELECTED_TYPE_SELECTED);
        mSelectedPositionA = position;
    }

    /**
     * 设置第二个位置.
     */
    private void selectPositionB(int position) {
        if (mSelectedPositionB == position) {
            return;
        }

        if (mSelectedPositionB != -1) {
            setPositionSelected(mSelectedPositionB, CalendarEntity.SELECTED_TYPE_UNSELECTED);
            mSelectedPositionB = -1;
        }

        if (position == -1) {
            return;
        }

        setPositionSelected(position, SELECTED_TYPE_SELECTED);
        mSelectedPositionB = position;
    }

    /**
     * 返回两个位置的选中天数.
     */
    private int getPositionABSelectedCount(int positionA, int positionB) {
        if (positionA == -1 || positionB == -1) {
            return 0;
        }

        int fromPosition = Math.min(positionA, positionB);
        int toPosition = Math.max(positionA, positionB);

        int selectedCount = 0;
        for (int i = fromPosition; i <= toPosition; i++) {
            if (mCalendarAdapter.getCalendarEntity(i).itemType == CalendarEntity.ITEM_TYPE_DAY) {
                ++selectedCount;
            }
        }

        return selectedCount;
    }

    /**
     * 取消双选选中.
     */
    private void unselectPositionAB() {
        if (mSelectedPositionA != -1 && mSelectedPositionB != -1) {
            for (int i = mSelectedPositionA; i <= mSelectedPositionB; i++) {
                setPositionSelected(i, CalendarEntity.SELECTED_TYPE_UNSELECTED);
            }

            mSelectedPositionA = -1;
            mSelectedPositionB = -1;

            return;
        }

        selectPositionA(-1);
        selectPositionB(-1);
    }

    /**
     * 双选选中.
     */
    private void selectPositionAB(int positionA, int positionB) {
        if (positionA == -1 || positionB == -1) {
            return;
        }

        int fromPosition = Math.min(positionA, positionB);
        int toPosition = Math.max(positionA, positionB);

        selectPositionA(fromPosition);
        selectPositionB(toPosition);

        for (int i = fromPosition + 1; i < toPosition; i++) {
            setPositionSelected(i, CalendarEntity.SELECTED_TYPE_RANGED);
        }
    }

    /**
     * 设置位置的选中状态.
     */
    private void setPositionSelected(int position, int selected) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarData().get(position);
        if (calendarEntity.itemType == CalendarEntity.ITEM_TYPE_DAY) {
            calendarEntity.selectedType = selected;
        }
    }

    /**
     * 返回指定日期的位置, 如果没找到则返回 -1.
     */
    private int getPosition(int[] date) {
        for (int position = 0; position < mCalendarAdapter.getCalendarData().size(); position++) {
            CalendarEntity calendarEntity = mCalendarAdapter.getCalendarData().get(position);
            if (calendarEntity.itemType == CalendarEntity.ITEM_TYPE_DAY
                    && Util.isDateEqual(calendarEntity.date, date)) {
                return position;
            }
        }

        return -1;
    }

    //*****************************************************************************************************************
    // 滚动.

    /**
     * 滚动到的位置, 如果为 -1 则不滚动.
     */
    private int mScrollToPosition = -1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        scrollToPosition(mScrollToPosition);
    }

    /**
     * 滚动到今天.
     */
    public void scrollToToday() {
        scrollToPosition(getPosition(mTodayDate));
    }

    /**
     * 滚动到今天前一天.
     */
    public void scrollToTodayBeforeOneDay() {
        scrollToPosition(getPosition(mTodayDateBeforeOneDay));
    }


    /**
     * 滚动到选中的位置, 如果没有选中的位置则滚动到今天.
     */
    public void scrollToSelected() {
        if (mDoubleSelectedMode && mSelectedPositionA != -1) {
            if (mSelectedPositionB == -1) {
                scrollToPosition(mSelectedPositionA);
            } else {
                scrollToPosition(Math.min(mSelectedPositionA, mSelectedPositionB));
            }
        } else if (!mDoubleSelectedMode && mSelectedPositionA != -1) {
            scrollToPosition(mSelectedPositionA);
        } else {
            scrollToToday();
        }
    }

    /**
     * 滚动到指定的位置, 如果为 -1 则不滚动.
     */
    private void scrollToPosition(int position) {
        mScrollToPosition = position;

        int calendarRecyclerViewMeasuredHeight = mCalendarRecyclerView.getMeasuredHeight();
        if (mScrollToPosition == -1 || calendarRecyclerViewMeasuredHeight == 0) {
            return;
        }

        int offset = calendarRecyclerViewMeasuredHeight / 2;
        mCalendarLayoutManager.scrollToPositionWithOffset(mScrollToPosition, offset);
        mScrollToPosition = -1;
    }

    //*****************************************************************************************************************
    // 回调.

    /**
     * 单选回调.
     */
    private void onSingleSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
        selectDataStar =  Util.getDateString(calendarEntity.date);

        if(mCallBackSelectListener!=null){
            mCallBackSelectListener.getCalendarString(selectDataStar);
        }

       // Toast.makeText(getContext(), Util.getDateString(calendarEntity.date), Toast.LENGTH_SHORT).show();
    }

    public CalendarSelectListener mCallBackSelectListener ;

    public void setCallBackSelectListener(CalendarSelectListener callBackSelectListener){
        mCallBackSelectListener =  callBackSelectListener;
    };

    /**
     * 双选回调.
     */
    private void onDoubleSelected(int positionFrom, int positionTo, int dayCounts) {
        CalendarEntity calendarEntityFrom = mCalendarAdapter.getCalendarEntity(positionFrom);
        CalendarEntity calendarEntityTo = mCalendarAdapter.getCalendarEntity(positionTo);

        selectDataStar =Util.getDateString(calendarEntityFrom.date);
        selectDataEnd =Util.getDateString(calendarEntityTo.date);
        dataCount=dayCounts;
        /*Toast.makeText(getContext(), Util.getDateString(calendarEntityFrom.date) + "~" +
                Util.getDateString(calendarEntityTo.date) + "," + dayCounts, Toast.LENGTH_SHORT).show();*/

        if(mCallBackSelectListener!=null){
            mCallBackSelectListener.getCalendarString(selectDataStar+"~"+selectDataEnd);
        }

    }

    /**
     * 双选选中第一个日期回调.
     */
    private void onDoubleFirstSelected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
        Toast.makeText(getContext(), "已选中:" + Util.getDateString(calendarEntity.date), Toast.LENGTH_SHORT).show();
    }

    /**
     * 双选取消第一个日期回调.
     */
    private void onDoubleFirstUnselected(int position) {
        CalendarEntity calendarEntity = mCalendarAdapter.getCalendarEntity(position);
        Toast.makeText(getContext(), "已取消:" + Util.getDateString(calendarEntity.date), Toast.LENGTH_SHORT).show();
    }

    /**
     * 超过最大双选天数回调.
     */
    private void onExceedMaxDoubleSelectedCount(int dayCount) {
        Toast.makeText(getContext(), "" + dayCount, Toast.LENGTH_SHORT).show();
    }


    /**
     * 滚动到昨天.
     */
    public void scrollToYesterday() {
        int positions = getPosition(mTodayDateBeforeOneDay);
        clickPosition(positions, true, true);
        scrollToPosition(getPosition(mTodayDateBeforeOneDay));
    }

    /**
     * 滚动到底部.
     */
    public void scrollToBottom() {
        //scrollToPosition(getPosition(mTodayDateBeforeOneDay));
        scrollToPosition(mCalendarAdapter.getCalendarData().size() - 1);

    }


    /**
     * 判断是否可以点击
     */
    public Boolean dayCanClick(int position) {
        if (position > getPosition(mTodayDateBeforeOneDay)) {
            return false;
        }
        return true;
    }

    /**
     * 滚动到某一天，并选中该天.
     * String someDay= "2011-07-09 ";
     */
    public void scrollToSomedayDay(String someDay,Boolean callback) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = formatter.parse(someDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Date beginDate =new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int[] someDate = new int[3];
        someDate[0] = calendar.get(Calendar.YEAR);
        someDate[1] = calendar.get(Calendar.MONTH) + 1;
        someDate[2] = calendar.get(Calendar.DATE);
        int positions = getPosition(someDate);
        clickPosition(positions, true, callback);
        scrollToPosition(getPosition(someDate));
    }

    /**
     * 获取选中的日期
     * String someDay= "2011-07-09 ";
     */
    public String getSelectData() {
        /*//法一：
        List<CalendarEntity> calendarEntityList = mCalendarAdapter.getCalendarData();
        CalendarEntity calendarEntity ;
        for (int i = 0; i < calendarEntityList.size(); i++) {
            calendarEntity = new CalendarEntity();
            calendarEntity = calendarEntityList.get(i);
            if(calendarEntity.selectedType==SELECTED_TYPE_SELECTED){
                int[] data =calendarEntity.date;
                return  data[0]+"-"+data[1]+"-"+data[2];
            }
        }*/
        String selectData  = "";
        if(isDoubleSelectedMode()){
            selectData = selectDataStar+"~"+selectDataEnd;
        }else{
            selectData = selectDataStar;
        }
        return selectData;
    }



}



```

