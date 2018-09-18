import java.math.*;
import java.util.*;

public class Manipulation {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.printf("java Manipulation [blocks-per-epoch] [attacker-stake]\n");
      System.exit(1);
    }
    int blocksPerEpoch = Integer.parseInt(args[0]);
    double attackerStake = Double.parseDouble(args[1]);

    double manipulatedRewards = expectedBlockRewards(blocksPerEpoch, attackerStake);
    double normalRewards = blocksPerEpoch * attackerStake;
    double ratio = manipulatedRewards / normalRewards;
    double advantage = ratio - 1;
    System.out.printf("Advantage: %.5f%%\n", advantage * 100);
  }

  static double expectedBlockRewards(int blocksPerEpoch, double p) {
    double sum = 0;
    for (int bits = 0; bits <= blocksPerEpoch; ++bits) {
      // The probability of being able to manipulate exactly p bits.
      double probabilityOfBits = Math.pow(p, bits) * (1 - p);

      // The maximum possible reward, weighted by the probability of getting this many bits.
      double maxWeightedReward = probabilityOfBits * blocksPerEpoch;

      if (maxWeightedReward < 1e-4) {
        // The proper calculation may be expensive, but since we have an upper bound which is
        // very small, we add that instead.
        sum += maxWeightedReward;
      } else {
        sum += probabilityOfBits * expectedBlockRewardsGivenBits(blocksPerEpoch, p, bits);
      }
    }
    return sum;
  }

  static double expectedBlockRewardsGivenBits(int blocksPerEpoch, double p, int bits) {
    // We compute the expected value by summing the probabilities of exceeding each potential value.
    // This is Fubini's theorem + linearity of expectation.
    double sum = 0;
    for (int blockRewards = 0; blockRewards <= blocksPerEpoch; ++blockRewards) {
      sum += probOfExceeding(blockRewards, blocksPerEpoch, p, bits);
    }
    return sum;
  }

  static double probOfExceeding(int blockRewards, int blocksPerEpoch, double p, int bits) {
    // TODO: Explain how I came up with the precision.
    MathContext context = new MathContext(20);

    BigDecimal probNoneExceed = new BigDecimal(1.0);
    for (int skips = 0; skips <= bits; ++skips) {
      BigInteger numSeedsWithSkips = BinomialMath.binomial(bits, skips);
      BigDecimal probOfNotExceeding = BinomialMath.binomialCdf(
          blockRewards + skips, blocksPerEpoch, p);
      probNoneExceed = probNoneExceed.multiply(pow(probOfNotExceeding, numSeedsWithSkips), context);
    }
    return 1 - probNoneExceed.doubleValue();
  }

  static BigInteger getNumSeeds(int bits) {
    return BigInteger.valueOf(2).pow(bits);
  }

  static BigDecimal pow(BigDecimal x, BigInteger n) {
    // Exponentiation by squaring.
    if (n.equals(BigInteger.ZERO)) {
      return BigDecimal.ONE;
    }
    if (n.equals(BigInteger.ONE)) {
      return x;
    }

    // TODO: Explain how I came up with the precision.
    MathContext context = new MathContext(20);

    BigInteger two = BigInteger.valueOf(2);
    BigDecimal xSquared = x.multiply(x, context);
    BigInteger nDiv2 = n.divide(two);
    BigDecimal result = pow(xSquared, nDiv2);
    if (n.mod(two).equals(BigInteger.ONE)) {
      // Exponent is odd.
      result = result.multiply(x, context);
    }
    return result;
  }
}
