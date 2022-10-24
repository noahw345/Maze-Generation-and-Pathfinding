import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.*;

//represents the player cell
class Player {
  Vertex v;

  Player(Vertex v) {
    this.v = v;
  }

  // draws the player cell
  WorldImage drawPlayer() {
    return new RectangleImage(MazeWorld.CSIZE - 1, MazeWorld.CSIZE - 1, // makes room for walls
            OutlineMode.SOLID, Color.red).movePinhole((-1 / 2) * MazeWorld.CSIZE,
            (-1 / 2) * MazeWorld.CSIZE);
  }

  // ensures every keystore is a acceptable game move
  boolean goodKey(String k) {
    if (k.equals("right") && this.v.right != null) {
      return !this.v.top.goRight;
    }
    else if (k.equals("down") && this.v.bottom != null) {
      return !this.v.top.goBottom;
    }
    else if (k.equals("left") && this.v.left != null) {
      return !this.v.left.goRight;
    }
    else if (k.equals("up") && this.v.top != null) {
      return !this.v.left.goBottom;
    }
    else if (k.equals("d")) {
      return true;
    }
    else if (k.equals("b")) {
      return true;
    }
    return false;
  }
}

//represents a Vertex, the vertex in vedge
class Vertex {
  // neighboring Vertexes
  Vertex right;
  Vertex top;
  Vertex left;
  Vertex bottom;

  // positions
  int x;
  int y;

  // crossed Edges

  ArrayList<Edge> crossedEdges = new ArrayList<Edge>();

  // going right or below
  boolean goRight;
  boolean goBottom;

  // previous vertex
  Vertex prev;

  boolean crossed;

  Vertex(int x, int y) {
    this.x = x;
    this.y = y;
    this.left = null;
    this.right = null;
    this.top = null;
    this.bottom = null;
    this.goRight = true;
    this.goBottom = true;
    this.crossed = false;
    this.prev = null;
  }

  // these methods draw walls below and to the right
  WorldImage drawRight() {
    return new LineImage(new Posn(0, MazeWorld.CSIZE), Color.black)
            .movePinhole(-1 * MazeWorld.CSIZE, -1 * (1 / 2) * MazeWorld.CSIZE);
  }

  WorldImage drawBottom() {
    return new LineImage(new Posn(MazeWorld.CSIZE, 0), Color.black)
            .movePinhole(-1 * (1 / 2) * MazeWorld.CSIZE, -1 * MazeWorld.CSIZE);
  }

  // draws a rectangle given a pinhole position and color

  WorldImage drawRect(Color color) {
    return new RectangleImage(MazeWorld.CSIZE - 1, MazeWorld.CSIZE - 1,
            OutlineMode.SOLID, color) // to

            .movePinhole(-1 * MazeWorld.CSIZE / 2, (-1 * MazeWorld.CSIZE) / 2);
  }

  // identifies and mutates the previous cell
  void prevCell() {
    // Is the previous cell the top?
    if (this.top != null && this.top.prev == null && !this.top.goBottom) {
      this.prev = this.top;
    }
    // Is the previous cell the left?
    else if (this.left != null && this.left.prev == null && !this.left.goRight) {
      this.prev = this.left;
    }
    // Is the previous cell the bottom?
    else if (this.bottom != null && this.bottom.prev == null && !this.goBottom) {
      this.prev = this.bottom;
    }
    //// Is the previous cell the right cell?
    else if (this.right != null && this.right.prev == null && !this.goRight) {
      this.prev = this.right;
    }
  }
}

//the edge in vedge
class Edge {
  // previous and next vertexes
  Vertex from;
  Vertex to;

  int weight;

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}

//compares edges by weight
class CompareEdge implements Comparator<Edge> {

  // returns positive int if e1's weight is greater
  // returns negative in if e2's weight is greater
  // returns 0 if weights are equal
  @Override
  public int compare(Edge e1, Edge e2) {
    return e1.weight - e2.weight;
  }
}

//either a queue or a stack
interface ICollection<T> {
  // removes element to this queue
  public T remove();

  // adds element to this queue
  public void add(T el);

  //returns size of this queue
  public int size();

}

//for depth first search
class Stack<T> implements ICollection<T> {
  Deque<T> deq;

  Stack() {
    this.deq = new ArrayDeque<T>();
  }

  //removes element to this queue
  public T remove() {
    return this.deq.removeFirst();
  }

  // adds element to this queue
  public void add(T el) {
    this.deq.addFirst(el);
  }

  //returns size of this queue
  public int size() {
    return this.deq.size();
  }
}

class Queue<T> implements ICollection<T> {
  Deque<T> deq;

  Queue() {
    this.deq = new ArrayDeque<T>();
  }

  //removes element to this queue
  public T remove() {
    return this.deq.removeFirst();
  }

  // adds element to this queue
  public void add(T el) {
    this.deq.addLast(el);
  }

  //returns size of this queue
  public int size() {
    return this.deq.size();
  }
}

//represents a graph for dfs and bfs
class Graph {
  ArrayList<Vertex> vertices;

  Graph() {
  }

  // abstraction of DFS/BFS
  // uses a queue or stack and produces a list path of vertices
  ArrayList<Vertex> createPath(Vertex from, Vertex to, ICollection<Vertex> qos) {
    ArrayList<Vertex> resultPath = new ArrayList<Vertex>();

    qos.add(from);

    while (qos.size() > 0) {
      Vertex next = qos.remove();

      if (next.equals(to)) {
        return resultPath;
      }
      else if (resultPath.contains(next)) {
        //do nothing
      }
      else {
        for (Edge e : next.crossedEdges) {
          qos.add(e.from);
          qos.add(e.to);
          if (resultPath.contains(from)) {
            next.prev = e.from;
          }
          else if (resultPath.contains(e.to)) {
            next.prev = e.to;
          }
        }
        resultPath.add(next);
      }
    }
    return resultPath;
  }

  // produces list path using stack for DFS, rather than queue for BFS
  ArrayList<Vertex> getDFS(Vertex from, Vertex to) {
    return this.createPath(from, to, new Stack<Vertex>());
  }

  // produces list path using queue for BFS, rather than stack for DFS
  ArrayList<Vertex> getBFS(Vertex from, Vertex to) {
    return this.createPath(from, to, new Queue<Vertex>());
  }
}

//the world class, extends World
class MazeWorld extends World {
  // don't change... the size of each cell
  static final int CSIZE = 20;
  static final int Difficulty = 50;

  // finishin

  // number of cells in each row/column
  int bWidth;
  int bHeight;

  // hash of all vertexes
  HashMap<Vertex, Vertex> hash = new HashMap<Vertex, Vertex>();

  // list of edges
  ArrayList<Edge> loEdge = new ArrayList<Edge>();
  ArrayList<Edge> minSpanTree = new ArrayList<Edge>();

  // path of vertexes
  ArrayList<Vertex> vPath = new ArrayList<Vertex>();

  // finish cell
  Vertex goalCell;

  // to reduce memory, scene is stored as field of this class
  WorldScene scene = new WorldScene(0, 0);

  // 2d list of vertexes for board
  ArrayList<ArrayList<Vertex>> board;

  // Is the goalCell reached?
  boolean finito;

  // Player
  Player player;

  MazeWorld(int bWidth, int bHeight) {
    this.bWidth = bWidth;
    this.bHeight = bHeight;

    // instantiate board
    this.board = this.getGrid(bWidth, bHeight);
    this.getEdges(this.board);
    this.getHash(board);
    this.kruskalsAlgo();
    this.player = new Player(board.get(0).get(0));
    this.goalCell = this.board.get(bWidth - 1).get(bHeight - 1);
    this.makeWorld();
    this.finito = false;

    this.scene.placeImageXY(
            new RectangleImage(CSIZE * bWidth, CSIZE * bHeight, OutlineMode.OUTLINE, Color.black), 0,
            0);
  }

  // need to make in place of makeScene
  WorldScene makeWorld() {
    // first cell
    this.scene.placeImageXY(board.get(0).get(0).drawRect(Color.red), 1, 1);

    // finishing cell
    this.scene.placeImageXY(board.get(bHeight - 1).get(bWidth - 1).drawRect(Color.green),
            bWidth * CSIZE - CSIZE + 1, bHeight * CSIZE - CSIZE + 1); // leave room for walls

    // whole grid (nested loop)
    // row first nested loop
    for (int r = 0; r < bHeight; r++) {
      for (int c = 0; c < bWidth; c++) {
        this.changeBottom();
        this.changeRight();

        // has the current cell has been crossed?
        if (this.board.get(r).get(c).crossed) {
          // make the cell yellow....
          this.scene.placeImageXY(board.get(r).get(c).drawRect(Color.YELLOW), r * CSIZE, c * CSIZE);
        }
        // are we going right?
        if (board.get(r).get(c).goRight) {
          this.scene.placeImageXY(board.get(r).get(c).drawRight(), (c * CSIZE), (r * CSIZE));
        }

        // are we going down?
        if (board.get(r).get(c).goBottom) {
          this.scene.placeImageXY(board.get(c).get(r).drawBottom(), (c * CSIZE), (r * CSIZE));
        }
      }
    }

    // player cell
    this.scene.placeImageXY(player.drawPlayer(), CSIZE * player.v.x, CSIZE * player.v.x);

    return scene;
  }

  // runs every 1/28 of a second, or every tick
  @Override
  public WorldScene makeScene() {
    // if our path had more than one element, then current becomes previous and next
    // becomes current
    if (vPath.size() > 1) {
      this.solPath();
    }
    // then there is only one or zero vertices left
    else if (vPath.size() > 0) {
      this.solution();
    }

    return scene;
  }

  // makes a simple grid
  ArrayList<ArrayList<Vertex>> getGrid(int width, int height) {
    ArrayList<ArrayList<Vertex>> board = new ArrayList<ArrayList<Vertex>>();
    for (int row = 0; row < height; row++) {
      board.add(new ArrayList<Vertex>());
      ArrayList<Vertex> r = board.get(row);
      for (int col = 0; col < width; col++) {
        r.add(new Vertex(row, col));
      }
    }
    this.connectVertices(board);
    this.getEdges(board);
    this.getHash(board);
    return board;
  }

  //
  // EFFECT: mutates the right wall
  void changeRight() {
    for (Edge e : this.minSpanTree) {
      if (e.to.y == e.from.y) { // should the right wall be mutated
        e.from.goRight = false;
      }
    }
  }

  // EFFECT: mutates the bottom wall
  void changeBottom() {
    for (Edge e : this.minSpanTree) {
      if (e.to.x == e.from.x) { // should the bottom wall be mutated?
        e.from.goBottom = false;
      }
    }
  }

  // defines arraylist of edges with random weights
  ArrayList<Edge> getEdges(ArrayList<ArrayList<Vertex>> l) {
    Random rand = new Random();
    for (int i = 0; i < l.size(); i++) {
      for (int j = 0; j < l.get(i).size(); j++) {
        // nested loop to iterate through rows and columns of given 2d list
        if (j < l.get(i).size() - 1) {
          loEdge.add(
                  new Edge(l.get(i).get(j), l.get(i).get(j).right, rand.nextInt(MazeWorld.Difficulty)));
        }
        if (i < l.size() - 1) {
          loEdge.add(new Edge(l.get(i).get(j), l.get(i).get(j).bottom, (int) rand.nextInt(50)));
        }
      }
    }
    Collections.sort(loEdge, new CompareEdge());
    return loEdge;
  }

  // produces initial hashmap where every vertex is linked to itseld
  HashMap<Vertex, Vertex> getHash(ArrayList<ArrayList<Vertex>> vertex) {
    for (int r = 0; r < vertex.size(); r++) {
      for (int c = 0; c < vertex.get(r).size(); c++) {
        this.hash.put(vertex.get(r).get(c), vertex.get(r).get(c));
      }
    }
    return hash;
  }

  // connects each cell to its right, bottom, left, and right neighbors
  // EFFECT: defines neighbor cells using nested loop
  void connectVertices(ArrayList<ArrayList<Vertex>> board) {
    for (int row = 0; row < bHeight; row++) {
      for (int col = 0; col < bWidth; col++) {
        if (col + 1 < bWidth) { // is there a next colomn of vertices?
          board.get(row).get(col).right = board.get(row).get(col + 1);
          // then the cell to its rightis the cell with equal row but column + 1
        }
        if (col - 1 >= 0) { // is there a left colomn of vertices?
          board.get(row).get(col).left = board.get(row).get(col - 1);
          // then the cell to its leftis the cell with equal rowbut column - 1
        }
        if (row + 1 < bHeight) { // is there a lower row of vertices?
          board.get(row).get(col).bottom = board.get(row + 1).get(col);
          // then the cell to itsright is the cell withqual column but row + 1
        }
        if (row - 1 >= 0) { // is there a upper row of vertices?
          board.get(row).get(col).top = board.get(row - 1).get(col);
          // then the cell to its right is the cell with equal columb but row - 1
        }
      }
    }
  }

  // changes key value for hashmap
  void union(Vertex vert, Vertex newVert) {
    this.hash.put(find(vert), find(newVert));
  }

  //finds vertex of this node
  Vertex find(Vertex v) {
    if (v.equals(this.hash.get(v))) { // is the given vertex the correct node
      return v;
    }
    else {
      return find(this.hash.get(v)); // recursive call
    }
  }

  //creates minimum spanning tree
  ArrayList<Edge> kruskalsAlgo() {
    int i = 0;
    while (this.minSpanTree.size() < this.loEdge.size() && i < this.loEdge.size()) {
      Edge e = loEdge.get(i);
      if (this.find(this.find(e.from)).equals(this.find(this.find(e.to)))) {
        // if from and to edges r equal then do nothing
      }
      else {
        this.minSpanTree.add(e);
        this.union(this.find(e.from), this.find(e.to));
      }
      i++;
    }
    // add all crossed edges
    for (int r = 0; r < this.bHeight; r++) { // row first nested loop
      for (int c = 0; c < this.bWidth; c++) {
        for (Edge e : this.minSpanTree) {
          if (this.board.get(r).get(c).equals(e.from) || this.board.get(r).get(c).equals(e.to)) {
            this.board.get(r).get(c).crossedEdges.add(e);
          }
        }
      }
    }
    return minSpanTree;
  }

  // onKey method for up, down, left and right (manual controls)
  // d for depth first search
  // b for breadth first search
  public void onKeyEvent(String key) {
    if (key.equals("right") && player.goodKey("up")) {
      // to make sure any unwanted keys arent pressed
      player.v = player.v.right;
      player.v.crossed = true;
    }
    if (key.equals("right") && player.goodKey("right")) {
      // to make sure any unwanted keys arent pressed
      player.v = player.v.right;
      player.v.crossed = true;
    }
    if (key.equals("down") && player.goodKey("down")) {
      // to make sure any unwanted keys arent pressed
      player.v = player.v.bottom;
      player.v.crossed = true;
    }
    if (key.equals("left") && player.goodKey("left")) {
      // to make sure any unwanted keys arent pressed
      player.v = player.v.left;
      player.v.crossed = true;
    }
    if (key.equals("top") && player.goodKey("top")) {
      // to make sure any unwanted keys arent ressed
      player.v = player.v.top;
      player.v.crossed = true;
    }
    if (key.equals("d") && player.goodKey("d")) {
      // to make sure any unwanted keys arent pressed
      this.goalCell = this.board.get(this.bHeight - 1).get(this.bWidth - 1);
      this.vPath = new Graph().getDFS(this.board.get(0).get(0),
              this.board.get(this.bHeight - 1).get(this.bWidth - 1));
      // start a graph and call DFS with the first vertex and the last vertex as end
      // points
    }
    if (key.equals("b") && player.goodKey("b")) { // to make sure any unwanted keys arent pressed
      this.goalCell = this.board.get(this.bHeight - 1).get(this.bWidth - 1);
      this.vPath = new Graph().getBFS(this.board.get(0).get(0),
              this.board.get(this.bHeight - 1).get(this.bWidth - 1));
      // define a graph and call BFS with the first vertex and the last vertex as end
      // points
    }
    if (key.equals("d") && player.goodKey("d")) { // to make sure any unwanted keys arent pressed
      this.goalCell = this.board.get(this.bHeight - 1).get(this.bWidth - 1);
      this.vPath = new Graph().getDFS(this.board.get(0).get(0),
              this.board.get(this.bHeight - 1).get(this.bWidth - 1));
      // define a graph and call DFS with the first vertex and the last vertex as end
      // points
    }
    this.scene.placeImageXY(player.drawPlayer(), CSIZE * player.v.x, CSIZE * player.v.y);
    this.makeWorld();
  }


  @Override
  public void onTick() {
    // not necessary for our purposes but needed for World class
  }

  // draws solution path
  void solPath() {
    Vertex pathV = vPath.remove(0);
    this.scene.placeImageXY(pathV.drawRect(Color.blue), CSIZE * pathV.x, CSIZE * pathV.y);
  }

  // updates path with solution cells
  void solution() {
    Vertex pathV = vPath.remove(0);
    this.scene.placeImageXY(pathV.drawRect(Color.blue), CSIZE * pathV.x, CSIZE * pathV.y);
    if (!this.goalCell.left.goRight && this.goalCell.left.prev != null) {
      // Is the second to last cell to the right
      this.goalCell.prev = this.goalCell.left;
    }
    else if (!this.goalCell.top.goRight && this.goalCell.top.prev != null) {
      // Is the second to last cell to the top
      this.goalCell.prev = this.goalCell.top;
    }
    else {
      this.goalCell.prev = pathV;
    }
    this.finito = true; // we are done...
  }
}

class ExamplesMazeGame {

  public ExamplesMazeGame() {
  }

  MazeWorld game;

  Edge e1;
  Edge e2;
  Edge e3;

  Vertex v;
  Vertex vRight;
  Vertex vBottom;
  Vertex vLeft;
  Vertex vTop;
  Vertex prev;

  Player p;

  MazeWorld w;

  ArrayList<Vertex> crossedEdges;

  Stack<Integer> stack;
