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
import android.widget.Toast;

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
import ua.a5.newnotes.dto.notesDTO.IdeaDTO;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_IDEAS_KEY_DATE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_IDEAS_KEY_DESCRIPTION;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_IDEAS_KEY_TITLE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_IDEAS_NAME;
import static ua.a5.newnotes.activities.OptionsMenuActivity.mIsPremium;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_IDEAS;
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

public class CreateNoteIdeasActivity extends AppCompatActivity {

    boolean isSavedFlagIdea;

    public static SpannableString bufferSpannableString = null;

    String initialWord;
    String strRegExp;

    TextView tvCreateNoteDate;
    TextView tvCreateNoteTime;

    EditText etCreateNoteTitle;
    EditText etNoteDescription;


    //для работы с БД.
    DBHelper dbHelper;

    IdeaDTO note;
    String noteCategory;
    String noteTitle;
    String noteDescription;
    String noteDate;

    IdeaDTO ideaDTO;


    //для баннера////////////////////////////////////////////////////
    protected AdView mAdView;
    //для баннера////////////////////////////////////////////////////


    //для Interstitial////////////////////////////////////////////////////
    InterstitialAd mInterstitialAd;
    //для Interstitial////////////////////////////////////////////////////


    private Toolbar toolbarIdea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note_ideas);

        toolbarIdea = (Toolbar) findViewById(R.id.toolbar_idea);
        toolbarIdea.setTitle("IDEA");
        setSupportActionBar(toolbarIdea);

        if (mIsPremium == false) {
//Initializing the Google Mobile Ads SDK
            MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
//Initializing the Google Mobile Ads SDK


            //для баннера////////////////////////////////////////////////////
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    // Check the LogCat to get your test device ID
                    .addTestDevice("9E89078D0DC2D94ADC3D89109C5B6E24")
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

        isSavedFlagIdea = false;

        //для работы с БД.
        dbHelper = new DBHelper(this);
        setDateAndTime();

        noteCategory = "ideas";

        etCreateNoteTitle = (EditText) findViewById(R.id.note_etCreateNoteIdeasTitle);
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


        etNoteDescription = (EditText) findViewById(R.id.et_note_ideas_description);
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
            ideaDTO = (IdeaDTO) getIntent().getSerializableExtra(KEY_UPDATE_IDEAS);
            etCreateNoteTitle.setText(ideaDTO.getTitle());
            etNoteDescription.setText(ideaDTO.getDescription());
        } else {
            ideaDTO = new IdeaDTO(
                    new String(""),
                    new String(""),
                    new String(
                            getCurrentDay() + "-" +
                                    getCurrentMonth() + "-" +
                                    getCurrentYear()
                    )
            );
        }



       /*


        btnCreateNoteSave = (Button) findViewById(R.id.note_btnCreateNoteIdeasSave);
        btnCreateNoteSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCardForUpdate == true) {
                    deleteItemFromTable(ideaDTO);
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


                noteTitle = etCreateNoteTitle.getText().toString();
                noteDescription = etNoteDescription.getText().toString();
                noteDate = tvCreateNoteDate.getText().toString();

                note = new IdeaDTO(noteTitle, noteDescription, noteDate);

                contentValues.put(TABLE_NOTES_IDEAS_KEY_TITLE, noteTitle);
                contentValues.put(TABLE_NOTES_IDEAS_KEY_DATE, noteDate);
                contentValues.put(TABLE_NOTES_IDEAS_KEY_DESCRIPTION, noteDescription);
                //id заполнится автоматически.

                //вставляем подготовленные строки в таблицу.
                //второй аргумент используется для вставки пустой строки,
                //сейчас он нам не нужен, поэтому он = null.
                sqLiteDatabase.insert(DBHelper.TABLE_NOTES_IDEAS_NAME, null, contentValues);
                Log.d(LOG_TAG, "Date inserted");
                //закрываем соединение с БД.
                dbHelper.close();
////////////////////////
                isSavedFlagIdea = true;

            }
        });

        btnCreateNoteNotesMenu = (Button) findViewById(R.id.note_btnCreateNoteIdeasNotesMenu);
        btnCreateNoteNotesMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCardForUpdate = false;
                onBackPressed();
                finish();
            }
        });


        */

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_idea, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_idea_save:
                Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();

                if (isCardForUpdate == true) {
                    deleteItemFromTable(ideaDTO);
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


                noteTitle = etCreateNoteTitle.getText().toString();
                noteDescription = etNoteDescription.getText().toString();
                noteDate = tvCreateNoteDate.getText().toString();

                note = new IdeaDTO(noteTitle, noteDescription, noteDate);

                contentValues.put(TABLE_NOTES_IDEAS_KEY_TITLE, noteTitle);
                contentValues.put(TABLE_NOTES_IDEAS_KEY_DATE, noteDate);
                contentValues.put(TABLE_NOTES_IDEAS_KEY_DESCRIPTION, noteDescription);
                //id заполнится автоматически.

                //вставляем подготовленные строки в таблицу.
                //второй аргумент используется для вставки пустой строки,
                //сейчас он нам не нужен, поэтому он = null.
                sqLiteDatabase.insert(DBHelper.TABLE_NOTES_IDEAS_NAME, null, contentValues);
                Log.d(LOG_TAG, "Date inserted");
                //закрываем соединение с БД.
                dbHelper.close();
////////////////////////
                isSavedFlagIdea = true;

                Toast.makeText(this, note.toString(), Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_idea_delete:
                Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();


                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteIdeasActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("Delete?");
                builder.setMessage("Do You Really Want To Delete?");

                //positive button.
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItemFromTable(ideaDTO);
                        CreateNoteIdeasActivity.this.finish();

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
        }

        isCardForUpdate = false;
        return true;

    }


    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }


    private void deleteItemFromTable(IdeaDTO ideaDTO) {
//////////////////---------------------->
        //для работы с БД.
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NOTES_IDEAS_NAME,
                TABLE_NOTES_IDEAS_KEY_TITLE + " = ? AND "
                        + TABLE_NOTES_IDEAS_KEY_DATE + " = ? AND "
                        + TABLE_NOTES_IDEAS_KEY_DESCRIPTION + " = ? ",
                new String[]{
                        ideaDTO.getTitle(),
                        String.valueOf(ideaDTO.getDate()),
                        String.valueOf(ideaDTO.getDescription())
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


        tvCreateNoteDate = (TextView) findViewById(R.id.note_tvCreateNoteIdeasDate);
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

        tvCreateNoteTime = (TextView) findViewById(R.id.note_tvCreateNoteIdeasTime);
        tvCreateNoteTime.setText(strHourOfDay + " : " + strMinute + " : " + strSecond);
    }

    @Override
    public void onBackPressed() {


        if (!isSavedFlagIdea) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteIdeasActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle("Save Changes?");
            builder.setMessage("Do You Want To Save Changes?");

            //positive button.
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(CreateNoteIdeasActivity.this, "saved", Toast.LENGTH_SHORT).show();



                    if (isCardForUpdate == true) {
                        deleteItemFromTable(ideaDTO);
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


                    noteTitle = etCreateNoteTitle.getText().toString();
                    noteDescription = etNoteDescription.getText().toString();
                    noteDate = tvCreateNoteDate.getText().toString();

                    note = new IdeaDTO(noteTitle, noteDescription, noteDate);

                    contentValues.put(TABLE_NOTES_IDEAS_KEY_TITLE, noteTitle);
                    contentValues.put(TABLE_NOTES_IDEAS_KEY_DATE, noteDate);
                    contentValues.put(TABLE_NOTES_IDEAS_KEY_DESCRIPTION, noteDescription);
                    //id заполнится автоматически.

                    //вставляем подготовленные строки в таблицу.
                    //второй аргумент используется для вставки пустой строки,
                    //сейчас он нам не нужен, поэтому он = null.
                    sqLiteDatabase.insert(DBHelper.TABLE_NOTES_IDEAS_NAME, null, contentValues);
                    Log.d(LOG_TAG, "Date inserted");
                    //закрываем соединение с БД.
                    dbHelper.close();
////////////////////////
                    isSavedFlagIdea = true;


                    isCardForUpdate = false;
                    CreateNoteIdeasActivity.this.finish();

                }

            });

            //negative button.
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    isCardForUpdate = false;
                    CreateNoteIdeasActivity.this.finish();
                }
            });

            //negative button.
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.show();
        } else {
            isCardForUpdate = false;
            CreateNoteIdeasActivity.this.finish();
            super.onBackPressed();
        }

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