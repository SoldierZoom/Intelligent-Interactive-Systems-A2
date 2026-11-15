package ac.uk.iisa.claimcheck.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import java.util.ArrayList;
import java.util.Comparator;

public class ClaimChecker {
    private final ChatModel model;

    public ClaimChecker() {
        //gets AI_API_KEY from environment variables and throws an error if it has no value or doesn't exist
        String key = System.getenv("AI_API_KEY");
        if (key==null || key.isBlank()) {throw new IllegalStateException("Set AI_API_KEY first.");}
        //initialise groq model
        model = OpenAiChatModel.builder().baseUrl("https://api.groq.com/openai/v1")
                .apiKey(key).modelName("openai/gpt-oss-20b").build();
    }

    public String check(String response, String sources,boolean useAI) {
        if(useAI) {
            //sends prompt to ai with users inputted response and sources
            String prompt = """
                Break the AI response into individual factual claims.
                For each claim, use only the supplied source passages.
                Give a status: supported, partly supported, contradicted,
                or not enough evidence. Include the relevant passage.
                Do not invent evidence.
                Display in .md format as a table with headings: Claim, Support Label, Source Passage.

                AI response:
                %s

                Source passages:
                %s""".formatted(response,sources);
            return model.chat(prompt);
        } else {
            ArrayList<String> claims = new ArrayList<>();
            Document doc = new Document(response);

            //loops for how many sentences in document
            for(Sentence sentence : doc.sentences()) {
                //takes most confident triple from the sentence
                sentence.openieTriples().stream()
                        .max(Comparator.comparingDouble(triple -> triple.confidence))
                        .ifPresent(triple ->//if there adds it to claims
                                claims.add(triple.subjectLemmaGloss()+" "+
                                        triple.relationLemmaGloss()+" "+
                                        triple.objectLemmaGloss()));
            }

            return (claims.toString());
        }
    }
}