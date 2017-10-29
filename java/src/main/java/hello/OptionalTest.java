package hello;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

public class OptionalTest {

    public static void main(String... args) {
        OptionalTest optionalTest = new OptionalTest();

        System.out.println(optionalTest.getValue("Test").orElse("Test2").equals("Test"));
        System.out.println(optionalTest.getValueAsEmpty().orElse("Test2").equals("Test2"));
        System.out.println(optionalTest.getValueAsEmpty().orElseGet(() -> "Test3").equals("Test3"));
        try {
            optionalTest.getValueAsEmpty().get();
            System.out.println(false);
        } catch (NoSuchElementException e) {
            System.out.println(true);
        }
        System.out.println(!optionalTest.getValueAsEmpty().isPresent());
        try {
            optionalTest.getValueAsEmpty().orElseThrow((Supplier<Throwable>) () -> new Throwable("Empty"));
            System.out.println(false);
        } catch (Throwable e) {
            System.out.println(true);
        }
    }

    Optional<String> getValue(String val) {
        return Optional.of(val);
    }

    Optional<String> getValueAsEmpty() {
        return Optional.empty();
    }
}
