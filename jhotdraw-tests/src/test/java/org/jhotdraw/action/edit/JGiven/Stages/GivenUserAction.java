package org.jhotdraw.action.edit.JGiven.Stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jhotdraw.api.app.Application;
import org.jhotdraw.draw.*;
import org.jhotdraw.samples.svg.figures.SVGRectFigure;
import org.jhotdraw.samples.teddy.TeddyView;
import org.jhotdraw.undo.UndoRedoManager;

public class GivenUserAction extends Stage<GivenUserAction> {

	@ProvidedScenarioState
	Drawing drawing;
	@ProvidedScenarioState
	UndoRedoManager undoRedoManager;
	@ProvidedScenarioState
	SVGRectFigure rect;

	public GivenUserAction the_user_has_made_a_mistake() {
		drawing = new DefaultDrawing();
		undoRedoManager = new UndoRedoManager();
		drawing.addUndoableEditListener(undoRedoManager);
		rect = new SVGRectFigure(10, 10, 50, 100);
		drawing.add(rect);
		return self();
	}
}
