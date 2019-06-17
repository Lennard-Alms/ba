import java.util.List;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import javafx.scene.text.*;
import java.util.Collections;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.geometry.Bounds;

import java.util.TreeSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Random;

class ConvexDecompositionStrategy implements TextStrategy{
  double fontsize;
  Pane textLayer;
  boolean drawing = false;
  int counter = 0;
  int counter2 = 0;
  double m = 1;


  public ConvexDecompositionStrategy(){
  }

  // public List<LineSegment> getVerticalIntersections(VertexPolygon polygon, double x) {
  //
  // }

  public void drawText(VertexPolygon poly, Pane textLayer) {
    this.textLayer = textLayer;
    this.m = GlobalOptions.getMargin();
    Random r = new Random();
    try {
      VertexList outline = poly.getDlOutline();
      Vertex[] orderedVertices = sort(outline);
      List<VerticalTrapezoid> trapezoids = getTrapezoidalDecomposition(outline, orderedVertices);
      double weightedAverageHeight = getWeightedAverageHeight(trapezoids);
      double minHeight = weightedAverageHeight * 0.5;
      VerticalTrapezoid head = trapezoids.get(0);
      head = trimConvex(head, head, minHeight);
      // VertexPolygon _p = trapezoidsToPolygon(head);
      // lineBreak(_p.getDlOutline(), 2);

      List<BoundingBox> boxes = getRectrangles(head, poly.getText().length(), m);

      drawTrapezoids(head, null);
    } catch (Exception e){
      e.printStackTrace(new java.io.PrintStream(System.out));
    }
  }

  public Vertex[] sort(VertexList hashset) {
    Vertex[] v = (Vertex[])hashset.toArray();
    Arrays.sort(v, new VertexXComparator());
    return v;
  }

  public void placeLetterInBox(BoundingBox box, String letter) {
    box.top += 2;
    box.bot -= 2;
    Font monospacedFont;
    Text t = new Text();
    // monospacedFont = Font.font("Monospaced", FontWeight.NORMAL, box.right - box.left);
    // if(monospacedFont.getFamily() != "Monospaced") {
    //   monospacedFont = Font.font("Courier New", FontWeight.NORMAL, box.right - box.left);
    // }
    //monospacedFont = new Font("Cousine Bold", fontsize);

    monospacedFont = Font.loadFont("file:./ttf/Cousine-Bold.ttf", box.right - box.left);
    double baseline = box.bot;
    t.setFont(monospacedFont);
    t.setText(letter);
    Bounds b = t.getLayoutBounds();
    double boundingBot = b.getMaxY();
    double boundingTop = b.getMinY();
    double ascent = Math.abs(boundingTop);
    double descent = Math.abs(boundingBot);
    double middle = Math.abs(boundingTop + boundingBot) / 2;
    double boundingRight = b.getMaxX();
    double boundingLeft = b.getMinX();
    t.setY(baseline);

    t.setX(box.left);

    double requiredHeight = Math.abs(box.top - box.bot);
    double requiredWidth = Math.abs(box.left - box.right);
    // double actualHeight = Math.abs(Math.abs(boundingTop) - Math.abs(boundingBot));
    double actualHeight = ascent * 0.8;
    double actualWidth = Math.abs(boundingRight - boundingLeft);
    t.setScaleX(requiredWidth / actualWidth);


    double scale = (requiredHeight / (actualHeight));
    // scale = 1;
    t.setScaleY(scale);
    t.setTranslateY( - middle * (scale - 1));
    t.setTranslateX(((requiredWidth / actualWidth) - 1) * (actualWidth / 2));
    textLayer.getChildren().add(t);
  }

  /**
   * get Most Left Point, get most Right point, rectwidth = (right - left) / count
   * Sweep über Trapeze maxTop und minBot speichern
   *
   * @method getRectrangles
   * @param  head           [description]
   * @param  count          [description]
   */
  public List<BoundingBox> getRectrangles(VerticalTrapezoid head, int count, double margin) {
    double mostLeftPoint = head.left.start.x;
    double mostRightPoint;
    VerticalTrapezoid rightest = head;
    while(rightest.hasNextExplicit()) {
      rightest = rightest.getNextExplicit();
    }
    mostRightPoint = rightest.right.start.x;
    double width = (mostRightPoint - mostLeftPoint) / count;

    VerticalTrapezoid current = head;



    List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();

    for(int rectCounter = 0; rectCounter < count; rectCounter++) {
      double x1 = mostLeftPoint + width * (rectCounter);
      double x2 = mostLeftPoint + width * (rectCounter + 1);
      double x1_m = x1 + (width / 100.0 * margin);
      double x2_m = x2 - (width / 100.0 * margin);
      double top = getTopInInterval(current, x1_m, x2_m) + GlobalOptions.getMarginBT();
      double bot = (getBotInInterval(current, x1_m, x2_m) - GlobalOptions.getMarginBT());
      // current = getTrapezoidAtPosition(current, x2);
      boundingBoxes.add(new BoundingBox(top, x2_m, bot, x1_m));
    }

    // for(BoundingBox b : boundingBoxes) {
    //   // drawRectangle(b);
    // }

    return boundingBoxes;
  }

  public void drawRectangle(BoundingBox b) {
    Rectangle r = new Rectangle();
    r.setX(b.left);
    r.setY(b.top);
    r.setWidth(b.right - b.left);
    r.setHeight(b.bot - b.top);
    // r.setArcWidth(20);
    // r.setArcHeight(20);
    textLayer.getChildren().add(r);
  }

  public VerticalTrapezoid getTrapezoidAtPosition(VerticalTrapezoid t, double x) {
    while(t.right.start.x < x) {
      t = t.getNextExplicit();
    }
    return t;
  }

  public double getTopInInterval(VerticalTrapezoid t, double x1, double x2) {
    t = getTrapezoidAtPosition(t, x1);
    double top = getTopAtPosition(t, x1);
    double lastX = x1;
    double lastT = top;
    double gesamt = x2 - x1;
    if(GlobalOptions.getAverage())
        top = 0;
    while(t.right.start.x < x2) {
      if(GlobalOptions.getAverage()) {
          double width = t.right.start.x - lastX;
          top += width * (t.right.start.y + lastT) / 2;
          lastX = t.right.start.x;
          lastT = t.right.start.y;
      }
      else {
          top = Math.max(top, t.right.start.y);
      }
      t = t.getNextExplicit();
    }
    if(GlobalOptions.getAverage()) {
        double tNew = getTopAtPosition(t, x2);
        top += (tNew + lastT) / 2 * (x2 - lastX);
        return top / gesamt;
    }
    return Math.max(top, getTopAtPosition(t, x2));
  }

  public double getTopAtPosition(VerticalTrapezoid t, double x) {
    if(t.left.start.x == x) return t.top.start.y;
    if(t.right.start.x == x) return t.top.end.y;
    Vertex v = new Vertex(0,0);
    t.top.getLineIntersection(new LineSegment(x,0,x,1000), v);
    return v.y;
  }

  public double getBotInInterval(VerticalTrapezoid t, double x1, double x2) {
    t = getTrapezoidAtPosition(t, x1);
    double bot = getBotAtPosition(t, x1);
    double lastX = x1;
    double lastB = bot;
    double gesamt = x2 - x1;
    double gesamttmp = 0;
    if(GlobalOptions.getAverage())
        bot = 0;
    while(t.right.end.x < x2) {
        if(GlobalOptions.getAverage()) {
            double width = Math.abs(t.right.end.x - lastX);
            gesamttmp += width;
            bot += width * (t.right.end.y + lastB) / 2;
            lastX = t.right.end.x;
            lastB = t.right.end.y;
        } else {
            bot = Math.min(bot, t.right.end.y);
        }

      t = t.getNextExplicit();
    }
    // System.out.println(gesamttmp + " : " + gesamt);
    if(GlobalOptions.getAverage()) {
        double tNew = getBotAtPosition(t, x2);
        bot += (tNew + lastB) / 2 * (x2 - lastX);
        return bot / gesamt;
    }
    return Math.min(bot, getBotAtPosition(t, x2));
  }

  public double getBotAtPosition(VerticalTrapezoid t, double x) {
    if(t.left.end.x == x) return t.left.end.y;
    if(t.right.end.x == x) return t.right.end.y;
    Vertex v = new Vertex(0,0);
    t.bot.getLineIntersection(new LineSegment(x,0,x,1000), v);
    return v.y;
  }


  public List<VerticalTrapezoid> getTrapezoidalDecomposition(VertexList outline, Vertex[] orderedVertices) {

    int size = orderedVertices.length;
    //Active Trapezoid Tree
    TreeSet<ProxyVerticalTrapezoidVertex> att = new TreeSet<ProxyVerticalTrapezoidVertex>(new VerticalTrapezoidVertexComparator());
    VerticalTrapezoid tHead = null;
    List<VerticalTrapezoid> trapezoidList= new LinkedList<VerticalTrapezoid>();
    HashSet<Vertex> vertexSet = new HashSet<Vertex>();


    for(int i = 0; i < size; i++) {
      Vertex v = orderedVertices[i];
      List<VerticalTrapezoid> leftSideTrapezoids = new ArrayList<VerticalTrapezoid>();
      List<VerticalTrapezoid> rightSideTrapezoids = new ArrayList<VerticalTrapezoid>();
      ProxyVerticalTrapezoidVertex pv = new ProxyVerticalTrapezoidVertex(v);

      //Wenn der Vertex in einem Trapez liegt
      if(att.contains(pv) && !vertexSet.contains(v)) {
        ProxyVerticalTrapezoidVertex pt = att.floor(pv);
        VerticalTrapezoid trapezoid = pt.toTrapezoid();
        leftSideTrapezoids.add(trapezoid);

        if(vertexIsLeftTrap(outline, v) && hasIntersection(trapezoid, v)) {
          ProxyVerticalTrapezoidVertex pt1 = att.higher(pt);
          ProxyVerticalTrapezoidVertex pt2 = att.lower(pt);
          if(pt1 == null) {
            leftSideTrapezoids.add(pt2.toTrapezoid());
          } else if(pt2 == null) {
            leftSideTrapezoids.add(pt1.toTrapezoid());
          } else if(pt1.toTrapezoid().contains(v)) {
            leftSideTrapezoids.add(pt1.toTrapezoid());
          } else if(pt2.toTrapezoid().contains(v)) {
            leftSideTrapezoids.add(pt2.toTrapezoid());
          }
        }
        rightSideTrapezoids = createTrapezoids(leftSideTrapezoids, v, outline);
      }
      // else if(vertexIsLeftTrap(outline, v) || vertexIsRightTrap(outline,v)) {
      //   rightSideTrapezoids = createTrapezoids(leftSideTrapezoids, v, outline);
      //
      // }
      else if(!vertexSet.contains(v)) {
        rightSideTrapezoids = createTrapezoids(leftSideTrapezoids, v, outline);
      }

      if(tHead == null) tHead = rightSideTrapezoids.get(0);

      for(VerticalTrapezoid trapezoid : leftSideTrapezoids) {
        att.remove(new ProxyVerticalTrapezoidVertex(trapezoid));
      }
      for(VerticalTrapezoid trapezoid : rightSideTrapezoids) {
        if(!(trapezoid instanceof VerticalTrapezoidFiller)) {
          att.add(new ProxyVerticalTrapezoidVertex(trapezoid));
        }
        vertexSet.add(trapezoid.left.start);
        vertexSet.add(trapezoid.left.end);
        //Sortiere nach Anfang des Polygons
        trapezoidList.add(trapezoid);
      }
    }
    // drawTrapezoids(tHead, null);
    return trapezoidList;
  }

  public void drawTrapezoids(VerticalTrapezoid t, VerticalTrapezoid previous) {
    drawTrapezoid(t);
    for(VerticalTrapezoid _t : t.getNext()) {
      if(!_t.equals(previous))
        drawTrapezoids(_t, t);
    }
    for(VerticalTrapezoid _t : t.getPrev()) {
      if(!_t.equals(previous))
        drawTrapezoids(_t, t);
    }
  }

  public void drawTrapezoid(VerticalTrapezoid t) {
    double x;

    Line l1;
    Line l2;
    Line l3;
    Line l4;
    Random r = new Random();
    Color c;
    if(t.isActive()) c = Color.color(0,1,0,1);
    else c = Color.color(1,0,0,1);
    double r1 = r.nextDouble();
    double r2 = r.nextDouble();
    double r3 = r.nextDouble();
    if(t.left != null) {
      l1 = new Line(t.left.start.x, t.left.start.y, t.left.end.x, t.left.end.y);
      l1.setStroke(c);
      textLayer.getChildren().add(l1);
    }
    if(t.right != null) {
      l2 = new Line(t.right.start.x, t.right.start.y, t.right.end.x, t.right.end.y);
      l2.setStroke(c);
      textLayer.getChildren().add(l2);
    }
    if(t.top != null) {
      l3 = new Line(t.top.start.x, t.top.start.y, t.top.end.x, t.top.end.y);
      l3.setStroke(c);
      textLayer.getChildren().add(l3);
    }
    if(t.bot != null) {
      l4 = new Line(t.bot.start.x, t.bot.start.y, t.bot.end.x, t.bot.end.y);
      l4.setStroke(c);
      textLayer.getChildren().add(l4);
    }

  }

  public void drawLineSegment(LineSegment l) {
    Line l1 = new Line(l.start.x, l.start.y, l.end.x, l.end.y);
    l1.setStroke(Color.color(1,0,0,0.3));
    textLayer.getChildren().add(l1);
  }

  /**
   * TODO Die Überprüfung kann theoretisch auch ein Start für einen neuen Punkt sein.
   * @method VertexIsRightTrap
   * @param  outline           [description]
   * @param  v                 [description]
   * @return                   [description]
   */
  public boolean vertexIsRightTrap(VertexList outline, Vertex v) {
    Vertex next = outline.getNext(v);
    Vertex prev = outline.getPrev(v);
    if(next.x > v.x && prev.x > v.x) return true;

    return false;
  }

  public boolean vertexIsLeftTrap(VertexList outline, Vertex v) {
    Vertex next = outline.getNext(v);
    Vertex prev = outline.getPrev(v);
    if(next.x < v.x && prev.x < v.x) return true;

    return false;
  }

  public Vertex[] intersection(VerticalTrapezoid trapezoid, Vertex v) {
    LineSegment vertical = new LineSegment(v.x, 0, v.x, 1000);
    Vertex intersectionTop = new VirtualVertex(-1, -1);
    Vertex intersectionBot = new VirtualVertex(-1, -1);
    vertical.getLineIntersection(trapezoid.top, intersectionTop);
    vertical.getLineIntersection(trapezoid.bot, intersectionBot);

    return new Vertex[] {intersectionTop, intersectionBot};
  }

  public boolean hasIntersection(VerticalTrapezoid trapezoid, Vertex v) {
    Vertex[] intersections = intersection(trapezoid, v);
    if(!intersections[0].equals(v) && !intersections[0].equals(new Vertex(-1, -1))) {
      return true;
    }
    if(!intersections[1].equals(v) && !intersections[1].equals(new Vertex(-1, -1))) {
      return true;
    }
    return false;
  }

  public List<VerticalTrapezoid> createTrapezoids(List<VerticalTrapezoid> leftSideTrapezoids, Vertex v, VertexList outline) {
    List<VerticalTrapezoid> rightSideTrapezoids = new ArrayList<VerticalTrapezoid>();

    Vertex next = outline.getNext(v);
    Vertex prev = outline.getPrev(v);
    LineSegment vNext = new LineSegment(v, next);
    LineSegment vPrev = new LineSegment(v, prev);
    Vertex nextTop = vNext.slope() < vPrev.slope() ? next : prev;
    Vertex nextBot = vNext.slope() > vPrev.slope() ? next : prev;
    Vertex nextRight = next.x > v.x ? next : (prev.x > v.x ? prev : null);
    Vertex nextLeft = next.x < v.x ? next : (prev.x < v.x ? prev : null);

    if(leftSideTrapezoids.size() == 1) {
      VerticalTrapezoid trapezoid = leftSideTrapezoids.get(0);
      Vertex[] intersections = intersection(trapezoid, v);
      Vertex vTop;
      Vertex vBot;

      if(!intersections[0].equals(v) && !intersections[0].equals(new Vertex(-1, -1))) {
        vTop = new VirtualVertex(intersections[0]);
      } else {
        vTop = null;
      }
      if(!intersections[1].equals(v) && !intersections[1].equals(new Vertex(-1, -1))) {
        vBot = new VirtualVertex(intersections[1]);
      } else {
        vBot = null;
      }

      if(vertexIsRightTrap(outline, v)) {

        //Merge 2 Polygons into 1
        /**
         * 1
         * Wenn es in einem der Fälle keinen Schnittpunkt gibt,
         * dann liegen die Punkte auf der gleichen X-Koordinate
         * Allgemeine Lage verhindert das.
         * ABER TODO
         */

        if(trapezoid.top.end.x == v.x) {
          vTop = trapezoid.top.end;
        }
        if(trapezoid.bot.end.x == v.x) {
          vBot = trapezoid.bot.end;
        }

        VerticalTrapezoid topTrap = new VerticalTrapezoid(vTop, v);
        VerticalTrapezoid botTrap = new VerticalTrapezoid(v, vBot);
        rightSideTrapezoids.add(topTrap);
        rightSideTrapezoids.add(botTrap);

        trapezoid.right = new LineSegment(vTop, vBot);
        if(vTop.equals(trapezoid.top.end)) {
          Vertex eNext = outline.getNext(vTop);
          Vertex ePrev = outline.getPrev(vTop);
          topTrap.top = new LineSegment(vTop, eNext.x > ePrev.x ? eNext : ePrev);
        } else {
          topTrap.top = trapezoid.top;
        }
        topTrap.bot = new LineSegment(v, nextTop);

        if(vBot.equals(trapezoid.bot.end)) {
          Vertex eNext = outline.getNext(vBot);
          Vertex ePrev = outline.getPrev(vBot);
          botTrap.bot = new LineSegment(vTop, eNext.x > ePrev.x ? eNext : ePrev);
        } else {
          botTrap.bot = trapezoid.bot;
        }
        botTrap.top = new LineSegment(v, nextBot);


        trapezoid.top = new LineSegment(trapezoid.left.start, vTop);
        trapezoid.bot = new LineSegment(trapezoid.left.end, vBot);

        topTrap.addPreviousTrapezoid(trapezoid);
        botTrap.addPreviousTrapezoid(trapezoid);
        trapezoid.addNextTrapezoid(topTrap);
        trapezoid.addNextTrapezoid(botTrap);

      } else {
        //Attach 1 Polygon to another
        //Fall Eckpunkt oder Punkte liegen übereinander
        boolean topIndicator = false;
        boolean botIndicator = false;
        if(trapezoid.top.end.x == v.x) {
          topIndicator = true;
          vTop = trapezoid.top.end;
        }
        if(trapezoid.bot.end.x == v.x) {
          botIndicator = true;
          vBot = trapezoid.bot.end;
        }

        //Fall Eckpunkt
        if(vBot.equals(vTop) || trapezoid.top.end.equals(v) && trapezoid.bot.end.equals(nextBot) && trapezoid.bot.end.x == v.x) {
          if(vBot.equals(vTop)) {
            trapezoid.right = new LineSegment(v,v);
            trapezoid.top = new LineSegment(trapezoid.left.start, v);
            trapezoid.bot = new LineSegment(trapezoid.left.end, v);
          } else if(trapezoid.top.end.equals(v) && trapezoid.bot.end.x == v.x) {
            //Fall Eckpunkt mit Vertikaler Linie
            trapezoid.right = new LineSegment(v, trapezoid.bot.end);
            trapezoid.top = new LineSegment(trapezoid.left.start, v);
            trapezoid.bot = new LineSegment(trapezoid.left.end, nextBot);
          } else if(trapezoid.bot.end.equals(v)) {
            //Do Nothing, Eckpunkt mit vertikaler Linie, unterer Punkt schon abgearbeitet
          }
          VerticalTrapezoid endTrap = new VerticalTrapezoidFiller(trapezoid.right);
          endTrap.top = new LineSegment(trapezoid.right.start, trapezoid.right.start);
          endTrap.bot = new LineSegment(trapezoid.right.end, trapezoid.right.end);
          endTrap.right = trapezoid.right;
          trapezoid.addNextTrapezoid(endTrap);
          endTrap.addPreviousTrapezoid(trapezoid);
          rightSideTrapezoids.add(endTrap);
          //Previous schon vorhanden. Next = Null
        } else {
          //Fall Mittelpunkt
          if(trapezoid.top.end.equals(v)) {

            trapezoid.right = new LineSegment(v, vBot);
            VerticalTrapezoid newTrapezoid = new VerticalTrapezoid(trapezoid.right);
            newTrapezoid.top = new LineSegment(v, nextRight);
            if(topIndicator) {
              Vertex eNext = outline.getNext(vTop);
              Vertex ePrev = outline.getPrev(vTop);
              newTrapezoid.top = new LineSegment(vTop, eNext.x > v.x ? eNext : ePrev);
            }
            newTrapezoid.bot = trapezoid.bot;
            if(botIndicator) {
              Vertex eNext = outline.getNext(vBot);
              Vertex ePrev = outline.getPrev(vBot);
              newTrapezoid.bot = new LineSegment(vBot, eNext.x > v.x ? eNext : ePrev);
            }
            trapezoid.top = new LineSegment(trapezoid.left.start, v);
            trapezoid.bot = new LineSegment(trapezoid.left.end, vBot);
            trapezoid.addNextTrapezoid(newTrapezoid);
            newTrapezoid.addPreviousTrapezoid(trapezoid);
            rightSideTrapezoids.add(newTrapezoid);
          } else {
            if(trapezoid.left.start.x == v.x) {
              rightSideTrapezoids.add(trapezoid);
            } else {
              trapezoid.right = new LineSegment(vTop, v);
              VerticalTrapezoid newTrapezoid = new VerticalTrapezoid(trapezoid.right);
              newTrapezoid.bot = new LineSegment(v, nextRight);
              newTrapezoid.top = trapezoid.top;
              trapezoid.top = new LineSegment(trapezoid.left.start, vTop);
              trapezoid.bot = new LineSegment(trapezoid.left.end, v);
              trapezoid.addNextTrapezoid(newTrapezoid);
              newTrapezoid.addPreviousTrapezoid(trapezoid);
              rightSideTrapezoids.add(newTrapezoid);
            }
          }
        }
      }
    } else if(leftSideTrapezoids.size() == 0) {
      //Erstelle neues Polygon
      VerticalTrapezoid newTrapezoid = null;
      //Top Left StartVertex
      if(v.x == next.x && v.y < next.y || v.x == prev.x && v.y < prev.y) {
        Vertex leftBot = next.y > v.y ? next : prev;
        // Vertex rightTop = next.y > v.y ? prev : next;
        Vertex rightTop = next.equals(leftBot) ? prev : next;
        Vertex rightBot = outline.getNext(leftBot).equals(v) ? outline.getPrev(leftBot) : outline.getNext(leftBot);
        newTrapezoid = new VerticalTrapezoid(new LineSegment(v, leftBot));
        newTrapezoid.bot = new LineSegment(leftBot, rightBot);
        newTrapezoid.top = new LineSegment(v, rightTop);
      } else {
        newTrapezoid = new VerticalTrapezoid(new LineSegment(v,v));
        newTrapezoid.top = new LineSegment(v,nextTop);
        newTrapezoid.bot = new LineSegment(v,nextBot);
      }
      rightSideTrapezoids.add(newTrapezoid);
    } else {
      //Fall Left Trap / leftside_1 = top, leftside_2 = bot
      VerticalTrapezoid trapTop = leftSideTrapezoids.get(0);
      VerticalTrapezoid trapBot = leftSideTrapezoids.get(1);
      if(trapTop.left.start.y > trapBot.left.start.y) {
        VerticalTrapezoid tmp = trapTop;
        trapTop = trapBot;
        trapBot = tmp;
      }

      Vertex[] intersections = intersection(trapTop, v);
      VirtualVertex vTop = null;
      VirtualVertex vBot = null;
      if(!intersections[0].equals(v) && !intersections[0].equals(new Vertex(-1, -1))) {
        vTop = new VirtualVertex(intersections[0]);
      }
      // if(!intersections[1].equals(v) && !intersections[1].equals(new Vertex(-1, -1))) {
      //   vBot = new VirtualVertex(intersections[1]);
      // }

      intersections = intersection(trapBot, v);
      // if(!intersections[0].equals(v) && !intersections[0].equals(new Vertex(-1, -1))) {
      //   vTop = new VirtualVertex(intersections[0]);
      // }
      if(!intersections[1].equals(v) && !intersections[1].equals(new Vertex(-1, -1))) {
        vBot = new VirtualVertex(intersections[1]);
      }

      VerticalTrapezoid newTrapezoid = new VerticalTrapezoid(new LineSegment(vTop, vBot));
      newTrapezoid.top = trapTop.top;
      newTrapezoid.bot = trapBot.bot;
      trapTop.right = new LineSegment(vTop, v);
      trapTop.top = new LineSegment(trapTop.left.start, vTop);
      trapTop.bot = new LineSegment(trapTop.left.end, v);
      trapBot.right = new LineSegment(v, vBot);
      trapBot.top = new LineSegment(trapBot.left.start, v);
      trapBot.bot = new LineSegment(trapBot.left.end, vBot);
      trapTop.addNextTrapezoid(newTrapezoid);
      trapBot.addNextTrapezoid(newTrapezoid);
      newTrapezoid.addPreviousTrapezoid(trapTop);
      newTrapezoid.addPreviousTrapezoid(trapBot);
      double aslkdjalsd = newTrapezoid.left.start.y;
      rightSideTrapezoids.add(newTrapezoid);
    }

    // for(int i = 0; i < rightSideTrapezoids.size(); i++) {
    //   drawTrapezoid(rightSideTrapezoids.get(i));
    // }

    return rightSideTrapezoids;
  }

  // public double[] calculateUniformAreasize(List<VerticalTrapezoid> trapList, int lineCount) {
  //   double area = 0;
  //   for(VerticalTrapezoid trapezoid : trapList) {
  //     area += trapezoid.area();
  //   }
  //   return area;
  // }

  public List<LineSegment> lineBreak(VertexList outline, int lineCount) {
    rotateCounterClockwise(outline);

    //Linked List mit konstanter Einfüge Operation
    try {
      Vertex[] orderedVertices = sort(outline);

      // for(Vertex v : orderedVertices) {
      //   drawCircle(v);
      // }

      LinkedList<VerticalTrapezoid> trapList = (LinkedList<VerticalTrapezoid>)getTrapezoidalDecomposition(outline, orderedVertices);
      double areaPolygon = area(trapList);
      HashSet<VerticalTrapezoid> ats = new HashSet<VerticalTrapezoid>();
      // double area[] = new double[trapList.size()];
      double area = 0;
      double area_last = 0;
      double sweep = 0;
      double a1 = 0;
      double a2 = 0;
      double b1 = 0;
      double b2 = 0;
      double s_last = -9999;
      double counter = 1;

      List<LineSegment> breakingLines = new LinkedList<LineSegment>();

      for(VerticalTrapezoid trapezoid : trapList) {
        sweep = trapezoid.left.start.x;
        if(s_last == -9999) {
          s_last = sweep;
        }

        // area += a1/2 * Math.pow(sweep,2) + b1*sweep - (a1/2 * Math.pow(s_last,2) + b1 * s_last)
        //   - (a2/2 * Math.pow(sweep,2) + b2 * sweep - (a2/2 * Math.pow(s_last,2) + b2 * s_last));
        if(s_last != sweep) {
          double addition = ((a1/2 * Math.pow(sweep,2) + b1*sweep - (a1/2 * Math.pow(s_last,2) + b1 * s_last))
            - (a2/2 * Math.pow(sweep,2) + b2 * sweep - (a2/2 * Math.pow(s_last,2) + b2 * s_last)));

          // area += a2/2 * Math.pow(sweep,2) + b2*sweep - (a2/2 * Math.pow(s_last,2) + b2 * s_last)
          //   - (a1/2 * Math.pow(sweep,2) + b1 * sweep - (a1/2 * Math.pow(s_last,2) + b1 * s_last));
          area += addition;
          while(area > areaPolygon * (counter/(lineCount + 1)) && counter <= lineCount) {
            //Trapez gefunden
            double Fleft = areaPolygon * counter / (lineCount + 1) - area_last;
            // double x = -((b1 - b2 + Math.sqrt(Math.pow((b1-b2),2) + (2*(b1*s_last-b2*s_last+Fleft)+a1*Math.pow(s_last,2)-a2*Math.pow(s_last,2))*(a1-a2)))/(a1-a2));
            double x = (-(b1 - b2 - Math.sqrt(Math.pow((b1-b2),2) + (2*(b1*s_last-b2*s_last+Fleft)+a1*Math.pow(s_last,2)-a2*Math.pow(s_last,2))*(a1-a2)))/(a1-a2));
            LineSegment vLine = new LineSegment(new Vertex(x, 0), new Vertex(x, 1000));
            vLine.start.rotateClockwise();
            vLine.end.rotateClockwise();
            breakingLines.add(vLine);
            // drawLineSegment(vLine);
            counter++;
          }
        }



        //Erst rechnen, dann hinzufügen



        if(!(trapezoid instanceof VerticalTrapezoidFiller)) {
          a1 += trapezoid.bot.slope();
          a2 += trapezoid.top.slope();
          b1 += trapezoid.bot.functionOffset();
          b2 += trapezoid.top.functionOffset();
          ats.add(trapezoid);
        }
        HashSet<VerticalTrapezoid> traps = trapezoid.getPrev();
        for(VerticalTrapezoid t : traps) {
          if(ats.contains(t)) {
            a1 -= t.bot.slope();
            a2 -= t.top.slope();
            b1 -= t.bot.functionOffset();
            b2 -= t.top.functionOffset();
          }
          ats.remove(t);
        }

        s_last = sweep;
        area_last = area;
      }
      rotateClockwise(outline);
      return breakingLines;
    } catch(Exception e) {
      rotateClockwise(outline);
      return null;
    }


  }

  public void rotateCounterClockwise(VertexList outline) {
    Vertex v = null;
    for(int i = 0; i < outline.size(); i++) {
      v = outline.next(v);
      if(!v.isRotated)
        v.rotateCounterClockwise();
    }
  }

  public void rotateClockwise(VertexList outline) {
    Vertex v = null;
    for(int i = 0; i < outline.size(); i++) {
      v = outline.next(v);
      if(v.isRotated)
        v.rotateClockwise();
    }
  }

  public double area(List<VerticalTrapezoid> trapezoids) {
    double area = 0;
    for(VerticalTrapezoid t : trapezoids) {
      area += t.area();
    }
    return area;
  }

  public VerticalTrapezoid trimConvex(VerticalTrapezoid head, VerticalTrapezoid trapezoid, double minHeight) {
    double heightLeft = trapezoid.left.height();
    double heightRight = trapezoid.right.height();

    // if(!head.equals(trapezoid)) {
    //   return head;
    // }

    if(heightLeft < heightRight) {
      //Sind auf linker Seite
      if(heightRight < minHeight) {
        trapezoid.getNextExplicit().removePrev(trapezoid);
        head = trapezoid.getNextExplicit();
        return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
      } else if(heightLeft < minHeight) {
        double slope = Math.abs(trapezoid.bot.slope() - trapezoid.top.slope());
        double b = trapezoid.left.height();
        double x = (minHeight - b) / slope;
        double offset = x + trapezoid.left.start.x;
        LineSegment line = new LineSegment(offset, 0, offset, 1000);
        Vertex top = new VirtualVertex(-1,-1);
        Vertex bot = new VirtualVertex(-1,-1);
        line.getLineIntersection(trapezoid.top, top);
        line.getLineIntersection(trapezoid.bot, bot);
        trapezoid.bot = new LineSegment(bot, trapezoid.bot.end);
        trapezoid.top = new LineSegment(top, trapezoid.top.end);
        trapezoid.left = new LineSegment(top, bot);

        if(trapezoid.getNextExplicit() != null) {
          return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
        } else return head;
      } else {
        if(trapezoid.getNextExplicit() != null) {
          return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
        } else return head;
      }

    } else if(heightLeft > heightRight) {
      //Sind auf rechten Seite
      if(heightLeft < minHeight) {
        VerticalTrapezoid prev = trapezoid.getPrevExplicit();
        if(prev != null)
          prev.removeNext(trapezoid);
        return head;
      } else if(heightRight < minHeight) {
        double slope = - Math.abs(trapezoid.bot.slope() - trapezoid.top.slope());
        double b = trapezoid.left.height();
        double x = (minHeight - b) / slope;

        double offset = x + trapezoid.left.start.x;
        LineSegment line = new LineSegment(offset, 0, offset, 1000);
        Vertex top = new VirtualVertex(-1,-1);
        Vertex bot = new VirtualVertex(-1,-1);
        line.getLineIntersection(trapezoid.top, top);
        line.getLineIntersection(trapezoid.bot, bot);
        trapezoid.bot = new LineSegment(trapezoid.bot.start, bot);
        trapezoid.top = new LineSegment(trapezoid.top.start, top);
        trapezoid.right = new LineSegment(top, bot);
        trapezoid.removeNext(trapezoid.getNextExplicit());
        return head;
      } else {
        if(trapezoid.getNextExplicit() != null) {
          return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
        } else return head;
      }
    } else {
      if(trapezoid.getNextExplicit() != null) {
        return trimConvex(head, trapezoid.getNextExplicit(), minHeight);
      } else return head;
    }
  }

  public void trimFast(VerticalTrapezoid head, VerticalTrapezoid tail, double minHeight) {
    VerticalTrapezoid t = head;
    while(t.left.height() < minHeight) {
      t.deactivate(0);
      t = t.getNextExplicit();
    }

    t = tail;
    while(t.right.height() < minHeight) {
      t.deactivate(0);
      t = t.getPrevExplicit();
    }
  }

  public double getWeightedAverageHeight(List<VerticalTrapezoid> trapezoids) {
    double leftest = 10000;
    double rightest = -10000;
    double weightedAverageHeight = 0;
    for(VerticalTrapezoid t : trapezoids) {
      if(t.left.start.x < leftest) leftest = t.left.start.x;
      if(t.right.start.x > rightest) rightest = t.left.start.x;
      double width = t.right.start.x - t.left.start.x;
      double height = (t.left.height() + t.right.height()) / 2.0;
      weightedAverageHeight += height * width;
    }
    return weightedAverageHeight / (rightest - leftest);
  }


  public void drawCircle(Vertex v) {
    Circle circle = new Circle(v.x, v.y, 5);
    circle.setStroke(Color.color(1,0,0,1));
    textLayer.getChildren().add(circle);
  }

  public VertexPolygon trapezoidsToPolygon(VerticalTrapezoid trapezoid) {
    VertexPolygon p = new VertexPolygon();
    trapezoidToPolygon(p, trapezoid);
    return p;
  }

  public Vertex trapezoidToPolygon(VertexPolygon polygon, VerticalTrapezoid trapezoid) {
    HashSet<VerticalTrapezoid> tPrev = trapezoid.getPrev();
    HashSet<VerticalTrapezoid> tNext = trapezoid.getNext();
    Vertex prev = null;
    Vertex current = null;
    HashSet<VerticalTrapezoid> done = new HashSet<VerticalTrapezoid>();

    if(polygon.isEmpty()) {
      polygon.addVertex(trapezoid.left.start);
      polygon.addVertex(trapezoid.right.start);
      current = polygon.getLastVertex();
      for(int i = 0; i < 2; i++) {
        for(VerticalTrapezoid t: tNext) {
          if(t.contains(current) && !done.contains(t)) {
            done.add(t);
            current = trapezoidToPolygon(polygon, t);
          }
        }
      }
      polygon.addVertex(trapezoid.left.end);

    } else {
      prev = polygon.getLastVertex();
      if(trapezoid.contains(prev)) {
        if(trapezoid.top.start.equals(prev)) {
          polygon.addVertex(trapezoid.top.end);
        } else {
          polygon.addVertex(trapezoid.bot.start);
        }
        current = polygon.getLastVertex();
      }

      if(!trapezoid.hasNext()) {
        if(!trapezoid.right.start.equals(trapezoid.right.end)) {
          polygon.addVertex(trapezoid.right.end);
        }
        polygon.addVertex(trapezoid.left.end);
        return polygon.getLastVertex();
      }
      if(!trapezoid.hasPrev()) {
        if(!trapezoid.left.start.equals(trapezoid.left.end)) {
          polygon.addVertex(trapezoid.left.start);
        }
        polygon.addVertex(trapezoid.right.start);
        return polygon.getLastVertex();
      }

      for(int i = 0; i < 3; i++) {
        for(VerticalTrapezoid t: tPrev) {
          if(t.contains(current) && !done.contains(t)) {
            done.add(t);
            current = trapezoidToPolygon(polygon, t);
          }
        }
        for(VerticalTrapezoid t: tNext) {
          if(t.contains(current) && !done.contains(t)) {
            done.add(t);
            current = trapezoidToPolygon(polygon, t);
          }
        }
      }
    }
    return current;
  }

  public VertexPolygon _trapezoidToPolygonMonotone(VerticalTrapezoid trapezoid) {
    VertexPolygon polygon = new VertexPolygon();
    trapezoidToPolygonMonotone(polygon, trapezoid);
    return polygon;
  }

  public void trapezoidToPolygonMonotone(VertexPolygon polygon, VerticalTrapezoid trapezoid) {
    VerticalTrapezoid nextTrap = trapezoid.getNextExplicit();

    if(polygon.isEmpty()) {
      polygon.addVertex(trapezoid.left.start);
      polygon.addVertex(trapezoid.right.start);
      if(nextTrap != null) {
        trapezoidToPolygon(polygon, nextTrap);
      }

    } else {
      if(!polygon.getLastVertex().equals(trapezoid.left.start)) {
        polygon.addVertex(trapezoid.left.start);
      }
      polygon.addVertex(trapezoid.right.start);
      if(nextTrap != null) {
        trapezoidToPolygon(polygon, nextTrap);
      } else {
        polygon.addVertex(trapezoid.right.end);
      }
    }

    if(!polygon.getLastVertex().equals(trapezoid.right.end)) {
      polygon.addVertex(trapezoid.right.end);
    }

    if(!polygon.getLastVertex().equals(trapezoid.left.end)) {
      polygon.addVertex(trapezoid.left.end);
    }
  }

}
