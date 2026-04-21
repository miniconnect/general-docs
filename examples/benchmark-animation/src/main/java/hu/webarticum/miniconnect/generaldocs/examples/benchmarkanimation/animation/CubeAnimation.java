package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.inject.Singleton;

@Singleton
public class CubeAnimation implements Animation {

    public static final String NAME = "cube";

    public static final String DESCRIPTION = "Cube animation";

    public static final int ORDER = 2;

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
        return new FrameRendererImpl(connection, width, height);
    }

    private static class FrameRendererImpl implements FrameRenderer {

        private static final int CUBE_HALF_EDGE = 100;

        private static final double PROJECTION_X_SCALE = 0.72d;

        private static final double PROJECTION_Y_XY_SCALE = 0.30d;

        private static final double PROJECTION_Y_Z_SCALE = 0.82d;

        private static final double BASE_ZOOM_SCALE = 1.75d;

        private static final double ZOOM_PULSE_AMPLITUDE = 0.12d;

        private static final double ZOOM_PULSE_FREQUENCY = 1.7d;

        private static final double CUBE_CENTER_PULSE_AMPLITUDE = 300.0d;

        private static final double CUBE_CENTER_X_FREQUENCY = 0.23d;

        private static final double CUBE_CENTER_Y_FREQUENCY = 0.31d;

        private static final double CUBE_CENTER_Z_FREQUENCY = 0.17d;

        private static final double ROTATION_SPEED = 0.65d;

        private static final String PARTICLES_SQL = """
                SELECT
                    ROUND(? + ((((
                            (((p.x - ?) * COS(?)) - ((p.y - ?) * SIN(?)))
                            - (((p.x - ?) * SIN(?)) + ((p.y - ?) * COS(?)))
                        ) * %1$s) * ?))) AS center_x,
                    ROUND(? + ((((
                            (((p.x - ?) * COS(?)) - ((p.y - ?) * SIN(?)))
                            + (((p.x - ?) * SIN(?)) + ((p.y - ?) * COS(?)))
                        ) * %2$s) - ((p.z - ?) * %3$s)) * ?)) AS center_y,
                    ROUND(CASE
                            WHEN t.size = 'SMALL' THEN 3
                            WHEN t.size = 'NORMAL' THEN 5
                            WHEN t.size = 'BIG' THEN 7
                            ELSE 5
                        END * ?) AS radius,
                    t.color AS color
                FROM simulation.particles p
                LEFT JOIN simulation.type t ON p.type_id = t.id
                WHERE p.x BETWEEN ? AND ?
                  AND p.y BETWEEN ? AND ?
                  AND p.z BETWEEN ? AND ?
                ORDER BY p.z ASC
                """.formatted(
                        Double.toString(PROJECTION_X_SCALE),
                        Double.toString(PROJECTION_Y_XY_SCALE),
                        Double.toString(PROJECTION_Y_Z_SCALE));

        private final Connection connection;

        private final int centerX;

        private final int centerY;

        private PreparedStatement statement = null;

        private FrameRendererImpl(Connection connection, int width, int height) {
            this.connection = connection;
            this.centerX = width / 2;
            this.centerY = height / 2;
        }

        @Override
        public void render(Graphics2D g, double time) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double zoomScale = BASE_ZOOM_SCALE + (ZOOM_PULSE_AMPLITUDE * Math.sin(time * ZOOM_PULSE_FREQUENCY));
            double cubeCenterX = CUBE_CENTER_PULSE_AMPLITUDE * Math.sin(time * CUBE_CENTER_X_FREQUENCY);
            double cubeCenterY = CUBE_CENTER_PULSE_AMPLITUDE * Math.sin(time * CUBE_CENTER_Y_FREQUENCY);
            double cubeCenterZ = CUBE_CENTER_PULSE_AMPLITUDE * Math.sin(time * CUBE_CENTER_Z_FREQUENCY);
            double rotationAngle = time * ROTATION_SPEED;
            int minX = (int) Math.round(cubeCenterX - CUBE_HALF_EDGE);
            int maxX = (int) Math.round(cubeCenterX + CUBE_HALF_EDGE);
            int minY = (int) Math.round(cubeCenterY - CUBE_HALF_EDGE);
            int maxY = (int) Math.round(cubeCenterY + CUBE_HALF_EDGE);
            int minZ = (int) Math.round(cubeCenterZ - CUBE_HALF_EDGE);
            int maxZ = (int) Math.round(cubeCenterZ + CUBE_HALF_EDGE);
            try (ResultSet resultSet = executeQuery(
                    centerX,
                    centerY,
                    zoomScale,
                    cubeCenterX,
                    cubeCenterY,
                    cubeCenterZ,
                    rotationAngle,
                    minX,
                    maxX,
                    minY,
                    maxY,
                    minZ,
                    maxZ)) {
                while (resultSet.next()) {
                    int radius = resultSet.getInt("radius");
                    int diameter = radius * 2;
                    int x = resultSet.getInt("center_x") - radius;
                    int y = resultSet.getInt("center_y") - radius;
                    g.setColor(Color.decode(resultSet.getString("color")));
                    g.fillOval(x, y, diameter, diameter);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to render cube animation frame", e);
            }
        }

        @Override
        public void close() throws IOException {
            PreparedStatement localStatement = statement;
            statement = null;
            if (localStatement == null) {
                return;
            }

            try {
                localStatement.close();
            } catch (SQLException e) {
                throw new IOException("Failed to close cube animation statement", e);
            }
        }

        private ResultSet executeQuery(
                int centerX,
                int centerY,
                double zoomScale,
                double cubeCenterX,
                double cubeCenterY,
                double cubeCenterZ,
                double rotationAngle,
                int minX,
                int maxX,
                int minY,
                int maxY,
                int minZ,
                int maxZ) throws SQLException {
            PreparedStatement statement = requireStatement();
            statement.setInt(1, centerX);
            statement.setDouble(2, cubeCenterX);
            statement.setDouble(3, rotationAngle);
            statement.setDouble(4, cubeCenterY);
            statement.setDouble(5, rotationAngle);
            statement.setDouble(6, cubeCenterX);
            statement.setDouble(7, rotationAngle);
            statement.setDouble(8, cubeCenterY);
            statement.setDouble(9, rotationAngle);
            statement.setDouble(10, zoomScale);
            statement.setInt(11, centerY);
            statement.setDouble(12, cubeCenterX);
            statement.setDouble(13, rotationAngle);
            statement.setDouble(14, cubeCenterY);
            statement.setDouble(15, rotationAngle);
            statement.setDouble(16, cubeCenterX);
            statement.setDouble(17, rotationAngle);
            statement.setDouble(18, cubeCenterY);
            statement.setDouble(19, rotationAngle);
            statement.setDouble(20, cubeCenterZ);
            statement.setDouble(21, zoomScale);
            statement.setDouble(22, zoomScale);
            statement.setInt(23, minX);
            statement.setInt(24, maxX);
            statement.setInt(25, minY);
            statement.setInt(26, maxY);
            statement.setInt(27, minZ);
            statement.setInt(28, maxZ);
            return statement.executeQuery();
        }

        private PreparedStatement requireStatement() throws SQLException {
            PreparedStatement localStatement = statement;
            if (localStatement == null) {
                localStatement = connection.prepareStatement(PARTICLES_SQL);
                statement = localStatement;
            }
            return localStatement;
        }

    }

}
