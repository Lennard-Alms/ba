
class StrategyFactory {


  public static final String CenterX = "Center X Strategy";
  public static final String Default = "Default";
  public static final String ConvexDecomposition = "Convex Decomposition";
  public static final String ConcaveDecomposition = "Concave Decomposition";

  public static TextStrategy getStrategy(String name) {
    switch(name) {
      case CenterX:
        return new CenterXStrategy();
      case ConcaveDecomposition:
        return new ConcaveDecompositionStrategy();
      default:
        return new CenterXStrategy();
    }
  }

  public static String getName(TextStrategy strategy) {
    if(strategy instanceof CenterXStrategy) return CenterX;
    if(strategy instanceof ConvexDecompositionStrategy) return ConvexDecomposition;
    if(strategy instanceof ConcaveDecompositionStrategy) return ConcaveDecomposition;
    return Default;
  }
}
