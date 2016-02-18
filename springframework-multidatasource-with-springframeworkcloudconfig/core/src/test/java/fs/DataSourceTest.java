package fs;

import fs.theother.TheOtherEntity;
import fs.theother.TheOtherEntityRepository;
import fs.one.OneEntity;
import fs.one.OneEntityRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CoreApplication.class)
public class DataSourceTest {

    @Autowired
    OneEntityRepository oneEntityRepository;

    @Autowired
    TheOtherEntityRepository theOtherEntityRepository;

    @Test
    @Transactional
    @Rollback(true)
    public void connect1() {
        OneEntity oneEntity = new OneEntity("TEST");
        oneEntityRepository.save(oneEntity);
        oneEntityRepository.flush();
        long count = oneEntityRepository.count();
        System.out.println(count);
    }

    @Test(expected = Exception.class)
    @Transactional(readOnly = true)
    @Rollback(true)
    public void connect2() {
        OneEntity oneEntity = new OneEntity("TEST");
        oneEntityRepository.save(oneEntity);
        oneEntityRepository.flush();
    }

    @Test
    @Transactional
    @Rollback(true)
    public void connect3() {
        TheOtherEntity dealInfo = new TheOtherEntity("TEST");
        theOtherEntityRepository.save(dealInfo);
        theOtherEntityRepository.flush();
        long count = theOtherEntityRepository.count();
        System.out.println(count);
    }

}
