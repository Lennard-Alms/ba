

class BoundingBox {
  public double top;
  public double right;
  public double bot;
  public double left;

  public BoundingBox(double top, double right, double bot, double left) {
    this.top = top;
    this.right = right;
    this.bot = bot;
    this.left = left;
  }

  public double height() {
    return bot - top;
  }

  public double width() {
    return right - left;
  }

  public String toString() {
    return Double.toString(top) + ", " + Double.toString(right) + ", " + Double.toString(bot) + ", " + Double.toString(left);
  }
}
