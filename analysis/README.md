# Profitability of entropy manipulation

This code can be used to calculate upper bounds on the profitability of entropy manipulation. See the paper for details.

Note that the calculations can take a long time, depending on the parameters. A lot of the is spent computing binomial CDFs. We could speed it up using a polynomial approximation, but we would need to be very careful about the approximation error, since we sometimes have probabilities very close to 1 which we then raise to a large power.


## Running the code

```shell
javac *.java
# Calculate profitability with n=1000, p=0.25
java Manipulation 1000 0.25
```
