package lpoo.math;

/**
 * Utility class for floating-point comparisons.
 * @author Paulo Pagliosa (adapted)
 */
public final class Real
{
  public static final float eps = 1e-6f;

  public static boolean isEqual(float a, float b)
  {
    return Math.abs(a - b) <= eps;
  }

  private Real() {}

} // Real
