package tv.codealong.tutorials.various.yandex.test6_3_real;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 *  Декоратор через наследование
 */
public class SafePrintStream extends PrintStream {
    public SafePrintStream() throws UnsupportedEncodingException {
        super(System.out, true, StandardCharsets.UTF_8.name());
    }
}
