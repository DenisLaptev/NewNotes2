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
import ua.a5.newnotes.activities.CreateNoteDifferentActivity;
import ua.a5.newnotes.dto.notesDTO.DifferentDTO;

import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_KEY_DATE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_KEY_DESCRIPTION;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_KEY_TITLE;
import static ua.a5.newnotes.DAO.DBHelper.TABLE_NOTES_DIFFERENT_NAME;
import static ua.a5.newnotes.utils.Constants.KEY_UPDATE_DIFFERENT;
import static ua.a5.newnotes.utils.Constants.isCardForUpdate;

/**
 * Created by A5 Android Intern 2 on 15.05.2017.
 */

public class DifferentListAdapter extends RecyclerView.Adapter<DifferentListAdapter.DifferentViewHolder> {

    public interface DifferentClickListener {
        void onClick(DifferentDTO differentDTO);
    }

    //Локальный слушатель для адаптера.
    public interface ItemClickListener {
        void onClick(int position);
    }

    private Context context;
    //хранилище данных.
    private List<DifferentDTO> differentDTOList;
    private DifferentClickListener differentClickListener;
    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onClick(int position) {
            //TODO
            differentClickListener.onClick(differentDTOList.get(position));
        }
    };


    public DifferentListAdapter(Context context,
                                List<DifferentDTO> differentDTOList,
                                DifferentClickListener differentClickListener
    ) {
        this.context = context;
        this.differentDTOList = differentDTOList;
        this.differentClickListener = differentClickListener;
    }

    @Override
    public DifferentListAdapter.DifferentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notes_different, parent, false);
        return new DifferentListAdapter.DifferentViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(final DifferentListAdapter.DifferentViewHolder holder, final int position) {
        final DifferentDTO item = differentDTOList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());
        holder.tvDate.setText(item.getDate());
        holder.ivPictureDifferentMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCardForUpdate = true;
                Intent intent = new Intent(context, CreateNoteDifferentActivity.class);
                intent.putExtra(KEY_UPDATE_DIFFERENT, item);
                context.startActivity(intent);
            }
        });

        holder.ivPictureDifferentMenuDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
                builder.setTitle(R.string.deletedialog_title);
                builder.setMessage(R.string.deletedialog_message);

                //positive button.
                builder.setPositiveButton(R.string.deletedialog_positivebutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteItem(position, differentDTOList);
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

    private void deleteItem(int position, List<DifferentDTO> differentDTOList) {
        int currentPosition = position;
        deleteItemFromTable(position, differentDTOList);
        notifyItemRemoved(currentPosition);
        differentDTOList.remove(currentPosition);
        notifyItemRemoved(currentPosition);
    }


    private void deleteItemFromTable(int position, List<DifferentDTO> differentDTOList) {
        int currentPosition = position;

//////////////////---------------------->
        //для работы с БД.
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.delete(TABLE_NOTES_DIFFERENT_NAME,
                TABLE_NOTES_DIFFERENT_KEY_TITLE + " = ? AND "
                        + TABLE_NOTES_DIFFERENT_KEY_DATE + " = ? AND "
                        + TABLE_NOTES_DIFFERENT_KEY_DESCRIPTION + " = ? ",
                new String[]{
                        differentDTOList.get(currentPosition).getTitle(),
                        String.valueOf(differentDTOList.get(currentPosition).getDate()),
                        String.valueOf(differentDTOList.get(currentPosition).getDescription())
                });

        //закрываем соединение с БД.
        dbHelper.close();
//////////////////---------------------->
    }

    @Override
    public int getItemCount() {
        return differentDTOList.size();
    }

    public static class DifferentViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDate;
        ImageView ivPictureDifferentMenu;
        ImageView ivPictureDifferentMenuDelete;

        ItemClickListener itemClickListener;

        public DifferentViewHolder(View itemView, final ItemClickListener itemClickListener) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view_different);
            tvTitle = (TextView) itemView.findViewById(R.id.title_different);
            tvDescription = (TextView) itemView.findViewById(R.id.tv_description_different);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date_different);
            ivPictureDifferentMenu = (ImageView) itemView.findViewById(R.id.different_card_menu);
            ivPictureDifferentMenuDelete = (ImageView) itemView.findViewById(R.id.different_card_menu_delete);

            this.itemClickListener = itemClickListener;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onClick(getAdapterPosition());
                }
            });
        }
    }

    public void setDifferentDTOList(List<DifferentDTO> differentDTOList) {
        this.differentDTOList = differentDTOList;
    }
}
