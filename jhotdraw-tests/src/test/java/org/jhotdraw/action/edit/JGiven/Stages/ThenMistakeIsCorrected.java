package org.jhotdraw.action.edit.JGiven.Stages;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.undo.UndoRedoManager;

import java.util.List;

import static org.junit.Assert.*;

public class ThenMistakeIsCorrected extends Stage<ThenMistakeIsCorrected> {

	@ProvidedScenarioState
	Drawing drawing;
	@ProvidedScenarioState
	UndoRedoManager undoRedoManager;

	public ThenMistakeIsCorrected the_mistake_is_corrected() {
		List<Figure> figures = drawing.getFiguresFrontToBack();
		assertTrue(figures.isEmpty());
		return self();
	}
}
