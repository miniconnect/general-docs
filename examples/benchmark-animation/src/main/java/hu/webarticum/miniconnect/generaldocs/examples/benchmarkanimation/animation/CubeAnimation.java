package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation;

import java.awt.BasicStroke;
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

        private static final double FACE_VISIBILITY_XY_FACTOR =
                PROJECTION_Y_Z_SCALE / (2.0d * PROJECTION_Y_XY_SCALE);

        private static final Color BACKGROUND_EDGE_COLOR = new Color(100, 120, 150, 120);

        private static final float BACKGROUND_EDGE_STROKE_WIDTH = 6f;

        private static final Color FOREGROUND_EDGE_COLOR = new Color(110, 90, 80, 180);

        private static final float FOREGROUND_EDGE_STROKE_WIDTH = 9f;

        private static final int[][] CUBE_VERTEX_OFFSETS = {
                { -1, -1, -1 },
                { 1, -1, -1 },
                { -1, 1, -1 },
                { 1, 1, -1 },
                { -1, -1, 1 },
                { 1, -1, 1 },
                { -1, 1, 1 },
                { 1, 1, 1 },
        };

        private static final int[][] CUBE_EDGES = {
                { 0, 1 }, { 0, 2 }, { 1, 3 }, { 2, 3 },
                { 4, 5 }, { 4, 6 }, { 5, 7 }, { 6, 7 },
                { 0, 4 }, { 1, 5 }, { 2, 6 }, { 3, 7 },
        };

        private static final int[][] CUBE_FACES = {
                { 0, 4, 6, 2 },
                { 1, 3, 7, 5 },
                { 0, 1, 5, 4 },
                { 2, 6, 7, 3 },
                { 0, 2, 3, 1 },
                { 4, 5, 7, 6 },
        };

        private static final String PARTICLES_SQL = """
                SELECT
                    ROUND(? + ((((
                            (((p.x - ?) * COS(?)) - ((p.y - ?) * SIN(?))) -
                            (((p.x - ?) * SIN(?)) + ((p.y - ?) * COS(?)))
                        ) * %1$s) * ?))) AS center_x,
                    ROUND(? + ((((
                            (((p.x - ?) * COS(?)) - ((p.y - ?) * SIN(?))) +
                            (((p.x - ?) * SIN(?)) + ((p.y - ?) * COS(?)))
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
            Wireframe wireframe = createWireframe(centerX, centerY, zoomScale, rotationAngle);

            drawBackgroundEdges(g, wireframe);
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
                drawParticles(g, resultSet);
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to render cube animation frame", e);
            }
            drawForegroundEdges(g, wireframe);
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

        private void drawBackgroundEdges(Graphics2D g, Wireframe wireframe) {
            drawEdges(g, wireframe, false, BACKGROUND_EDGE_COLOR, BACKGROUND_EDGE_STROKE_WIDTH);
        }

        private void drawParticles(Graphics2D g, ResultSet resultSet) throws SQLException {
            while (resultSet.next()) {
                int radius = resultSet.getInt("radius");
                int diameter = radius * 2;
                int x = resultSet.getInt("center_x") - radius;
                int y = resultSet.getInt("center_y") - radius;
                g.setColor(Color.decode(resultSet.getString("color")));
                g.fillOval(x, y, diameter, diameter);
            }
        }

        private void drawForegroundEdges(Graphics2D g, Wireframe wireframe) {
            drawEdges(g, wireframe, true, FOREGROUND_EDGE_COLOR, FOREGROUND_EDGE_STROKE_WIDTH);
        }

        private void drawEdges(
                Graphics2D g,
                Wireframe wireframe,
                boolean foreground,
                Color color,
                float strokeWidth) {
            java.awt.Stroke originalStroke = g.getStroke();
            Color originalColor = g.getColor();
            g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(color);
            for (ProjectedEdge edge : wireframe.edges()) {
                if (edge.foreground() != foreground) {
                    continue;
                }

                g.drawLine(edge.x1(), edge.y1(), edge.x2(), edge.y2());
            }
            g.setStroke(originalStroke);
            g.setColor(originalColor);
        }

        private Wireframe createWireframe(
                int centerX,
                int centerY,
                double zoomScale,
                double rotationAngle) {
            ProjectedVertex[] vertices = new ProjectedVertex[CUBE_VERTEX_OFFSETS.length];
            double sin = Math.sin(rotationAngle);
            double cos = Math.cos(rotationAngle);
            for (int i = 0; i < CUBE_VERTEX_OFFSETS.length; i++) {
                int[] offset = CUBE_VERTEX_OFFSETS[i];
                double localX = offset[0] * CUBE_HALF_EDGE;
                double localY = offset[1] * CUBE_HALF_EDGE;
                double localZ = offset[2] * CUBE_HALF_EDGE;
                double rotatedX = (localX * cos) - (localY * sin);
                double rotatedY = (localX * sin) + (localY * cos);
                int projectedX = (int) Math.round(centerX + (((rotatedX - rotatedY) * PROJECTION_X_SCALE) * zoomScale));
                int projectedY = (int) Math.round(centerY + ((((rotatedX + rotatedY) * PROJECTION_Y_XY_SCALE) - (localZ * PROJECTION_Y_Z_SCALE)) * zoomScale));
                vertices[i] = new ProjectedVertex(projectedX, projectedY, rotatedX, rotatedY, localZ);
            }

            boolean[] visibleFaces = new boolean[CUBE_FACES.length];
            for (int i = 0; i < CUBE_FACES.length; i++) {
                visibleFaces[i] = isFaceVisible(vertices, CUBE_FACES[i]);
            }

            ProjectedEdge[] edges = new ProjectedEdge[CUBE_EDGES.length];
            for (int i = 0; i < CUBE_EDGES.length; i++) {
                int[] vertexIndexes = CUBE_EDGES[i];
                ProjectedVertex start = vertices[vertexIndexes[0]];
                ProjectedVertex end = vertices[vertexIndexes[1]];
                edges[i] = new ProjectedEdge(
                        start.x(),
                        start.y(),
                        end.x(),
                        end.y(),
                        isForegroundEdge(vertexIndexes, visibleFaces));
            }

            return new Wireframe(edges);
        }

        private boolean isFaceVisible(ProjectedVertex[] vertices, int[] face) {
            ProjectedVertex vertex0 = vertices[face[0]];
            ProjectedVertex vertex1 = vertices[face[1]];
            ProjectedVertex vertex2 = vertices[face[2]];
            double edge1X = vertex1.modelX() - vertex0.modelX();
            double edge1Y = vertex1.modelY() - vertex0.modelY();
            double edge1Z = vertex1.modelZ() - vertex0.modelZ();
            double edge2X = vertex2.modelX() - vertex0.modelX();
            double edge2Y = vertex2.modelY() - vertex0.modelY();
            double edge2Z = vertex2.modelZ() - vertex0.modelZ();
            double normalX = (edge1Y * edge2Z) - (edge1Z * edge2Y);
            double normalY = (edge1Z * edge2X) - (edge1X * edge2Z);
            double normalZ = (edge1X * edge2Y) - (edge1Y * edge2X);
            double visibility =
                    (normalX * FACE_VISIBILITY_XY_FACTOR) +
                    (normalY * FACE_VISIBILITY_XY_FACTOR) +
                    normalZ;
            return visibility > 0.0d;
        }

        private boolean isForegroundEdge(int[] edge, boolean[] visibleFaces) {
            for (int i = 0; i < CUBE_FACES.length; i++) {
                if (visibleFaces[i] && containsEdge(CUBE_FACES[i], edge)) {
                    return true;
                }
            }
            return false;
        }

        private boolean containsEdge(int[] face, int[] edge) {
            for (int i = 0; i < face.length; i++) {
                int vertexA = face[i];
                int vertexB = face[(i + 1) % face.length];
                if ((vertexA == edge[0] && vertexB == edge[1]) || (vertexA == edge[1] && vertexB == edge[0])) {
                    return true;
                }
            }
            return false;
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

        private record ProjectedVertex(
                int x,
                int y,
                double modelX,
                double modelY,
                double modelZ) { }

        private record ProjectedEdge(
                int x1,
                int y1,
                int x2,
                int y2,
                boolean foreground) { }

        private record Wireframe(
                ProjectedEdge[] edges) { }

    }

}
