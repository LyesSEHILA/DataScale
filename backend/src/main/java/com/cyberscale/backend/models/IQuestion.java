package com.cyberscale.backend.models;

import java.util.List;
import java.util.Map;
import com.cyberscale.backend.models.AnswerOption;

public interface IQuestion {
    public static enum categorieQuestion{THEORY, TECHNIQUE}
    
    public static enum difficultyQuestion{EASY(3), MEDIUM(6), HARD(9);

        private final int value;

        difficultyQuestion(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    // La question
    String getText();

    // La liste des options
    List<AnswerOption> getOptions();

    // La Map pour correction rapide (ID réponse -> Vrai/Faux)
    Map<Long, Boolean> getAnswerKeyMap(); 

}