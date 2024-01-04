package org.jhotdraw.samples.svg.undo.Stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.samples.svg.figures.SVGRectFigure;
import org.jhotdraw.undo.UndoRedoManager;

import static org.junit.Assert.assertNotNull;

public class WhenUserUndoes extends Stage<WhenUserUndoes> {

    @ProvidedScenarioState
    Drawing drawing;
    @ProvidedScenarioState
    UndoRedoManager undoRedoManager;
    @ProvidedScenarioState
    SVGRectFigure rect;


    public WhenUserUndoes the_user_undoes_the_mistake() {
        assertNotNull(drawing);
        undoRedoManager.undo();
        return self();
    }
}
