package ac.uk.iisa.claimcheck.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

import java.io.IOException;
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
            ArrayList<String> passages = new ArrayList<>();
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
            //Passage finder

            try {//BM25 initialise
                Analyzer analyzer = new StandardAnalyzer();
                Directory directory = new ByteBuffersDirectory();
                IndexWriterConfig config = new IndexWriterConfig(analyzer);
                config.setSimilarity(new BM25Similarity());
                IndexWriter writer = new IndexWriter(directory,config);

                //Split sources into sentences and add to searchable items
                Document sourcePassages = new Document(sources);
                for (Sentence passage : sourcePassages.sentences()) {
                    org.apache.lucene.document.Document document =
                            new org.apache.lucene.document.Document();
                    document.add(new Field("passage", passage.toString(), TextField.TYPE_STORED));
                    writer.addDocument(document);
                }
                writer.close();

                //set up reader and searcher
                DirectoryReader reader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(reader);
                searcher.setSimilarity(new BM25Similarity());
                QueryParser parser = new QueryParser("passage", analyzer);

                for(int i=0; i<claims.size(); i++) {
                    String relevantPassages = "";

                    String claim = claims.get(i);
                    Query query = parser.parse(QueryParser.escape(claim));
                    TopDocs hits = searcher.search(query, 3);

                    for (ScoreDoc hit : hits.scoreDocs) {
                        org.apache.lucene.document.Document passage =
                                searcher.storedFields().document(hit.doc);

                        relevantPassages+='"'+passage.get("passage")+'"'+", ";
                    }
                    //remove last ", "
                    if(!relevantPassages.isEmpty()) {
                        relevantPassages = relevantPassages.substring(0,relevantPassages.length()-2);
                    }

                    passages.add(relevantPassages);
                }
                reader.close();
                directory.close();

            } catch (IOException | ParseException e) {
                return "Problem retrieving passages";
            }

            //structuring return
            String returnText = """
                                | Claims | Source Passage |
                                | :---: | --- |
                                """;
            for(int i=0;i<claims.size();i++) {
                returnText += "| %s | %s |\n".formatted(claims.get(i),passages.get(i));
            }
            return (returnText);
        }
    }
}