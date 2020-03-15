package fs.playground;

public class MandatoryUserData implements AppData<String> {
    private String data;

    public MandatoryUserData(String data) {
        this.data = data;
    }

    @Override
    public String getData() {
        return data;
    }
}
