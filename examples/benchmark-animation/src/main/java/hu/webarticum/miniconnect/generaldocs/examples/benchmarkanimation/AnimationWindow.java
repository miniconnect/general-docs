package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation.Animation;
import hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation.FrameRenderer;

public class AnimationWindow extends JFrame {

    private final Connection connection;

    private AnimationWindow(Connection connection, Animation animation) {
        super(animation.description());
        this.connection = connection;

        AnimationCanvas canvas = new AnimationCanvas(connection, animation);
        ContainPanel containPanel = new ContainPanel(canvas);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(containPanel);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                canvas.start();
            }

            @Override
            public void windowClosed(WindowEvent event) {
                canvas.stop();
                closeConnection();
            }

        });
    }

    static void open(Connection connection, Animation animation) {
        AnimationWindow frame = new AnimationWindow(connection, animation);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to close connection", e);
        }
    }

    private static class ContainPanel extends JPanel {

        private static final int DEFAULT_EDGE_LENGTH = 700;

        private final AnimationCanvas canvas;

        ContainPanel(AnimationCanvas canvas) {
            this.canvas = canvas;
            setBackground(Color.BLACK);
            setLayout(null);
            add(canvas);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DEFAULT_EDGE_LENGTH, DEFAULT_EDGE_LENGTH);
        }

        @Override
        public void doLayout() {
            int edgeLength = Math.min(getWidth(), getHeight());
            int x = (getWidth() - edgeLength) / 2;
            int y = (getHeight() - edgeLength) / 2;
            canvas.setBounds(x, y, edgeLength, edgeLength);
        }
    }

    private static class AnimationCanvas extends JPanel {

        private static final int FRAME_SAMPLE_COUNT = 50;
        private static final int LOGICAL_EDGE_LENGTH = 1000;
        private static final int FPS_PADDING = 12;
        private static final int FPS_FONT_SIZE = 17;
        private static final Font FPS_FONT = new Font(Font.SANS_SERIF, Font.BOLD, FPS_FONT_SIZE);

        private final long[] frameTimes = new long[FRAME_SAMPLE_COUNT];
        private final AtomicBoolean repaintPending = new AtomicBoolean(false);
        private final Connection connection;
        private final Animation animation;
        private final int logicalWidth;
        private final int logicalHeight;
        private volatile FrameRenderer frameRenderer = null;
        private int frameIndex = 0;
        private int frameCount = 0;
        private volatile boolean fpsVisible = true;
        private volatile double fps = 0.0d;
        private volatile BufferedImage currentFrameImage = null;
        private volatile boolean running = false;
        private volatile Thread renderThread = null;

        AnimationCanvas(Connection connection, Animation animation) {
            this.connection = connection;
            this.animation = animation;
            this.logicalWidth = LOGICAL_EDGE_LENGTH;
            this.logicalHeight = LOGICAL_EDGE_LENGTH;
            setDoubleBuffered(true);
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    toggleFpsVisibility();
                }
            });
        }

        void start() {
            if (running) {
                return;
            }

            frameRenderer = animation.createFrameRenderer(connection, logicalWidth, logicalHeight);
            frameIndex = 0;
            frameCount = 0;
            fps = 0.0d;
            currentFrameImage = null;
            running = true;
            Thread thread = new Thread(this::runRenderLoop, "animation-renderer");
            thread.setDaemon(true);
            renderThread = thread;
            thread.start();
        }

        void stop() {
            running = false;
            Thread thread = renderThread;
            renderThread = null;
            if (thread != null) {
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            closeFrameRenderer();
            currentFrameImage = null;
        }

        private void runRenderLoop() {
            while (running) {
                nextFrame();
            }
        }

        private void nextFrame() {
            long now = System.nanoTime();
            frameTimes[frameIndex] = now;
            frameIndex = (frameIndex + 1) % FRAME_SAMPLE_COUNT;
            frameCount = Math.min(frameCount + 1, FRAME_SAMPLE_COUNT);
            updateFps();
            renderFrameImage(now);
            requestPresentation();
        }

        private void updateFps() {
            if (frameCount < 2) {
                fps = 0.0d;
                return;
            }

            int newestIndex = (frameIndex + FRAME_SAMPLE_COUNT - 1) % FRAME_SAMPLE_COUNT;
            int oldestIndex = frameCount == FRAME_SAMPLE_COUNT ? frameIndex : 0;
            long elapsedNanos = frameTimes[newestIndex] - frameTimes[oldestIndex];
            if (elapsedNanos <= 0L) {
                fps = 0.0d;
                return;
            }

            fps = (frameCount - 1) * 1_000_000_000d / elapsedNanos;
        }

        private void renderFrameImage(long now) {
            FrameRenderer localFrameRenderer = frameRenderer;
            if (localFrameRenderer == null) {
                return;
            }

            int targetWidth = Math.max(1, getWidth());
            int targetHeight = Math.max(1, getHeight());
            BufferedImage frameImage =
                    new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = frameImage.createGraphics();
            try {
                graphics.setColor(getBackground());
                graphics.fillRect(0, 0, targetWidth, targetHeight);
                graphics.scale(
                        targetWidth / (double) logicalWidth,
                        targetHeight / (double) logicalHeight);
                double timeSeconds = now / 1_000_000_000d;
                localFrameRenderer.render(graphics, timeSeconds);
                if (fpsVisible) {
                    drawFps(graphics, fps);
                }
            } finally {
                graphics.dispose();
            }

            currentFrameImage = frameImage;
        }

        private void requestPresentation() {
            if (!repaintPending.compareAndSet(false, true)) {
                return;
            }

            SwingUtilities.invokeLater(() -> {
                repaintPending.set(false);
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            BufferedImage frameImage = currentFrameImage;
            if (frameImage != null) {
                graphics.drawImage(frameImage, 0, 0, null);
            }
        }

        private void toggleFpsVisibility() {
            fpsVisible = !fpsVisible;
        }

        private void drawFps(Graphics2D graphics, double fps) {
            String text = String.format(Locale.US, "FPS: %.1f", fps);
            graphics.setFont(FPS_FONT);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            int x = FPS_PADDING;
            int y = FPS_PADDING + fontMetrics.getAscent();
            int rectIncrement = 2 * FPS_PADDING;
            int width = fontMetrics.stringWidth(text) + rectIncrement;
            int height = fontMetrics.getHeight() + rectIncrement;

            graphics.setColor(new Color(0, 0, 0, 128));
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.WHITE);
            graphics.drawString(text, x, y);
        }

        private void closeFrameRenderer() {
            FrameRenderer localFrameRenderer = frameRenderer;
            frameRenderer = null;
            if (localFrameRenderer == null) {
                return;
            }

            try {
                localFrameRenderer.close();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to close frame renderer", e);
            }
        }

    }

}
