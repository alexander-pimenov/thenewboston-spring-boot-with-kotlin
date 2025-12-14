package tv.codealong.tutorials.various.yandex.test6_4_real_java_with_record;

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
