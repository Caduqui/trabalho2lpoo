import java.awt.geom.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import lpoo.geom.*;

/**
 *
 * @author Paulo Pagliosa (adaptado para trabalho LPOO)
 */
public class QuadtreeViewer<P extends Point2> extends JFrame
{
  private final QuadtreeControl<P> control;

  public QuadtreeViewer(Quadtree<P> qt)
  {
    super("Quadtree Viewer");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());

    control = new QuadtreeControl<>(qt);
    getContentPane().add(control, BorderLayout.CENTER);

    // Painel de botões
    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

    JButton btnClear = new JButton("Limpar busca");
    btnClear.addActionListener(e -> { control.clearSearch(); control.repaint(); });
    toolbar.add(btnClear);

    JLabel hint = new JLabel("  Teclas: '+' zoom in | '-' zoom out");
    hint.setForeground(Color.DARK_GRAY);
    toolbar.add(hint);

    getContentPane().add(toolbar, BorderLayout.SOUTH);

    setSize(800, 640);
    setLocationRelativeTo(null); // centraliza na tela
    setVisible(true);
    toFront();
  }

  // Destaca os k vizinhos mais próximos encontrados pelo KNN.
  public void showKNN(P queryPoint, KNN<P> knn)
  {
    control.setKNNResult(queryPoint, knn);
    control.repaint();
  }

  // Destaca os pontos encontrados pela busca por raio.
  public void showRadius(P queryPoint, float radius, List<P> results)
  {
    control.setRadiusResult(queryPoint, radius, results);
    control.repaint();
  }

  // Canvas interno
  private static final class QuadtreeControl<P extends Point2> extends Canvas
  {
    private final Quadtree<P> qt;

    // Escala e translação calculadas em componentResized / paint
    private float scale  = 1f;
    private float offX   = 0f;
    private float offY   = 0f;
    // Zoom manual + / -
    private float zoom   = 1f;

    // Estado da busca exibida
    private P queryPoint;
    private KNN<P> knn;
    private float searchRadius;
    private List<P> radiusResults;
    private boolean showingRadius;

    QuadtreeControl(Quadtree<P> qt)
    {
      this.qt = qt;
      setBackground(Color.WHITE);

      addKeyListener(new KeyAdapter()
      {
        @Override
        public void keyTyped(KeyEvent e)
        {
          char c = e.getKeyChar();
          if      (c == '+') { zoom *= 1.15f; repaint(); }
          else if (c == '-') { zoom /= 1.15f; repaint(); }
        }
      });

      setFocusable(true);
    }

    void clearSearch()
    {
      queryPoint    = null;
      knn           = null;
      radiusResults = null;
    }

    void setKNNResult(P qp, KNN<P> result)
    {
      queryPoint    = qp;
      knn           = result;
      radiusResults = null;
      showingRadius = false;
    }

    void setRadiusResult(P qp, float radius, List<P> results)
    {
      queryPoint    = qp;
      searchRadius  = radius;
      radiusResults = results;
      knn           = null;
      showingRadius = true;
    }

    // Calcula escala para encaixar a árvore no canvas com margem
    private void computeTransform()
    {
      int w = getWidth();
      int h = getHeight();
      if (w <= 0 || h <= 0) return;

      Bounds2 b  = qt.bounds();
      Point2  p1 = b.p1();
      Point2  sz = b.size();

      if (sz.x <= 0 || sz.y <= 0) return;

      float margin = 40f; // pixels de margem em cada lado
      float scaleX = (w - 2 * margin) / sz.x;
      float scaleY = (h - 2 * margin) / sz.y;
      scale = Math.min(scaleX, scaleY) * zoom;

      // Centralizamos a árvore no canvas
      offX = margin + ((w - 2 * margin) - sz.x * scale) / 2f - p1.x * scale;
      offY = margin + ((h - 2 * margin) - sz.y * scale) / 2f - p1.y * scale;
    }

    // Converte coordenadas para pixel
    private float wx(float x) { return x * scale + offX; }
    private float wy(float y) { return y * scale + offY; }

    private Shape shapeBox(Bounds2 bounds)
    {
      Point2 p1 = bounds.p1();
      Point2 sz = bounds.size();
      return new Rectangle2D.Float(wx(p1.x), wy(p1.y), sz.x * scale, sz.y * scale);
    }

    private Shape shapePoint(Point2 p)
    {
      float px = wx(p.x);
      float py = wy(p.y);
      return new Ellipse2D.Float(px - 3, py - 3, 6, 6);
    }

    private Shape shapeCircle(Point2 center, float radius)
    {
      float px = wx(center.x);
      float py = wy(center.y);
      float r  = radius * scale;
      return new Ellipse2D.Float(px - r, py - r, r * 2, r * 2);
    }

    @Override
    public void paint(Graphics g)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      computeTransform();

      // Conjunto de pontos em destaque (vermelho)
      Set<P> highlighted = new HashSet<>();
      if (knn != null)
        for (int i = 0; i < knn.size(); i++)
          highlighted.add(knn.get(i).point);
      if (radiusResults != null)
        highlighted.addAll(radiusResults);

      // Desenha nós da árvore
      for (Quadtree.NodeData<P> node : qt)
      {
        Shape r = shapeBox(node.bounds());

        // folha vazia -> verde, folha com pontos -> laranja
        g2.setColor(node.isEmpty() ? new Color(220, 255, 220) : new Color(255, 240, 200));
        g2.fill(r);
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(0.5f));
        g2.draw(r);

        for (P p : node)
          drawPoint(g2, p, highlighted.contains(p));
      }

      // Círculo de raio
      if (showingRadius && queryPoint != null)
      {
        g2.setColor(new Color(0, 100, 255, 50));
        g2.fill(shapeCircle(queryPoint, searchRadius));
        g2.setColor(new Color(0, 80, 200, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(shapeCircle(queryPoint, searchRadius));
      }

      // Linhas KNN: ponto de consulta → cada vizinho
      if (knn != null && queryPoint != null)
      {
        g2.setColor(new Color(180, 0, 0, 160));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < knn.size(); i++)
        {
          P np = knn.get(i).point;
          g2.draw(new Line2D.Float(wx(queryPoint.x), wy(queryPoint.y), wx(np.x), wy(np.y)));
        }
      }

      // Ponto de consulta (azul, maior)
      if (queryPoint != null)
      {
        float px = wx(queryPoint.x);
        float py = wy(queryPoint.y);
        g2.setColor(Color.BLUE);
        g2.fill(new Ellipse2D.Float(px - 6, py - 6, 12, 12));
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Float(px - 6, py - 6, 12, 12));
      }

      // Legenda
      g2.setStroke(new BasicStroke(1f));
      int ly = getHeight() - 8;
      g2.setColor(Color.DARK_GRAY);
      g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
      if (knn != null)
        g2.drawString("KNN: " + knn.size() + " vizinhos  (vermelho = vizinhos | azul = consulta)", 8, ly);
      else if (radiusResults != null)
        g2.drawString("Raio: " + radiusResults.size() + " pontos  (vermelho = resultado | azul = consulta | círculo = raio)", 8, ly);
      else
        g2.drawString("Pontos: " + qt.pointCount() + " | Nós: " + qt.size() + " | Folhas: " + qt.leafCount(), 8, ly);
    }

    private void drawPoint(Graphics2D g2, P p, boolean highlight)
    {
      if (highlight)
      {
        g2.setColor(Color.RED);
        float px = wx(p.x);
        float py = wy(p.y);
        g2.fill(new Ellipse2D.Float(px - 4, py - 4, 8, 8));
        return;
      }

      // Partículas usam sua cor RGB
      if (p instanceof Particle2)
      {
        Particle2 part = (Particle2) p;
        g2.setColor(new Color(part.r, part.g, part.b));
      }
      else
      {
        g2.setColor(Color.BLACK);
      }
      g2.fill(shapePoint(p));
    }

  } // QuadtreeControl

} // QuadtreeViewer
