package lpoo.geom;

import lpoo.math.*;

/**
 *
 * @author Paulo Pagliosa
 */
public class Point2
{
  public static float distance(Point2 p1, Point2 p2)
  {
    float dx = p2.x - p1.x;
    float dy = p2.y - p1.y;

    return (float)Math.sqrt(dx * dx + dy * dy);
  }

  public float x;
  public float y;

  public Point2()
  {
    // do nothing
  }

  public Point2(float x, float y)
  {
    this.x = x;
    this.y = y;
  }

  public Point2(Point2 p)
  {
    this(p.x, p.y);
  }

  public boolean isEqual(Point2 p)
  {
    return Real.isEqual(x, p.x) && Real.isEqual(y, p.y);
  }

  public Point2 mul(float s)
  {
    return new Point2(x * s, y * s);
  }

  public Point2 add(Point2 p)
  {
    return new Point2(x + p.x, y + p.y);
  }

  public Point2 sub(Point2 p)
  {
    return new Point2(x - p.x, y - p.y);
  }

  @Override
  public String toString()
  {
    return String.format("<%.2f,%.2f>", x, y);
  }

  public void print(String label)
  {
    System.out.println(label + toString());
  }

} // Point2
