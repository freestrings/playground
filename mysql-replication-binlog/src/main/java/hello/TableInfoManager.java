package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TableInfoManager {

    private final Logger log = LoggerFactory.getLogger(TableInfoManager.class);

    /**
     * 모니터링 테이블 이름(table, table2, ...)
     */
    private HashSet<String> tableNames;

    /**
     * 테이블별 컬럼정보
     */
    private Map<String, Map<Integer, String>> columnInfos;

    /**
     * tableId vs 테이블 이름 매핑
     */
    private Map<Long, String> tableIdToTableName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${hello.meta.available-tables}")
    private String[] availableTables;

    private Map<Integer, String> buildColumnInfo(String tableName) throws SQLException {
        return jdbcTemplate.query(
                "SELECT COLUMN_NAME, ORDINAL_POSITION" +
                        "FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = '" + tableName + "' ", rs -> {
                    Map<Integer, String> ret = new HashMap<>();
                    while (rs.next()) {
                        ret.put(rs.getInt("ORDINAL_POSITION"), rs.getString("COLUMN_NAME"));
                    }
                    return ret;
                });
    }

    private Map<String, Map<Integer, String>> buildTableInfo(String[] tableNames) {
        return Arrays.asList(tableNames).stream()
                .collect(Collectors.toMap(tableName -> tableName, tableName -> {
                    try {
                        return buildColumnInfo(tableName);
                    } catch (SQLException e) {
                        log.error("컬럼정보 생성 실패", e);
                        return Collections.emptyMap();
                    }
                }));
    }

    @PostConstruct
    private void init() {
        this.tableNames = new HashSet<>(Arrays.asList(availableTables));
        this.columnInfos = buildTableInfo(availableTables);
    }

    public Map<Integer, String> getColumnInfo(Long tableId) {
        String tableName = this.tableIdToTableName.get(tableId);
        if (tableName == null) {
            return null;
        }
        return Collections.unmodifiableMap(this.columnInfos.get(tableName));
    }

    public boolean isAvailableTable(Long tableId) {
        return this.tableIdToTableName.containsKey(tableId);
    }

    public void updateTableInfo(Long tableId, String tableName) {
        if (this.tableNames.contains(tableName)) {
            this.tableIdToTableName.put(tableId, tableName);
        }
    }
}
