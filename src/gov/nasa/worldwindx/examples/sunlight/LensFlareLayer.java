package gov.nasa.worldwindx.examples.sunlight;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * Displays a lens flare effect when the Sun is in view.
 *
 * @author Patrick Murris
 * @version $Id: LensFlareLayer.java 13909 2010-09-30 06:33:58Z pabercrombie $
 */
public class LensFlareLayer extends RenderableLayer {

    public static class FlareImage extends ScreenAnnotation {

        private final BufferedImage image;
        private double scale = 1;
        private double position = 0; // 0=Sun, 1=viewport center, 2=opposite Sun from center...
        private double opacity = .5;

        public FlareImage(BufferedImage image, double scale, double position, double opacity) {
            super("", new Point(0, 0));
            this.image = image;
            this.scale = scale;
            this.position = position;
            this.opacity = opacity;
            this.initialize();
        }

        private void initialize() {
            AnnotationAttributes aa = this.getAttributes();
            aa.setBorderWidth(0);
            aa.setImageSource(this.image);
            aa.setAdjustWidthToText(AVKey.SIZE_FIXED);
            aa.setSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
            aa.setBackgroundColor(new Color(0, 0, 0, 0));
            aa.setCornerRadius(0);
            aa.setInsets(new Insets(0, 0, 0, 0));
            aa.setDrawOffset(new Point(0, -this.image.getHeight() / 2));
        }

        public void update(Point sun, Point center) {
            double x = sun.x - (double) (sun.x - center.x) * position;
            double y = sun.y - (double) (sun.y - center.y) * position;
            this.setScreenPoint(new Point((int) x, (int) y));
            this.getAttributes().setScale(this.scale);
            this.getAttributes().setOpacity(this.opacity);
        }
    }

    //*** LensFlareLayer ***
    private static double SUN_DISTANCE = 149597892e3;

    private Vec4 sunDirection;
    private Vec4 sunPoint;

    public LensFlareLayer() {
        this.setName("Lens Flare");
        this.setPickEnabled(false);
    }

    public Vec4 getSunDirection() {
        return this.sunDirection;
    }

    public void setSunDirection(Vec4 direction) {
        if (direction != null) {
            this.sunDirection = direction.normalize3();
            this.sunPoint = this.sunDirection.multiply3(SUN_DISTANCE);
        } else {
            this.sunDirection = null;
            this.sunPoint = null;
        }
    }

    public void render(DrawContext dc) {
        if (sunPoint == null) {
            return;
        }

        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(sunPoint) < 0) {
            return; // Sun is behind the eye
        }
        Vec4 sunPos = dc.getView().project(this.sunPoint);
        if (sunPos == null) {
            return; // Sun does not project at all
        }
        Rectangle viewport = dc.getView().getViewport();
        if (!viewport.contains(sunPos.x, sunPos.y)) {
            return; // Sun is not in viewport
        }
        // Test for terrain occlusion
        Line ray = new Line(dc.getView().getEyePoint(), this.sunDirection);
        if (dc.getSurfaceGeometry().intersect(ray) != null) {
            return; // Some terrain is between the eye and the Sun
        }
        Point center = new Point(viewport.width / 2, viewport.height / 2);
        Point sun = new Point((int) sunPos.x, (int) sunPos.y);

        // Update all flare images
        for (Renderable r : this.getRenderables()) {
            if (r instanceof FlareImage) {
                ((FlareImage) r).update(sun, center);
            }
        }

        // Render
        super.render(dc);
    }

    //*** Presets ***
    public static final String PRESET_BOLD = "LensFlare.PresetBold";

    public static LensFlareLayer getPresetInstance(String preset) {
        LensFlareLayer lensFlareLayer = new LensFlareLayer();
        BufferedImage sun = createDiskImage(64, Color.YELLOW);
        BufferedImage sunDisk = createHaloImage(64, new Color(1f, 1f, .8f), 2f);
        BufferedImage disk = createDiskImage(128, Color.WHITE);
        BufferedImage star = createStarImage(128, Color.WHITE);
        BufferedImage halo = createHaloImage(128, Color.WHITE);
        BufferedImage rainbow = createRainbowImage(128);
        BufferedImage rays = createRaysImage(128, 12, Color.WHITE);

        if (PRESET_BOLD.equals(preset)) {
            // Image, scale, position, opacity
            // Sun dressing - pos = 0
            lensFlareLayer.addRenderable(new FlareImage(rays, 4, 0, .05));
            lensFlareLayer.addRenderable(new FlareImage(star, 1.4, 0, .1));
            lensFlareLayer.addRenderable(new FlareImage(star, 2.5, 0, .04));
            lensFlareLayer.addRenderable(new FlareImage(sunDisk, .6, 0, .9));
            lensFlareLayer.addRenderable(new FlareImage(halo, 1.0, 0, .9));
            lensFlareLayer.addRenderable(new FlareImage(halo, 4, 0, .9));
            lensFlareLayer.addRenderable(new FlareImage(rainbow, 2.2, 0, .03));
            lensFlareLayer.addRenderable(new FlareImage(rainbow, 1.2, 0, .04));
            // Diagonal flares - pos > 0 (center = 1)
            lensFlareLayer.addRenderable(new FlareImage(disk, .1, .4, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .15, .6, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .2, .7, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .5, 1.1, .2));
            lensFlareLayer.addRenderable(new FlareImage(disk, .2, 1.3, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .1, 1.4, .05));
            lensFlareLayer.addRenderable(new FlareImage(disk, .1, 1.5, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .1, 1.6, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .2, 1.65, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, .12, 1.71, .1));
            lensFlareLayer.addRenderable(new FlareImage(disk, 3, 2.2, .05));
            lensFlareLayer.addRenderable(new FlareImage(disk, .5, 2.4, .2));
            lensFlareLayer.addRenderable(new FlareImage(disk, .7, 2.6, .1));
            lensFlareLayer.addRenderable(new FlareImage(rainbow, 5, 3.0, .03));
            lensFlareLayer.addRenderable(new FlareImage(disk, .2, 3.5, .1));
        }

        return lensFlareLayer;
    }

    //*** Static utility methods ***
    public static BufferedImage createDiskImage(int size, Color color) {
        return PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE,
                new Dimension(size, size), .9f, color);
    }

    public static BufferedImage createBluredDiskImage(int size, Color color) {
        BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE,
                new Dimension(size, size), .6f, color);
        image = PatternFactory.blur(image, size / 5);
        image = PatternFactory.blur(image, 10);
        return image;
    }

    public static BufferedImage createStarImage(int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        float half = size / 2f;
        float r1 = half * .9f;
        float r2 = half * .1f;
        float r3 = half * .05f;

        // Four branch star
        GeneralPath path = new GeneralPath();
        path.moveTo(half - r1, half); // left
        path.lineTo(half - r2, half - r3);
        path.lineTo(half - r3, half - r2);
        path.lineTo(half, half - r1); // top
        path.lineTo(half + r3, half - r2);
        path.lineTo(half + r2, half - r3);
        path.lineTo(half + r1, half); // right
        path.lineTo(half + r2, half + r3);
        path.lineTo(half + r3, half + r2);
        path.lineTo(half, half + r1); // bottom
        path.lineTo(half - r3, half + r2);
        path.lineTo(half - r2, half + r3);
        path.lineTo(half - r1, half); // left
        g2.fill(path);

        // Second copy - smaller and rotated 45 deg.
        g2.translate(half, half);
        g2.rotate(Math.PI / 4);
        g2.scale(.7, .7);
        g2.translate(-half, -half);
        g2.fill(path);

        return image;
    }

    public static BufferedImage createRaysImage(int size, int rays, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        float half = size / 2f;
        float r1 = half * .9f;
        float r2 = half * .1f;

        GeneralPath path = new GeneralPath();
        path.moveTo(half, half); // center
        path.lineTo(half - r2, half - r1);
        path.lineTo(half + r2, half - r1);
        path.lineTo(half, half); // center

        Color c2 = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        GradientPaint gradient = new GradientPaint(half, half, color, half, half - r1, c2);
        g2.setPaint(gradient);

        for (int i = 0; i < rays; i++) {
            g2.translate(half, half);
            g2.rotate(Math.PI * 2 / rays);
            g2.translate(-half, -half);
            g2.fill(path);
        }

        return image;
    }

    public static BufferedImage createHaloImage(int size, Color color) {
        return createHaloImage(size, color, .2f);
    }

    public static BufferedImage createHaloImage(int size, Color color, float alphaExp) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(1.5f));
        float[] colorComp = new float[4];
        color.getRGBComponents(colorComp);
        float half = size / 2f;
        float r1 = 0f;
        float r2 = half * .9f;
        for (float r = r1; r <= r2; r++) {
            float alpha = 1f - (float) Math.pow(r / r2, alphaExp);
            g2.setColor(new Color(colorComp[0], colorComp[1], colorComp[2], alpha));
            g2.drawOval((int) (half - r), (int) (half - r), (int) (r * 2), (int) (r * 2));
        }
        return image;
    }

    public static BufferedImage createRainbowImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float half = size / 2f;
        float r1 = half * .7f;
        float r2 = half * .9f;
        for (float r = r1; r <= r2; r++) {
            float hue = (r - r1) / (r2 - r1);
            g2.setColor(new Color(Color.HSBtoRGB(hue, 1, 1)));
            g2.drawOval((int) (half - r), (int) (half - r), (int) (r * 2), (int) (r * 2));
        }
        return image;
    }
}
