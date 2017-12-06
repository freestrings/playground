package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SpringframeworkRestdocApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringframeworkRestdocApplication.class, args);
    }
}

@RestController
class Ctrl {

    @GetMapping("/")
    public String index() {
        return "{\n" +
                "    \"books\": [\n" +
                "        {\n" +
                "            \"title\": \"제목1\",\n" +
                "            \"author\": \"저자1\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"title\": \"제목2\",\n" +
                "            \"author\": \"저자2\"\n" +
                "        }\n" +
                "    ]\n" +
                "}\n";
    }

    @GetMapping("/weather")
    public String weather() {
        return "{\n" +
                "    \"weather\": {\n" +
                "        \"wind\": {\n" +
                "            \"speed\": 15.3,\n" +
                "            \"direction\": 287.0\n" +
                "        },\n" +
                "        \"temperature\": {\n" +
                "            \"high\": 21.2,\n" +
                "            \"low\": 14.8\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
    }

    @GetMapping(value = "/paging", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String paging(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer perPage) {
        if (perPage == null) {
            perPage = 10;
        }

        if (page == null || page == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < perPage; i++) {
                sb.append(i);
                if (i < perPage - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        } else {
            return "[1]";
        }
    }
}


