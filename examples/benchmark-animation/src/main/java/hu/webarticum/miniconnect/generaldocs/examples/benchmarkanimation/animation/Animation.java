package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation;

import java.sql.Connection;

public interface Animation {

    public abstract String name();

    public abstract String description();

    public abstract int order();

    public abstract FrameRenderer createFrameRenderer(Connection connection, int width, int height);

}
