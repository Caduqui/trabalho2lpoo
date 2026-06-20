import java.util.*;
import java.io.*;
import lpoo.geom.*;

/**
 *
 * @author Guilherme Lima Conte, João Pedro de Abreu Ferri, Ricardo Honório Trindade Tavares
 */

public class Main
{

  private static final String[] COLOR_NAMES = {"RED", "GREEN", "BLUE", "YELLOW", "CYAN", "MAGENTA"};
  private static final int[][]  COLOR_VALUES = {Particle2.RED, Particle2.GREEN, Particle2.BLUE, Particle2.YELLOW, Particle2.CYAN, Particle2.MAGENTA};
  public static void main(String[] args)
  {
    Console.println("=== Quadtree KNN ===");
    Console.println("Tipo de ponto:");
    Console.println("  1 - Point2");
    Console.println("  2 - Particle2");

    if (Console.readInt("Escolha") == 1)
      runPoint2Mode();
    else
      runParticle2Mode();
  }

  //Point2
  private static void runPoint2Mode()
  {
    Console.println("\n--- Modo Point2 ---");
    Point2[] points = loadOrGenerate(false);
    if (points == null || points.length == 0)
    {
      Console.println("Nenhum ponto disponivel."); 
      return;
    }

    Quadtree<Point2> qt = new Quadtree<>(points, Console.readPositiveInt("Pontos por no (minimo 5)"));
    printTreeInfo(qt);

    QuadtreeViewer<Point2> viewer = askViewer(qt);

    boolean run = true;
    while (run)
    {
      Console.println("\nOperacoes:");
      Console.println("  1 - KNN");
      Console.println("  2 - Busca por raio");
      Console.println("  3 - Imprimir vizinhos por indice");
      Console.println("  0 - Sair");
      switch (Console.readInt("Operacao"))
      {
        case 1: 
          doKNN(qt, points, null, viewer);
          break;
        case 2: 
          doRadius(qt, points, null, viewer);       
          break;
        case 3: 
          printNeighborsByIndex(qt, points, null);  
          break;
        case 0: 
          run = false;                              
          break;
        default: 
          Console.error("invalid option");
      }
    }
  }

  //Particle2
  private static void runParticle2Mode()
  {
    Console.println("\n--- Modo Particle2 ---");
    Particle2[] particles = loadOrGenerateParticle2();
    if (particles == null || particles.length == 0)
    {
      Console.println("Nenhuma particula disponivel."); 
      return;
    }

    Quadtree<Particle2> qt = new Quadtree<>(particles, Console.readPositiveInt("Pontos por no (minimo 5)"));
    printTreeInfo(qt);

    Console.println("\nCores presentes:");
    showColors(particles);

    QuadtreeViewer<Particle2> viewer = askViewer(qt);

    boolean run = true;
    while (run)
    {
      Console.println("\nOperacoes:");
      Console.println("  1 - KNN sem filtro de cor");
      Console.println("  2 - KNN com filtro de cor");
      Console.println("  3 - Busca por raio sem filtro");
      Console.println("  4 - Busca por raio com filtro de cor");
      Console.println("  5 - Imprimir vizinhos por indice");
      Console.println("  0 - Sair");
      switch (Console.readInt("Operacao"))
      {
        case 1:
          doKNN(qt, particles, null, viewer);   
          break;
        case 2:
        {
          int[] color = askColor();
          doKNN(qt, particles, p -> p.hasColor(color), viewer); 
          break;
        }
        case 3:
          doRadius(qt, particles, null, viewer);   
          break;
        case 4:
        {
          int[] color = askColor();
          doRadius(qt, particles, p -> p.hasColor(color), viewer); 
          break;
        }
        case 5:
          printNeighborsByIndex(qt, particles, null);  
          break;
        case 0:
          run = false;
          break;
        default:
          Console.error("invalid option");
      }
    }
  }

  private static <P extends Point2> void doKNN(Quadtree<P> qt, P[] points, PointFunc<P> filter, QuadtreeViewer<P> viewer)
  {
    int idx = readIndex(points.length); if (idx < 0) return;
    int k = Console.readPositiveInt("k");

    P query = points[idx];
    KNN<P> knn = qt.findNeighbors(query, k, filter);

    Console.printf("%nConsulta[%d]: %s%n", idx, query);
    Console.printf("Vizinhos (k=%d, encontrados=%d):%n", k, knn.size());
    for (int i = 0; i < knn.size(); i++)
    {
      KNN.Entry<P> e = knn.get(i);
      Console.printf("  [%d] dist=%.4f  %s%n", i, e.distance, e.point);
    }

    if (viewer != null) viewer.showKNN(query, knn);
  }

  private static <P extends Point2> void doRadius(Quadtree<P> qt, P[] points, PointFunc<P> filter, QuadtreeViewer<P> viewer)
  {
    int idx = readIndex(points.length); if (idx < 0) return;
    float radius = Console.readPositiveFloat("Raio");

    P query = points[idx];
    List<P> found = new ArrayList<>();
    long count = qt.forEachNeighbor(query, radius, p -> { found.add(p); return true; }, filter);

    Console.printf("%nConsulta[%d]: %s  raio=%.4f%n", idx, query, radius);
    Console.printf("Encontrados: %d%n", count);
    for (int i = 0; i < found.size(); i++)
      Console.printf("  [%d] dist=%.4f  %s%n", i, Point2.distance(query, found.get(i)), found.get(i));

    if (viewer != null) viewer.showRadius(query, radius, found);
  }

  private static <P extends Point2> void printNeighborsByIndex(Quadtree<P> qt, P[] points, PointFunc<P> filter)
  {
    int idx = readIndex(points.length); if (idx < 0) return;
    int k = Console.readPositiveInt("k");

    P query = points[idx];
    KNN<P> knn = qt.findNeighbors(query, k, filter);

    Console.printf("%nPonto[%d]: %s%n", idx, query);
    Console.printf("Vizinhos (k=%d):%n", k);
    for (int i = 0; i < knn.size(); i++)
    {
      KNN.Entry<P> e = knn.get(i);
      Console.printf("  [%d] dist=%.4f  %s%n", i, e.distance, e.point);
    }
  }

  // Viewer
  private static <P extends Point2> QuadtreeViewer<P> askViewer(Quadtree<P> qt)
  {
    char resp = Console.readOption("Abrir visualizador grafico (s/n)", "sSnN");
    if (resp == 's' || resp == 'S')
    {
      try
      {
        return new QuadtreeViewer<>(qt);
      }
      catch (Exception e)
      {
        Console.error("could not open viewer: " + e.getMessage());
      }
    }
    return null;
  }

  private static Point2[] loadOrGenerate(boolean particle)
  {
    Console.println("Fonte:");
    Console.println("  1 - Arquivo texto");
    Console.println("  2 - Gerar aleatoriamente");
    return Console.readInt("Escolha") == 1 ? loadPoint2FromFile() : generatePoint2();
  }

  // Carrega ou gera – Particle2
  private static Particle2[] loadOrGenerateParticle2()
  {
    Console.println("Fonte:");
    Console.println("  1 - Arquivo texto");
    Console.println("  2 - Gerar aleatoriamente");
    return Console.readInt("Escolha") == 1 ? loadParticle2FromFile() : generateParticle2();
  }

  // Geração aleatória – Point2
  private static Point2[] generatePoint2()
  {
    int n = Console.readPositiveInt("Numero de pontos");
    float max = Console.readPositiveFloat("Coordenada maxima (ex: 100.0)");
    Random rng = new Random();
    Point2[] pts = new Point2[n];

    for (int i = 0; i < n; i++)
      pts[i] = new Point2(rng.nextFloat() * max, rng.nextFloat() * max);
    
    Console.printf("%d pontos gerados.%n", n);
    return pts;
  }

  // Geração aleatória – Particle2
  private static Particle2[] generateParticle2()
  {
    int n = Console.readPositiveInt("Numero de particulas");
    float max = Console.readPositiveFloat("Coordenada maxima (ex: 100.0)");
    Random rng = new Random();
    int[][] palette = {
      Particle2.RED, Particle2.GREEN, Particle2.BLUE,
      Particle2.YELLOW, Particle2.CYAN, Particle2.MAGENTA
    };
    Particle2[] ps = new Particle2[n];
    for (int i = 0; i < n; i++)
    {
      Particle2 p = new Particle2(rng.nextFloat() * max, rng.nextFloat() * max);
      p.vx = (rng.nextFloat() - 0.5f) * 10f;
      p.vy = (rng.nextFloat() - 0.5f) * 10f;
      p.setColor(palette[i % palette.length]);
      ps[i] = p;
    }
    Console.printf("%d particulas geradas.%n", n);
    return ps;
  }

  // Leitura de arquivo – Point2  (formato: x y por linha)
  private static Point2[] loadPoint2FromFile()
  {
    String path = Console.readString("Caminho do arquivo");
    List<Point2> list = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(path)))
    {
      String line;
      while ((line = br.readLine()) != null)
      {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        String[] p = line.split("\\s+");
        if (p.length >= 2)
          list.add(new Point2(Float.parseFloat(p[0]), Float.parseFloat(p[1])));
      }
      Console.printf("%d pontos lidos.%n", list.size());
    }
    catch (IOException e)
    {
      Console.error("reading file: " + e.getMessage());
    }
    return list.toArray(new Point2[0]);
  }

  // Leitura de arquivo – Particle2  (formato: x y vx vy r g b por linha)
  private static Particle2[] loadParticle2FromFile()
  {
    String path = Console.readString("Caminho do arquivo");
    List<Particle2> list = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(path)))
    {
      String line;
      while ((line = br.readLine()) != null)
      {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        String[] p = line.split("\\s+");
        if (p.length >= 7)
        {
          Particle2 pt = new Particle2(Float.parseFloat(p[0]), Float.parseFloat(p[1]));
          pt.vx = Float.parseFloat(p[2]);
          pt.vy = Float.parseFloat(p[3]);
          pt.setColor(Integer.parseInt(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6]));
          list.add(pt);
        }
      }
      Console.printf("%d particulas lidas.%n", list.size());
    }
    catch (IOException e)
    {
      Console.error("reading file: " + e.getMessage());
    }
    return list.toArray(new Particle2[0]);
  }

  // Utilitários
  private static void printTreeInfo(Quadtree<?> qt)
  {
    Console.printf("%nArvore: %d pontos | %d nos | %d folhas%n", qt.pointCount(), qt.size(), qt.leafCount());
    Console.printf("Bounds: %s%n", qt.bounds());
  }

  private static void showColors(Particle2[] ps)
  {
    Set<String> seen = new LinkedHashSet<>();
    for (Particle2 p : ps)
      seen.add("rgb(" + p.r + "," + p.g + "," + p.b + ")");
    for (String c : seen)
      Console.println("  " + c);
  }

   private static int[] askColor()
  {
    Console.println("Cor do filtro:");
    for (int i = 0; i < COLOR_NAMES.length; i++)
      Console.printf("  %d - %s%n", i + 1, COLOR_NAMES[i]);
    int choice = Console.readInt("Escolha") - 1;
    if (choice < 0 || choice >= COLOR_VALUES.length)
    {
      Console.error("invalid color");
      return COLOR_VALUES[0];
    }
    return COLOR_VALUES[choice];
  }

  private static int readIndex(int max)
  {
    int idx = Console.readInt("Indice do ponto [0.." + (max - 1) + "]");
    if (idx < 0 || idx >= max)
    {
      Console.error("index out of range");
      return -1;
    }
    return idx;
  }

} // Main
