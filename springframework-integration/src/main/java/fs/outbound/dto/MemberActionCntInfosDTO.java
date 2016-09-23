package fs.outbound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MemberActionCntInfosDTO {

    @JsonProperty("_id")
    private String id;

    private MemberActionCntInfos value;
}

@Data
class MemberActionCntInfos {

    int totalCount;
    int searchCnt;
    int clickCnt;
    int orderCnt;

}
