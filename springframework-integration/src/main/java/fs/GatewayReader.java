package fs;

import org.springframework.batch.item.ItemReader;

/**
 * ItemReader는 null이 리턴되어야 끝남
 *
 * @param <T>
 */
public abstract class GatewayReader<T> implements ItemReader<T> {

    private boolean flush = false;

    @Override
    public T read() throws Exception {
        if (flush) {
            return null;
        }
        flush = true;
        return readMessage();
    }

    protected abstract T readMessage();

}
