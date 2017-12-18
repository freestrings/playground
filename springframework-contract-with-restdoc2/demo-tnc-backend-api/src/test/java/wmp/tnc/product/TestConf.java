package wmp.tnc.product;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.snippet.RestDocumentationContextPlaceholderResolverFactory;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.StandardWriterResolver;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TestConfiguration
public class TestConf {

    @Bean
    @ConditionalOnMissingBean(MockMvcRestDocumentationConfigurer.class)
    public MockMvcRestDocumentationConfigurer restDocsMockMvcConfigurer(
            ObjectProvider<RestDocsMockMvcConfigurationCustomizer> configurationCustomizerProvider,
            RestDocumentationContextProvider contextProvider) {
        MockMvcRestDocumentationConfigurer configurer = MockMvcRestDocumentation.documentationConfiguration(contextProvider);
//        RestDocsMockMvcConfigurationCustomizer configurationCustomizer = configurationCustomizerProvider.getIfAvailable();
        RestDocsMockMvcConfigurationCustomizer configurationCustomizer = new WireMockRestDocsConfiguration() {
            @Override
            public void customize(MockMvcRestDocumentationConfigurer configurer) {
                configurer.snippets().withAdditionalDefaults(new _WireMockSnippet());
            }
        };
        if (configurationCustomizer != null) {
            configurationCustomizer.customize(configurer);
        }
        return configurer;
    }
}

class _WireMockSnippet implements Snippet {

    private String snippetName = "stubs";

    private Set<String> headerBlackList = new HashSet<>(
            Arrays.asList("host", "content-length"));

    private Set<String> jsonPaths = new LinkedHashSet<>();

    private MediaType contentType;

    private StubMapping stubMapping;

    private boolean hasJsonBodyRequestToMatch = false;

    private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(
            "{", "}");

    private static final TemplateFormat TEMPLATE_FORMAT = new TemplateFormat() {

        @Override
        public String getId() {
            return "json";
        }

        @Override
        public String getFileExtension() {
            return "json";
        }
    };

    @Override
    public void document(Operation operation) throws IOException {
        extractMatchers(operation);
        if (this.stubMapping == null) {
            this.stubMapping = request(operation).willReturn(response(operation)).build();
        }
        String json = Json.write(this.stubMapping);
        RestDocumentationContext context = (RestDocumentationContext) operation
                .getAttributes().get(RestDocumentationContext.class.getName());
        RestDocumentationContextPlaceholderResolverFactory placeholders = new RestDocumentationContextPlaceholderResolverFactory();
        WriterResolver writerResolver = new StandardWriterResolver(placeholders, "UTF-8",
                TEMPLATE_FORMAT);
        String path = this.propertyPlaceholderHelper.replacePlaceholders(operation.getName(),
                placeholders.create(context));
        try (Writer writer = writerResolver.resolve(this.snippetName, path, context)) {
            writer.append(json);
        }
    }

    private void extractMatchers(Operation operation) {
        this.stubMapping = (StubMapping) operation.getAttributes()
                .get("contract.stubMapping");
        if (this.stubMapping != null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Set<String> jsonPaths = (Set<String>) operation.getAttributes()
                .get("contract.jsonPaths");
        this.jsonPaths = jsonPaths;
        this.contentType = (MediaType) operation.getAttributes()
                .get("contract.contentType");
        if (this.contentType == null) {
            this.hasJsonBodyRequestToMatch = hasJsonContentType(operation);
        }
    }

    private boolean hasJsonContentType(Operation operation) {
        return operation.getRequest().getHeaders().getContentType() != null
                && (operation.getRequest().getHeaders().getContentType()
                .isCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private ResponseDefinitionBuilder response(Operation operation) {
        return aResponse().withHeaders(responseHeaders(operation))
                .withBody(operation.getResponse().getContentAsString())
                .withStatus(operation.getResponse().getStatus().value());
    }

    private MappingBuilder request(Operation operation) {
        return requestHeaders(requestBuilder(operation), operation);
    }

    private MappingBuilder requestHeaders(MappingBuilder request, Operation operation) {
        org.springframework.http.HttpHeaders headers = operation.getRequest()
                .getHeaders();
        // TODO: whitelist headers
        for (String name : headers.keySet()) {
            if (!this.headerBlackList.contains(name.toLowerCase())) {
                if ("content-type".equalsIgnoreCase(name) && this.contentType != null) {
                    continue;
                }
                request = request.withHeader(name, equalTo(headers.getFirst(name)));
            }
        }
        if (this.contentType != null) {
            request = request.withHeader("Content-Type",
                    matching(Pattern.quote(this.contentType.toString()) + ".*"));
        }
        return request;
    }

    private MappingBuilder requestBuilder(Operation operation) {
        switch (operation.getRequest().getMethod()) {
            case DELETE:
                return delete(requestPattern(operation));
            case POST:
                return bodyPattern(post(requestPattern(operation)),
                        operation.getRequest().getContentAsString());
            case PUT:
                return bodyPattern(put(requestPattern(operation)),
                        operation.getRequest().getContentAsString());
            case PATCH:
                return bodyPattern(patch(requestPattern(operation)),
                        operation.getRequest().getContentAsString());
            case GET:
                return get(requestPattern(operation));
            case HEAD:
                return head(requestPattern(operation));
            case OPTIONS:
                return options(requestPattern(operation));
            case TRACE:
                return trace(requestPattern(operation));
            default:
                throw new UnsupportedOperationException(
                        "Unsupported method type: " + operation.getRequest().getMethod());
        }
    }

    private MappingBuilder bodyPattern(MappingBuilder builder, String content) {
        if (this.jsonPaths != null && !this.jsonPaths.isEmpty()) {
            for (String jsonPath : this.jsonPaths) {
                builder.withRequestBody(matchingJsonPath(jsonPath));
            }
        } else if (!StringUtils.isEmpty(content)) {
            if (this.hasJsonBodyRequestToMatch) {
                builder.withRequestBody(equalToJson(content));
            } else {
                builder.withRequestBody(equalTo(content));
            }
        }
        return builder;
    }

    /**
     * http://wiremock.org/docs/request-matching/
     * wiremock queryParameters가 생성 되지 않는다. 임시로 구현 해 둔다.
     *
     * @param operation
     * @return
     */
    private UrlPattern requestPattern(Operation operation) {
        OperationRequest request = operation.getRequest();
        URI uri = request.getUri();
        String rawQuery = uri.getRawQuery();
        if (rawQuery != null) {
            return urlEqualTo(uri.getPath() + "?" + uri.getRawQuery());
        } else {
            return urlEqualTo(uri.getPath());
        }
    }

    private HttpHeaders responseHeaders(Operation operation) {
        org.springframework.http.HttpHeaders headers = operation.getResponse()
                .getHeaders();
        HttpHeaders result = new HttpHeaders();
        for (String name : headers.keySet()) {
            if (!this.headerBlackList.contains(name.toLowerCase())) {
                result = result.plus(new HttpHeader(name, headers.get(name)));
            }
        }
        return result;
    }

}
