package ua.a5.newnotes.fragments.events_fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;

import ua.a5.newnotes.DAO.DBHelper;
import ua.a5.newnotes.R;
import ua.a5.newnotes.activities.CreateEventActivity;
import ua.a5.newnotes.adapter.eventsListAdapters.TodayEventsListAdapter;
import ua.a5.newnotes.dto.eventsDTO.EventDTO;
import ua.a5.newnotes.fragments.AbstractTabFragment;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_BEGIN_DAY;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_BEGIN_HOUR;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_BEGIN_MINUTE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_BEGIN_MONTH;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_BEGIN_YEAR;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_DESCRIPTION;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_END_DAY;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_END_HOUR;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_END_MINUTE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_END_MONTH;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_ID;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_LOCATION;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_STRING_BEGIN_MONTH;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_KEY_TITLE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_EVENTS_NAME;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_EVENTS;
import static ua.a5.newnotes.utils.Constants.LOG_TAG;
import static ua.a5.newnotes.utils.Constants.flagWhenItemDeletedAll;
import static ua.a5.newnotes.utils.Constants.isCardForUpdate;


/**
 * Created by A5 Android Intern 2 on 28.04.2017.
 */

public class AllEventsFragment extends AbstractTabFragment implements TodayEventsListAdapter.EventClickListener {

    FloatingActionsMenu menuMultipleActions;

    private static final int LAYOUT = R.layout.fragment_events_all;


    //для работы с БД.
    DBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;
    String orderBy;
    String strConsoleOutput = "";

    RecyclerView recyclerView;
    TodayEventsListAdapter adapter;


    public static AllEventsFragment getInstance(Context context) {

        Bundle args = new Bundle();
        AllEventsFragment fragment = new AllEventsFragment();
        fragment.setArguments(args);
        fragment.setContext(context);
        fragment.setTitle(context.getString(R.string.menu_events_item_all_events));

        return fragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        adapter = new TodayEventsListAdapter(context, getAllEventsList(), this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void update(){
        adapter = new TodayEventsListAdapter(context, getAllEventsList(), this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_view_events_all);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new TodayEventsListAdapter(context, getAllEventsList(), this);
        recyclerView.setAdapter(adapter);

        menuMultipleActions = (FloatingActionsMenu) getActivity().findViewById(R.id.multiple_actions_events);

        if (flagWhenItemDeletedAll) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
            flagWhenItemDeletedAll = false;
        }
        return view;


    }


    public  List<EventDTO> getAllEventsList() {
        List<EventDTO> eventsData = new ArrayList<>();

//////////////////---------------------->

        //для работы с БД.
        dbHelper = new DBHelper(getActivity());


        //класс SQLiteDatabase предназначен для управления БД SQLite.
        //если БД не существует, dbHelper вызовет метод onCreate(),
        //если версия БД изменилась, dbHelper вызовет метод onUpgrade().

        //в любом случае вернётся существующая, толькочто созданная или обновлённая БД.
        sqLiteDatabase = dbHelper.getWritableDatabase();

        //метод rawQuery() возвращает объект типа Cursor,
        //его можно рассматривать как набор строк с данными.

        cursor = sqLiteDatabase.query(TABLE_EVENTS_NAME,null,null,null,null,null,null);
        //метод cursor.moveToFirst() делает 1-ю запись в cursor активной
        //и проверяет, есть ли в cursor что-то.
        if (cursor.moveToFirst()) {
            //получаем порядковые номера столбцов по их именам.
            int idIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_ID);
            int titleIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_TITLE);
            int locationIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_LOCATION);
            int begindayIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_BEGIN_DAY);
            int beginmonthIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_BEGIN_MONTH);
            int stringBeginmonthIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_STRING_BEGIN_MONTH);
            int beginyearIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_BEGIN_YEAR);

            int beginhourIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_BEGIN_HOUR);
            int beginminuteIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_BEGIN_MINUTE);
            int enddayIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_END_DAY);
            int endmonthIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_END_MONTH);
            int endhourIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_END_HOUR);
            int endminuteIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_END_MINUTE);
            int descriptionIndex = cursor.getColumnIndex(TABLE_EVENTS_KEY_DESCRIPTION);


            //с помощью метода .moveToNext() перебираем все строки в cursor-е.
            do {
                eventsData.add(new EventDTO(
                        cursor.getString(titleIndex),
                        cursor.getString(descriptionIndex),
                        cursor.getInt(begindayIndex),
                        cursor.getInt(beginmonthIndex),
                        cursor.getInt(beginyearIndex)
                ));
            } while (cursor.moveToNext());

        } else {
            Log.d(LOG_TAG, "0 rows");
        }
        System.out.println(strConsoleOutput);

        //в конце закрываем cursor. Освобождаем ресурс.
        cursor.close();

        //закрываем соединение с БД.
        dbHelper.close();

//////////////////---------------------->

        return eventsData;
    }

    @Override
    public void onClick(EventDTO eventDTO) {
        isCardForUpdate = true;
        Intent intent = new Intent(getContext(), CreateEventActivity.class);
        intent.putExtra(KEY_UPDATE_EVENTS, eventDTO);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        menuMultipleActions.collapse();
    }
}
