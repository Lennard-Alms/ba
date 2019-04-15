
class StrategyFactory {


  public static final String CenterX = "Center X Strategy";
  public static final String Default = "Default";
  public static final String Convex = "Convex";
  public static final String Concave = "Concave";

  public static TextStrategy getStrategy(String name) {
    switch(name) {
      case CenterX:
        return new CenterXStrategy();
      case Convex:
        return new ConvexStrategy();
      case Concave:
        return new ConcaveStrategy();
      default:
        return new ConcaveStrategy();
    }
  }

  public static String getName(TextStrategy strategy) {
    if(strategy instanceof CenterXStrategy) return CenterX;
    if(strategy instanceof ConvexStrategy) return Convex;
    if(strategy instanceof ConcaveStrategy) return Concave;
    return Default;
  }
}
