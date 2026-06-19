package lpoo.geom;

/**
 * Stores up to k nearest neighbor pairs (point, squared distance),
 * kept sorted from closest to farthest.
 *
 * @author trabalho LPOO
 */
public class KNN<P extends Point2>
{
  public static class Entry<P extends Point2>
  {
    public final P point;
    public final float distance;

    Entry(P point, float distance)
    {
      this.point    = point;
      this.distance = distance;
    }
  }

  private final int k;
  private final Entry<P>[] entries;
  private int count;

   @SuppressWarnings("unchecked")
  public KNN(int k)
  {
    this.k = k;
    this.entries = new Entry[k];
    this.count = 0;
  }

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

  public Entry<P> get(int i)
  {
    return entries[i];
  }

  // Distância ao quadrado do vizinho mais distante (ou MAX_VALUE se vazio).
  public float maxDistanceSq()
  {
    if (count == 0)
      return Float.MAX_VALUE;
    // farthest is last; we stored actual distance, square it for comparison
    float d = entries[count - 1].distance;
    return d * d;
  }

  // Tenta inserir o ponto com distância ao quadrado dada.
  public boolean add(P point, float squaredDist)
  {
    float dist = (float) Math.sqrt(squaredDist);

    if (isFull() && dist >= entries[count - 1].distance)
      return false;

    int pos = count;
    for (int i = 0; i < count; i++)
      if (dist < entries[i].distance)
      {
        pos = i;
        break;
      }

    int end = isFull() ? k - 1 : count;
    for (int i = end; i > pos; i--)
      entries[i] = entries[i - 1];

    entries[pos] = new Entry<>(point, dist);
    if (!isFull())
      count++;
    return true;
  }

  public void print()
  {
    for (int i = 0; i < count; i++)
    {
      Entry<P> e = entries[i];
      System.out.printf("  [%d] dist=%.4f point=%s%n", i, e.distance, e.point);
    }
  }

} // KNN