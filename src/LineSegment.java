


class LineSegment {
  public Vertex start;
  public Vertex end;
  public double area = 0;


  public LineSegment(Vertex start, Vertex end) {
    this.start = start;
    this.end = end;
  }

  public LineSegment(double x1, double y1, double x2, double y2) {
    this(new Vertex(x1, y1), new Vertex(x2, y2));
  }

  public boolean intersects(LineSegment l) {
    return getLineIntersection(l, new Vertex(0, 0));
  }

  public boolean getLineIntersection(LineSegment l, Vertex i) {
    return getLineIntersection(this.start.x, this.start.y, this.end.x, this.end.y,
      l.start.x, l.start.y, l.end.x, l.end.y, i);
  }

  public double getHeight() {
    return Math.abs(end.y - start.y);
  }
  public double height() {
    return getHeight();
  }

  public double getWidth() {
    return Math.abs(end.x - start.x);
  }

  // vergleiche https://stackoverflow.com/questions/39819001/javascript-test-if-two-lines-are-overlapping-or-parallel abgerufen am 14.04.19

  public boolean getLineIntersection(double p0_x, double p0_y, double p1_x, double p1_y,
    double p2_x, double p2_y, double p3_x, double p3_y, Vertex i)
  {
      double s1_x, s1_y, s2_x, s2_y;
      s1_x = p1_x - p0_x;     s1_y = p1_y - p0_y;
      s2_x = p3_x - p2_x;     s2_y = p3_y - p2_y;

      double s, t;
      s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
      t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

      if (s > 0 && s < 1 && t > 0 && t < 1)
      {
          // Collision detected
          i.x = p0_x + (t * s1_x);
          i.y = p0_y + (t * s1_y);
          return true;
      }

      return false; // No collision
  }
  public String toString(){
    if(this.start == null || this.end == null) return "null";
    return "start:x:" + this.start.x + ",y:" + this.start.y + "end:x:" + this.end.x + ",y:" + this.end.y;
  }

  public double slope() {
    if(end.x == start.x) {
      if(start.y > end.y) return -9999999;
      else return 9999999;
    }
    return (start.y - end.y) / (start.x - end.x);
  }

  public double functionOffset() {
    return - slope() * start.x + start.y;
  }

  public boolean contains(Vertex v) {
    if(start.equals(v) || end.equals(v)) return true;
    return false;
  }
}
