package ua.a5.newnotes.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.Calendar;

import ua.a5.newnotes.DAO.DBHelper;
import ua.a5.newnotes.R;
import ua.a5.newnotes.dto.notesDTO.BirthdayDTO;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_DAY;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_MONTH;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_NAME;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_YEAR;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_NAME;
import static ua.a5.newnotes.activities.OptionsMenuActivity.mIsPremium;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_BIRTHDAYS;
import static ua.a5.newnotes.utils.Constants.LOG_TAG;
import static ua.a5.newnotes.utils.Constants.isCardForUpdate;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentDay;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentMonth;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentYear;

public class CreateNoteBirthdaysActivity extends AppCompatActivity {

    boolean isSavedFlagBirthday;

    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID = 0;


    EditText etName;
    private TextView dateDisplay;
    private ImageView ivPickDate;


    //для работы с БД.
    DBHelper dbHelper;

    BirthdayDTO note;
    String noteCategory;
    String noteName;
    int noteDay;
    int noteMonth;
    int noteYear;

    BirthdayDTO birthdayDTO;


    //для баннера////////////////////////////////////////////////////
    protected AdView mAdView;
    //для баннера////////////////////////////////////////////////////

    //для Interstitial////////////////////////////////////////////////////
    InterstitialAd mInterstitialAd;
    //для Interstitial////////////////////////////////////////////////////


    private Toolbar toolbarBirthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note_birthdays);

        toolbarBirthday = (Toolbar) findViewById(R.id.toolbar_birthday);
        toolbarBirthday.setTitle("BIRTHDAY");
        setSupportActionBar(toolbarBirthday);

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
            }
//для Interstitial////////////////////////////////////////////////////
        }

        isSavedFlagBirthday = false;

        //для работы с БД.
        dbHelper = new DBHelper(this);


        etName = (EditText) findViewById(R.id.et_name);
        //этот слушатель позволяет убирать клавиатуру EditText
        //при нажатии на пустое пространство.
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });


        dateDisplay = (TextView) findViewById(R.id.tv_birthday_date);
        ivPickDate = (ImageView) findViewById(R.id.iv_date_picker);

        ivPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date
        updateDisplay();


        if (isCardForUpdate == true && getIntent() != null) {
            birthdayDTO = (BirthdayDTO) getIntent().getSerializableExtra(KEY_UPDATE_BIRTHDAYS);
            etName.setText(birthdayDTO.getName());


            if (birthdayDTO.getMonth() + 1 < 10) {
                dateDisplay.setText(
                        new StringBuilder()
                                // Month is 0 based so add 1
                                .append(birthdayDTO.getDay()).append("-0")
                                .append(birthdayDTO.getMonth() + 1).append("-")
                                .append(birthdayDTO.getYear()).append(" "));
            } else {
                dateDisplay.setText(
                        new StringBuilder()
                                // Month is 0 based so add 1
                                .append(birthdayDTO.getDay()).append("-")
                                .append(birthdayDTO.getMonth() + 1).append("-")
                                .append(birthdayDTO.getYear()).append(" "));
            }

        } else {
            birthdayDTO = new BirthdayDTO(
                    new String(""),
                    getCurrentDay(),
                    getCurrentMonth(),
                    getCurrentYear()
            );
        }







        /*

        btnSaveBirthdayNote = (Button) findViewById(R.id.btn_save);
        btnSaveBirthdayNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isCardForUpdate == true) {
                    deleteItemFromTable(birthdayDTO);
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

                noteCategory = "birthdays";
                noteName = etName.getText().toString();
                noteDay = mDay;
                noteMonth = mMonth;
                noteYear = mYear;

                note = new BirthdayDTO(noteName, noteDay, noteMonth, noteYear);

                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_NAME, noteName);
                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_DAY, noteDay);
                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_MONTH, noteMonth);
                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_YEAR, noteYear);
                //id заполнится автоматически.

                //вставляем подготовленные строки в таблицу.
                //второй аргумент используется для вставки пустой строки,
                //сейчас он нам не нужен, поэтому он = null.
                sqLiteDatabase.insert(DBHelper.TABLE_NOTES_BIRTHDAYS_NAME, null, contentValues);
                Log.d(LOG_TAG, "Date inserted");
                //закрываем соединение с БД.
                dbHelper.close();
////////////////////////

                isSavedFlagBirthday = true;
            }
        });

        btnAllNotes = (Button) findViewById(R.id.btn_allnotes);
        btnAllNotes.setOnClickListener(new View.OnClickListener() {
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
        getMenuInflater().inflate(R.menu.menu_birthday, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_birthday_save:
                Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();


                if (isCardForUpdate == true) {
                    deleteItemFromTable(birthdayDTO);
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

                noteCategory = "birthdays";
                noteName = etName.getText().toString();
                noteDay = mDay;
                noteMonth = mMonth;
                noteYear = mYear;

                note = new BirthdayDTO(noteName, noteDay, noteMonth, noteYear);

                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_NAME, noteName);
                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_DAY, noteDay);
                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_MONTH, noteMonth);
                contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_YEAR, noteYear);
                //id заполнится автоматически.

                //вставляем подготовленные строки в таблицу.
                //второй аргумент используется для вставки пустой строки,
                //сейчас он нам не нужен, поэтому он = null.
                sqLiteDatabase.insert(DBHelper.TABLE_NOTES_BIRTHDAYS_NAME, null, contentValues);
                Log.d(LOG_TAG, "Date inserted");
                //закрываем соединение с БД.
                dbHelper.close();
////////////////////////

                isSavedFlagBirthday = true;

                Toast.makeText(this, note.toString(), Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_birthday_delete:
                Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteBirthdaysActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("Delete?");
                builder.setMessage("Do You Really Want To Delete?");

                //positive button.
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItemFromTable(birthdayDTO);
                        CreateNoteBirthdaysActivity.this.finish();
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


    private void deleteItemFromTable(BirthdayDTO birthdayDTO) {
//////////////////---------------------->
        //для работы с БД.
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NOTES_BIRTHDAYS_NAME,
                TABLE_NOTES_BIRTHDAYS_KEY_NAME + " = ? AND "
                        + TABLE_NOTES_BIRTHDAYS_KEY_DAY + " = ? AND "
                        + TABLE_NOTES_BIRTHDAYS_KEY_MONTH + " = ? AND "
                        + TABLE_NOTES_BIRTHDAYS_KEY_YEAR + " = ? ",
                new String[]{
                        birthdayDTO.getName(),
                        String.valueOf(birthdayDTO.getDay()),
                        String.valueOf(birthdayDTO.getMonth()),
                        String.valueOf(birthdayDTO.getYear())
                });

        //закрываем соединение с БД.
        dbHelper.close();
//////////////////---------------------->
    }


    //метод, для убирания клавиатуры EditText при нажатии на пустое пространство.
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    //For DatePicker
    private void updateDisplay() {

        if (mMonth + 1 < 10) {
            this.dateDisplay.setText(
                    new StringBuilder()
                            // Month is 0 based so add 1
                            .append(mDay).append("-0")
                            .append(mMonth + 1).append("-")
                            .append(mYear).append(" "));
        } else {
            this.dateDisplay.setText(
                    new StringBuilder()
                            // Month is 0 based so add 1
                            .append(mDay).append("-")
                            .append(mMonth + 1).append("-")
                            .append(mYear).append(" "));
        }

    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }


    @Override
    public void onBackPressed() {


        if (!isSavedFlagBirthday) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteBirthdaysActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle("Save Changes?");
            builder.setMessage("Do You Want To Save Changes?");

            //positive button.
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(CreateNoteBirthdaysActivity.this, "saved", Toast.LENGTH_SHORT).show();


                    if (isCardForUpdate == true) {
                        deleteItemFromTable(birthdayDTO);
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

                    noteCategory = "birthdays";
                    noteName = etName.getText().toString();
                    noteDay = mDay;
                    noteMonth = mMonth;
                    noteYear = mYear;

                    note = new BirthdayDTO(noteName, noteDay, noteMonth, noteYear);

                    contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_NAME, noteName);
                    contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_DAY, noteDay);
                    contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_MONTH, noteMonth);
                    contentValues.put(TABLE_NOTES_BIRTHDAYS_KEY_YEAR, noteYear);
                    //id заполнится автоматически.

                    //вставляем подготовленные строки в таблицу.
                    //второй аргумент используется для вставки пустой строки,
                    //сейчас он нам не нужен, поэтому он = null.
                    sqLiteDatabase.insert(DBHelper.TABLE_NOTES_BIRTHDAYS_NAME, null, contentValues);
                    Log.d(LOG_TAG, "Date inserted");
                    //закрываем соединение с БД.
                    dbHelper.close();
////////////////////////

                    isSavedFlagBirthday = true;


                    isCardForUpdate = false;
                    CreateNoteBirthdaysActivity.this.finish();

                }

            });

            //negative button.
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    isCardForUpdate = false;
                    CreateNoteBirthdaysActivity.this.finish();
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
            CreateNoteBirthdaysActivity.this.finish();
            super.onBackPressed();
        }
    }



    /*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isCardForUpdate = false;

        if (isSavedFlagBirthday) {
            BirthdayDTO newBirthdayDTO = new BirthdayDTO(
                    etName.getText().toString(),
                    mDay,
                    mMonth,
                    mYear
            );

            Intent intent = new Intent(this, BirthdayActivity.class);
            intent.putExtra(KEY_BIRTHDAY_DTO, newBirthdayDTO);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, BirthdayActivity.class);
            intent.putExtra(KEY_BIRTHDAY_DTO, birthdayDTO);
            startActivity(intent);
            finish();
        }
    }

*/

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
