package fs;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.List;

@SpringBootApplication
@EnableTransactionManagement
public class FsTransactionApplication {

    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() {
        return new UserTransactionManager();
    }

    @Bean(name = "atomikosUserTransaction")
    public UserTransactionImp atomikosUserTransaction() {
        return new UserTransactionImp();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("atomikosUserTransaction") UserTransactionImp userTransactionImp,
            @Qualifier("atomikosTransactionManager") UserTransactionManager userTransactionManager
    ) throws Throwable {
        return new JtaTransactionManager(userTransactionImp, userTransactionManager);
    }

    public static void main(String... args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(FsTransactionApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run();

        FsTransactionService fsTransactionService = ctx.getBean(FsTransactionService.class);
        saveOnRequireNewWithError2(fsTransactionService);
    }

    private static void saveWithError(FsTransactionService fsTransactionService) {
        fsTransactionService.saveWithError();
        List<?> all = fsTransactionService.findAll();
        System.out.println(all);
    }

    private static void saveOnRequireNewWithError(FsTransactionService fsTransactionService) {
        try {
            fsTransactionService.saveOnRequireNewWithError();
        } catch (Exception e) {
        }
        List<?> all = fsTransactionService.findAll();
        System.out.println(all);
    }

    private static void saveOnRequireNewWithError2(FsTransactionService fsTransactionService) {
        try {
            fsTransactionService.saveOnRequireNewWithError2();
        } catch (Exception e) {
        }
        List<?> all = fsTransactionService.findAll();
        System.out.println(all);
    }
}
