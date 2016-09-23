package fs.batch.job;

import fs.batch.SingleItemReader;
import fs.outbound.IOutboundService;
import fs.outbound.dto.MemberActionCntInfosDTO;
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
public class PrahaMemberActionCntInInfosJobConfig<D extends ResponseDTO<MemberActionCntInfosDTO>> {

    @Autowired
    private IOutboundService outboundService;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job prahaJob() {
        return jobBuilderFactory.get("prahaMemberActionCntInInfosJob")
                .incrementer(new RunIdIncrementer())
                .start(prahaMemberActionCntInInfosStep())
                .build();
    }

    @Bean
    public Step prahaMemberActionCntInInfosStep() {
        return stepBuilderFactory.get("prahaMemberActionCntInInfosStep")
                .allowStartIfComplete(true)
                .<ResponseDTO<MemberActionCntInfosDTO>, ResponseDTO<MemberActionCntInfosDTO>>chunk(1)
                .reader(prahaMemberActionCntInInfosReader())
                .processor(prahaMemberActionCntInInfosProcessor())
                .writer(prahaWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<ResponseDTO<MemberActionCntInfosDTO>> prahaMemberActionCntInInfosReader() {
        return new SingleItemReader<ResponseDTO<MemberActionCntInfosDTO>>() {
            @Override
            protected ResponseDTO<MemberActionCntInfosDTO> readMessage() {
                D response = (D) outboundService.praha_memberActionCntInfos("");

                if (log.isDebugEnabled()) {
                    log.debug("prahaMemberActionCntInInfosReader:" + response.toString());
                }
                return response;
            }
        };
    }

    @Bean
    public ItemProcessor<ResponseDTO<MemberActionCntInfosDTO>, ResponseDTO<MemberActionCntInfosDTO>> prahaMemberActionCntInInfosProcessor() {
        // do nothing
        return item -> item;
    }

    @Bean
    public ItemWriter prahaWriter() {
        return items -> {
            // 어딘가에 저장
        };
    }

}
