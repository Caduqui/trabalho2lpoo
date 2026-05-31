# Quadtree KNN - Trabalho LPOO

## Estrutura de arquivos

```
quadtree/
├── lpoo/
│   ├── math/
│   │   └── Real.java          – utilitário de comparação float
│   └── geom/
│       ├── Point2.java        – ponto 2D (agora não-final, extensível)
│       ├── Bounds2.java       – caixa delimitadora (com squaredDistance)
│       ├── PointFunc.java     – interface funcional de filtro/processamento
│       ├── KNN.java           – estrutura de k vizinhos mais próximos (A2)
│       ├── Particle2.java     – partícula com velocidade e cor RGB (A3)
│       └── Quadtree.java      – quadtree genérica + findNeighbors + forEachNeighbor (A1, A2)
├── Console.java               – utilitário de entrada texto
├── Main.java                  – aplicação de teste interativa (A4)
├── QuadtreeViewer.java        – visualizador com destaque de resultados (A5 bônus)
├── pontos.txt                 – exemplo de arquivo de pontos Point2
└── particulas.txt             – exemplo de arquivo de partículas Particle2
```

## Compilação (dentro do diretório quadtree/)

```bash
javac -cp . lpoo/math/Real.java \
             lpoo/geom/Point2.java \
             lpoo/geom/Bounds2.java \
             lpoo/geom/PointFunc.java \
             lpoo/geom/KNN.java \
             lpoo/geom/Particle2.java \
             lpoo/geom/Quadtree.java \
             Console.java \
             Main.java \
             QuadtreeViewer.java
```

Ou de uma vez (dependendo do SO):
```bash
# Linux/Mac
find . -name "*.java" | xargs javac -cp .

# Windows
dir /s /b *.java | xargs javac -cp .
```

## Execução

```bash
java -cp . Main
```

## Formato dos arquivos de entrada

### pontos.txt (Point2)
```
# comentário (linhas com # são ignoradas)
x y
```
Exemplo: `10.0 20.5`

### particulas.txt (Particle2)
```
x y vx vy r g b
```
Exemplo: `10.0 20.5 1.0 -0.5 255 0 0`  (partícula vermelha)

## Tarefas implementadas

| Tarefa | Descrição |
|--------|-----------|
| A1 | `Quadtree<P extends Point2>` – classe genérica |
| A2 | `KNN<P>` + `findNeighbors()` + `forEachNeighbor()` |
| A3 | `Particle2` estendida com cor RGB |
| A4 | `Main.java` interativo com leitura de arquivo/geração aleatória, filtros lambda por cor |
| A5 | `QuadtreeViewer` com destaque de resultados KNN e raio |

## Notas de implementação

- `Bounds2.squaredDistance(Point2 p)` calcula a distância ao quadrado do ponto à caixa,
  usada para poda eficiente da árvore sem `Math.sqrt` no loop principal.
- `KNN.add()` mantém os pares ordenados por distância crescente usando inserção direta.
- `findNeighbors()` visita os filhos do nó sempre em ordem crescente de distância,
  permitindo poda precoce quando o KNN já está cheio.
- `forEachNeighbor()` usa a mesma poda: ignora subárvores cujo nó mais próximo
  está além do raio.
- Filtros de cor usam expressões lambda: `p -> p.hasColor(cor)`.
