package fs.playground.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.core.io.buffer.DataBuffer;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
@Slf4j
public class PayloadReader {

    private Optional<String> payload = Optional.empty();

    protected Consumer<DataBuffer> create() {
        return dataBuffer -> {
            if (payload.isPresent()) {
                return;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                this.payload = Optional.of(IOUtils.toString(baos.toByteArray(), "UTF-8"));
            } catch (IOException e) {
                log.error("fail to read response body", e);
            }
        };
    }
}
