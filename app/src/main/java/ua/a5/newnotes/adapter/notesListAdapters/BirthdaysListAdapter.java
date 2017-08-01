package ua.a5.newnotes.adapter.notesListAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ua.a5.newnotes.DAO.DBHelper;
import ua.a5.newnotes.R;
import ua.a5.newnotes.activities.CreateNoteBirthdaysActivity;
import ua.a5.newnotes.dto.notesDTO.BirthdayDTO;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_DAY;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_MONTH;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_NAME;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_KEY_YEAR;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_BIRTHDAYS_NAME;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_BIRTHDAYS;
import static ua.a5.newnotes.utils.Constants.isCardForUpdate;

/**
 * Created by A5 Android Intern 2 on 15.05.2017.
 */

public class BirthdaysListAdapter extends RecyclerView.Adapter<BirthdaysListAdapter.BirthdaysViewHolder> {

    public interface BirthdayClickListener {
        void onClick(BirthdayDTO birthdayDTO);
    }

    //Локальный слушатель для адаптера.
    public interface ItemClickListener {
        void onClick(int position);
    }

    private Context context;
    //хранилище данных.
    private List<BirthdayDTO> birthdaysDTOList;
    private BirthdayClickListener birthdayClickListener;
    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onClick(int position) {
            //TODO
            birthdayClickListener.onClick(birthdaysDTOList.get(position));
        }
    };

    public BirthdaysListAdapter(Context context,
                                List<BirthdayDTO> birthdaysDTOList,
                                BirthdayClickListener birthdayClickListener
    ) {
        this.context = context;
        this.birthdaysDTOList = birthdaysDTOList;
        this.birthdayClickListener = birthdayClickListener;
    }

    @Override
    public BirthdaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notes_birthdays, parent, false);
        return new BirthdaysViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(final BirthdaysViewHolder holder, final int position) {
        final BirthdayDTO item = birthdaysDTOList.get(position);
        holder.tvName.setText(item.getName());
        //holder.tvDate.setText(item.getDay() + " " + item.getStringMonth());
        holder.tvDate.setText(item.getDay() + " " + generateStringMonth(item.getMonth()));
        holder.ivPictureBirthdayMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCardForUpdate = true;
                Intent intent = new Intent(context, CreateNoteBirthdaysActivity.class);
                intent.putExtra(KEY_UPDATE_BIRTHDAYS, item);
                context.startActivity(intent);
            }
        });


        holder.ivPictureBirthdayMenuDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                builder.setTitle(R.string.deletedialog_title);
                builder.setMessage(R.string.deletedialog_message);

                //positive button.
                builder.setPositiveButton(R.string.deletedialog_positivebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteItem(position, birthdaysDTOList);

                        notifyItemRemoved(position);
                    }

                });

                //negative button.
                builder.setNegativeButton(R.string.deletedialog_negativebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
                builder.show();
            }
        });

















    }
    public String generateStringMonth(int month){
        String stringMonth = null;
        switch (month){
            case 0:
                //stringMonth = "января";
                stringMonth = context.getResources().getString(R.string.january);
                break;

            case 1:
                //stringMonth = "февраля";
                stringMonth = context.getResources().getString(R.string.february);
                break;

            case 2:
                //stringMonth = "марта";
                stringMonth = context.getResources().getString(R.string.march);
                break;

            case 3:
                //stringMonth = "апреля";
                stringMonth = context.getResources().getString(R.string.april);
                break;

            case 4:
                //stringMonth = "мая";
                stringMonth = context.getResources().getString(R.string.may);
                break;

            case 5:
                //stringMonth = "июня";
                stringMonth = context.getResources().getString(R.string.june);
                break;

            case 6:
                //stringMonth = "июля";
                stringMonth = context.getResources().getString(R.string.july);
                break;

            case 7:
                //stringMonth = "августа";
                stringMonth = context.getResources().getString(R.string.august);
                break;

            case 8:
                //stringMonth = "сентября";
                stringMonth = context.getResources().getString(R.string.september);
                break;

            case 9:
                //stringMonth = "октября";
                stringMonth = context.getResources().getString(R.string.october);
                break;

            case 10:
                //stringMonth = "ноября";
                stringMonth = context.getResources().getString(R.string.november);
                break;

            case 11:
                //stringMonth = "декабря";
                stringMonth = context.getResources().getString(R.string.december);
                break;
        }
        return stringMonth;
    }

    private void deleteItem(int position, List<BirthdayDTO> birthdaysDTOList) {
        int currentPosition = position;
        deleteItemFromTable(position, birthdaysDTOList);
        notifyItemRemoved(currentPosition);
        birthdaysDTOList.remove(currentPosition);
        notifyItemRemoved(currentPosition);
    }


    private void deleteItemFromTable(int position, List<BirthdayDTO> birthdaysDTOList) {
        int currentPosition = position;

//////////////////---------------------->
        //для работы с БД.
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NOTES_BIRTHDAYS_NAME,
                TABLE_NOTES_BIRTHDAYS_KEY_NAME + " = ? AND "
                        + TABLE_NOTES_BIRTHDAYS_KEY_DAY + " = ? AND "
                        + TABLE_NOTES_BIRTHDAYS_KEY_MONTH + " = ? AND "
                        + TABLE_NOTES_BIRTHDAYS_KEY_YEAR + " = ? ",
                new String[]{
                        birthdaysDTOList.get(currentPosition).getName(),
                        String.valueOf(birthdaysDTOList.get(currentPosition).getDay()),
                        String.valueOf(birthdaysDTOList.get(currentPosition).getMonth()),
                        String.valueOf(birthdaysDTOList.get(currentPosition).getYear())
                });

        //закрываем соединение с БД.
        dbHelper.close();
//////////////////---------------------->
    }

    @Override
    public int getItemCount() {
        return birthdaysDTOList.size();
    }

    public static class BirthdaysViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView tvName;
        TextView tvDate;
        ImageView ivPictureBirthdayMenu;
        ImageView ivPictureBirthdayMenuDelete;

        ItemClickListener itemClickListener;

        public BirthdaysViewHolder(View itemView, final ItemClickListener itemClickListener) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view_birthdays);
            tvName = (TextView) itemView.findViewById(R.id.title_birthdays);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date_birthdays);
            ivPictureBirthdayMenu = (ImageView) itemView.findViewById(R.id.birthdays_card_menu);
            ivPictureBirthdayMenuDelete = (ImageView) itemView.findViewById(R.id.birthdays_card_menu_delete);

            this.itemClickListener = itemClickListener;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }

    public void setBirthdaysDTOList(List<BirthdayDTO> birthdaysDTOList) {
        this.birthdaysDTOList = birthdaysDTOList;
    }
}
