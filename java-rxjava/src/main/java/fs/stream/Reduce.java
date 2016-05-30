package fs.stream;

import java.util.Arrays;
import java.util.HashMap;

public class Reduce {

    public Reduce(String[]... properties) {
        HashMap<Object, Object> reduce = Arrays.asList(properties)
                .stream()
                .reduce(new HashMap<>(), (map, strings) -> {
                    map.put(strings[0], strings[1]);
                    return map;
                }, (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                });
        System.out.println(reduce);
    }

    public static void main(String... args) {
        new Reduce(
                new String[]{"a", "b"},
                new String[]{"c", "d"},
                new String[]{"c", "d"});
    }
}
