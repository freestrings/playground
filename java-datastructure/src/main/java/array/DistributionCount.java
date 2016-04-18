package array;

import java.util.Arrays;

public class DistributionCount {

    public static void main(String... args) {

        print(sort(new int[]{
                1, 3, 2, 2, 3, 1, 3, 2, 4, 2, 4, 3, 1, 2, 1, 2, 5, 1, 5, 3
        }, 5));

        print(sort(new int[]{
                1, 3, 3, 1, 3, 4, 4, 3, 1, 1, 5, 1, 5, 3
        }, 5));

    }

    static void print(int[] sorted) {
        Arrays.stream(sorted).forEach(value -> System.out.format("%d ", value));
        System.out.println();
    }

    static int[] sort(int[] array, int max) {

        int resultArray[] = new int[array.length];
        int distributionCount[] = new int[array.length];

        // 빈도수
        for (int i = 0; i < array.length; i++) {
            distributionCount[array[i]]++;
        }

        // 누적 분포 계산
        for (int i = 2; i <= max; i++) {
            distributionCount[i] = distributionCount[i] + distributionCount[i - 1];
        }

        for (int i = array.length - 1; i > -1; i--) {
            int originValue = array[i];
            resultArray[distributionCount[originValue] - 1] = originValue;
            distributionCount[originValue]--;
        }

        return resultArray;
    }

}
