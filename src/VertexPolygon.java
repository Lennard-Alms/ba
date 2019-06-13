import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.text.*;
import java.util.Arrays;
import java.util.LinkedHashSet;

class VertexPolygon {
  protected ArrayList<Vertex> outline;
  protected String text = "";
  protected TextStrategy strategy;
  protected VertexList dlOutline;

  public VertexPolygon() {
    outline = new ArrayList<Vertex>();
    // dlOutline = new LinkedHashSet<Vertex>();
    dlOutline = new VertexList();
  }

  public VertexPolygon(String text) {
    outline = new ArrayList<Vertex>();
    dlOutline = new VertexList();
    this.text = text.toUpperCase();
  }

  public ArrayList<Vertex> getOutline() {
    return outline;
  }

  public VertexList getDlOutline() {
    return dlOutline;
  }

  public void addVertex(Vertex v) {
    if(!this.contains(v)){
      outline.add(v);
      dlOutline.add(v);
    }
  }

  public boolean contains(Vertex v) {
    return dlOutline.contains(v);
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text.toUpperCase();
  }

  /**
   * Gaußsche Trapezformel
   * @method getAreaSize
   * @return [description]
   */
  public double getAreaSize() {
    return Geometry.getAreaSizeOfPolygon(this);
  }

  public void colorizeVertecies(double r, double g, double b, double opacity) {
    for(Vertex v : outline){
      v.point.setFill(Color.color(r,g,b,opacity));
    }
  }

  public double[] getLargestRectangle(){
    double[] bounding = getBoundingBox();
    double leftest = bounding[0] + 0.01;
    double rightest = bounding[1] + 0.01;
    double highest = bounding[2] + 0.01;
    double lowest = bounding[3] + 0.01;
    double bestArea = 0;
    double[] biggestRectangle= new double[]{0,0,0,0};
    for(double x = leftest; x <= rightest; x += 5){
      for(double y = highest; y <= lowest; y += 5){
        if(!vertexInPolygon(new Vertex(x,y))) continue;
        for(double offset_x = 1; x + offset_x <= rightest; offset_x += 5){
          for(double offset_y= 1; y + offset_y <= lowest; offset_y += 5){
            //check nach schnitt mit poly
            //wenn kein schnitt und fläche größer als altes dann replace
            LineSegment top = new LineSegment(x, y, x + offset_x, y);
            LineSegment left = new LineSegment(x, y, x, y + offset_y);
            LineSegment right = new LineSegment(x + offset_x, y, x + offset_x, y + offset_y);
            LineSegment bot = new LineSegment(x, y + offset_y, x + offset_x, y + offset_y);
            boolean conflict = false;
            for(int i = 0; i < outline.size(); i++){
              LineSegment line = getLineSegment(i);
              conflict = line.intersects(top) || line.intersects(bot) || line.intersects(left) || line.intersects(right);
              if(conflict) break;
            }
            if(!conflict){
              double area = bot.getWidth() * left.getHeight();
              if(bestArea < area){
                bestArea = area;
                biggestRectangle = new double[]{x,x + offset_x,y,y + offset_y};
              }
            }
          }
        }
      }
    }
    return biggestRectangle;
  }

  public boolean vertexInPolygon(Vertex v){
    LineSegment verticalLine = new LineSegment(v, new Vertex(v.x, 0));
    int intersectionCount = 0;
    for(int i = 0; i < outline.size(); i++){
      LineSegment line = getLineSegment(i);
      if(line.intersects(verticalLine)) intersectionCount++;
    }
    if(intersectionCount % 2 == 0) return false;
    return true;
  }



  public double[] getBoundingBox() {
    double leftest = 1000;
    double rightest = 0;
    double highest = 1000;
    double lowest = 0;
    for(Vertex v : outline) {
      if(v.x < leftest) leftest = v.x;
      if(v.x > rightest) rightest = v.x;
      if(v.y < highest) highest = v.y;
      if(v.y > lowest) lowest = v.y;
    }
    return new double[]{leftest, rightest, highest, lowest};
  }



  public void setTextStrategy(TextStrategy strategy){
    this.strategy = strategy;
  }

  public TextStrategy getStrategy() {
    return strategy;
  }

  public void drawText(Pane textLayer){
    strategy.drawText(this, textLayer);
  }

  public LineSegment getLineSegment(int index) {
    if(index < outline.size()) {
      Vertex start;
      Vertex end;
      start = outline.get(index);
      if(index + 1 < outline.size()) {
        end = outline.get(index + 1);
      } else {
        end = outline.get(0);
      }
      return new LineSegment(start, end);
    }
    return null;
  }

  public ArrayList<LineSegment> getLineSegments() {
    ArrayList<LineSegment> segments= new ArrayList<LineSegment>();
    for(int i = 0; i < outline.size(); i++) {
      segments.add(getLineSegment(i));
    }
    return segments;
  }


  public Boolean canSee(Vertex v, Vertex w) {
    if(v.equals(w)) return false;
    LineSegment visionLine = new LineSegment(v,w);
    Vertex intersect = new Vertex(-1,-1);
    for(int i = 0; i < this.getOutline().size(); i++) {
      LineSegment edge = this.getLineSegment(i);
      if(visionLine.getLineIntersection(edge, intersect)) {
        if(!intersect.equals(v) && !intersect.equals(w)) {
          return false;
        }
      }
    }
    if(!this.vertexInPolygon(v.add(w.sub(v).mult(0.5)))) return false;
    return true;
  }

  public String toString(){
    return "txt:" + text + " outline:" + outline;
  }

  public Boolean isWithinBox(double minX, double minY, double maxX, double maxY) {
    for(int i = 0; i < outline.size(); i++) {
      Vertex v = outline.get(i);
      if(!(v.x >= minX && v.x <= maxX && v.y >= minY && v.y <= maxY))
        return false;
    }
    return true;
  }

  public Boolean isEmpty() {
    return dlOutline.size() == 0 ? true : false;
  }

  public Vertex getLastVertex() {
    return dlOutline.tail();
  }

}
