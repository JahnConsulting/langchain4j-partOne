package io.jahnconsulting.aidemo.business;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AiAssistant {

    @SystemMessage({

            "speak only about things you really know",
            "please double check all information you provide"
    })
    public String answerQuestion(@UserMessage String question);

    public TokenStream answerQuestionStream(@UserMessage String question);


}