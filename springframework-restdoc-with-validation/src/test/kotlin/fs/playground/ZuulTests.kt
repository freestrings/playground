package fs.playground

import com.palantir.docker.compose.DockerComposeRule
import com.palantir.docker.compose.connection.waiting.ImmutableSuccessOrFailure
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Profile
import org.springframework.test.context.junit4.SpringRunner

/**
 * FIXME 테스트 끝나면 컨테이너 삭제
 * 한번씩 에러가 난다. 필요할 때 수동으로 실행
 */
//@RunWith(SpringRunner::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ZuulTests {

    object ContainerWrap {

        @ClassRule
        @JvmStatic
        val docker: DockerComposeRule = DockerComposeRule.builder()
                .file("src/test/resources/docker-compose-test.yml")
//                .removeConflictingContainersOnStartup(true)
                .waitingForService("http-echo") {
                    ImmutableSuccessOrFailure.builder().build().withOptionalFailureMessage("'http-echo' fail to start")
                }
                .waitingForHostNetworkedPort(5678) {
                    ImmutableSuccessOrFailure.builder().build().withOptionalFailureMessage("'http-echo' fail to wait")
                }
                .build()

    }

    private val restTemplate = TestRestTemplate()

    @Value("\${local.server.port}")
    var port: Int = 0;

    //    @Before
    fun init() {
        val container = ContainerWrap.docker.containers().container("http-echo")
        container.up()
    }

    //    @After
    fun done() {
        val container = ContainerWrap.docker.containers().container("http-echo")
        container.kill()
    }

    //    @Test
    fun `테스트 echo 서버 확인`() {
        val result = restTemplate.getForEntity("http://localhost:5678", String::class.java)
        assert(result.body!!.trim() == "hi")
    }

    //    @Test
    fun `Zuul 통한 echo 서버 확인`() {
        val result = restTemplate.getForEntity("http://localhost:${port}/echo/testa", String::class.java)
        assert(result.body!!.trim() == "hi")
    }

}
