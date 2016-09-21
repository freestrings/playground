package fs;

import fs.config.JobLaunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @GetMapping("/a")
    public ResponseEntity<?> a() throws Exception {
        jobLaunchService.runMessageA_Job();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/b")
    public ResponseEntity<?> b() throws Exception {
        jobLaunchService.runMessageB_Job();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
