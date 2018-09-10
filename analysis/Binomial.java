import java.math.*;
import java.util.*;

class BinomialMath {
  private static final Map<BinomialParams, BigDecimal> binomialPdfResults = new HashMap<>();
  private static final Map<BinomialParams, BigDecimal> binomialCdfResults = new HashMap<>();

  static BigDecimal binomialPdf(int successes, int trials, double p) {
    BinomialParams params = new BinomialParams(successes, trials, p);
    return binomialPdfResults.computeIfAbsent(params, BinomialMath::binomialPdf);
  }

  private static BigDecimal binomialPdf(BinomialParams params) {
    BigDecimal result = new BigDecimal(binomial(params.trials, params.successes));
    BigDecimal pBig = new BigDecimal(params.p), qBig = new BigDecimal(1 - params.p);
    for (int i = 0; i < params.successes; ++i) {
      result = result.multiply(pBig);
    }
    for (int i = 0; i < params.trials - params.successes; ++i) {
      result = result.multiply(qBig);
    }
    return result;
  }

  static BigDecimal binomialCdf(int maxSuccesses, int trials, double p) {
    BinomialParams params = new BinomialParams(maxSuccesses, trials, p);
    return binomialCdfResults.computeIfAbsent(params, BinomialMath::binomialCdf);
  }

  private static BigDecimal binomialCdf(BinomialParams params) {
    BigDecimal sum = BigDecimal.ZERO;
    for (int i = 0; i <= params.successes; ++i) {
      sum = sum.add(binomialPdf(i, params.trials, params.p));
    }
    return sum;
  }

  /** Computes n choose k. */
  static BigInteger binomial(int n, int k) {
    BigInteger result = BigInteger.ONE;
    for (int i = 0; i < k; i++) {
      result = result.multiply(BigInteger.valueOf(n - i))
        .divide(BigInteger.valueOf(i+1));
    }
    System.out.printf("%d %d %s\n", n, k, result.toString().length());
    return result;
  }

  private static class BinomialParams {
    final int successes;
    final int trials;
    final double p;

    BinomialParams(int successes, int trials, double p) {
      this.successes = successes;
      this.trials = trials;
      this.p = p;
    }

    @Override public boolean equals(Object o) {
      if (!(o instanceof BinomialParams)) {
        return false;
      }
      BinomialParams that = (BinomialParams) o;
      return Objects.equals(this.successes, that.successes)
        && Objects.equals(this.trials, that.trials)
        && Objects.equals(this.p, that.p);
    }

    @Override public int hashCode() {
      return Objects.hash(successes, trials, p);
    }
  }
}
