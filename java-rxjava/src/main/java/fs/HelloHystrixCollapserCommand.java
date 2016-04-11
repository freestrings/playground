package fs;

import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HelloHystrixCollapserCommand extends HystrixCollapser<List<String>, String, Integer> {

    private Integer key;

    public HelloHystrixCollapserCommand(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getRequestArgument() {
        return key;
    }

    @Override
    protected HystrixCommand<List<String>> createCommand(Collection<CollapsedRequest<String, Integer>> requests) {

        return new HystrixCommand<List<String>>(
                HystrixCommand.Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey("PlaygroundGroup"))
                        .andCommandKey(HystrixCommandKey.Factory.asKey("CollapseForKey"))
        ) {

            @Override
            protected List<String> run() throws Exception {
                System.out.format("run: size {%d}%n", requests.size());
                return requests
                        .stream()
                        .map(request -> "ValueForKey: " + request.getArgument())
                        .collect(Collectors.toList());
            }
        };

    }

    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> requests) {
        System.out.format("map: responseSize {%d}, requestSize {%d}%n", batchResponse.size(), requests.size());
        int count = 0;
        for (CollapsedRequest<String, Integer> request : requests) {
            request.setResponse(batchResponse.get(count++));
        }
    }

}
