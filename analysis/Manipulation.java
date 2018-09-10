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
    System.out.printf("Advantage: %.3f%%\n", advantage * 100);
  }

  static double expectedBlockRewards(int blocksPerEpoch, double p) {
    double sum = 0;
    for (int bits = 0; bits <= blocksPerEpoch; ++bits) {
      double probabilityOfBits = Math.pow(p, bits) - Math.pow(p, bits + 1);

      // The maximum possible reward, weighted by the probability of getting this many bits.
      double maxWeightedReward = probabilityOfBits * blocksPerEpoch;

      if (maxWeightedReward < 1e-5) {
        // The proper calculation may be expensive, but since we have an upper bound which is
        // negligible anyway, we just use that.
        sum += maxWeightedReward;
      } else {
        sum += probabilityOfBits * expectedBlockRewardsGivenBits(blocksPerEpoch, p, bits);
      }
    }
    return sum;
  }

  // Upper bound; doesn't account for sacrificed rewards.
  static double expectedBlockRewardsGivenBits(int blocksPerEpoch, double p, int bits) {
    double sum = 0;
    for (int blockRewards = 0; blockRewards <= blocksPerEpoch; ++blockRewards) {
      //System.out.printf("prob of exceeding %d\n", blockRewards);
      sum += probOfExceeding(blockRewards, blocksPerEpoch, p, bits);
    }
    //System.out.printf("for bits=%d: %f\n", bits, sum);
    return sum;
  }

  static double probOfExceeding(int blockRewards, int blocksPerEpoch, double p, int bits) {
    BigDecimal probOfNotExceeding = BinomialMath.binomialCdf(blockRewards, blocksPerEpoch, p);
    BigInteger numSeeds = getNumSeeds(bits);
    BigDecimal probOfNoneExceeding = pow(probOfNotExceeding, numSeeds);
    //System.out.printf("    %s^%s = %s\n", probOfNotExceeding, numSeeds, probOfNoneExceeding);
    return 1 - probOfNoneExceeding.doubleValue();
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

    MathContext context = new MathContext(100); // TODO revisit precision
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
