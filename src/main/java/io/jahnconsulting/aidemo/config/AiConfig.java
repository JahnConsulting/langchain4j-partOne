package io.jahnconsulting.aidemo.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.jahnconsulting.aidemo.business.AiAssistant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.time.Duration.ofSeconds;

@Configuration
public class AiConfig {

    private static final String MODEL_NAME = "gpt-4o";
    private final String apiKey;


    public AiConfig(@Value("${OPENAI_API_KEY}") final String apiKey) {
        this.apiKey = apiKey;
    }

    @Bean
    AiAssistant assistantCreator(final StreamingChatLanguageModel streamingModel, final ChatLanguageModel model, final EmbeddingStore<TextSegment> embeddingStore) {

        return AiServices.builder(AiAssistant.class)
                .chatLanguageModel(model)
                .streamingChatLanguageModel(streamingModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(0))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();

    }


    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        int maxResults = 600;
        double minScore = 0.6;

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }


    @Bean
    EmbeddingStore<TextSegment> embeddingStore(final EmbeddingModel embeddingModel) {
        List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively("./resources/story");
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(DocumentSplitters.recursive(100, 20, new OpenAiTokenizer(MODEL_NAME)))

                .build();

        if (!documents.isEmpty()) {
            ingestor.ingest(documents);
        }
        return embeddingStore;

    }


    @Bean
    StreamingChatLanguageModel streamingModel() {

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public ChatLanguageModel model() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .timeout(ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .responseFormat("json_schema")
                .strictJsonSchema(true)
                .build();
    }

}
