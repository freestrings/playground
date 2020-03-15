package fs.playground;

public class OptionalUserData implements AppData<Number> {

    private final Number data;

    public OptionalUserData(Number data) {
        this.data = data;
    }

    @Override
    public Number getData() {
        return data;
    }
}
