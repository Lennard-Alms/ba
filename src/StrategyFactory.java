
class StrategyFactory {


  public static final String CenterX = "Center X Strategy";
  public static final String Default = "Default";
  public static final String ConcaveDecomposition = "Concave Decomposition Strategy";

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
    if(strategy instanceof ConcaveDecompositionStrategy) return ConcaveDecomposition;
    return Default;
  }
}
