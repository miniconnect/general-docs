package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.sql.Connection;

import jakarta.inject.Singleton;

@Singleton
public class BasicAnimation implements Animation {

    public static final String NAME = "basic";

    public static final String DESCRIPTION = "Basic example animation";

    public static final int ORDER = 1;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public int order() {
        return ORDER;
    }

    @Override
    public String toString() {
        return description();
    }

    @Override
    public FrameRenderer createFrameRenderer(Connection connection, int width, int height) {
        return new FrameRendererImpl(width, height);
    }

    private static class FrameRendererImpl implements FrameRenderer {

        private static final int CIRCLE_DIAMETER = 24;

        private static final double SPEED_PIXELS_PER_SECOND = 62.5d;

        private final int width;

        private final int height;

        private FrameRendererImpl(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(Graphics2D g, double time) {
            int radius = CIRCLE_DIAMETER / 2;
            int centerX = (int) (time * SPEED_PIXELS_PER_SECOND) % Math.max(1, width);
            int centerY = height / 2;
            int x = centerX - radius;
            int y = centerY - radius;

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLUE);
            g.fillOval(x, y, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
        }

        @Override
        public void close() throws IOException {
            // nothing to do
        }

    }

}
