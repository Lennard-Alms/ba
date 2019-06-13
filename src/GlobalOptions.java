

class GlobalOptions {
    private static double margin = 0;
    private static double alpha = 1;
    private static DefaultView view = null;

    public static void setMargin(double m) {
       margin = Math.abs(m);
       view.refresh();
    }
    public static double getMargin() {
       return margin;
    }
    public static void setAlpha(double a) {
       alpha = Math.abs(a);
       view.refresh();
    }
    public static double getAlpha() {
       return alpha;
    }

    public static void setView(DefaultView v) {
        view = v;
    }
}
