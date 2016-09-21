package fs.config.batch;

import fs.GatewayReader;
import fs.MessageA;
import fs.ServiceGateway;
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

@Configuration
public class BatchForMessageA {

    @Autowired
    private ServiceGateway gateway;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job messageAJob() {
        return jobBuilderFactory.get("messageAJob")
                .incrementer(new RunIdIncrementer())
                .start(messageAStep())
                .build();
    }

    @Bean
    public Step messageAStep() {
        return stepBuilderFactory.get("messageAStep")
                .allowStartIfComplete(true)
                .<MessageA, MessageA>chunk(1)
                .reader(messageAReadder())
                .processor(messageAProcessor())
                .writer(messageAWriter())
                .build();
    }

    @Bean
    @StepScope // important!
    public ItemReader<MessageA> messageAReadder() {
        return new GatewayReader<MessageA>() {
            @Override
            protected MessageA readMessage() {
                print("read A");
                MessageA a = gateway.getA("");
                print("read A done");
                return a;
            }
        };
    }

    @Bean
    public ItemProcessor<MessageA, MessageA> messageAProcessor() {
        return item -> item;
    }

    @Bean
    public ItemWriter messageAWriter() {
        return items -> {
            print("write done");
        };
    }

    private void print(String msg) {
        System.out.println(Thread.currentThread().getName() + ": " + msg);
    }
}
