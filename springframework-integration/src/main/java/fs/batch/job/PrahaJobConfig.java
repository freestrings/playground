package fs.batch.job;

import fs.batch.SingleItemReader;
import fs.outbound.IOutboundService;
import fs.outbound.dto.PrahaDTO;
import fs.outbound.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PrahaJobConfig<D extends ResponseDTO<PrahaDTO>> {

    public static final String JOB_NAME = "prahaJob";

    @Autowired
    private IOutboundService outboundService;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job prahaJob() {
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(prahaStep())
                .build();
    }

    @Bean
    protected Step prahaStep() {
        return stepBuilderFactory.get("prahaStep")
                .allowStartIfComplete(true)
                .<D, D>chunk(1)
                .reader(prahaReader())
                .processor(prahaProcessor())
                .writer(prahaWriter())
                .build();
    }

    @Bean
    @StepScope
    protected ItemReader<D> prahaReader() {
        return new SingleItemReader<D>() {
            @Override
            protected D readMessage() {
                D response = (D) outboundService.praha("");

                if (log.isDebugEnabled()) {
                    log.debug("prahaReader:" + response.toString());
                }
                return response;
            }
        };
    }

    @Bean
    protected ItemProcessor<D, D> prahaProcessor() {
        // do nothing
        return item -> item;
    }

    @Bean
    protected ItemWriter prahaWriter() {
        return items -> {
            // 어딘가에 저장
        };
    }

}
