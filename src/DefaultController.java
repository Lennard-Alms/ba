import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javafx.scene.control.TextField;
import java.util.Random;

class DefaultController {

  DefaultControllerState state = new DefaultControllerState();
  ArrayList<VertexPolygon> polygonList = new ArrayList<VertexPolygon>();

  VertexPolygon newPolygon;
  DefaultView view;

  public DefaultController(DefaultView view) {
    this.view = view;
  }

  public void addVertexAndPoint(double x, double y) {
    if(state.isBuildingNewPolygon()) {
      Vertex v = new Vertex(x, y);
      state.addVertex(v);
      view.addPointToVertex(v);
      v.point.setFill(Color.LAWNGREEN);
      newPolygon.addVertex(v);
    }
  }

  public void handleMouseDragged(MouseEvent event, Circle point, Vertex vertex){
    double deltaX = Math.abs(point.getCenterX() - event.getSceneX());
    double deltaY = Math.abs(point.getCenterY() - event.getSceneY());
    if(deltaX + deltaY > 2){
      point.setCenterX(event.getSceneX());
      point.setCenterY(event.getSceneY());
      vertex.x = event.getSceneX();
      vertex.y = event.getSceneY();
      vertex.dragged = true;
      view.refreshDrag();
    }
  }

  public void handleMousePressed(Circle point, Vertex vertex){
    if(state.isBuildingNewPolygon() && !newPolygon.contains(vertex)) {
      newPolygon.addVertex(vertex);
      point.setFill(Color.LAWNGREEN);
    }
  }

  public void handlePreviousButton() {
    int prevIndex = polygonList.lastIndexOf(state.getSelectedPolygon()) - 1;
    if(prevIndex >= 0) {
      selectPolygon(polygonList.get(prevIndex));
    }
  }

  public void handleNextButton() {
    int nextIndex = polygonList.lastIndexOf(state.getSelectedPolygon()) + 1;
    if(nextIndex < polygonList.size()){
      selectPolygon(polygonList.get(nextIndex));
    }
  }

  public void handleUpdateButton() {
    TextField polygonTextField = view.getPolygonTextField();
    state.getSelectedPolygon().setText(polygonTextField.getText());
    view.refresh();
  }

  public void handleNewButton() {
    if(!state.isBuildingNewPolygon()) {
      state.startBuildingNewPolygon();
      newPolygon = new VertexPolygon(String.valueOf(polygonList.size())+String.valueOf(polygonList.size())+String.valueOf(polygonList.size())+String.valueOf(polygonList.size()));
      newPolygon.setTextStrategy(StrategyFactory.getStrategy(StrategyFactory.Default));
      view.getNewPolyButton().setText("End Polygon");
    } else {
      polygonList.add(newPolygon);
      view.refresh();
      selectPolygon(newPolygon);
      view.getNewPolyButton().setText("New Polygon");
      state.stopBuildingNewPolygon();
    }
  }

  public void selectPolygon(VertexPolygon poly) {
    state.setSelectedPolygon(poly);
    view.getPolygonTextField().setText(poly.getText());
    view.getStrategyCombobox().setValue(StrategyFactory.getName(poly.getStrategy()));
  }

  public void handleSaveButton() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showSaveDialog(null);
    if(file != null) {
      PolygonWriter writer = new PolygonWriter(polygonList);
      writer.save(file);
    }
  }

  public void handleLoadButton() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(null);
    if(file != null) {
      PolygonReader reader = new PolygonReader(file);
      polygonList = reader.get();
      state.resetVertices();
      for(int i = 0; i < polygonList.size(); i++) {
        VertexPolygon polygon = polygonList.get(i);
        polygonList.get(i).setTextStrategy(StrategyFactory.getStrategy(StrategyFactory.Default));
        for(int j = 0; j < polygon.getOutline().size(); j++) {
          if(!state.containsVertex(polygon.getOutline().get(j))) {
            state.addVertex(polygon.getOutline().get(j));
          }
        }
      }
      newPolygon = null;
      state.stopBuildingNewPolygon();
      view.refresh();
      selectPolygon(polygonList.get(0));
    }
  }

  public void handleOpacityButton() {
    double opacity = state.togglePolygonOpacity() ? 1.0 : 0.0;
    view.setEdgeLayerOpacity(opacity);
    view.setMainLayerOpacity(opacity);
  }

  public void handleStrategyDropDown() {
    String strategy = (String) view.getStrategyCombobox().getValue();
    state.getSelectedPolygon().setTextStrategy(StrategyFactory.getStrategy(strategy));
    //view.refresh();
  }

  public void handleStrategyDropDownAll() {
    String strategy = (String) view.getStrategyAllCombobox().getValue();
    for(VertexPolygon poly : polygonList) {
      poly.setTextStrategy(StrategyFactory.getStrategy(strategy));
    }
    view.refresh();
  }

  public void handleScaleInButton() {
    ArrayList<Vertex> vertices = state.getVertices();
    for(int i = 0; i < vertices.size(); i++) {
      Vertex v = vertices.get(i);
      v.x = v.x * 2.0 - 500;
      v.y = v.y * 2.0 - 400;
    }
    view.refresh();
  }

  public void handleScaleOutButton() {
    ArrayList<Vertex> vertices = state.getVertices();
    for(int i = 0; i < vertices.size(); i++) {
      Vertex v = vertices.get(i);
      v.x = v.x / 2.0 + 500 / 2;
      v.y = v.y / 2.0 + 400 / 2;
    }
    view.refresh();
  }
  public void handleMoveLeftButton() {
    ArrayList<Vertex> vertices = state.getVertices();
    for(int i = 0; i < vertices.size(); i++) {
      Vertex v = vertices.get(i);
      v.x = v.x + 100;
    }
    view.refresh();
  }
  public void handleMoveRightButton() {
    ArrayList<Vertex> vertices = state.getVertices();
    for(int i = 0; i < vertices.size(); i++) {
      Vertex v = vertices.get(i);
      v.x = v.x - 100;
    }
    view.refresh();
  }
  public void handleMoveUpButton() {
    ArrayList<Vertex> vertices = state.getVertices();
    for(int i = 0; i < vertices.size(); i++) {
      Vertex v = vertices.get(i);
      v.y = v.y + 100;
    }
    view.refresh();
  }
  public void handleMoveDownButton() {
    ArrayList<Vertex> vertices = state.getVertices();
    for(int i = 0; i < vertices.size(); i++) {
      Vertex v = vertices.get(i);
      v.y = v.y - 100;
    }
    view.refresh();
  }
  public void handleMarginUpdate() {

  }
}
