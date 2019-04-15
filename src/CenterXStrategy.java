import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;
import javafx.scene.shape.Line;
import java.lang.Math;
import javafx.scene.paint.Color;
import java.util.Collections;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.distribution.NormalDistribution;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.geometry.Bounds;
import java.util.List;

class CenterXStrategy implements TextStrategy{


  public CenterXStrategy() {}


  public void drawText(VertexPolygon originalPoly, Pane textLayer){

    // ZENTRALE ACHSE

    ArrayList<Vertex> ereignisstruktur = new ArrayList<>();
    ereignisstruktur.addAll(buildOutlinePointsX(originalPoly));
    ArrayList<LineSegment> outlineSegments = originalPoly.getLineSegments();
    Collections.sort(ereignisstruktur, new VertexXComparator());
    double e = 0.0002;

    ArrayList<LineSegment> bisectorSegments = new ArrayList<LineSegment>();


    for(int i = 0; i < ereignisstruktur.size() - 1; i++) {
      Vertex v = ereignisstruktur.get(i);
      Vertex u = ereignisstruktur.get(i+1);
      if(Math.abs(u.x - v.x) < 0.002) continue;

      LineSegment leftLine = new LineSegment(v.x + e, 0, v.x + e , 1000);
      LineSegment rightLine = new LineSegment(u.x - e, 0, u.x - e , 1000);

      ArrayList<Vertex> cutListL = new ArrayList<Vertex>();
      ArrayList<Vertex> cutListR = new ArrayList<Vertex>();

      for(LineSegment l : outlineSegments) {
        Vertex cutL = new Vertex(0,0);
        Vertex cutR = new Vertex(0,0);
        if(leftLine.getLineIntersection(l, cutL)) cutListL.add(cutL);
        if(rightLine.getLineIntersection(l, cutR)) cutListR.add(cutR);
      }

      Collections.sort(cutListL, new VertexYComparator());
      Collections.sort(cutListR, new VertexYComparator());
      //double entireArea = originalPoly.getAreaSize();
      for(int j = 0; j < cutListL.size(); j += 2) {
        LineSegment upper = new LineSegment(cutListL.get(j), cutListR.get(j));
        LineSegment lower = new LineSegment(cutListL.get(j + 1), cutListR.get(j + 1));
        double y1 = (upper.start.y + lower.start.y) / 2;
        double y2 = (upper.end.y + lower.end.y) / 2;
        LineSegment bisector = new LineSegment(upper.start.x, y1, upper.end.x, y2);
        double area = (Math.abs(upper.start.y - lower.start.y) + Math.abs(upper.end.y - lower.end.y)) / 4;
        bisector.area = Math.max(area, 0.01);
        bisectorSegments.add(bisector);

      }

    }

    // REDUZIERUNG

    ArrayList<LineSegment> oldBisectorSegments = new ArrayList<LineSegment>();
    oldBisectorSegments.addAll(bisectorSegments);
    double alpha = 0.4;
    ArrayList<LineSegment> newBisectorSegments = new ArrayList<LineSegment>();
    while(true) {

      double mu = 0;
      for(LineSegment l : bisectorSegments) {
        mu += l.area;
      }
      mu = mu / bisectorSegments.size();

      /*double std = 0;
      for(LineSegment l : bisectorSegments) {
        std += Math.pow(l.area - mu, 2);
      }
      std = Math.sqrt(std / (bisectorSegments.size() - 1));
      System.out.println(std);
      */

      for(LineSegment l : bisectorSegments) {
        if(l.area >= alpha * mu){
          newBisectorSegments.add(l);
        }
      }

      if(newBisectorSegments.size() == bisectorSegments.size()){
        break;
      }

      bisectorSegments = newBisectorSegments;
      newBisectorSegments = new ArrayList<LineSegment>();

    }

    // AUSWAHL


    Collections.sort(bisectorSegments, new LineSegmentStartXComparator());


    double deltax = 2.0;
    ArrayList<LineSegment> lastColumn = new ArrayList<LineSegment>();
    ArrayList<LineSegment> newColumn = new ArrayList<LineSegment>();

    GraphCenterX G = new GraphCenterX();
    for(LineSegment l : bisectorSegments) {
      G.addVertex(l.end);
      G.addVertex(l.start);
      if(newColumn.size() > 0 && l.start.x != newColumn.get(0).start.x) {
        lastColumn = newColumn;
        newColumn = new ArrayList<LineSegment>();
      }
      for(LineSegment r : lastColumn) {

        if(l.start.sub(r.end).mag() < 1 || originalPoly.canSee(r.end,l.start) && l.start.x - r.end.x <= deltax){
          G.addEdge(r.end, l.start);
        }
      }
      newColumn.add(l);
      G.addEdge(l.start, l.end);

    }

    ArrayList<Vertex> longestPath = G.findLongestPath();


    // DISKRETISIERUNG

    int tau = 5;
    double deltaX = longestPath.get(longestPath.size() - 1).x - longestPath.get(0).x;
    deltaX = deltaX / (tau * originalPoly.text.length());

    ArrayList<Vertex> discretePath = new ArrayList<Vertex>();

    double xPointer = longestPath.get(0).x + deltaX / 2;

    while(xPointer < longestPath.get(longestPath.size() - 1).x) {

      for(int i = 0; i < longestPath.size() - 1; i++) {

        Vertex start = longestPath.get(i);
        Vertex end = longestPath.get(i+1);
        if(start.x == xPointer && xPointer == end.x) {
          discretePath.add(new Vertex(xPointer, Math.max(start.y, end.y)));
        }
        else if(start.x <= xPointer && xPointer < end.x) {
          LineSegment kante = new LineSegment(start, end);
          LineSegment verticalLine = new LineSegment(xPointer, 0, xPointer, 1000);
          Vertex cut = new Vertex(0,0);
          if(verticalLine.getLineIntersection(kante, cut)) {
            discretePath.add(cut);
          } else { System.out.println("Error"); }
        }

      }

      xPointer += deltaX;
    }

    // HOEHENBESTIMMUNG

    ArrayList<Double> heights = new ArrayList<Double>();

    for(Vertex v : discretePath) {
      LineSegment verticalLine = new LineSegment(v.x, 0, v.x, 1000);
      double height = 10000.0;
      for(LineSegment l : outlineSegments) {
        Vertex cut = new Vertex(0,0);
        if(verticalLine.getLineIntersection(l, cut)){
          double dist = Math.abs(cut.y - v.y);
          if(dist < height) {
            height = dist;
          }
        }
      }
      heights.add(height);
    }

    // ZENTRENBESTIMMUNG

    ArrayList<Vertex> zentren = new ArrayList<Vertex>();

    // NORMALVERTEILUNG

    for(int i = 0; i < originalPoly.text.length(); i++){
      Vertex z = new Vertex(discretePath.get(i * tau + tau / 2).x,0);
      NormalDistribution norm = new NormalDistribution(z.x, deltaX);
      double g = 0.0;
      double count = 0.0;
      for(Vertex v : discretePath) {
        g = norm.probability(v.x - deltaX / 2, v.x + deltaX / 2);
        count += g;
        z.y += g * v.y;
      }
      z.y = z.y / count;
      zentren.add(z);
    }

    // ArrayList<Vertex> zentren1 = new ArrayList<Vertex>();
    // ArrayList<Vertex> zentren2 = new ArrayList<Vertex>();
    // ArrayList<Vertex> zentren3 = new ArrayList<Vertex>();
    // ArrayList<Vertex> zentren4 = new ArrayList<Vertex>();
    //
    // // NUR DER MITTLERE
    // //
    // for(int i = 0; i < originalPoly.text.length(); i++){
    //   zentren1.add(discretePath.get(i * tau + tau / 2));
    // }
    //
    // // EIGENE VERTECIES
    //
    // for(int i = 0; i < originalPoly.text.length(); i++){
    //   Vertex z = new Vertex(discretePath.get(i * tau + tau / 2).x,0);
    //   for(int j = 0; j < tau; j++){
    //     z.y += discretePath.get(i * tau + j).y;
    //   }
    //   z.y = z.y / tau;
    //   zentren2.add(z);
    // }
    //
    // // ALLE KNOTEN
    // double mue = 0.0;
    // for(Vertex v : discretePath){ mue += v.y; }
    // mue = mue / discretePath.size();
    // for(int i = 0; i < originalPoly.text.length(); i++){
    //   zentren3.add(new Vertex(discretePath.get(i * tau + tau / 2).x, mue));
    // }
    //
    // // EIGENE + links und rechts
    //
    // for(int i = 0; i < originalPoly.text.length(); i++){
    //   Vertex z = new Vertex(discretePath.get(i * tau + tau / 2).x,0);
    //   int count = 0;
    //   for(Vertex v : discretePath) {
    //     if(Math.abs(v.x - z.x) <= tau * 1.6 * deltaX) {
    //       z.y += v.y;
    //       count++;
    //     }
    //   }
    //   z.y = z.y / count;
    //   zentren1.add(z);
    // }
    //

    // HOEHENBERECHNUNG


    ArrayList<Double> zHeights = new ArrayList<Double>();

    for(int i = 0; i < originalPoly.text.length(); i++){
      Vertex z = zentren.get(i);
      double zHeight = 0.0;
      NormalDistribution norm = new NormalDistribution(z.x, deltaX);
      double g = 0.0;
      double count = 0.0;
      for(int j = 0; j < discretePath.size(); j++){
        Vertex v = discretePath.get(j);
        g = norm.probability(v.x - deltaX / 2, v.x + deltaX / 2);
        count += g;
        zHeight += g * heights.get(j);
      }
      zHeight = zHeight / count;
      zHeights.add(zHeight);
    }




    // MINHEIGHT

    // for(int i = 0; i < originalPoly.text.length(); i++){
    //   double zHeight = 100000.0;
    //   for(int j = 0; j < tau; j++){
    //     zHeight = Math.min(zHeight, heights.get(i * tau + j));
    //   }
    //   zHeights.add(zHeight);
    //   System.out.println(zHeight);
    // }

    // Bewertung

    Boolean tooHigh = false;
    for(int i = 0; i < originalPoly.text.length(); i++){
      tooHigh = zHeights.get(i) * 2 > 1.5 * deltaX * tau * 4;
      if(tooHigh) break;
    }
    Boolean foundCut = false;
    if(tooHigh && originalPoly.text.length() != 1) {

      //SPLIT TEXT

      String[] textParts = splitText(originalPoly.text);
      double upperRatio = (double)textParts[0].length() / originalPoly.text.length();
      //double upperRatio = ;//(double)textParts[1].length() / originalPoly.text.length();

      //SPLIT POLYGON

      ereignisstruktur = new ArrayList<>();
      ereignisstruktur.addAll(buildOutlinePointsY(originalPoly));
      outlineSegments = originalPoly.getLineSegments();
      Collections.sort(ereignisstruktur, new VertexYComparator());
      e = 0.0002;

      bisectorSegments = new ArrayList<LineSegment>();

      double originalArea = originalPoly.getAreaSize();
      double wantedUpperArea = originalArea * upperRatio;
      VertexPolygon upperP = null;
      VertexPolygon lowerP = null;
      mainloop: for(int i = 0; i < ereignisstruktur.size() - 1; i++) {
        Vertex v = ereignisstruktur.get(i);
        Vertex u = ereignisstruktur.get(i+1);
        if(Math.abs(u.y - v.y) < e * 10.0) continue;

        LineSegment upLine = new LineSegment(0, v.y + e, 1000, v.y + e);
        LineSegment downLine = new LineSegment(0, u.y - e, 1000, u.y - e);

        ArrayList<Vertex> cutListU = new ArrayList<Vertex>();
        ArrayList<Vertex> cutListD = new ArrayList<Vertex>();

        for(LineSegment l : outlineSegments) {
          Vertex cutU = new Vertex(0,0);
          Vertex cutD = new Vertex(0,0);
          if(upLine.getLineIntersection(l, cutU)) cutListU.add(cutU);
          if(downLine.getLineIntersection(l, cutD)) cutListD.add(cutD);
          cutU.connectedEdge = l;
          cutD.connectedEdge = l;
        }
        Collections.sort(cutListU, new VertexXComparator());
        Collections.sort(cutListD, new VertexXComparator());

        for(int j = 0; j < cutListU.size(); j += 2) {
          LineSegment upper = new LineSegment(cutListU.get(j), cutListU.get(j + 1));
          LineSegment lower = new LineSegment(cutListD.get(j), cutListD.get(j + 1));
          double areaT = (upper.start.y + lower.start.y) * (Math.abs(upper.start.x - upper.end.x) + Math.abs(lower.start.x - lower.end.x)) / 2;
          areaT = Math.max(areaT, 0.01);

          VertexPolygon[] polygonParts = Geometry.splitPolygon(originalPoly, upper.start, upper.start.connectedEdge, upper.end, upper.end.connectedEdge);
          upperP = polygonParts[0];
          lowerP = polygonParts[1];
          double upperArea = upperP.getAreaSize();
          if(Math.abs(upperArea - wantedUpperArea) < originalArea / 95) {
            foundCut = true;
            break mainloop;
          } else if (Math.abs(upperArea + areaT - wantedUpperArea) < originalArea / 95) {

          }
        }

      }
      if(foundCut) {
        upperP.text = textParts[0];
        lowerP.text = textParts[1];
        drawText(upperP, textLayer);
        drawText(lowerP, textLayer);
      }
    }
    if(!foundCut){

      for(int i = 0; i < originalPoly.text.length(); i++){
        Vertex z = zentren.get(i);
        double fontsize = deltaX * tau;

        Font monospacedFont;
        Text t = new Text();
        monospacedFont = new Font("Cousine Bold", fontsize);

        String letterI = originalPoly.getText().substring(i, i + 1).toUpperCase();
        t.setFont(monospacedFont);
        t.setText(letterI);
        t.setBoundsType(TextBoundsType.VISUAL);

        Bounds b = t.getLayoutBounds();
        double height = b.getHeight();
        double width = b.getWidth();
        double boundingLeft = b.getMinX();
        double boundingBot = b.getMaxY();

        t.setX(z.x - width / 2 - boundingLeft);
        t.setScaleX(1.7);
        t.setY(z.y + height / 2 - boundingBot);

        if(letterI.equals(",") || letterI.equals(".")) t.setY(z.y + 0.80* zHeights.get(i));

        if(!letterI.equals("-") && !letterI.equals(",") && !letterI.equals(".")) t.setScaleY(2 * 0.95 * zHeights.get(i)/height);

        textLayer.getChildren().add(t);

      }



    }



    // PLACEMENT OF THE TEXT








    // for(Vertex start : G.vertecies) {
    //   for(Vertex end : start.neighbours) {
    //     Line line = new Line(start.x, start.y, end.x, end.y);
    //     line.setStroke(Color.color(1,0,0,0.7));
    //     line.setStrokeWidth(2);
    //     textLayer.getChildren().add(line);
    //   }
    // }
    //
    //
    // for(int i = 0; i < longestPath.size() - 1; i++) {
    //     Vertex start = longestPath.get(i);
    //     Vertex end = longestPath.get(i+1);
    //     Line line = new Line(start.x, start.y, end.x, end.y);
    //     line.setStroke(Color.color(0,0,1,0.7));
    //     line.setStrokeWidth(2);
    //     textLayer.getChildren().add(line);
    // }
    //
    //
    // for(int i = 0; i < discretePath.size(); i++) {
    //   Vertex v = discretePath.get(i);
    //   Circle c = new Circle();
    //   c.setCenterX(v.x);
    //   c.setCenterY(v.y);
    //   c.setRadius(2);
    //   c.setFill(Color.color(0,0,1,0.7));
    //   textLayer.getChildren().add(c);
    //
    // }

    // for(int i = 0; i < originalPoly.text.length(); i++){
    //   Vertex v = zentren.get(i);
    //   double zHeight = zHeights.get(i);
    //   Line line = new Line(v.x, v.y + zHeight, v.x, v.y - zHeight);
    //   line.setStroke(Color.color(1,0,0,0.7));
    //   line.setStrokeWidth(2);
    //   textLayer.getChildren().add(line);
    // }
    //
    // for(Vertex v : zentren1) {
    //   Circle c = new Circle();
    //   c.setCenterX(v.x);
    //   c.setCenterY(v.y);
    //   c.setRadius(3);
    //   c.setFill(Color.color(1,0,0,1));
    //   textLayer.getChildren().add(c);
    // }



    // for(LineSegment l : bisectorSegments) {
    //   Line line = new Line(l.start.x, l.start.y, l.end.x, l.end.y);
    //   line.setStroke(Color.color(0,0,1,0.7));
    //   line.setStrokeWidth(2);
    //   textLayer.getChildren().add(line);
    // }



    /*
    ArrayList<WeightedObservedPoint> observations = new ArrayList<WeightedObservedPoint>();

    for(LineSegment l : bisectorSegments) {
      observations.add(new WeightedObservedPoint(l.area,l.start.x, l.start.y));
      observations.add(new WeightedObservedPoint(l.area,l.end.x, l.end.y));
    }

    double[] bb = originalPoly.getBoundingBox();
    ArrayList<Integer> xAxis = new ArrayList<Integer>();

    for(int i = (int) bb[0]; i <= (int) bb[1]; i++) {
      xAxis.add(i);
    }

    PolynomialCurveFitter fitter = PolynomialCurveFitter.create(3);
    double[] coeff = fitter.fit(observations);

    ArrayList<Double> yAxis = evaluateF(coeff, xAxis);

    for(int i = 0; i < xAxis.size(); i++) {
      Circle point = new Circle();
      point.setCenterX(xAxis.get(i));
      point.setCenterY(yAxis.get(i));
      point.setRadius(1.0);
      point.setFill(Color.color(0,1,0,0.5));
      textLayer.getChildren().add(point);
    }
    */



  }

  public ArrayList<Double> evaluateF(double[] coeff, ArrayList<Integer> xAxis) {
    ArrayList<Double> yAxis = new ArrayList<Double>();
    int degree = coeff.length;
    for(int x : xAxis) {
      double y = 0;
      for(int i = 0; i < degree; i++ ) {
        y += coeff[i] * Math.pow(x,i);
      }
      yAxis.add(y);
    }
    return yAxis;
  }

  private String[] splitText(String text) {

    if(text.substring(0,text.length() - 1).contains(" ") || text.substring(0,text.length() - 1).contains("-")){

      /*
      loop through text to find positions of spaces and dashes
      wich naturally seperate the text.
      Retruns the two substrings with the most center seperator.
       */

      ArrayList<Integer> indexList = new ArrayList<Integer>();

      for (int i = 0; i < text.length() - 1; i++){

        char c = text.charAt(i);

        if(c == ' ' || c == '-') {

          indexList.add(i);

        }

      }

      int mid = indexList.get(indexList.size()/2);

      /*
      If we seperate at a dash we want to keep the dash with the first substring.
      If we find a space we will not add it to either substring since they are allready seperated now.
       */

      if(text.charAt(mid) == '-') {

        return new String[]{text.substring(0, mid + 1), text.substring(mid + 1)};

      } else {

        return new String[]{text.substring(0, mid),text.substring(mid + 1)};

      }


    } else {

      HyphenGenerator hyphi = new HyphenGenerator("de");
      List<String> hyphenatedText = hyphi.hyphenate(text);

      if(hyphenatedText.size() > 1) {

        String subPoly1Text = "";
        String subPoly2Text = "";

        for(int j = 0; j < hyphenatedText.size(); j++) {

          if(j < hyphenatedText.size() / 2){
            subPoly1Text += hyphenatedText.get(j);
          } else {
            subPoly2Text += hyphenatedText.get(j);
          }

        }

        // return new String[]{subPoly1Text + "-", subPoly2Text};
        return new String[]{subPoly1Text, subPoly2Text};

      } else {

        int mid = text.length() / 2; //get the middle of the String
        // return new String[]{text.substring(0, mid) + "-",text.substring(mid)};
        return new String[]{text.substring(0, mid),text.substring(mid)};

      }
    }
  }

  public ArrayList<Vertex> buildOutlinePointsX(VertexPolygon poly) {
    ArrayList<Vertex> pointList = new ArrayList<Vertex>();
    double[] bb = poly.getBoundingBox();
    double width = Math.abs(bb[0] - bb[1]);
    for(int i = 0; i < poly.getOutline().size(); i++) {
      LineSegment edge = poly.getLineSegment(i);
      pointList.add(edge.end);
      if(Math.abs(edge.end.x - edge.start.x) > 0.05 * width){
        Vertex directionV = edge.end.sub(edge.start).mult(1/edge.end.distance(edge.start));
        for(double j = 0.04 * width; j < Math.abs(edge.end.x - edge.start.x); j += 0.04 * width) {
          Vertex newPoint = edge.start.add(directionV.mult(j));
          pointList.add(newPoint);
        }
      }
    }
    return pointList;
  }
  public ArrayList<Vertex> buildOutlinePointsY(VertexPolygon poly) {
    ArrayList<Vertex> pointList = new ArrayList<Vertex>();
    double[] bb = poly.getBoundingBox();
    double width = Math.abs(bb[2] - bb[3]);
    for(int i = 0; i < poly.getOutline().size(); i++) {
      LineSegment edge = poly.getLineSegment(i);
      pointList.add(edge.end);
      if(Math.abs(edge.end.y - edge.start.y) > 0.03 * width){
        Vertex directionV = edge.end.sub(edge.start).mult(1/edge.end.distance(edge.start));
        for(double j = 0.02 * width; j < Math.abs(edge.end.y - edge.start.y); j += 0.02 * width) {
          Vertex newPoint = edge.start.add(directionV.mult(j));
          pointList.add(newPoint);
        }
      }
    }
    return pointList;
  }

}
