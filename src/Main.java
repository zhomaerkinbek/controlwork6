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
//        LocalDate currentDate = LocalDate.now();
//        var dow = currentDate.getDayOfWeek().getValue();
//        int dom = currentDate.getDayOfMonth();
//        int doy = currentDate.getDayOfYear();
//        String m = currentDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
//        int y = currentDate.getYear();
//        System.out.println("current local date : " + currentDate);
//        System.out.println("dayOfWeek from a date in Java 8 : " + dow);
//        System.out.println("dayOfMonth from date in JDK 8: " + dom);
//        System.out.println("dayOfYear from a date in Java SE 8 : " + doy);
//        System.out.println("month from a date in Java 1.8 : " + m);
//        System.out.println("year from date in JDK 1.8 : " + y);
//        var calen = new CalendarModel();
//        System.out.println(calen.getDays());
//
//        calendarModel.getDays().get(0).getTasks().add(new Task("asd", "asds", "asd", Arrays.stream(TypeOfTask.values()).filter(e -> e.getName().equalsIgnoreCase("обычная задача")).findFirst().get()));
//        System.out.println(calendarModel.getDays().get(0).getTasks().get(0).getType());
//        CalendarModel.writeFile(calendarModel.getDays());
    }
}
