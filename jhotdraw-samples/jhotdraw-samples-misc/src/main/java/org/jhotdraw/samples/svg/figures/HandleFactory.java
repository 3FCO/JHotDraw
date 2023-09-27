package org.jhotdraw.samples.svg.figures;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.handle.Handle;

import java.util.Collection;

public interface HandleFactory {

    Collection<Handle> createHandles(int detailLevel);
}
