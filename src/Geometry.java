import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;

import javafx.scene.layout.Pane;
import javafx.scene.text.*;


class Geometry {






  public static double getAreaSizeOfPolygon(VertexPolygon p) {
    ArrayList<Vertex> outline = p.getOutline();
    return getAreaSizeOfPolygon(outline);
  }

  public static double getAreaSizeOfPolygon(ArrayList<Vertex> outline) {
    double area = 0;
    Vertex v;
    Vertex w;

    for(int i = 0; i < outline.size() - 1; i++) {
      v = outline.get(i);
      w = outline.get(i + 1);
      area += (v.x * w.y - v.y * w.x);
    }

    Vertex first = outline.get(0);
    Vertex last = outline.get(outline.size() - 1);
    area += last.x * first.y - last.y * first.x;
    area /= 2;
    return Math.abs(area);
  }



  public static VertexPolygon[] splitPolygon(VertexPolygon poly, Vertex v, LineSegment vEdge, Vertex w, LineSegment wEdge) {


    VertexPolygon upper = new VertexPolygon();
    VertexPolygon lower = new VertexPolygon();
    Boolean lowerArc = true;
    for(Vertex u : poly.getOutline()) {
      if(!u.equals(vEdge.start) && !u.equals(wEdge.start)) {
        if(lowerArc) lower.addVertex(u);
        if(!lowerArc) upper.addVertex(u);
      } else if(u.equals(vEdge.start)) {
        if(lowerArc) {
          lower.addVertex(u);
          lower.addVertex(v);
          upper.addVertex(v);
          lowerArc = false;
        } else {
          upper.addVertex(u);
          upper.addVertex(v);
          lower.addVertex(v);
          lowerArc = true;
        }
      } else if(u.equals(wEdge.start)) {
        if(lowerArc) {
          lower.addVertex(u);
          lower.addVertex(w);
          upper.addVertex(w);
          lowerArc = false;
        } else {
          upper.addVertex(u);
          upper.addVertex(w);
          lower.addVertex(w);
          lowerArc = true;
        }
      }
    }
    eliminateDuplicates(upper);
    eliminateDuplicates(lower);

    /*
    We now need to check if the upper and lower polygons are named correctly.
    We are assuming that the bottleneck line from v to w goes from left to right (v.y < w.y).
    Also we are assuming that v is not equal to either vEdge.start or vEdge.end.
    We know that the upper polygon is left of the vector from v to w.
    So we can determin which point of vEdge belongs to the upper polygon
    by looking at the angle between v->w and v->vEdge.(start / end).
    Is the angle smaller than pi the point belongs to the upper polygon.
    This does not work in all cases. Wide rectangles with a downward facing splitline
    will be categorized wrong. In these cases the splitting angle needs to be smaller or something.
    Not sure how to fix this.
     */

    Vertex vTOw = w.sub(v);
    Vertex vTOvEdgeStart = vEdge.start.sub(v);

    double angle = Math.atan2(vTOvEdgeStart.y, vTOvEdgeStart.x) - Math.atan2(vTOw.y, vTOw.x);
    if(angle < 0) angle += 2 * Math.PI;

    if(angle > Math.PI) {
      if(upper.contains(vEdge.start)) {
        return new VertexPolygon[]{upper, lower};
      } else {
        return new VertexPolygon[]{lower, upper};
      }
    } else {
      if(upper.contains(vEdge.start)) {
        return new VertexPolygon[]{lower, upper};
      } else {
        return new VertexPolygon[]{upper, lower};
      }
    }

  }


  public static void eliminateDuplicates(VertexPolygon poly) {
    HashSet<Vertex> checklist = new HashSet<Vertex>();
    ArrayList<Vertex> newOutline = new ArrayList<Vertex>();
    for(Vertex v : poly.getOutline()) {
      if(!checklist.contains(v)) {
        newOutline.add(v);
        checklist.add(v);
      }
    }
    poly.outline = newOutline;
  }


  public static Boolean canSee(VertexPolygon poly, Vertex v, Vertex w) {
    if(v.equals(w)) return false;
    LineSegment visionLine = new LineSegment(v,w);
    Vertex intersect = new Vertex(-1,-1);
    for(int i = 0; i < poly.getOutline().size(); i++) {
      LineSegment edge = poly.getLineSegment(i);
      if(visionLine.getLineIntersection(edge, intersect)) {
        if(!intersect.equals(v) && !intersect.equals(w)) {
          return false;
        }
      }
    }
    if(!poly.vertexInPolygon(v.add(w.sub(v).mult(0.5)))) return false;
    return true;
  }



  public static Vertex[] getFurthestPointsInPolygon(VertexPolygon polygon) {
    Vertex x = null;
    Vertex y = null;
    double farthestDistance = 0;
    ArrayList<Vertex> outline = polygon.getOutline();
    for(int i = 0; i < outline.size(); i++) {
      for(int j = 0; j < outline.size(); j++) {
        double distance = outline.get(i).distance(outline.get(j));
        if(distance > farthestDistance) {
          farthestDistance = distance;
          x = outline.get(i);
          y = outline.get(j);
        }
      }
    }
    return new Vertex[]{x, y};
  }


}
