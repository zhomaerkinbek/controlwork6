import controlwork.CalendarModel;
import controlwork.Control;
import controlwork.Task;
import controlwork.TypeOfTask;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;

public class Main {
    private static CalendarModel calendarModel = new CalendarModel();
    public static void main(String[] args){
        try {
            new Control("localhost", 9889).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
