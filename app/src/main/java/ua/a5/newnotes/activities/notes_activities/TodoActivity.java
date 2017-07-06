package ua.a5.newnotes.activities.notes_activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import ua.a5.newnotes.DAO.DBHelper;
import ua.a5.newnotes.R;
import ua.a5.newnotes.activities.CreateNoteTODOActivity;
import ua.a5.newnotes.dto.notesDTO.TodoDTO;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_KEY_DAY;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_KEY_DESCRIPTION;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_KEY_ISDONE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_KEY_MONTH;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_KEY_TITLE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_KEY_YEAR;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_TODO_NAME;
import static ua.a5.newnotes.R.id.delete_item;
import static ua.a5.newnotes.R.id.update_item;
import static ua.a5.newnotes.utils.Constants.KEY_TODO_DTO;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_TODO;
import static ua.a5.newnotes.utils.Constants.isCardForUpdate;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.DATE_REGEXPS;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.DAYS_OF_THE_WEEK;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.TIME_WORDS;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentDay;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentHour;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentMinute;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentMonth;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentYear;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getDifferenceBetweenPlannedDayAndToday;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsWords.getIntMonthFromString;

public class TodoActivity extends AppCompatActivity {

    public static SpannableString bufferSpannableString = null;

    String initialWord;
    String strRegExp;

    @BindView(R.id.tv_todo_activity_title)
    TextView tvTitle;

    @BindView(R.id.tv_todo_activity_date)
    TextView tvDate;

    @BindView(R.id.chbx_todo_activity)
    CheckBox chbxIsDone;

    int isDone;

    @BindView(R.id.tv_todo_activity_description)
    TextView tvDescription;

    @BindView(R.id.iv_todo_menu)
    ImageView ivTodoMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        ButterKnife.bind(this);

        if (getIntent() != null) {
            final TodoDTO todoDTO = (TodoDTO) getIntent().getSerializableExtra(KEY_TODO_DTO);
            tvTitle.setText(todoDTO.getTitle());
            if(todoDTO.getMonth()+1 < 10) {
                tvDate.setText(todoDTO.getDay() + "-0" + (todoDTO.getMonth() + 1) + "-" + todoDTO.getYear());
            }else{
                tvDate.setText(todoDTO.getDay() + "-" + (todoDTO.getMonth() + 1) + "-" + todoDTO.getYear());
            }
            isDone = todoDTO.getIsDone();
            if (isDone == 0) {
                chbxIsDone.setChecked(false);

            } else {
                chbxIsDone.setChecked(true);
            }
            chbxIsDone.setText("ВЫПОЛНЕНО");
            chbxIsDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        todoDTO.setIsDone(1);
                        updateTodoCheckboxUIandDB(todoDTO);
                    } else {
                        todoDTO.setIsDone(0);
                        updateTodoCheckboxUIandDB(todoDTO);
                    }
                }
            });


            try {
                SpannableString spannableString = new SpannableString(todoDTO.getDescription());
                bufferSpannableString = new SpannableString(spannableString);

                for (String days : DAYS_OF_THE_WEEK.keySet()) {
                    initialWord = days;
                    spannableString = convertString(bufferSpannableString, initialWord);
                    bufferSpannableString = spannableString;
                }

                for (String timeWords : TIME_WORDS.keySet()) {
                    initialWord = timeWords;
                    spannableString = convertString(bufferSpannableString, initialWord);
                    bufferSpannableString = spannableString;
                }

                for (String dateRegExp : DATE_REGEXPS) {
                    strRegExp = dateRegExp;
                    spannableString = convertDateRegExps(bufferSpannableString, strRegExp);
                    bufferSpannableString = spannableString;
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            tvDescription.setText(bufferSpannableString);
            tvDescription.setLinksClickable(true);
            tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

            ivTodoMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu cardPopupMenu = new PopupMenu(TodoActivity.this, ivTodoMenu);
                    cardPopupMenu.getMenuInflater().inflate(R.menu.menu_card, cardPopupMenu.getMenu());

                    cardPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem it) {

                            switch (it.getItemId()) {
                                case delete_item:

                                    AlertDialog.Builder builder = new AlertDialog.Builder(TodoActivity.this, R.style.MyAlertDialogStyle);
                                    builder.setTitle("Delete?");
                                    builder.setMessage("Do You Really Want To Delete?");

                                    //positive button.
                                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(TodoActivity.this, "delete", Toast.LENGTH_SHORT).show();
                                            deleteItemFromTable(todoDTO);
                                            TodoActivity.this.finish();
                                        }

                                    });

                                    //negative button.
                                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }

                                    });
                                    builder.show();
                                    break;

                                case update_item:
                                    isCardForUpdate = true;
                                    Intent intent = new Intent(TodoActivity.this, CreateNoteTODOActivity.class);
                                    intent.putExtra(KEY_UPDATE_TODO, todoDTO);
                                    startActivity(intent);
                                    finish();
                                    break;
                            }
                            return true;
                        }
                    });
                    cardPopupMenu.show();
                }
            });
        }
    }


    private void updateTodoCheckboxUIandDB(TodoDTO item) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(TABLE_NOTES_TODO_KEY_ISDONE, item.getIsDone());

        sqLiteDatabase.update(DBHelper.TABLE_NOTES_TODO_NAME, newValues, TABLE_NOTES_TODO_KEY_TITLE + " = ? ", new String[]{item.getTitle()});

        //закрываем соединение с БД.
        dbHelper.close();
    }

    private void deleteItemFromTable(TodoDTO todoDTO) {

//////////////////---------------------->
        //для работы с БД.
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NOTES_TODO_NAME,
                TABLE_NOTES_TODO_KEY_TITLE + " = ? AND "
                        + TABLE_NOTES_TODO_KEY_DAY + " = ? AND "
                        + TABLE_NOTES_TODO_KEY_MONTH + " = ? AND "
                        + TABLE_NOTES_TODO_KEY_YEAR + " = ? AND "
                        + TABLE_NOTES_TODO_KEY_DESCRIPTION + " = ? ",
                new String[]{
                        todoDTO.getTitle(),
                        String.valueOf(todoDTO.getDay()),
                        String.valueOf(todoDTO.getMonth()),
                        String.valueOf(todoDTO.getYear()),
                        String.valueOf(todoDTO.getDescription())
                });

        //закрываем соединение с БД.
        dbHelper.close();
//////////////////---------------------->
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    public SpannableString convertDateRegExps(SpannableString initialSpannableString, String strRegExp) {

        Pattern p = Pattern.compile(strRegExp);
        Matcher m = p.matcher(initialSpannableString.toString().replaceAll("[\\p{Cf}]", ""));

        SpannableString newSpannableString = new SpannableString(initialSpannableString);
        while (m.find()) {

            final int day = Integer.parseInt(m.group(1));

            final int month;
            if (m.group(3) != null) {
                month = getIntMonthFromString(m.group(3));
            } else {
                month = getCurrentMonth();
            }


            int year1 = getCurrentYear();
            if (m.group(5) != null) {
                year1 = Integer.parseInt(m.group(5));
                if (year1 < 100) {
                    if (year1 < 50) {
                        year1 += 2000;
                    } else {
                        year1 += 1900;
                    }
                }
            }

            final int year = year1;

            final int hour = getCurrentHour();
            final int minute = getCurrentMinute();


//String regExp = "(\\d{1,2})(\\s*)(января|янв|февраля|фев|марта|мар|апреля|апр|май|мая|июня|июн|июля|июл|августа|авг|сентября|сен|октября|окт|ноября|ноя|декабря|дек)(\\s*)(\\d{2,4}){0,1}";


            if (m.group(5) != null) {

                newSpannableString.setSpan(new ClickableSpan() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(View widget) {

                        Intent calIntent = createCalendarIntent(year, month, day, hour, minute);
                        startActivity(calIntent);

                    }
                }, m.start(1), m.end(5), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            } else if (m.group(3) != null) {

                newSpannableString.setSpan(new ClickableSpan() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(View widget) {

                        Intent calIntent = createCalendarIntent(year, month, day, hour, minute);
                        startActivity(calIntent);


                    }
                }, m.start(1), m.end(3), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {

                newSpannableString.setSpan(new ClickableSpan() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(View widget) {

                        Intent calIntent = createCalendarIntent(year, month, day, hour, minute);
                        startActivity(calIntent);

                    }
                }, m.start(1), m.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return newSpannableString;
    }

    @NonNull
    private Intent createCalendarIntent(int year, int month, int day, int hour, int minute) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");

        GregorianCalendar calDateBegin = new GregorianCalendar(
                year,
                month,
                day
        );
        GregorianCalendar calDateEnd = new GregorianCalendar(
                year,
                month,
                day
        );

        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                calDateBegin.getTimeInMillis() + hour * 60 * 60 * 1000 + minute * 60 * 1000);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                calDateEnd.getTimeInMillis() + hour * 60 * 60 * 1000 + minute * 60 * 1000);
        return calIntent;
    }


    public SpannableString convertString(SpannableString initialSpannableString, final String initialWord) {

        List<Integer> indexesOfFirstLetters = getIndexesOfFirstLetters(initialSpannableString, initialWord);

        SpannableString newSpannableString = new SpannableString(initialSpannableString);

        for (Integer indexesOfFirstLetter : indexesOfFirstLetters) {

            boolean middleWordCondition = indexesOfFirstLetter != 0
                    && (
                    newSpannableString.charAt(indexesOfFirstLetter - 1) == ' '
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '('
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '"'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '.'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == ','
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == ':'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == ';'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '-'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '='
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '\\'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '/'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '|'
            )
                    && indexesOfFirstLetter + initialWord.length() < newSpannableString.length()
                    && (
                    newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ' '
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ')'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '"'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '.'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ','
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ':'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ';'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '-'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '='
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '\\'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '/'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '|'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '!'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '?'
            );
            boolean firstWordCondition = indexesOfFirstLetter == 0
                    && indexesOfFirstLetter + initialWord.length() < newSpannableString.length()
                    && (
                    newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ' '
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '.'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ','
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ':'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == ';'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '-'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '='
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '\\'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '/'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '|'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '!'
                            || newSpannableString.charAt(indexesOfFirstLetter + initialWord.length()) == '?'
            );
            boolean lastWordCondition = indexesOfFirstLetter != 0
                    && (
                    newSpannableString.charAt(indexesOfFirstLetter - 1) == ' '
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '.'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == ','
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == ':'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == ';'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '-'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '='
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '\\'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '/'
                            || newSpannableString.charAt(indexesOfFirstLetter - 1) == '|'
            )
                    && indexesOfFirstLetter + initialWord.length() == newSpannableString.length();
            if (middleWordCondition || firstWordCondition || lastWordCondition) {
                newSpannableString.setSpan(new ClickableSpan() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(View widget) {

                        Intent calIntent = new Intent(Intent.ACTION_INSERT);
                        calIntent.setType("vnd.android.cursor.item/event");


                        int beginDay = getCurrentDay();
                        int beginMonth = getCurrentMonth();
                        int beginYear = getCurrentYear();

                        int beginHour = getCurrentHour();
                        int beginMinute = getCurrentMinute();

                        int endDay = getCurrentDay();
                        int endMonth = getCurrentMonth();
                        int endYear = getCurrentYear();

                        int endHour = getCurrentHour();
                        int endMinute = getCurrentMinute();

                        GregorianCalendar calDateBegin = new GregorianCalendar(
                                beginYear,
                                beginMonth,
                                beginDay + getDifferenceBetweenPlannedDayAndToday(initialWord)
                        );
                        GregorianCalendar calDateEnd = new GregorianCalendar(
                                endYear,
                                endMonth,
                                endDay + getDifferenceBetweenPlannedDayAndToday(initialWord)
                        );

                        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                calDateBegin.getTimeInMillis() + beginHour * 60 * 60 * 1000 + beginMinute * 60 * 1000);
                        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                calDateEnd.getTimeInMillis() + endHour * 60 * 60 * 1000 + endMinute * 60 * 1000);

                        startActivity(calIntent);

                    }
                }, indexesOfFirstLetter, indexesOfFirstLetter + initialWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return newSpannableString;
    }


    public List<Integer> getIndexesOfFirstLetters(SpannableString initialSpannableString, String initialWord) {
        List<Integer> indexesOfFirstLetters = new ArrayList<Integer>();

        StringBuilder text = new StringBuilder(initialSpannableString);
        StringBuilder newString = new StringBuilder("");
        int intIndex;
        while (text.length() > initialWord.length()) {
            intIndex = text.indexOf(initialWord);
            if (intIndex == -1) {
                newString.append(text);
                break;
            } else {
                indexesOfFirstLetters.add(newString.length() + intIndex);
                newString.append(text.substring(0, intIndex)).append(initialWord);
                text = new StringBuilder(text.substring(intIndex + initialWord.length()));
            }
        }
        return indexesOfFirstLetters;
    }
}
