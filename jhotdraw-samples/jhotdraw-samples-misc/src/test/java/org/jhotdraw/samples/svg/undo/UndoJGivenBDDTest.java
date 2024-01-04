package org.jhotdraw.samples.svg.undo;

import com.tngtech.jgiven.junit.ScenarioTest;
import org.jhotdraw.samples.svg.undo.Stages.GivenUserAction;
import org.jhotdraw.samples.svg.undo.Stages.ThenMistakeIsCorrected;
import org.jhotdraw.samples.svg.undo.Stages.WhenUserUndoes;
import org.junit.Test;

public class UndoJGivenBDDTest extends ScenarioTest<GivenUserAction, WhenUserUndoes, ThenMistakeIsCorrected> {

    @Test
    public void user_undoes_mistake() {
        given().the_user_has_made_a_mistake();
        when().the_user_undoes_the_mistake();
        then().the_mistake_is_corrected();
    }
}
