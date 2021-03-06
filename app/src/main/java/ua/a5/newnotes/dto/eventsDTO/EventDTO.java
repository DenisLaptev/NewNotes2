package ua.a5.newnotes.dto.eventsDTO;

import java.io.Serializable;

/**
 * Created by A5 Android Intern 2 on 15.05.2017.
 */

public class EventDTO  implements Serializable{
    private String title;
    private String description;
    private int day;
    private int month;
    private int year;

    public EventDTO(String title, String description, int day, int month, int year) {
        this.title = title;
        this.description = description;
        this.day = day;
        this.month = month;
        this.year = year;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


    @Override
    public String toString() {
        return "EventDTO{" +
                "title='" + title +
                '}';
    }
}
