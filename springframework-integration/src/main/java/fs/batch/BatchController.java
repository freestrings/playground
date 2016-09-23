package fs.batch;

import fs.batch.JobLaunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest로 배치를 실행
 */
@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    private JobLaunchService jobLaunchService;

    @Autowired
    private MessageChannel inHttp;

    @GetMapping("/praha")
    public ResponseEntity<?> praha() throws Exception {
        jobLaunchService.praha();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/praha/memberActionCntInfos")
    public ResponseEntity<?> prahaMemberActionCntInfos() throws Exception {
        jobLaunchService.prahaMemberActionCntInInfosJob();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
