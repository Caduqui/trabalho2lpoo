package lpoo.geom;

/**
 *
 * @author Paulo Pagliosa
 */
public class Bounds2
{
  public Bounds2()
  {
    p1 = new Point2(+Float.MAX_VALUE, +Float.MAX_VALUE);
    p2 = new Point2(-Float.MAX_VALUE, -Float.MAX_VALUE);
  }

  public Bounds2(Point2 p, Point2 size)
  {
    p1 = new Point2(p);
    p2 = new Point2(p.x + size.x, p.y + size.y);
  }

  public Bounds2(Bounds2 b)
  {
    p1 = new Point2(b.p1);
    p2 = new Point2(b.p2);
  }

  public Point2 p1()
  {
    return new Point2(p1);
  }

  public Point2 p2()
  {
    return new Point2(p2);
  }

  public Bounds2 inflate(float x, float y)
  {
    if (x < p1.x)
      p1.x = x;
    if (y < p1.y)
      p1.y = y;
    if (x > p2.x)
      p2.x = x;
    if (y > p2.y)
      p2.y = y;
    return this;
  }

  public Bounds2 inflate(Point2 p)
  {
    return inflate(p.x, p.y);
  }

  public Bounds2 inflate(float s)
  {
    Point2 t = center().mul(1 - s);

    p1.x = p1.x * s + t.x;
    p1.y = p1.y * s + t.y;
    p2.x = p2.x * s + t.x;
    p2.y = p2.y * s + t.y;
    return this;
  }

  public Point2 center()
  {
    return new Point2((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
  }

  public Point2 size()
  {
    return new Point2(p2.x - p1.x, p2.y - p1.y);
  }

  public boolean contains(Point2 p)
  {
    return p1.x <= p.x && p2.x > p.x && p1.y <= p.y && p2.y > p.y;
  }

  // Necessário para a poda da árvore nas buscas
  public float squaredDistance(Point2 p)
  {
    float dx = Math.max(0, Math.max(p1.x - p.x, p.x - p2.x));
    float dy = Math.max(0, Math.max(p1.y - p.y, p.y - p2.y));
    return dx * dx + dy * dy;
  }

  @Override
  public String toString()
  {
    return String.format("p1%s p2%s", p1.toString(), p2.toString());
  }

  public void print(String label)
  {
    System.out.println(label + toString());
  }

  private final Point2 p1;
  private final Point2 p2;

} // Bounds2
