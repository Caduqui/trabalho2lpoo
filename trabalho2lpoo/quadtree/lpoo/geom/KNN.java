package lpoo.geom;

/**
 * Stores up to k nearest neighbor pairs (point, squared distance),
 * kept sorted from closest to farthest.
 *
 * @author trabalho LPOO
 */
public class KNN<P extends Point2>
{
  /** Pair of (point, squared distance). */
  public static class Entry<P extends Point2>
  {
    public final P point;
    public final float distance; // actual Euclidean distance

    private Entry(P point, float distance)
    {
      this.point = point;
      this.distance = distance;
    }
  }

  private final int k;
  private final Object[] entries; // Entry<P>[]
  private int count;

  public KNN(int k)
  {
    this.k = k;
    this.entries = new Object[k];
    this.count = 0;
  }

  /** Number of pairs currently stored. */
  public int size()
  {
    return count;
  }

  /** Maximum capacity (k). */
  public int capacity()
  {
    return k;
  }

  /** Returns true if already holds k entries. */
  public boolean isFull()
  {
    return count == k;
  }

  /** Returns entry at position i (0 = closest). */
  @SuppressWarnings("unchecked")
  public Entry<P> get(int i)
  {
    if (i < 0 || i >= count)
      throw new IndexOutOfBoundsException("index " + i);
    return (Entry<P>) entries[i];
  }

  /**
   * Returns the squared distance of the farthest stored neighbor,
   * or Float.MAX_VALUE if not yet full.
   */
  @SuppressWarnings("unchecked")
  public float maxDistanceSq()
  {
    if (count == 0)
      return Float.MAX_VALUE;
    // farthest is last; we stored actual distance, square it for comparison
    float d = ((Entry<P>) entries[count - 1]).distance;
    return d * d;
  }

  /**
   * Attempts to add (point, squaredDist) to the KNN.
   * Insertion is by actual distance (sqrt of squaredDist).
   * Returns true if the point was inserted.
   */
  public boolean add(P point, float squaredDist)
  {
    float dist = (float) Math.sqrt(squaredDist);

    // If full and this point is farther than the current worst, skip
    if (count == k)
    {
      @SuppressWarnings("unchecked")
      Entry<P> worst = (Entry<P>) entries[count - 1];
      if (dist >= worst.distance)
        return false;
    }

    // Find insertion position (sorted ascending by distance)
    int pos = count;
    for (int i = 0; i < count; i++)
    {
      @SuppressWarnings("unchecked")
      Entry<P> e = (Entry<P>) entries[i];
      if (dist < e.distance)
      {
        pos = i;
        break;
      }
    }

    // Shift entries right to make room
    int end = (count < k) ? count : k - 1;
    for (int i = end; i > pos; i--)
      entries[i] = entries[i - 1];

    entries[pos] = new Entry<>(point, dist);
    if (count < k)
      count++;
    return true;
  }

  /** Print all entries to stdout. */
  public void print()
  {
    for (int i = 0; i < count; i++)
    {
      @SuppressWarnings("unchecked")
      Entry<P> e = (Entry<P>) entries[i];
      System.out.printf("  [%d] dist=%.4f point=%s%n", i, e.distance, e.point);
    }
  }

} // KNN
