package fs.outbound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PrahaDTO {

    @JsonProperty("_id")
    private String id;

}
