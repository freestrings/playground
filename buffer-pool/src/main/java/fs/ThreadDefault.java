package fs;

import java.util.Random;

public class ThreadDefault {

    static class MyThread extends Thread {

        private final String name;
        private final int sleep;
        private int iterCount = 10;

        MyThread(String name) {
            this.name = name;
            this.sleep = new Random().nextInt(100);
            System.out.println("Sleep :" + this.sleep);
        }

        @Override
        public void run() {

            int runCount = 0;
            while (true) {
                if (iterCount <= runCount++) {
                    break;
                }
                System.out.println(Thread.currentThread().getName() + ":" + name);
                try {
                    Thread.sleep(this.sleep);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void main(String... args) {
        new MyThread("↑").start();
        new MyThread("↓").start();
    }
}
