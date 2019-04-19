package fs.playground;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JacksonBench {

    public static void main(String... args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        String jsonStr = getExampleJsonString(args[0]);

        for (int i = 0; i < 1000; i++) {
            mapper.readValue(jsonStr, Example.class);
        }

        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);

        System.out.println("start");

        int loop = Integer.parseInt(args[1]);
        long time = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            mapper.readValue(jsonStr, Example.class);
        }
        System.out.println("java-jackson " + (System.currentTimeMillis() - time));

//        System.gc();
//
//        while (true) {
//            Thread.sleep(10);
//        }
    }

    private static String getExampleJsonString(String path) throws Exception {
        BufferedReader reader = Files.newBufferedReader(Paths.get(new URI(String.format("file://%s", path))));
        return reader.lines().collect(Collectors.joining());
    }

    static class Book {
        String category;
        String author;
        String title;
        float price;

        public Book() {
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }
    }

    static class Bicycle {
        String color;
        float price;

        public Bicycle() {
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }
    }

    static class Store {
        List<Book> book;
        Bicycle bicycle;

        public Store() {
        }

        public List<Book> getBook() {
            return book;
        }

        public void setBook(List<Book> book) {
            this.book = book;
        }

        public Bicycle getBicycle() {
            return bicycle;
        }

        public void setBicycle(Bicycle bicycle) {
            this.bicycle = bicycle;
        }
    }

    static class Example {
        Store store;
        int expensive;

        public Example() {
        }

        public Store getStore() {
            return store;
        }

        public void setStore(Store store) {
            this.store = store;
        }

        public int getExpensive() {
            return expensive;
        }

        public void setExpensive(int expensive) {
            this.expensive = expensive;
        }
    }
}
