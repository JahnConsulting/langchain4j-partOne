package io.jahnconsulting.aidemo.controller;


import dev.langchain4j.service.TokenStream;
import io.jahnconsulting.aidemo.business.AiAssistant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai-controller")
public class AiController {

    private static final String QUESTION = "Erzähle mir von Elina aus Ravenville in vier sätzen auf deutsch";
    private final AiAssistant aiAssistant;

    public AiController(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }


    @GetMapping
    public ResponseEntity<String> chatGpt() {
        return ResponseEntity.ok(aiAssistant.answerQuestion(QUESTION));
    }

    @GetMapping("/stream")
    public ResponseEntity<String> chatGptStream() {
        TokenStream ts = aiAssistant.answerQuestionStream(QUESTION);

        ts.onNext(System.out::println)
                .onRetrieved(System.out::println)
                // .onToolExecuted((ToolExecution toolExecution) -> System.out.println(toolExecution))
                //  .onNext((String text) ->System.out.println(text))
                .onComplete(System.out::println)
                .onError(Throwable::printStackTrace)
                .start();

        return ResponseEntity.ok("");
    }
}
