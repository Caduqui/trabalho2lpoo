package lpoo.math;

/**
 *
 * @author Paulo Pagliosa
 */
public class Real
  implements Comparable<Real>
{
  public static final float ZERO = 1e-6f;

  float value;

  static public boolean isZero(float a)
  {
    return Math.abs(a) <= ZERO;
  }

  static public boolean isEqual(float a, float b)
  {
    return isZero(a - b);
  }

  public Real(float value)
  {
    this.value = value;
  }

  public int compareTo(Real r)
  {
    return isEqual(value, r.value) ? 0 : value < r.value ? -1 : +1;
  }

} // Real
