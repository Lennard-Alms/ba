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

  public ConcaveDecompositionStrategy(){
    System.out.println("ConcaveDecomposition Constructor");
  }


  public boolean drawing = false;
  public void drawText(VertexPolygon poly, Pane textLayer) {
    this.textLayer = textLayer;

    Random r = new Random();
    try {
      VertexList outline = poly.getDlOutline();
      double last = 0;

      Vertex v = outline.head();
      for(int i = 0; i < outline.size(); i++) {
        if(v.x == last) {
          v.x += r.nextDouble() * 0.05;
        }
        last = v.x;
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
      // drawTrapezoid(head);
      // drawTrapezoid(tail);

      // drawTrapezoids(head, null);
      trimFast(head, tail, minHeight);
      // trimConvex(head, head, minHeight);
      head = getHead(head, head, null);
      tail = getTail(tail, tail, null);
      // VertexPolygon _p = _trapezoidToPolygonMonotone(head);
      drawing = true;
      // lineBreak(poly.getDlOutline(), 4);
      List<BoundingBox> boxes = getRectrangles(head, poly.getText().length(), m);

      for(int i = 0; i < boxes.size(); i++) {
        placeLetterInBox(boxes.get(i), poly.getText().substring(i, i+1));
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
    return getHead(head, trap, prev, 0);
  }

  public VerticalTrapezoid getHead(VerticalTrapezoid head, VerticalTrapezoid trap, VerticalTrapezoid prev, int recursion) {
    if(!head.isActive() && !(trap instanceof VerticalTrapezoidFiller)) {
      head = trap;
    }

    if(recursion == 0) {
      if(head.isActive()) System.out.println("head active");
    }

    if(!(trap instanceof VerticalTrapezoidFiller) && trap.left.start.x < head.left.start.x) {
      head = trap;
    }
    for(VerticalTrapezoid t : trap.getNext()) {
      if(!t.equals(prev)) {
        VerticalTrapezoid tmp = getHead(head, t, trap, recursion + 1);
        if(!head.isActive()) head = tmp;
        if(tmp.left.start.x < head.left.start.x && t.isActive()) head = tmp;
      }
    }
    for(VerticalTrapezoid t : trap.getPrev()) {
      if(!t.equals(prev)) {
        VerticalTrapezoid tmp = getHead(head, t, trap, recursion + 1);
        if(!head.isActive()) head = tmp;
        if(tmp.left.start.x < head.left.start.x && t.isActive()) head = tmp;
      }
    }
    // if(recursion == 0) {
    //   drawTrapezoid(head);
    // }
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
      System.out.println(trap.informationRight);
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
        System.out.println("HALSKDJLAKSJDLKASJDLKAJSDLKJASLKDJ");
        arr[1].deactivate(1);
      } else {
        System.out.println("ABCDEFGHIJKLMNQO");
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

  // public VerticalTrapezoid trimConcaveDecomposition(VerticalTrapezoid head, VerticalTrapezoid trapezoid, double minHeight) {
  //   double heightLeft = trapezoid.left.height();
  //   double heightRight = trapezoid.right.height();
  //
  //   // if(!head.equals(trapezoid)) {
  //   //   return head;
  //   // }
  //
  //   if(heightLeft < heightRight) {
  //     //Sind auf linker Seite
  //     if(heightRight < minHeight) {
  //       trapezoid.getNextExplicit().removePrev(trapezoid);
  //       head = trapezoid.getNextExplicit();
  //       return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
  //     } else if(heightLeft < minHeight) {
  //       double slope = Math.abs(trapezoid.bot.slope() - trapezoid.top.slope());
  //       double b = trapezoid.left.height();
  //       double x = (minHeight - b) / slope;
  //       double offset = x + trapezoid.left.start.x;
  //       LineSegment line = new LineSegment(offset, 0, offset, 1000);
  //       Vertex top = new VirtualVertex(-1,-1);
  //       Vertex bot = new VirtualVertex(-1,-1);
  //       line.getLineIntersection(trapezoid.top, top);
  //       line.getLineIntersection(trapezoid.bot, bot);
  //       trapezoid.bot = new LineSegment(bot, trapezoid.bot.end);
  //       trapezoid.top = new LineSegment(top, trapezoid.top.end);
  //       trapezoid.left = new LineSegment(top, bot);
  //
  //       if(trapezoid.getNextExplicit() != null) {
  //         return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
  //       } else return head;
  //     } else {
  //       if(trapezoid.getNextExplicit() != null) {
  //         return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
  //       } else return head;
  //     }
  //
  //   } else if(heightLeft > heightRight) {
  //     //Sind auf rechten Seite
  //     if(heightLeft < minHeight) {
  //       VerticalTrapezoid prev = trapezoid.getPrevExplicit();
  //       if(prev != null)
  //         prev.removeNext(trapezoid);
  //       return head;
  //     } else if(heightRight < minHeight) {
  //       double slope = - Math.abs(trapezoid.bot.slope() - trapezoid.top.slope());
  //       double b = trapezoid.left.height();
  //       double x = (minHeight - b) / slope;
  //
  //       double offset = x + trapezoid.left.start.x;
  //       LineSegment line = new LineSegment(offset, 0, offset, 1000);
  //       Vertex top = new VirtualVertex(-1,-1);
  //       Vertex bot = new VirtualVertex(-1,-1);
  //       line.getLineIntersection(trapezoid.top, top);
  //       line.getLineIntersection(trapezoid.bot, bot);
  //       trapezoid.bot = new LineSegment(trapezoid.bot.start, bot);
  //       trapezoid.top = new LineSegment(trapezoid.top.start, top);
  //       trapezoid.right = new LineSegment(top, bot);
  //       trapezoid.removeNext(trapezoid.getNextExplicit());
  //       return head;
  //     } else {
  //       if(trapezoid.getNextExplicit() != null) {
  //         return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
  //       } else return head;
  //     }
  //   } else {
  //     if(trapezoid.getNextExplicit() != null) {
  //       return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
  //     } else return head;
  //   }
  // }

}
