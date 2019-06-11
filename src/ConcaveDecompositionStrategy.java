import java.util.List;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import javafx.scene.text.*;
import java.util.Collections;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.TreeSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Random;

class ConcaveDecompositionStrategy extends ConvexDecompositionStrategy {

  public double margin = 10;
  int rec = 0;
  public ConcaveDecompositionStrategy(){
    System.out.println("ConcaveDecomposition Constructor");
  }


  public boolean drawing = false;
  public void drawText(VertexPolygon poly, Pane textLayer) {
    m -= 0.01;
    System.out.println("m: " + m);
    this.textLayer = textLayer;

    Random r = new Random();
    try {
      VertexList outline = poly.getDlOutline();
      double last = 0;
      double lasty = 0;

      Vertex v = outline.head();
      for(int i = 0; i < outline.size() + 2; i++) {
        if(v.x == last) {
          System.out.println("schieb");
          v.x += r.nextDouble() * 0.05;
        }
        if(v.y == lasty) {
          System.out.println("schieb");
          v.y += r.nextDouble() * 0.05;
        }
        last = v.x;
        lasty = v.y;
        v = outline.getNext(v);
      }
      Vertex[] orderedVertices = sort(outline);

      List<VerticalTrapezoid> trapezoids = getTrapezoidalDecomposition(outline, orderedVertices);
      double weightedAverageHeight = getWeightedAverageHeight(trapezoids);
      double minHeight = weightedAverageHeight * 0.5;
      VerticalTrapezoid head = trapezoids.get(0);
      VerticalTrapezoid tail = getTail(head, head, null);

      double m = 0;
      information(head, 1);
      information(head, -1);
      ditchInformation(head, null);
      head = getHead(head, head, null);
      tail = getTail(tail, head, null);

      if(rec == -1)
        drawTrapezoids(head, null);
      // trimFast(head, tail, minHeight);
      // trimConvex(head, head, minHeight);
      head = getHead(head, head, null);
      tail = getTail(tail, tail, null);
      // VertexPolygon _p = _trapezoidToPolygonMonotone(head);
      drawing = true;
      List<LineSegment> breakingLines = lineBreak(poly.getDlOutline(), 1);

      VertexPolygon[] polygons = null;
      polygons = cutPolygon(poly, breakingLines.get(0));
      if(rec == 0) {
        for(int i = 0; i < 2; i++) {
          VertexList _outline = polygons[i].getDlOutline();
          Vertex[] _orderedVertices = sort(_outline);
          List<VerticalTrapezoid> _trapezoids = getTrapezoidalDecomposition(_outline, _orderedVertices);
          // drawTrapezoids(_trapezoids.get(0), null);
          polygons[i].setText("Hallo");
          polygons[i].setTextStrategy(this);
          rec = -1;
          polygons[i].drawText(textLayer);
          rec = 0;
        }

      }

      List<BoundingBox> boxes = getRectrangles(head, poly.getText().length(), margin);

      if(rec == -1) {
        for(int i = 0; i < boxes.size(); i++) {
          placeLetterInBox(boxes.get(i), poly.getText().substring(i, i+1));
        }

      }
    } catch (Exception e){
      System.out.println("---Exception---");
      e.printStackTrace(new java.io.PrintStream(System.out));
    }
  }

  public VerticalTrapezoid getTail(VerticalTrapezoid tail, VerticalTrapezoid trap, VerticalTrapezoid prev) {
    if(!tail.isActive() && !(trap instanceof VerticalTrapezoidFiller)) tail = trap;
    if(!(trap instanceof VerticalTrapezoidFiller) && trap.right.start.x > tail.right.start.x) {
      tail = trap;
    }
    for(VerticalTrapezoid t : trap.getNext()) {
      if(!t.equals(prev)) {
        VerticalTrapezoid tmp = getTail(tail, t, trap);
        if(!tail.isActive()) tail = tmp;
        if(tmp.right.start.x > tail.right.start.x && t.isActive()) tail = tmp;
      }
    }
    for(VerticalTrapezoid t : trap.getPrev()) {
      if(!t.equals(prev)) {
        VerticalTrapezoid tmp = getTail(tail, t, trap);
        if(!tail.isActive()) tail = tmp;
        if(tmp.right.start.x > tail.right.start.x && t.isActive()) tail = tmp;
      }
    }
    return tail;
  }

  public VerticalTrapezoid getHead(VerticalTrapezoid head, VerticalTrapezoid trap, VerticalTrapezoid prev) {
    if(!head.isActive() && !(trap instanceof VerticalTrapezoidFiller)) {
      head = trap;
    }

    if(!(trap instanceof VerticalTrapezoidFiller) && trap.left.start.x < head.left.start.x) {
      head = trap;
    }
    for(VerticalTrapezoid t : trap.getNext()) {
      if(!t.equals(prev)) {
        VerticalTrapezoid tmp = getHead(head, t, trap);
        if(!head.isActive()) head = tmp;
        if(tmp.left.start.x < head.left.start.x && t.isActive()) head = tmp;
      }
    }
    for(VerticalTrapezoid t : trap.getPrev()) {
      if(!t.equals(prev)) {
        VerticalTrapezoid tmp = getHead(head, t, trap);
        if(!head.isActive()) head = tmp;
        if(tmp.left.start.x < head.left.start.x && t.isActive()) head = tmp;
      }
    }
    return head;
  }

  public double information(VerticalTrapezoid trap, int direction) {
    return information(trap, direction, null);
  }

  public double information(VerticalTrapezoid trap, int direction, VerticalTrapezoid previous) {
    if(trap == null) return 0;

    //Laufe von links nach rechts
    if(direction == 1) {
      if(trap.informationLeft != 0) return trap.informationLeft;

      for(VerticalTrapezoid t : trap.getPrev()) {
        trap.informationLeft = Math.max(information(t, direction, trap), t.informationLeft);
      }
      trap.informationLeft += trap.area();
      for(VerticalTrapezoid t : trap.getNext()) {
        if(!t.equals(previous))
          information(t, direction, trap);
      }
      return trap.informationLeft;
    } else {
      if(trap.informationRight != 0) return trap.informationRight;

      for(VerticalTrapezoid t : trap.getNext()) {
        trap.informationRight = Math.max(information(t, direction, trap), t.informationRight);
      }
      trap.informationRight += trap.area();
      for(VerticalTrapezoid t : trap.getPrev()) {
        if(!t.equals(previous))
          information(t, direction, trap);
      }
      return trap.informationRight;
    }

  }

  public void ditchInformation(VerticalTrapezoid trap, VerticalTrapezoid prev) {
    HashSet<VerticalTrapezoid> _next = trap.getNext();
    HashSet<VerticalTrapezoid> _prev = trap.getPrev();
    if(_next.size() == 2) {
      VerticalTrapezoid[] arr = _next.toArray(new VerticalTrapezoid[2]);
      System.out.println(arr[0].informationRight);
      if(arr[0].informationRight > arr[1].informationRight) {
        arr[1].deactivate(1);
      } else {
        arr[0].deactivate(1);
      }
    }
    if(_prev.size() == 2) {
      VerticalTrapezoid[] arr = _prev.toArray(new VerticalTrapezoid[2]);
      if(arr[0].informationLeft > arr[1].informationLeft) {
        arr[1].deactivate(-1);
      } else {
        arr[0].deactivate(-1);
      }
    }
    for(VerticalTrapezoid t : trap.getNext()) {
      if(!t.equals(prev)) {
        ditchInformation(t, trap);
      }
    }
    for(VerticalTrapezoid t : trap.getPrev()) {
      if(!t.equals(prev)) {
        ditchInformation(t, trap);
      }
    }
  }

  public void cutTrapezoids(VerticalTrapezoid head, List<LineSegment> lines) {

  }

  public VertexPolygon[] cutPolygon(VertexPolygon polygon, LineSegment line) {
    VertexList outline = polygon.getDlOutline();
    VertexList newOutline = new VertexList();

    //Kopieren
    Vertex current = outline.head();
    for(int i = 0; i < outline.size(); i++) {
      newOutline.add(current);
      current = outline.getNext(current);
    }
    outline = newOutline;

    System.out.println(outline);

    Vertex[] intersections = getFurthestIntersection(polygon, line);
    System.out.println(Arrays.toString(intersections));

    current = outline.head();
    for(int i = 0; i < outline.size(); i++) {
      Vertex next = outline.getNext(current);
      LineSegment edge = new LineSegment(current, next);
      Vertex intersection = new VirtualVertex(-1,-1);
      if(edge.getLineIntersection(line, intersection)) {
        System.out.println(intersection);
        if(intersection.equals(intersections[0]) || intersection.equals(intersections[1])) {
          outline.insertAfter(current, intersection);
        }
      }
      current = next;
    }
    System.out.println(outline);

    current = outline.head();
    int direction = 0;
    VertexPolygon upperPolygon = new VertexPolygon();
    VertexPolygon lowerPolygon = new VertexPolygon();
    VertexPolygon tmp = new VertexPolygon();

    for(int i = 0; i < outline.size(); i++) {
      Vertex next = outline.getNext(current);
      LineSegment edge = new LineSegment(current, next);
      if(current instanceof VirtualVertex) {
        System.out.println("Virtual");
        if(direction == 0) {
          if(next.y < line.start.y) {
            direction = 1;
            lowerPolygon = tmp;
            lowerPolygon.addVertex(current);
          }
          else{
            direction = -1;
            upperPolygon = tmp;
            upperPolygon.addVertex(current);
          }
        } else {
          if(next.y < line.start.y) {
            direction = 1;
            lowerPolygon.addVertex(current);
          }
          else{
            direction = -1;
            upperPolygon.addVertex(current);
          }
        }
      }

      if(direction == 1) {
        upperPolygon.addVertex(current);
      }
      if(direction == -1) {
        lowerPolygon.addVertex(current);
      }
      if(direction == 0) {
        tmp.addVertex(current);
      }
      current = next;
    }
    return new VertexPolygon[] {upperPolygon, lowerPolygon};
  }

  public Vertex[] getFurthestIntersection(VertexPolygon polygon, LineSegment line) {
    List<Vertex> intersections = new ArrayList();
    VertexList outline = polygon.getDlOutline();

    Vertex current = outline.head();
    for(int i = 0; i < outline.size(); i++) {
      Vertex next = outline.getNext(current);
      LineSegment edge = new LineSegment(current, next);
      Vertex intersection = new VirtualVertex(-1,-1);
      if(edge.getLineIntersection(line, intersection)) {
        intersections.add(intersection);
      }
      current = next;
    }
    Object[] oIntersections = intersections.toArray();
    Vertex[] aIntersections = new Vertex[oIntersections.length];

    for(int i = 0; i < oIntersections.length; i++) {
      aIntersections[i] = (Vertex) oIntersections[i];
    }

    Arrays.sort(aIntersections, new VertexXComparator());

    int furthestId = -1;
    double biggest = 0;
    for(int i = 0; i < aIntersections.length; i+= 2) {
      if(Math.abs(aIntersections[i].x - aIntersections[i+1].x) > biggest) {
        furthestId = i;
        biggest = Math.abs(aIntersections[i].x - aIntersections[i+1].x);
      }
    }
    return new Vertex[] {aIntersections[furthestId], aIntersections[furthestId+1]};
  }

}
