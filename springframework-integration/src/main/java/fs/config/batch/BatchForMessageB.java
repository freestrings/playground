package fs.config.batch;

import fs.GatewayReader;
import fs.MessageB;
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
public class BatchForMessageB {

    @Autowired
    private ServiceGateway gateway;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job messageBJob() {
        return jobBuilderFactory.get("messageBJob")
                .incrementer(new RunIdIncrementer())
                .start(messageBStep())
                .build();
    }

    @Bean
    public Step messageBStep() {
        return stepBuilderFactory.get("messageBStep")
                .allowStartIfComplete(true)
                .<MessageB, MessageB>chunk(1)
                .reader(messageBReadder())
                .processor(messageBProcessor())
                .writer(messageBWriter())
                .build();
    }

    @Bean
    @StepScope // important!
    public ItemReader<MessageB> messageBReadder() {
        return new GatewayReader<MessageB>() {
            @Override
            protected MessageB readMessage() {
                print("read B");
                MessageB b = gateway.getB("");
                print("read B done");
                return b;
            }
        };
    }

    @Bean
    public ItemProcessor<MessageB, MessageB> messageBProcessor() {
        return item -> item;
    }

    @Bean
    public ItemWriter messageBWriter() {
        return items -> {
            print("write done");
        };
    }

    private void print(String msg) {
        System.out.println(Thread.currentThread().getName() + ": " + msg);
    }
}
