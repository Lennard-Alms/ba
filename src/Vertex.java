import javafx.scene.shape.Circle;
import java.util.ArrayList;

public class Vertex {
    public double x;
    public double y;
    public Circle point;
    public Boolean dragged = false;
    ArrayList<Vertex> neighbours = new ArrayList<Vertex>();
    Vertex parent = null;
    double pathLengthEndingHere = 0.0;
    LineSegment connectedEdge = null;
    private String hash = "";

    boolean isRotated = false;

    public Vertex(double x, double y, Circle point) {
        this(x,y);
        this.point = point;
        hash = toString();
    }

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
        point = null;
    }

    public void addNeighbour(Vertex v){
      neighbours.add(v);
    }

    public void setPoint(Circle point) {
      this.point = point;
    }

    public boolean hasPoint(){
      if(this.point != null) return true;
      return false;
    }

    /**
     * Berechnet die Distanz zwischen 2 Vertices.
     * @method distance
     */
    public double distance(Vertex v) {
        return sub(v).mag();
    }

    /**
     * Subtrahiert den übergebenen Vertex von sich selbst.
     * @method sub
     */
    public Vertex sub(Vertex vertex) {
        return new Vertex(this.x - vertex.x, this.y - vertex.y);
    }

    /**
     * Addiert den übergebenen Vertex auf sich selbst.
     * @method add
     */
    public Vertex add(Vertex vertex) {
        return new Vertex(this.x + vertex.x, this.y + vertex.y);
    }

    /**
     * Multipliziert den Vertex mit einem Skalar
     * @method mult
     */
    public Vertex mult(double scalar) {
        return new Vertex(this.x * scalar, this.y * scalar);
    }

    /**
     * Berechnet die Länge des Vertex
     * @method mag
     */
    public double mag() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Berechnet das Skalarprodukt von 2 Vektoren
     * @method dot
     */
    public double dot(Vertex vertex) {
        return this.x * vertex.x + this.y * vertex.y;
    }

    /**
     * Berechnet das Kreuzprodukt von 2 Vektoren
     * @method cross
     */
    public double cross(Vertex vertex) {
        return this.y * vertex.x - this.x * vertex.y;
    }

    public boolean equals(Object v) {

      if (v instanceof Vertex){
          Vertex ptr = (Vertex) v;
          if(ptr.x == this.x && ptr.y == this.y) return true;
      }
      return false;
    }

    // @Override
    public int compareTo(Vertex o) {
      return Double.compare(x, o.x);
    }

    public String toString(){
      return "x:" + this.x + ",y:" + this.y;
    }

    public double getAngleInRadians() {
      return Math.atan2(y,x);
    }

    public void rotateCounterClockwise() {
      double x = this.x;
      double y = this.y;
      this.x = -y;
      this.y = x;
      this.x += 1000;
      isRotated = true;
    }

    public void rotateClockwise() {
      this.x -= 1000;
      double x = this.x;
      double y = this.y;
      this.x = y;
      this.y = -x;
      isRotated = true;
    }

    public int hashCode() {
      return hash.hashCode();
    }

}
