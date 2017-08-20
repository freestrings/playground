package hello;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class BinaryLogClientCreator {

    private final Logger log = LoggerFactory.getLogger(BinaryLogClientCreator.class);

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
    private TableInfoManager tableInfoManager;

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

        List<BinlogInfo> replicationInfo = jdbcTemplate.query(
                "SHOW MASTER STATUS",
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

    private void processEvent(EventData data) {
        if (data instanceof TableMapEventData) {
            processEvent((TableMapEventData) data);
        } else if (data instanceof UpdateRowsEventData) {
            processEvent((UpdateRowsEventData) data);
        } else if (data instanceof DeleteRowsEventData) {
            processEvent((DeleteRowsEventData) data);
        } else if (data instanceof WriteRowsEventData) {
            processEvent((WriteRowsEventData) data);
        }
    }

    private void processEvent(TableMapEventData data) {
        tableInfoManager.updateTableInfo(data.getTableId(), data.getTable());
    }

    private void processEvent(UpdateRowsEventData data) {
        if (!tableInfoManager.isAvailableTable(data.getTableId())) {
            return;
        }

        Map<Integer, String> columnInfo = tableInfoManager.getColumnInfo(data.getTableId());
    }

    private void processEvent(DeleteRowsEventData data) {
        if (!tableInfoManager.isAvailableTable(data.getTableId())) {
            return;
        }
    }

    private void processEvent(WriteRowsEventData data) {
        if (!tableInfoManager.isAvailableTable(data.getTableId())) {
            return;
        }
    }
}
