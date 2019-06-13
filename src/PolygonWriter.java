import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;

class PolygonWriter {

  ArrayList<VertexPolygon> polygonList;

  public PolygonWriter(ArrayList<VertexPolygon> polygonList) {
    this.polygonList = polygonList;
  }

  public void save(String filename) {
    try {
      JSONObject json = listToJSON();
      saveFile(json.toString(), filename);
    }
    catch(Exception e) {

    }
  }

  public void save(File file) {
    try {
      JSONObject json = listToJSON();
      saveFile(json.toString(), file);
    }
    catch(Exception e) {

    }
  }

  public void saveFile(String content, File file) {
    try {
      FileWriter fileWriter = null;
      fileWriter = new FileWriter(file);
      fileWriter.write(content);
      fileWriter.close();
    } catch (Exception e) {

    }
  }

  public void saveFile(String content, String filename) throws Exception {
    PrintWriter out = new PrintWriter(filename);
    out.close();
  }

  private JSONObject listToJSON() throws Exception {
    JSONObject root = new JSONObject();
    JSONArray polygonObjects = new JSONArray();


    for(VertexPolygon p : polygonList) {
      JSONObject polygon = new JSONObject();
      JSONArray outline = new JSONArray();

      for(Vertex v : p.getOutline()) {
        JSONObject vertex = new JSONObject();
        vertex.put("x", v.x);
        vertex.put("y", v.y);
        outline.put(vertex);
      }
      polygon.put("outline", outline);
      polygon.put("text", p.text);

      polygonObjects.put(polygon);
    }

    root.put("polygons", polygonObjects);
    return root;
  }

}
