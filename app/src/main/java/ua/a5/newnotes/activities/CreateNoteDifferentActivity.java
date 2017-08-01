package ua.a5.newnotes.activities;

import android.app.Activity;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.a5.newnotes.DAO.DBHelper;
import ua.a5.newnotes.R;
import ua.a5.newnotes.dto.notesDTO.DifferentDTO;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_KEY_DATE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_KEY_DESCRIPTION;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_KEY_TITLE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_NAME;
import static ua.a5.newnotes.activities.NotesActivity.mIsPremium;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_DIFFERENT;
import static ua.a5.newnotes.utils.Constants.LOG_TAG;
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

public class CreateNoteDifferentActivity extends AppCompatActivity {
    public static SpannableString bufferSpannableString = null;

    boolean isSavedFlagDifferent;

    String initialWord;
    String strRegExp;

    TextView tvCreateNoteDate;
    TextView tvCreateNoteTime;

    EditText etCreateNoteTitle;
    EditText etNoteDescription;

    //для работы с БД.
    DBHelper dbHelper;

    DifferentDTO note;
    DifferentDTO differentDTO;

    String noteCategory;
    String noteTitle;
    String noteDescription;
    String noteDate;

    //Записываем исходные значения полей. Если они не изменились, то когда нажимаем НАЗАД,
//не появляется диалог СОХРАНИТЬ ИЗМЕНЕНИЯ?
    String oldTitle;
    String oldDescription;

    //для баннера////////////////////////////////////////////////////
    protected AdView mAdView;
    //для баннера////////////////////////////////////////////////////

    //для Interstitial////////////////////////////////////////////////////
    InterstitialAd mInterstitialAd;
    //для Interstitial////////////////////////////////////////////////////

    private Toolbar toolbarDifferent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note_different);

        toolbarDifferent = (Toolbar) findViewById(R.id.toolbar_different);
        toolbarDifferent.setTitle(R.string.toolbartitle_different);
        toolbarDifferent.setTitleTextAppearance(this, R.style.toolbar_title_style);
        toolbarDifferent.setOverflowIcon(getResources().getDrawable(R.drawable.ic_dots_vertical));
        setSupportActionBar(toolbarDifferent);

        if (mIsPremium == false) {
//Initializing the Google Mobile Ads SDK
            MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
//Initializing the Google Mobile Ads SDK

            //для баннера////////////////////////////////////////////////////
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    // Check the LogCat to get your test device ID
                    .addTestDevice(getString(R.string.ads_testdevice_number))
                    .build();
            mAdView.loadAd(adRequest);
            //для баннера////////////////////////////////////////////////////

            if (isCardForUpdate == false) {
//для Interstitial////////////////////////////////////////////////////
                mInterstitialAd = new InterstitialAd(this);
                //set the ad unit ID
                mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

                //Load ads into Interstitial Ads
                mInterstitialAd.loadAd(adRequest);
                mInterstitialAd.setAdListener(new AdListener() {
                    public void onAdLoaded() {
                        showInterstitial();
                    }
                });
//для Interstitial////////////////////////////////////////////////////
            }
        }

        isSavedFlagDifferent = false;

        //для работы с БД.
        dbHelper = new DBHelper(this);
        setDateAndTime();


        etCreateNoteTitle = (EditText) findViewById(R.id.et_diff_title);
        etNoteDescription = (EditText) findViewById(R.id.et_diff_description);


//Записываем исходные значения полей. Если они не изменились, то когда нажимаем НАЗАД,
//не появляется диалог СОХРАНИТЬ ИЗМЕНЕНИЯ?
        oldTitle = etCreateNoteTitle.getText().toString();
        oldDescription = etNoteDescription.getText().toString();

        //этот слушатель позволяет убирать клавиатуру EditText
        //при нажатии на пустое пространство.
        etCreateNoteTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        etNoteDescription.addTextChangedListener(onTextChangedListener());
        //этот слушатель позволяет убирать клавиатуру EditText
        //при нажатии на пустое пространство.
        etNoteDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        if (isCardForUpdate == true && getIntent() != null) {
            differentDTO = (DifferentDTO) getIntent().getSerializableExtra(KEY_UPDATE_DIFFERENT);
            etCreateNoteTitle.setText(differentDTO.getTitle());
            etNoteDescription.setText(differentDTO.getDescription());

//Записываем исходные значения полей. Если они не изменились, то когда нажимаем НАЗАД,
//не появляется диалог СОХРАНИТЬ ИЗМЕНЕНИЯ?
            oldTitle = etCreateNoteTitle.getText().toString();
            oldDescription = etNoteDescription.getText().toString();
        } else {
            differentDTO = new DifferentDTO(
                    new String(""),
                    new String(""),
                    new String(
                            getCurrentDay() + "-" +
                                    getCurrentMonth() + "-" +
                                    getCurrentYear()
                    )
            );
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_different, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_different_save:
                if (isCardForUpdate == true) {
                    deleteItemFromTable(differentDTO);
                }
                isCardForUpdate = false;

////////////////
                //заполняем БД данными.

                //класс SQLiteDatabase предназначен для управления БД SQLite.
                //если БД не существует, dbHelper вызовет метод onCreate(),
                //если версия БД изменилась, dbHelper вызовет метод onUpgrade().

                //в любом случае вернётся существующая, толькочто созданная или обновлённая БД.
                SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

                //класс ContentValues используется для добавления новых строк в таблицу.
                //каждый объект этого класса представляет собой одну строку таблицы и
                //выглядит, как массив с именами столбцов и значениями, которые им соответствуют.
                ContentValues contentValues = new ContentValues();

                //добавляем пары ключ-значение.

                noteCategory = "different";
                noteTitle = etCreateNoteTitle.getText().toString();
                noteDescription = etNoteDescription.getText().toString();
                noteDate = tvCreateNoteDate.getText().toString();


                note = new DifferentDTO(noteTitle, noteDescription, noteDate);
                System.out.println(note);


                contentValues.put(TABLE_NOTES_DIFFERENT_KEY_TITLE, noteTitle);
                contentValues.put(TABLE_NOTES_DIFFERENT_KEY_DATE, noteDate);
                contentValues.put(TABLE_NOTES_DIFFERENT_KEY_DESCRIPTION, noteDescription);
                //id заполнится автоматически.

                //вставляем подготовленные строки в таблицу.
                //второй аргумент используется для вставки пустой строки,
                //сейчас он нам не нужен, поэтому он = null.
                sqLiteDatabase.insert(DBHelper.TABLE_NOTES_DIFFERENT_NAME, null, contentValues);
                Log.d(LOG_TAG, "Date inserted");
                //закрываем соединение с БД.
                dbHelper.close();
////////////////////////
                isSavedFlagDifferent = true;
                CreateNoteDifferentActivity.this.finish();
                break;

            case R.id.menu_different_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteDifferentActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle(R.string.deletedialog_title);
                builder.setMessage(R.string.deletedialog_message);

                //positive button.
                builder.setPositiveButton(R.string.deletedialog_positivebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItemFromTable(differentDTO);
                        CreateNoteDifferentActivity.this.finish();

                    }

                });

                //negative button.
                builder.setNegativeButton(R.string.deletedialog_negativebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                break;
        }
        isCardForUpdate = false;
        return true;
    }


    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void deleteItemFromTable(DifferentDTO differentDTO) {
//////////////////---------------------->
        //для работы с БД.
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NOTES_DIFFERENT_NAME,
                TABLE_NOTES_DIFFERENT_KEY_TITLE + " = ? AND "
                        + TABLE_NOTES_DIFFERENT_KEY_DATE + " = ? AND "
                        + TABLE_NOTES_DIFFERENT_KEY_DESCRIPTION + " = ? ",
                new String[]{
                        differentDTO.getTitle(),
                        String.valueOf(differentDTO.getDate()),
                        String.valueOf(differentDTO.getDescription())
                });

        //закрываем соединение с БД.
        dbHelper.close();
//////////////////---------------------->
    }


    private TextWatcher onTextChangedListener() {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                etNoteDescription.removeTextChangedListener(this);

                try {
                    String initialString = s.toString();
                    SpannableString spannableString = new SpannableString(initialString);
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

                    etNoteDescription.setText(bufferSpannableString);

                    etNoteDescription.setLinksClickable(true);
                    etNoteDescription.setMovementMethod(LinkMovementMethod.getInstance());
                    etNoteDescription.setSelection(etNoteDescription.getText().length());

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                etNoteDescription.addTextChangedListener(this);
            }
        };
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

    //метод, для убирания клавиатуры EditText при нажатии на пустое пространство.
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void setDateAndTime() {
        setTime();
        setDate();
    }

    private void setDate() {
        Calendar cal;

        cal = Calendar.getInstance();
        int dayofyear = cal.get(Calendar.DAY_OF_YEAR);

        int year = cal.get(Calendar.YEAR);
        String strYear = String.valueOf(year);

        int month = cal.get(Calendar.MONTH);
        String strMonth = null;
        if (month <= 8) {
            strMonth = "0" + (++month);
        } else {
            strMonth = String.valueOf(month);
        }

        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);

        int dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
        String strDayOfMonth = String.valueOf(dayofmonth);

        System.out.println("Date: (" + dayofweek + ") " + dayofmonth + "-" + (++month) + "-" + year);

        tvCreateNoteDate = (TextView) findViewById(R.id.note_tvCreateNoteDifferentDate);
        tvCreateNoteDate.setText(strDayOfMonth + "-" + strMonth + "-" + strYear);
    }

    private void setTime() {
        Calendar cal = Calendar.getInstance();
        int millisecond = cal.get(Calendar.MILLISECOND);
        int second = cal.get(Calendar.SECOND);
        String strSecond = null;
        if (second <= 9) {
            strSecond = "0" + second;
        } else {
            strSecond = String.valueOf(second);
        }
        int minute = cal.get(Calendar.MINUTE);
        String strMinute = null;
        if (minute <= 9) {
            strMinute = "0" + minute;
        } else {
            strMinute = String.valueOf(minute);
        }
        //12 hour format
        int hour = cal.get(Calendar.HOUR);
        //24 hour format
        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
        String strHourOfDay = String.valueOf(hourofday);
        System.out.println("Time: " + hourofday + ":" + minute + ":" + second);

        tvCreateNoteTime = (TextView) findViewById(R.id.note_tvCreateNoteDifferentTime);
        tvCreateNoteTime.setText(strHourOfDay + " : " + strMinute + " : " + strSecond);
    }

    @Override
    public void onBackPressed() {
        if (hasChanges()) {
            if (!isSavedFlagDifferent) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteDifferentActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle(R.string.savedialog_title);
                builder.setMessage(R.string.savedialog_message);

                //positive button.
                builder.setPositiveButton(R.string.savedialog_positivebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (isCardForUpdate == true) {
                            deleteItemFromTable(differentDTO);
                        }
                        isCardForUpdate = false;

////////////////
                        //заполняем БД данными.

                        //класс SQLiteDatabase предназначен для управления БД SQLite.
                        //если БД не существует, dbHelper вызовет метод onCreate(),
                        //если версия БД изменилась, dbHelper вызовет метод onUpgrade().

                        //в любом случае вернётся существующая, толькочто созданная или обновлённая БД.
                        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

                        //класс ContentValues используется для добавления новых строк в таблицу.
                        //каждый объект этого класса представляет собой одну строку таблицы и
                        //выглядит, как массив с именами столбцов и значениями, которые им соответствуют.
                        ContentValues contentValues = new ContentValues();

                        //добавляем пары ключ-значение.

                        noteCategory = "different";
                        noteTitle = etCreateNoteTitle.getText().toString();
                        noteDescription = etNoteDescription.getText().toString();
                        noteDate = tvCreateNoteDate.getText().toString();


                        note = new DifferentDTO(noteTitle, noteDescription, noteDate);
                        System.out.println(note);


                        contentValues.put(TABLE_NOTES_DIFFERENT_KEY_TITLE, noteTitle);
                        contentValues.put(TABLE_NOTES_DIFFERENT_KEY_DATE, noteDate);
                        contentValues.put(TABLE_NOTES_DIFFERENT_KEY_DESCRIPTION, noteDescription);
                        //id заполнится автоматически.

                        //вставляем подготовленные строки в таблицу.
                        //второй аргумент используется для вставки пустой строки,
                        //сейчас он нам не нужен, поэтому он = null.
                        sqLiteDatabase.insert(DBHelper.TABLE_NOTES_DIFFERENT_NAME, null, contentValues);
                        Log.d(LOG_TAG, "Date inserted");
                        //закрываем соединение с БД.
                        dbHelper.close();
////////////////////////
                        isSavedFlagDifferent = true;
                        isCardForUpdate = false;
                        CreateNoteDifferentActivity.this.finish();
                    }

                });

                //negative button.
                builder.setNegativeButton(R.string.savedialog_negativebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        isCardForUpdate = false;
                        CreateNoteDifferentActivity.this.finish();
                    }
                });

                //negative button.
                builder.setNeutralButton(R.string.savedialog_neutralbutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            } else {
                isCardForUpdate = false;
                CreateNoteDifferentActivity.this.finish();
                super.onBackPressed();
            }
        } else {
            CreateNoteDifferentActivity.this.finish();
        }
    }

    public boolean hasChanges() {
        boolean hasChanges = false;
        if (!oldTitle.equals(etCreateNoteTitle.getText().toString()) ||
                !oldDescription.equals(etNoteDescription.getText().toString())
                ) {
            hasChanges = true;
        }
        return hasChanges;
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
