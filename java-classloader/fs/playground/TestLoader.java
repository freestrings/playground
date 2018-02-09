package fs.playground;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TestLoader extends ClassLoader {

    private final String root;

    public TestLoader(String root) {
        this.root = root;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String path = root + File.separatorChar +
                    name.replace('.', File.separatorChar) + ".class";

            FileInputStream file = new FileInputStream(path);
            byte[] classByte = new byte[file.available()];
            file.read(classByte);

            return defineClass(name, classByte, 0, classByte.length);
        } catch (IOException ex) {
            throw new ClassNotFoundException();
        }

    }
}
