package tv.codealong.tutorials.springboot.thenewboston.second_service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String s) {
        super(s);
    }

    public InvalidDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
