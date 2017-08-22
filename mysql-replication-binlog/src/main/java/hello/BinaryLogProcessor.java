package hello;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class BinaryLogProcessor {

    private final Logger log = LoggerFactory.getLogger(BinaryLogProcessor.class);

    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";

    @Value("${hello.binary-log.host}")
    private String host;

    @Value("${hello.binary-log.port}")
    private int port;

    @Value("${hello.binary-log.user}")
    private String user;

    @Value("${hello.binary-log.password}")
    private String password;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HelloMetaProperties helloMetaProperties;

    private Map<String, Set<String>> availableTables;

    private class BinlogInfo {
        String file;
        int position;

        public BinlogInfo(String file, int position) {
            this.file = file;
            this.position = position;
        }
    }

    @PostConstruct
    private void init() {
        initProperties();
        initBinlogClient();
    }

    private void initBinlogClient() {
        List<BinlogInfo> replicationInfo = jdbcTemplate.query("SHOW MASTER STATUS",
                (row, i) -> new BinlogInfo(row.getString("FILE"), row.getInt("POSITION"))
        );

        Assert.isTrue(replicationInfo.size() == 1, "binlog 파일정보가 올바르지 않습니다.");

        String binlogFile = replicationInfo.get(0).file;
        int binlogPosition = replicationInfo.get(0).position;

        log.info("Binlog - file: {}", binlogFile);
        log.info("Binlog - position: {}", binlogPosition);

        BinaryLogClient client = new BinaryLogClient(host, port, user, password);
        client.setBinlogFilename(binlogFile);
        client.setBinlogPosition(binlogPosition);
        client.registerEventListener(event -> processEvent((EventData) event.getData()));

        try {
            client.connect();
        } catch (IOException e) {
            log.error("Binary log client 연결에러!!", e);
        }
    }

    private void initProperties() {
        Map<String, Set<String>> _availableTables = new HashMap<>();
        helloMetaProperties.getAvailableTables().forEach((tableName, columns) -> {
            _availableTables.put(tableName, new HashSet<>(columns));
        });

        this.availableTables = Collections.unmodifiableMap(_availableTables);
    }

    private void processEvent(EventData data) {
        try {
            if (data instanceof QueryEventData) {
                processEvent((QueryEventData) data);
            }
        } catch (Exception e) {
            log.error("Event 처리", e);
        }
    }

    private void processEvent(QueryEventData data) {
        String sql = data.getSql();

        int firstTokenIndex = sql.indexOf(" ");
        if (firstTokenIndex == -1) {
            return;
        }

        String command = sql.substring(0, firstTokenIndex);
        if (command.equalsIgnoreCase(INSERT) || command.equalsIgnoreCase(UPDATE) || command.equalsIgnoreCase(DELETE)) {
            try {
                CCJSqlParserUtil.parseStatements(sql).getStatements().forEach(stmt -> {
                    if (stmt instanceof Insert) {
                        processStatement((Insert) stmt);
                    } else if (stmt instanceof Update) {
                        processStatement((Update) stmt);
                    } else if (stmt instanceof Delete) {
                        processStatement((Delete) stmt);
                    }
                });
            } catch (JSQLParserException e) {
                log.error("Fail to parse", e);
            }
        }
    }

    private void processStatement(Insert stmt) {
        Table table = stmt.getTable();
        Set<String> availableColumns = this.availableTables.get(table.getName().toUpperCase());
        if (availableColumns == null) {
            return;
        }

        Map<Integer, String> columnInfo = new HashMap<>();
        List<Column> columns = stmt.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i).getColumnName().toUpperCase();
            if (availableColumns.contains(columnName)) {
                columnInfo.put(i, columnName);
            }
        }

        ItemsList itemsList = stmt.getItemsList();
        if (itemsList instanceof MultiExpressionList) {
            List<ExpressionList> exprList = ((MultiExpressionList) itemsList).getExprList();
            for (ExpressionList expressionList : exprList) {
                processInsert(table.getName(), columnInfo, expressionList.getExpressions());
            }

        } else if (itemsList instanceof ExpressionList) {
            processInsert(table.getName(), columnInfo, ((ExpressionList) itemsList).getExpressions());
        }

        Select select = stmt.getSelect();
        System.out.println(select);
    }

    private void processInsert(String tableName, Map<Integer, String> columnInfo, List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String columnName = columnInfo.get(i);
            String columnValue = expressions.get(i).toString();
            System.out.println(tableName + ", " + columnName + ", " + columnValue);
        }
    }

    private void processStatement(Delete stmt) {

    }

    private void processStatement(Update stmt) {

    }
}
