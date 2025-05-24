package com.meialuaquadrado.nexusai.models.AiDTOs;

import java.util.List;

public class AiResponse {
    public List<Choice> choices;

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public String getFirstAnswer() {
        return choices.get(0).getMessage().getContent();
    }

}


class Choice {
    private MessageDto message;

    public MessageDto getMessage() {
        return message;
    }

    public void setMessage(MessageDto message) {
        this.message = message;
    }
}