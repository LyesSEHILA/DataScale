package com.cyberscale.backend.models;

import java.util.List;
import java.util.Map;

public interface IQuestion {

    enum CategorieQuestion { THEORY, TECHNIQUE }
    enum DifficultyQuestion { EASY, MEDIUM, HARD }

    String getText();
    List<AnswerOption> getOptions();
    
    CategorieQuestion getCategorie();
    DifficultyQuestion getDifficulty();

    Map<Long, Boolean> getAnswerKeyMap(); 
}