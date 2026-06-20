package lpoo.geom;

/**
 * Functional interface for filtering/processing points.
 * @author Paulo Pagliosa
 */
@FunctionalInterface
public interface PointFunc<P>
{
  boolean run(P point);

} // PointFunc
