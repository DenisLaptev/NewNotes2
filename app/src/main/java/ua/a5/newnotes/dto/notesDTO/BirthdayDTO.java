package ua.a5.newnotes.dto.notesDTO;

import java.io.Serializable;

/**
 * Created by A5 Android Intern 2 on 15.05.2017.
 */

public class BirthdayDTO implements Serializable {
    private String name;
    private int day;
    private int month;
    private String stringMonth;
    private int year;

    public BirthdayDTO(String name, int day, int month, int year) {
        this.name = name;
        this.day = day;
        this.month = month;
        stringMonth = generateStringMonth(month);
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getStringMonth() {
        return stringMonth;
    }

    public void setStringMonth(String stringMonth) {
        this.stringMonth = stringMonth;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }



    public String generateStringMonth(int month){
        String stringMonth = null;
        switch (month){
            case 0:
                stringMonth = "января";
                //stringMonth = context.getResources().getString(R.string.january);
                break;

            case 1:
                stringMonth = "февраля";
                //stringMonth = context.getResources().getString(R.string.february);
                break;

            case 2:
                stringMonth = "марта";
                //stringMonth = context.getResources().getString(R.string.march);
                break;

            case 3:
                stringMonth = "апреля";
                //stringMonth = context.getResources().getString(R.string.april);
                break;

            case 4:
                stringMonth = "мая";
                //stringMonth = context.getResources().getString(R.string.may);
                break;

            case 5:
                stringMonth = "июня";
                //stringMonth = context.getResources().getString(R.string.june);
                break;

            case 6:
                stringMonth = "июля";
                //stringMonth = context.getResources().getString(R.string.july);
                break;

            case 7:
                stringMonth = "августа";
                //stringMonth = context.getResources().getString(R.string.august);
                break;

            case 8:
                stringMonth = "сентября";
                //stringMonth = context.getResources().getString(R.string.september);
                break;

            case 9:
                stringMonth = "октября";
                //stringMonth = context.getResources().getString(R.string.october);
                break;

            case 10:
                stringMonth = "ноября";
                //stringMonth = context.getResources().getString(R.string.november);
                break;

            case 11:
                stringMonth = "декабря";
                //stringMonth = context.getResources().getString(R.string.december);
                break;
        }
        return stringMonth;
    }

}
