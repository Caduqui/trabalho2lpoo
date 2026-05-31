import java.util.*;
import java.io.*;
import lpoo.geom.*;

/**
 * Aplicação de teste interativa para a Quadtree genérica.
 * Cobre as tarefas A2, A3, A4 e integra o QuadtreeViewer (A5).
 */
public class Main
{
  // -----------------------------------------------------------------------
  // Entry point
  // -----------------------------------------------------------------------
  public static void main(String[] args)
  {
    Console console = new Console();
    console.println("=== Quadtree KNN ===");
    console.println("Tipo de ponto:");
    console.println("  1 - Point2");
    console.println("  2 - Particle2");
    int tipo = console.readInt("Escolha: ");

    if (tipo == 1)
      runPoint2Mode(console);
    else
      runParticle2Mode(console);
  }

  // -----------------------------------------------------------------------
  // Modo Point2
  // -----------------------------------------------------------------------
  private static void runPoint2Mode(Console console)
  {
    console.println("\n--- Modo Point2 ---");
    Point2[] points = loadOrGenerate(console, false);
    if (points == null || points.length == 0)
    {
      console.println("Nenhum ponto disponivel."); return;
    }

    int ppn = console.readInt("Pontos por no (minimo 5): ");
    Quadtree<Point2> qt = new Quadtree<>(points, ppn);
    printTreeInfo(qt, console);

    // Pergunta se quer abrir o viewer
    QuadtreeViewer<Point2> viewer = askViewer(qt, console);

    boolean run = true;
    while (run)
    {
      console.println("\nOperacoes:");
      console.println("  1 - KNN");
      console.println("  2 - Busca por raio");
      console.println("  3 - Imprimir vizinhos por indice");
      console.println("  0 - Sair");
      switch (console.readInt("Operacao: "))
      {
        case 1: doKNN(qt, points, console, null, viewer);           break;
        case 2: doRadius(qt, points, console, null, viewer);        break;
        case 3: printNeighborsByIndex(qt, points, console, null);   break;
        case 0: run = false;                                        break;
        default: console.println("Opcao invalida.");
      }
    }
  }

  // -----------------------------------------------------------------------
  // Modo Particle2
  // -----------------------------------------------------------------------
  private static void runParticle2Mode(Console console)
  {
    console.println("\n--- Modo Particle2 ---");

    // loadOrGenerate retorna Point2[], fazemos cast seguro via geração direta
    Particle2[] particles = loadOrGenerateParticles(console);
    if (particles == null || particles.length == 0)
    {
      console.println("Nenhuma particula disponivel."); return;
    }

    int ppn = console.readInt("Pontos por no (minimo 5): ");
    Quadtree<Particle2> qt = new Quadtree<>(particles, ppn);
    printTreeInfo(qt, console);

    console.println("\nCores presentes:");
    showColors(particles, console);

    QuadtreeViewer<Particle2> viewer = askViewer(qt, console);

    boolean run = true;
    while (run)
    {
      console.println("\nOperacoes:");
      console.println("  1 - KNN sem filtro de cor");
      console.println("  2 - KNN com filtro de cor");
      console.println("  3 - Busca por raio sem filtro");
      console.println("  4 - Busca por raio com filtro de cor");
      console.println("  5 - Imprimir vizinhos por indice");
      console.println("  0 - Sair");
      switch (console.readInt("Operacao: "))
      {
        case 1:
          doKNNParticle(qt, particles, console, null, viewer);
          break;
        case 2:
        {
          int[] cor = askColor(console);
          doKNNParticle(qt, particles, console, p -> p.hasColor(cor), viewer);
          break;
        }
        case 3:
          doRadiusParticle(qt, particles, console, null, viewer);
          break;
        case 4:
        {
          int[] cor = askColor(console);
          doRadiusParticle(qt, particles, console, p -> p.hasColor(cor), viewer);
          break;
        }
        case 5:
          printParticleNeighborsByIndex(qt, particles, console, null);
          break;
        case 0:
          run = false;
          break;
        default:
          console.println("Opcao invalida.");
      }
    }
  }

  // -----------------------------------------------------------------------
  // KNN – Point2
  // -----------------------------------------------------------------------
  private static void doKNN(Quadtree<Point2> qt,
                             Point2[] points,
                             Console console,
                             PointFunc<Point2> filter,
                             QuadtreeViewer<Point2> viewer)
  {
    int idx = readIndex(console, points.length); if (idx < 0) return;
    int k   = console.readInt("k: ");

    Point2 query = points[idx];
    KNN<Point2> knn = qt.findNeighbors(query, k, filter);

    console.printf("%nConsulta Point2[%d]: %s%n", idx, query);
    console.printf("Vizinhos (k=%d, encontrados=%d):%n", k, knn.size());
    for (int i = 0; i < knn.size(); i++)
    {
      KNN.Entry<Point2> e = knn.get(i);
      console.printf("  [%d] dist=%.4f  %s%n", i, e.distance, e.point);
    }

    if (viewer != null) viewer.showKNN(query, knn);
  }

  // -----------------------------------------------------------------------
  // KNN – Particle2
  // -----------------------------------------------------------------------
  private static void doKNNParticle(Quadtree<Particle2> qt,
                                     Particle2[] particles,
                                     Console console,
                                     PointFunc<Particle2> filter,
                                     QuadtreeViewer<Particle2> viewer)
  {
    int idx = readIndex(console, particles.length); if (idx < 0) return;
    int k   = console.readInt("k: ");

    Particle2 query = particles[idx];
    KNN<Particle2> knn = qt.findNeighbors(query, k, filter);

    console.printf("%nConsulta Particle2[%d]: %s%n", idx, query);
    console.printf("Vizinhos (k=%d, encontrados=%d):%n", k, knn.size());
    for (int i = 0; i < knn.size(); i++)
    {
      KNN.Entry<Particle2> e = knn.get(i);
      console.printf("  [%d] dist=%.4f  %s%n", i, e.distance, e.point);
    }

    if (viewer != null) viewer.showKNN(query, knn);
  }

  // -----------------------------------------------------------------------
  // Busca por raio – Point2
  // -----------------------------------------------------------------------
  private static void doRadius(Quadtree<Point2> qt,
                                Point2[] points,
                                Console console,
                                PointFunc<Point2> filter,
                                QuadtreeViewer<Point2> viewer)
  {
    int   idx    = readIndex(console, points.length); if (idx < 0) return;
    float radius = console.readFloat("Raio: ");

    Point2 query = points[idx];
    List<Point2> found = new ArrayList<>();

    long count = qt.forEachNeighbor(query, radius,
      p -> { found.add(p); return true; }, filter);

    console.printf("%nConsulta Point2[%d]: %s  raio=%.4f%n", idx, query, radius);
    console.printf("Encontrados: %d%n", count);
    for (int i = 0; i < found.size(); i++)
    {
      float d = Point2.distance(query, found.get(i));
      console.printf("  [%d] dist=%.4f  %s%n", i, d, found.get(i));
    }

    if (viewer != null) viewer.showRadius(query, radius, found);
  }

  // -----------------------------------------------------------------------
  // Busca por raio – Particle2
  // -----------------------------------------------------------------------
  private static void doRadiusParticle(Quadtree<Particle2> qt,
                                        Particle2[] particles,
                                        Console console,
                                        PointFunc<Particle2> filter,
                                        QuadtreeViewer<Particle2> viewer)
  {
    int   idx    = readIndex(console, particles.length); if (idx < 0) return;
    float radius = console.readFloat("Raio: ");

    Particle2 query = particles[idx];
    List<Particle2> found = new ArrayList<>();

    long count = qt.forEachNeighbor(query, radius,
      p -> { found.add(p); return true; }, filter);

    console.printf("%nConsulta Particle2[%d]: %s  raio=%.4f%n", idx, query, radius);
    console.printf("Encontradas: %d%n", count);
    for (int i = 0; i < found.size(); i++)
    {
      float d = Point2.distance(query, found.get(i));
      console.printf("  [%d] dist=%.4f  %s%n", i, d, found.get(i));
    }

    if (viewer != null) viewer.showRadius(query, radius, found);
  }

  // -----------------------------------------------------------------------
  // Imprimir vizinhos por índice – Point2
  // -----------------------------------------------------------------------
  private static void printNeighborsByIndex(Quadtree<Point2> qt,
                                             Point2[] points,
                                             Console console,
                                             PointFunc<Point2> filter)
  {
    int idx = readIndex(console, points.length); if (idx < 0) return;
    int k   = console.readInt("k: ");

    Point2 query = points[idx];
    console.printf("%nPoint2[%d]: %s%n", idx, query);
    KNN<Point2> knn = qt.findNeighbors(query, k, filter);
    console.printf("Vizinhos (k=%d):%n", k);
    for (int i = 0; i < knn.size(); i++)
    {
      KNN.Entry<Point2> e = knn.get(i);
      console.printf("  [%d] dist=%.4f  %s%n", i, e.distance, e.point);
    }
  }

  // -----------------------------------------------------------------------
  // Imprimir vizinhos por índice – Particle2
  // -----------------------------------------------------------------------
  private static void printParticleNeighborsByIndex(Quadtree<Particle2> qt,
                                                     Particle2[] particles,
                                                     Console console,
                                                     PointFunc<Particle2> filter)
  {
    int idx = readIndex(console, particles.length); if (idx < 0) return;
    int k   = console.readInt("k: ");

    Particle2 query = particles[idx];
    console.printf("%nParticle2[%d]: %s%n", idx, query);
    KNN<Particle2> knn = qt.findNeighbors(query, k, filter);
    console.printf("Vizinhos (k=%d):%n", k);
    for (int i = 0; i < knn.size(); i++)
    {
      KNN.Entry<Particle2> e = knn.get(i);
      console.printf("  [%d] dist=%.4f  %s%n", i, e.distance, e.point);
    }
  }

  // -----------------------------------------------------------------------
  // Viewer – pergunta se quer abrir e devolve instância ou null
  // -----------------------------------------------------------------------
  private static <P extends Point2> QuadtreeViewer<P> askViewer(Quadtree<P> qt,
                                                                  Console console)
  {
    console.println("\nAbrir visualizador grafico? (s/n)");
    String resp = console.readLine("> ").toLowerCase();
    if (resp.startsWith("s"))
    {
      try
      {
        return new QuadtreeViewer<>(qt);
      }
      catch (Exception e)
      {
        console.println("Nao foi possivel abrir o visualizador: " + e.getMessage());
      }
    }
    return null;
  }

  // -----------------------------------------------------------------------
  // Carrega ou gera pontos Point2
  // -----------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  private static Point2[] loadOrGenerate(Console console, boolean particle)
  {
    console.println("Fonte:");
    console.println("  1 - Arquivo texto");
    console.println("  2 - Gerar aleatoriamente");
    int fonte = console.readInt("Escolha: ");
    if (fonte == 1)
      return loadPoint2FromFile(console);
    else
      return generatePoint2(console);
  }

  private static Particle2[] loadOrGenerateParticles(Console console)
  {
    console.println("Fonte:");
    console.println("  1 - Arquivo texto");
    console.println("  2 - Gerar aleatoriamente");
    int fonte = console.readInt("Escolha: ");
    if (fonte == 1)
      return loadParticle2FromFile(console);
    else
      return generateParticle2(console);
  }

  // -----------------------------------------------------------------------
  // Geração aleatória – Point2
  // -----------------------------------------------------------------------
  private static Point2[] generatePoint2(Console console)
  {
    int   n        = console.readInt("Numero de pontos: ");
    float maxCoord = console.readFloat("Coordenada maxima (ex: 100.0): ");
    Random rng = new Random();
    Point2[] pts = new Point2[n];
    for (int i = 0; i < n; i++)
      pts[i] = new Point2(rng.nextFloat() * maxCoord, rng.nextFloat() * maxCoord);
    console.printf("%d pontos gerados.%n", n);
    return pts;
  }

  // -----------------------------------------------------------------------
  // Geração aleatória – Particle2
  // -----------------------------------------------------------------------
  private static Particle2[] generateParticle2(Console console)
  {
    int   n        = console.readInt("Numero de particulas: ");
    float maxCoord = console.readFloat("Coordenada maxima (ex: 100.0): ");
    Random rng = new Random();
    int[][] palette = {
      Particle2.RED, Particle2.GREEN, Particle2.BLUE,
      Particle2.YELLOW, Particle2.CYAN, Particle2.MAGENTA
    };
    Particle2[] ps = new Particle2[n];
    for (int i = 0; i < n; i++)
    {
      Particle2 p = new Particle2(rng.nextFloat() * maxCoord,
                                   rng.nextFloat() * maxCoord);
      p.vx = (rng.nextFloat() - 0.5f) * 10f;
      p.vy = (rng.nextFloat() - 0.5f) * 10f;
      p.setColor(palette[i % palette.length]);
      ps[i] = p;
    }
    console.printf("%d particulas geradas.%n", n);
    return ps;
  }

  // -----------------------------------------------------------------------
  // Leitura de arquivo – Point2  (formato: x y por linha)
  // -----------------------------------------------------------------------
  private static Point2[] loadPoint2FromFile(Console console)
  {
    String path = console.readLine("Caminho do arquivo: ");
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
      console.printf("%d pontos lidos.%n", list.size());
    }
    catch (IOException e) { console.println("Erro: " + e.getMessage()); }
    return list.toArray(new Point2[0]);
  }

  // -----------------------------------------------------------------------
  // Leitura de arquivo – Particle2  (formato: x y vx vy r g b por linha)
  // -----------------------------------------------------------------------
  private static Particle2[] loadParticle2FromFile(Console console)
  {
    String path = console.readLine("Caminho do arquivo: ");
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
      console.printf("%d particulas lidas.%n", list.size());
    }
    catch (IOException e) { console.println("Erro: " + e.getMessage()); }
    return list.toArray(new Particle2[0]);
  }

  // -----------------------------------------------------------------------
  // Utilitários
  // -----------------------------------------------------------------------
  private static void printTreeInfo(Quadtree<?> qt, Console console)
  {
    console.printf("%nArvore: %d pontos | %d nos | %d folhas%n",
                   qt.pointCount(), qt.size(), qt.leafCount());
    console.printf("Bounds: %s%n", qt.bounds());
  }

  private static void showColors(Particle2[] ps, Console console)
  {
    Set<String> seen = new LinkedHashSet<>();
    for (Particle2 p : ps)
      seen.add("rgb(" + p.r + "," + p.g + "," + p.b + ")");
    for (String c : seen)
      console.println("  " + c);
  }

  private static int[] askColor(Console console)
  {
    console.println("Cor do filtro (RGB):");
    int r = console.readInt("  r [0-255]: ");
    int g = console.readInt("  g [0-255]: ");
    int b = console.readInt("  b [0-255]: ");
    return new int[]{r, g, b};
  }

  private static int readIndex(Console console, int max)
  {
    int idx = console.readInt("Indice do ponto [0.." + (max - 1) + "]: ");
    if (idx < 0 || idx >= max)
    {
      System.out.println("Indice invalido.");
      return -1;
    }
    return idx;
  }

} // Main
