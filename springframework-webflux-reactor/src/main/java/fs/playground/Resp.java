package fs.playground;

public class Resp<T> {
    private T data;

    public Resp() {}

    public Resp(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}