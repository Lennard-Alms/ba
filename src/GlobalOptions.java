

class GlobalOptions {
    private static double margin = 0;
    private static double alpha = 4;
    private static double sigmad = 1;
    private static double sigmay = 1;
    private static DefaultView view = null;

    public static void setMargin(double m) {
       margin = Math.abs(m);
       view.refresh();
    }
    public static double getMargin() {
       return margin;
    }
    public static void setSigmad(double m) {
       sigmad = Math.abs(m);
       view.refresh();
    }
    public static double getSigmad() {
       return sigmad;
    }
    public static void setSigmay(double m) {
       sigmay = Math.abs(m);
       view.refresh();
    }
    public static double getSigmay() {
       return sigmay;
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
