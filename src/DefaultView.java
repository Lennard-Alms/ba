import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.text.*;
import javafx.collections.ObservableList;

class DefaultView {
  int drawingAreaHeight = 800;
  int drawingAreaWidth = 1000;

  Pane root;

  VBox uiContainer = new VBox();

  Pane mainLayer = new Pane();
  Pane edgeLayer = new Pane();
  Pane textLayer = new Pane();
  Pane emptyLayer = new Pane();

  TextField polygonTextField;

  Button prevBtn = new Button("prev");
  Button updateBtn = new Button("update");
  Button nextBtn = new Button("next");
  Button newPolyBtn = new Button("New Polygon");
  Button opacityButton = new Button("Toggle Opacity");
  Button saveButton = new Button("Save file...");
  Button loadButton = new Button("Load file...");
  Button scaleInButton = new Button("Zoom in");
  Button scaleOutButton = new Button("Zoom Out");
  Button moveLeftButton = new Button("Left");
  Button moveRightButton = new Button("Right");
  Button moveDownButton = new Button("Down");
  Button moveUpButton = new Button("Up");
  ComboBox<String> strategySelector = new ComboBox<String>();

  DefaultController controller;

  public DefaultView(Pane root) {
    controller = new DefaultController(this);
    root.getChildren().add(emptyLayer);
    root.getChildren().add(uiContainer);
    textLayer.setMaxWidth(1000);
    edgeLayer.setMaxWidth(1000);
    mainLayer.setMaxWidth(1000);
    emptyLayer.setMaxWidth(1000);
    emptyLayer.getChildren().add(textLayer);
    emptyLayer.getChildren().add(edgeLayer);
    emptyLayer.getChildren().add(mainLayer);
    addOnClickActionListenerOnDrawingArea();
    addUserInteraction();
  }

  public void addOnClickActionListenerOnDrawingArea() {
    Rectangle r = new Rectangle();
    r.setX(0);
    r.setY(0);
    r.setWidth(drawingAreaWidth);
    r.setHeight(drawingAreaHeight);
    r.setFill(Color.color(0,0,0,0));
    r.setOnMousePressed(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        controller.addVertexAndPoint(event.getX(), event.getY());
      }
    });
    mainLayer.getChildren().add(r);
  }

  public void addPointToVertex(Vertex vertex) {
    Circle point = new Circle();
    vertex.point = point;
    double x = vertex.x;
    double y = vertex.y;
    point.setCenterX(x);
    point.setCenterY(y);
    point.setRadius(2.0);

    point.setOnMouseDragged(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        controller.handleMouseDragged(event, point, vertex);
      }
    });
    point.setOnMousePressed(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        controller.handleMousePressed(point, vertex);
      }
    });
    point.setOnMouseReleased(new EventHandler<MouseEvent>() { // need to rework
      public void handle(MouseEvent event){
        if(vertex.dragged){
          refresh();
          vertex.dragged = false;
        }
      }
    });
    drawPoint(point);
  }

  public void addUserInteraction(){
    polygonTextField = new TextField();
    uiContainer.getChildren().add(polygonTextField);

    HBox navigationContainer = new HBox();
    uiContainer.getChildren().add(navigationContainer);

    prevBtn.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handlePreviousButton();
      }
    });
    updateBtn.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleUpdateButton();
      }
    });
    nextBtn.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleNextButton();
      }
    });
    newPolyBtn.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleNewButton();
      }
    });
    saveButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleSaveButton();
      }
    });

    loadButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleLoadButton();
      }
    });

    opacityButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleOpacityButton();
      }
    });

    scaleInButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleScaleInButton();
      }
    });

    scaleOutButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleScaleOutButton();
      }
    });

    moveLeftButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleMoveLeftButton();
      }
    });

    moveRightButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleMoveRightButton();
      }
    });

    moveUpButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleMoveUpButton();
      }
    });

    moveDownButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleMoveDownButton();
      }
    });

    strategySelector.getItems().addAll(
      "Default",
      "Center X Strategy",
      "Convex",
      "Concave"
    );
    strategySelector.setValue("Default");
    strategySelector.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        controller.handleStrategyDropDown();
      }
    });

    navigationContainer.getChildren().add(prevBtn);
    navigationContainer.getChildren().add(updateBtn);
    navigationContainer.getChildren().add(nextBtn);
    uiContainer.getChildren().add(newPolyBtn);
    uiContainer.getChildren().add(opacityButton);
    uiContainer.getChildren().add(loadButton);
    uiContainer.getChildren().add(saveButton);
    uiContainer.getChildren().add(strategySelector);
    uiContainer.getChildren().add(scaleInButton);
    uiContainer.getChildren().add(scaleOutButton);
    uiContainer.getChildren().add(moveLeftButton);
    uiContainer.getChildren().add(moveRightButton);
    uiContainer.getChildren().add(moveUpButton);
    uiContainer.getChildren().add(moveDownButton);
  }

  public void drawPolygon(VertexPolygon vertexPolygon){
    Polygon poly = new Polygon();

    for(Vertex v : vertexPolygon.getOutline()) {
      poly.getPoints().addAll(new Double[] {v.x, v.y} );
    }

    poly.setFill(Color.color(0,0,0,0));
    poly.setStrokeWidth(2);
    poly.setStroke(Color.BLACK);
    edgeLayer.getChildren().add(poly);
  }

  public void drawUIPolygon(VertexPolygon vertexPolygon){
    Polygon poly = new Polygon();

    for(Vertex v : vertexPolygon.getOutline()) {
      poly.getPoints().addAll(new Double[] {v.x, v.y} );
    }

    poly.setFill(Color.color(0,0,0,0));
    poly.setStrokeWidth(0);

    poly.setOnMousePressed(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        controller.selectPolygon(vertexPolygon);
      }
    });

    mainLayer.getChildren().add(poly);
  }

  public void refreshDrag(){
    ArrayList<VertexPolygon> polygonList = controller.polygonList;
    edgeLayer.getChildren().clear();
    textLayer.getChildren().clear();
    for(VertexPolygon poly : polygonList) {
      drawPolygon(poly);
      //poly.drawText(textLayer);
    }
  }

  public void setMainLayerOpacity(double value) {
    mainLayer.setOpacity(value);
  }

  public void setEdgeLayerOpacity(double value) {
    edgeLayer.setOpacity(value);
  }

  public void drawRectangle(double[] outline) {
    double left = outline[0];
    double right = outline[1];
    double top = outline[2];
    double bottom = outline[3];
    Rectangle r = new Rectangle();
    r.setX(left);
    r.setY(top);
    r.setWidth(Math.abs(right - left));
    r.setHeight(Math.abs(bottom - top));
    r.setFill(Color.color(0,0,0,0));
    r.setStroke(Color.RED);
    r.setStrokeWidth(1);
    textLayer.getChildren().add(r);
  }


  public void refresh() {
    ArrayList<VertexPolygon> polygonList = controller.polygonList;
    mainLayer.getChildren().clear();
    edgeLayer.getChildren().clear();
    textLayer.getChildren().clear();
    // add click functionality back in
    addOnClickActionListenerOnDrawingArea();
    for(VertexPolygon poly : polygonList) {
      drawPolygon(poly);
      drawUIPolygon(poly);
      // if(poly.isWithinBox(0,0,1000,800)) {
        poly.drawText(textLayer);
      // }
    }
    for(VertexPolygon poly : polygonList) {
      for(Vertex v : poly.outline){
        v.point = null;
      }
    }
    for(VertexPolygon poly : polygonList) {
      for(Vertex v : poly.outline){
        if(v.point == null) addPointToVertex(v);
      }
    }
  }

  public void drawPoint(Circle point) {
    mainLayer.getChildren().add(point);
  }

  public TextField getPolygonTextField() {
    return polygonTextField;
  }

  public ComboBox<String> getStrategyCombobox() {
    return strategySelector;
  }

  public Button getNewPolyButton() {
    return newPolyBtn;
  }
}
