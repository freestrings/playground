package fs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BatchControllerTest.class,
        OutboundGatewayTest.class})
public class AllTests {
}
