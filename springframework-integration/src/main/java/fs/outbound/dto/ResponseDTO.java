package fs.outbound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ResponseDTO<T> {

    @JsonProperty(value = "_id", required = true)
    private String id;

    @JsonProperty("_size")
    private int size;

    @JsonProperty("_total_pages")
    private int totalPages;

    @JsonProperty(value = "_returned", required = true)
    private int returned;

    @JsonProperty(value = "_embedded")
    private Map<String, List<T>> embedded;

}
