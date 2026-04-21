package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation;

import java.awt.Graphics2D;
import java.io.Closeable;
import java.io.IOException;

public interface FrameRenderer extends Closeable {

    public void render(Graphics2D g, double time);

}
