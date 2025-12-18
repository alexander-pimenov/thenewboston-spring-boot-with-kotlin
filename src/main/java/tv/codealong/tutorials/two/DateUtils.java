package tv.codealong.tutorials.two;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateUtils {

    public LocalDate parseIsoDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Invalid ISO date format: " + dateString, e);
        }
    }

    public boolean isDateInFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    public LocalDate addBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int addedDays = 0;

        while (addedDays < days) {
            result = result.plusDays(1);
            if (!isWeekend(result)) {
                addedDays++;
            }
        }
        return result;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public String formatToIso(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}
