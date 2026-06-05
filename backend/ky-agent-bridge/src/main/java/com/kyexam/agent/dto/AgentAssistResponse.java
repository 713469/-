package com.kyexam.agent.dto;

import java.util.ArrayList;
import java.util.List;

public class AgentAssistResponse {
    private String answer;
    private List<String> weaknessHints = new ArrayList<String>();
    private String nextAction;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getWeaknessHints() {
        return weaknessHints;
    }

    public void setWeaknessHints(List<String> weaknessHints) {
        this.weaknessHints = weaknessHints;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }
}
