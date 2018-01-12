package fs.playground;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@Mapper
public interface TestaMapper {

    Map<String, Object> testa() throws Exception;
}
