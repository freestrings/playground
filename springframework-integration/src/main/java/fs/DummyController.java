package fs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * 원격지 서버임
 */
@RestController
@RequestMapping("/dummy")
public class DummyController {

    @GetMapping("/a")
    public MessageA a() {
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MessageA messageA = new MessageA();
        messageA.setA(new Random().nextInt());
        return messageA;
    }

    @GetMapping("/b")
    public MessageB b() {
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MessageB messageB = new MessageB();
        messageB.setB(new Random().nextInt() % 2 == 0 ? Boolean.TRUE : Boolean.FALSE);
        return messageB;
    }
}