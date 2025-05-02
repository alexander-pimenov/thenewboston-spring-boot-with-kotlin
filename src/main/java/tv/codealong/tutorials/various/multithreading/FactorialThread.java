package tv.codealong.tutorials.various.multithreading;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * https://youtu.be/5YLA29EybMo?t=5049
 */
public class FactorialThread {

    /**
     * Метод считающий факториал. Реализованный с помощью BigInteger,
     * чтобы посчитать длинные числа.
     * Факториалы считаются довольно медленно.
     */
    static BigInteger factorial(int i) {
        BigInteger result = BigInteger.ONE;
        while (i > 1) {
            result = result.multiply(BigInteger.valueOf(i));
            i--;
        }
        return result;
    }

    /**
     * Метод считающий комбинации по формуле:
     * C(n,k) = n!/k!/(n-k)!
     */
    static BigInteger combinationsSequence(int n, int k) {
        System.out.println("Вычисление комбинаций последовательно для: "+ n + ", " + k);
        // Берем факториалы отдельно, а потом делим их между собой.
        return factorial(n).divide(factorial(k)).divide(factorial(n - k));
//        return factorial(n).divide(factorial(k).multiply(factorial(n-k)));
    }

    static BigInteger combinationsParallel(int n, int k) {
        System.out.println("Вычисление комбинаций параллельно для: "+ n + ", " + k);
        CompletableFuture<BigInteger> facN = CompletableFuture.supplyAsync(() -> factorial(n));
        CompletableFuture<BigInteger> facK = CompletableFuture.supplyAsync(() -> factorial(k));
        CompletableFuture<BigInteger> facNminusK = CompletableFuture.supplyAsync(() -> factorial(n - k));
        return facN.thenCombine(facK, BigInteger::divide)
                .thenCombine(facNminusK, BigInteger::divide)
                .join();
    }

    public static void main(String[] args) {
        BigInteger result1 = factorial(18);
        System.out.println(result1);

        BigInteger result2 = combinationsSequence(10, 50);
        System.out.println(result2);

        BigInteger result3 = combinationsParallel(100, 500);
        System.out.println(result3);
    }
}

