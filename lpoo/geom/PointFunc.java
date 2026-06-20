package lpoo.geom;

/**
 *
 * @author Guilherme Lima Conte, João Pedro de Abreu Ferri, Ricardo Honório Trindade Tavares
 */

@FunctionalInterface
public interface PointFunc<P>
{
  boolean run(P point);

} // PointFunc
