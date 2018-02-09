package fs.playground;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * javac fs/playground/*.java
 * java -classpath . -Dlog4j2.debug=true -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector fs.playground.App
 */
public class App {

    public static void main(String... args) throws Exception {
        App app = new App();
//        app.log4j1();
        app.log4j2();
    }

    private void log4j1() throws Exception {
        URLClassLoader loader = getLoader("../../lib/log4j.jar");
        Class classToLoad = Class.forName("org.apache.log4j.LogManager", true, loader);
        Object logger = classToLoad.getDeclaredMethod("getRootLogger").invoke(null);
        Method debug = logger.getClass().getMethod("info", Object.class);
        debug.invoke(logger, "log4j1");
    }

    private void log4j2() throws Exception {
        URLClassLoader loader = getLoader("../../lib/log4j-api-2.10.0.jar", "../../lib/log4j-core-2.10.0.jar", "../../lib/disruptor-3.3.7.jar");
        Class<?> logManagerClass = Class.forName("org.apache.logging.log4j.LogManager", false, loader);

        Method getContext = logManagerClass.getDeclaredMethod("getContext", boolean.class);
        Object context = getContext.invoke(null, false);
        Method setConfigLocation = context.getClass().getMethod("setConfigLocation", URI.class);
        setConfigLocation.invoke(context, new File("./lib/log4j2.xml").toURI());

        Object logger = logManagerClass.getDeclaredMethod("getLogger", Class.class).invoke(null, App.class);
        Method info = logger.getClass().getMethod("info", String.class);
        info.invoke(logger, "log4j2");
    }

    private URLClassLoader getLoader(String... jars) {
        TestLoader testLoader = new TestLoader(this.getClass().getResource("../../").getFile());
        URL[] urls = Arrays.asList(jars).stream().map(jar -> this.getClass().getResource(jar)).toArray(value -> new URL[value]);
        return new URLClassLoader(urls, testLoader);
    }
}
