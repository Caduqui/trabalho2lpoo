package lpoo.geom;

import java.util.*;

/**
 * @param <P> any subtype of Point2
 * @author Paulo Pagliosa (adapted for LPOO assignment)
 */
public class Quadtree<P extends Point2>
  implements Iterable<Quadtree.NodeData<P>>
{
  public static class NodeData<P extends Point2>
    implements Iterable<P>
  {
    public final Bounds2 bounds()
    {
      return new Bounds2(bounds);
    }

    public final boolean contains(Point2 p)
    {
      return bounds.contains(p);
    }

    public final boolean isEmpty()
    {
      return points.isEmpty();
    }

    @Override
    public final Iterator<P> iterator()
    {
      return points.iterator();
    }

    public final int pointCount()
    {
      return points.size();
    }

    public final int depth()
    {
      return depth;
    }

    protected final Bounds2 bounds;
    protected final LinkedList<P> points = new LinkedList<>();
    protected final int depth;

    protected NodeData(final Bounds2 bounds)
    {
      this.bounds = bounds;
      depth = 0;
    }

    protected NodeData(Point2 p, Point2 size, int depth)
    {
      bounds = new Bounds2(p, size);
      this.depth = depth;
    }

  } // NodeData

  public static final float fatFactor      = 1.01f;
  public static final int   minPointsPerNode = 5;
  public static final int   maxDepth        = 8;

  public final int pointsPerNode;

  public Quadtree(final P[] points, int pointsPerNode)
  {
    this(bounds(points), pointsPerNode);
    for (P p : points)
      root.add(p);
    split(root);
  }

  public final Bounds2 bounds()
  {
    return root.bounds();
  }

  public final int pointCount()
  {
    return pointCount(root);
  }

  public final int size()
  {
    return nodeCount;
  }

  public final int leafCount()
  {
    return leafCount;
  }

  @Override
  public Iterator<NodeData<P>> iterator()
  {
    return new QuadtreeLeafIterator<>(root);
  }

  // Busca KNN
  public KNN<P> findNeighbors(P point, int k, PointFunc<P> filter)
  {
    KNN<P> knn = new KNN<>(k);
    findNeighbors(root, point, knn, filter);
    return knn;
  }

  // Busca por raio
  public long forEachNeighbor(P point, float radius, PointFunc<P> f, PointFunc<P> filter)
  {
    long[] counter = {0L};
    boolean[] stopped = {false};
    forEachNeighbor(root, point, radius * radius, f, filter, counter, stopped);
    return counter[0];
  }

  static class Node<P extends Point2> extends NodeData<P>
  {
    Node(final Bounds2 bounds)
    {
      super(bounds);
    }

    Node(Point2 p, Point2 size, int depth)
    {
      super(p, size, depth);
    }

    final boolean isLeaf()
    {
      return children == null;
    }

    final void clear()
    {
      points.clear();
    }

    final void add(P p)
    {
      points.add(p);
    }

    Node<P>[] children;

  } // Node

  private Node<P> root;
  private int nodeCount;
  private int leafCount;

  private Quadtree(final Bounds2 bounds, int pointsPerNode)
  {
    this.pointsPerNode = Math.max(minPointsPerNode, pointsPerNode);
    root = new Node<>(new Bounds2(bounds).inflate(fatFactor));
    nodeCount = 1;
    leafCount = 1;
  }

  private void split(Node<P> node)
  {
    if (node.pointCount() <= pointsPerNode || node.depth == maxDepth)
      return;

    Point2 p = node.bounds.p1();
    Point2 s = node.bounds.size().mul(0.5f);
    int d = node.depth + 1;

    @SuppressWarnings("unchecked")
    Node<P>[] ch = new Node[4];
    ch[0] = new Node<>(p, s, d);
    ch[1] = new Node<>(new Point2(p.x + s.x, p.y),           s, d);
    ch[2] = new Node<>(new Point2(p.x + s.x, p.y + s.y),     s, d);
    ch[3] = new Node<>(new Point2(p.x,       p.y + s.y),     s, d);
    node.children = ch;
    leafCount += 3;
    nodeCount  += 4;

    for (P pt : node)
      for (int i = 0; i < 4; i++)
        if (ch[i].contains(pt))
        {
          ch[i].add(pt);
          break;
        }
    node.clear();

    for (int i = 0; i < 4; i++)
      split(ch[i]);
  }

  private static <P extends Point2> Bounds2 bounds(final P[] points)
  {
    Bounds2 b = new Bounds2();
    for (P p : points)
      b.inflate(p);
    return b;
  }

  private static <P extends Point2> int pointCount(Node<P> node)
  {
    if (node.isLeaf())
      return node.pointCount();
    int count = 0;
    for (int i = 0; i < 4; i++)
      count += pointCount(node.children[i]);
    return count;
  }

  //KNN Busca Recursiva
  private void findNeighbors(Node<P> node, P point, KNN<P> knn, PointFunc<P> filter)
  {
    float nodeDistSq = node.bounds.squaredDistance(point);
    if (knn.isFull() && nodeDistSq >= knn.maxDistanceSq())
      return;

    if (node.isLeaf())
    {
      for (P p : node)
      {
        if (p == point)
          continue;
        if (filter != null && !filter.run(p))
          continue;
        float dx = p.x - point.x;
        float dy = p.y - point.y;
        knn.add(p, dx * dx + dy * dy);
      }
    }
    else
    {
      int[] order = childOrder(node, point);
      for (int idx : order)
        findNeighbors(node.children[idx], point, knn, filter);
    }
  }

  //Busca por raio recursiva
  private void forEachNeighbor(Node<P> node, P point, float radiusSq, PointFunc<P> f, PointFunc<P> filter, long[] counter, boolean[] stopped)
  {
    if (stopped[0])
      return;

    float nodeDistSq = node.bounds.squaredDistance(point);
    if (nodeDistSq > radiusSq)
      return;

    if (node.isLeaf())
    {
      for (P p : node)
      {
        if (stopped[0])
          return;
        if (p == point)
          continue;
        if (filter != null && !filter.run(p))
          continue;
        float dx = p.x - point.x;
        float dy = p.y - point.y;
        float distSq = dx * dx + dy * dy;
        if (distSq <= radiusSq)
        {
          counter[0]++;
          if (!f.run(p))
          {
            stopped[0] = true;
            return;
          }
        }
      }
    }
    else
    {
      int[] order = childOrder(node, point);
      for (int idx : order)
        forEachNeighbor(node.children[idx], point, radiusSq, f, filter, counter, stopped);
    }
  }

  private int[] childOrder(Node<P> node, P point)
  {
    float[] dists = new float[4];
    for (int i = 0; i < 4; i++)
      dists[i] = node.children[i].bounds.squaredDistance(point);

    Integer[] order = {0, 1, 2, 3};
    for (int i = 1; i < 4; i++)
    {
      Integer key = order[i];
      int j = i - 1;
      while (j >= 0 && dists[order[j]] > dists[key])
      {
        order[j + 1] = order[j];
        j--;
      }
      order[j + 1] = key;
    }
    return new int[]{order[0], order[1], order[2], order[3]};
  }

} // Quadtree

final class QuadtreeLeafIterator<P extends Point2>
  implements Iterator<Quadtree.NodeData<P>>
{
  @Override
  public boolean hasNext()
  {
    return !stack.empty();
  }

  @Override
  public Quadtree.NodeData<P> next()
  {
  Quadtree.Node<P> node = stack.pop();
  while (!node.isLeaf())
  {
    for (int i = 4; i > 0;)
    {
      stack.push(node.children[--i]);
    }
    node = stack.pop();
  }
  return node;
  }

  QuadtreeLeafIterator(Quadtree.Node<P> root)
  {
    stack = new Stack<>();
    stack.push(root);
  }

  private Stack<Quadtree.Node<P>> stack;

} // QuadtreeLeafIterator
