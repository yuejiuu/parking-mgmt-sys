import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A small picture of a vehicle (car, motorcycle, SUV / van or truck) drawn with Java2D,
 * so the project needs no image files. Shown on the slot map for every parked vehicle.
 *
 * body   = color of the vehicle
 * detail = color of the windows and wheel centers (use the background color)
 */
public class VehicleIcon implements Icon {
    private final String type;
    private final Color body;
    private final Color detail;
    private final int width;
    private final int height;

    public VehicleIcon(String type, Color body, Color detail, int width) {
        this.type = type;
        this.body = body;
        this.detail = detail;
        this.width = width;
        this.height = width / 2;
    }

    public int getIconWidth() {
        return width;
    }

    public int getIconHeight() {
        return height;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.scale(width / 60.0, height / 30.0);          // everything below is drawn on a 60 x 30 grid
        g2.setColor(body);

        if (type.equals("Motorcycle")) {
            drawMotorcycle(g2);
        } else if (type.equals("SUV / Van")) {
            drawVan(g2);
        } else if (type.equals("Truck")) {
            drawTruck(g2);
        } else {
            drawCar(g2);
        }
        g2.dispose();
    }

    private void drawCar(Graphics2D g2) {
        g2.fill(new RoundRectangle2D.Double(3, 12, 54, 10, 6, 6));         // lower body
        Path2D roof = new Path2D.Double();                                   // cabin
        roof.moveTo(14, 12);
        roof.lineTo(22, 4);
        roof.lineTo(40, 4);
        roof.lineTo(48, 12);
        roof.closePath();
        g2.fill(roof);

        g2.setColor(detail);                                                 // windows
        Path2D w1 = new Path2D.Double();
        w1.moveTo(20, 11); w1.lineTo(24, 6); w1.lineTo(29, 6); w1.lineTo(29, 11); w1.closePath();
        Path2D w2 = new Path2D.Double();
        w2.moveTo(31, 11); w2.lineTo(31, 6); w2.lineTo(38, 6); w2.lineTo(43, 11); w2.closePath();
        g2.fill(w1);
        g2.fill(w2);

        wheel(g2, 13, 23, 5);
        wheel(g2, 46, 23, 5);
    }

    private void drawVan(Graphics2D g2) {
        g2.fill(new RoundRectangle2D.Double(3, 4, 54, 19, 7, 7));            // tall boxy body

        g2.setColor(detail);                                                 // windows
        g2.fill(new RoundRectangle2D.Double(8, 8, 12, 7, 2, 2));
        g2.fill(new RoundRectangle2D.Double(23, 8, 12, 7, 2, 2));
        g2.fill(new RoundRectangle2D.Double(38, 8, 14, 7, 2, 2));

        wheel(g2, 14, 23, 5);
        wheel(g2, 46, 23, 5);
    }

    private void drawTruck(Graphics2D g2) {
        g2.fill(new RoundRectangle2D.Double(2, 4, 36, 19, 3, 3));            // cargo box
        Path2D cab = new Path2D.Double();                                    // cab
        cab.moveTo(40, 9);
        cab.lineTo(50, 9);
        cab.lineTo(57, 16);
        cab.lineTo(57, 23);
        cab.lineTo(40, 23);
        cab.closePath();
        g2.fill(cab);

        g2.setColor(detail);                                                 // cab window
        Path2D window = new Path2D.Double();
        window.moveTo(42, 11); window.lineTo(49, 11); window.lineTo(53, 16); window.lineTo(42, 16);
        window.closePath();
        g2.fill(window);

        wheel(g2, 10, 24, 4.5);
        wheel(g2, 25, 24, 4.5);
        wheel(g2, 48, 24, 4.5);
    }

    private void drawMotorcycle(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Double(3, 14, 15, 15));                        // rear wheel
        g2.draw(new Ellipse2D.Double(42, 14, 15, 15));                       // front wheel

        g2.drawLine(10, 21, 24, 13);                                         // frame
        g2.drawLine(24, 13, 38, 13);
        g2.drawLine(30, 13, 34, 21);
        g2.drawLine(41, 6, 49, 21);                                          // front fork
        g2.drawLine(37, 6, 44, 5);                                           // handlebar

        g2.fill(new RoundRectangle2D.Double(13, 8, 14, 4, 3, 3));            // seat
        g2.fill(new Ellipse2D.Double(28, 7, 11, 6));                         // fuel tank
    }

    /** A wheel: vehicle color with a ring and hub in the detail color. */
    private void wheel(Graphics2D g2, double cx, double cy, double r) {
        g2.setColor(detail);
        g2.fill(new Ellipse2D.Double(cx - r - 1.5, cy - r - 1.5, 2 * r + 3, 2 * r + 3));
        g2.setColor(body);
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g2.setColor(detail);
        g2.fill(new Ellipse2D.Double(cx - r / 2, cy - r / 2, r, r));
        g2.setColor(body);
    }
}
