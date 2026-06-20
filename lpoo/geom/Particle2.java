package lpoo.geom;

/**
 * A 2-D particle with velocity and an RGB color.
 * Extends Point2 so it can be stored in a Quadtree<Particle2>.
 *
 * @author Paulo Pagliosa (adapted for LPOO assignment – A3)
 */
public class Particle2
  extends Point2
{
  public float vx;
  public float vy;

  /** RGB color components in [0, 255]. */
  public int r;
  public int g;
  public int b;

  // Named color constants for convenience
  public static final int[] RED    = {255,   0,   0};
  public static final int[] GREEN  = {  0, 255,   0};
  public static final int[] BLUE   = {  0,   0, 255};
  public static final int[] WHITE  = {255, 255, 255};
  public static final int[] BLACK  = {  0,   0,   0};
  public static final int[] YELLOW = {255, 255,   0};
  public static final int[] CYAN   = {  0, 255, 255};
  public static final int[] MAGENTA= {255,   0, 255};

  public Particle2()
  {
    // do nothing
  }

  public Particle2(float x, float y)
  {
    super(x, y);
  }

  public Particle2(float x, float y, int r, int g, int b)
  {
    super(x, y);
    setColor(r, g, b);
  }

  public Particle2(Point2 p)
  {
    super(p);
  }

  public Particle2(Particle2 p)
  {
    super(p);
    vx = p.vx;
    vy = p.vy;
    r  = p.r;
    g  = p.g;
    b  = p.b;
  }

  public void setColor(int r, int g, int b)
  {
    this.r = clamp(r);
    this.g = clamp(g);
    this.b = clamp(b);
  }

  public void setColor(int[] rgb)
  {
    setColor(rgb[0], rgb[1], rgb[2]);
  }

  /** Returns true if this particle's color matches the given RGB values. */
  public boolean hasColor(int r, int g, int b)
  {
    return this.r == r && this.g == g && this.b == b;
  }

  public boolean hasColor(int[] rgb)
  {
    return hasColor(rgb[0], rgb[1], rgb[2]);
  }

  public int[] getColor()
  {
    return new int[]{r, g, b};
  }

  private static int clamp(int v)
  {
    return Math.max(0, Math.min(255, v));
  }

  @Override
  public String toString()
  {
    return super.toString()
           + String.format(" vel<%.2f,%.2f> rgb(%d,%d,%d)", vx, vy, r, g, b);
  }

} // Particle2
