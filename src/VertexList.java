import java.util.HashMap;
import java.util.Map;

class VertexList {
  VertexWrapper head = null;
  VertexWrapper tail = null;
  HashMap<Vertex, VertexWrapper> map;
  private int size = 0;

  public VertexList() {
    map = new HashMap<Vertex, VertexWrapper>();
  }

  public VertexList(Vertex v) {
    this();
    init(v);
  }

  public void init(Vertex v) {
    head = new VertexWrapper(v);
    tail = head;
    head.setPrev(head);
    head.setNext(head);
  }

  public void add(Vertex v) {
    VertexWrapper w = new VertexWrapper(v);
    if(head == null) {
      init(v);
      w = head;
    } else {
      tail.setNext(w);
      w.setPrev(tail);
      tail = w;
      head.setPrev(tail);
      tail.setNext(head);
    }
    map.put(v, w);
    size++;
  }

  public void insertAfter(Vertex position, Vertex value) {
    VertexWrapper pos = map.get(position);
    VertexWrapper v = new VertexWrapper(value);
    VertexWrapper next = pos.getNext();
    pos.setNext(v);
    v.setNext(next);
    v.setPrev(pos);
    next.setPrev(v);
    map.put(value, v);
    size++;
  }
  public void insertBefore(Vertex position, Vertex value) {
    VertexWrapper pos = map.get(position);
    VertexWrapper v = new VertexWrapper(value);
    VertexWrapper prev = pos.getPrev();
    pos.setPrev(v);
    v.setPrev(prev);
    v.setNext(pos);
    prev.setNext(v);
    map.put(value, v);
    size++;
  }

  public boolean contains(Vertex v) {
    if(map.containsKey(v)) return true;
    return false;
  }

  public Vertex next(Vertex v) {
    return getNext(v);
  }

  public Vertex getNext(Vertex v) {
    if(v == null && head != null) return head.value();
    if(contains(v)) {
      return map.get(v).getNext().value();
    }
    return null;
  }

  public Vertex getPrev(Vertex v) {
    if(contains(v)) {
      VertexWrapper w = map.get(v);
      return map.get(v).getPrev().value();
    }
    return null;
  }

  public Vertex head() {
    return head.value();
  }

  public Vertex tail() {
    return tail.value();
  }

  public VertexWrapper _getPrev(Vertex v) {
    if(contains(v)) {
      return map.get(v).getPrev();
    }
    return null;
  }

  public Vertex[] toArray() {
    if(size != 0) {
      Vertex[] result = new Vertex[size];
      VertexWrapper v = head;
      for(int i = 0; i < size; i++) {
        result[i] = v.value();
        v = v.getNext();
      }
      return result;
    } else return new Vertex[0];
  }

  public int size() {
    return size;
  }

  public String toString() {
    String str = "[";
    Vertex v = head();
    for(int i = 0; i < size(); i++) {
      if(v instanceof VirtualVertex) {
        str += "v_";
      }
      str += v.toString();
      str += ", ";
      v = getNext(v);
    }
    str += "]";
    return str;
  }


}
