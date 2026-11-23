# ClaimCheck

ClaimCheck is a Java desktop prototype for reviewing factual claims in an AI-generated answer against source material. It provides an AI-assisted review mode and a separate non-AI mode that extracts claims and retrieves relevant source passages.

## Project status

### Implemented

- A desktop interface built with Java Swing and IntelliJ GUI Designer. Users can paste an answer and source passages, choose whether to use AI, and view the review in the application.
- **AI mode:** sends the answer and supplied sources to Groq and asks the model to break the answer into factual claims, assess support, and include relevant passages. This mode needs a valid API key and an internet connection.
- **Non-AI mode:** uses Stanford CoreNLP OpenIE to extract a claim from each sentence where a triple is found. Apache Lucene indexes the supplied source sentences and uses BM25 to retrieve up to three relevant passages for each claim. This mode does not need an API key.
- Markdown rendering in the review area, including tables.
- The application icon is at `src/main/resources/images/logo.png`.

### Limitations and unfinished work

- The non-AI mode returns retrieved passages; it does not determine whether a claim is supported, contradicted, or only partly supported.
- OpenIE may produce no claim for some sentences and currently keeps only the highest-confidence triple for each sentence.
- Sources must be pasted as text. URL fetching/extraction is not implemented.
- AI assessments depend on the external model and may be slow, unavailable, or incorrect. Treat results as assistance for checking sources, not as a guarantee of truth.
- There is no automated test suite in the repository at present. The prototype has been inspected and tried manually during development; broader testing remains to be done.

## Technology stack

| Technology | Use in this project |
| --- | --- |
| Java 17 | Application language and runtime. |
| Swing and IntelliJ GUI Designer | Desktop interface; the form is `src/main/java/ac/uk/iisa/claimcheck/ui/GUI.form`. |
| Maven | Dependency and build configuration in `pom.xml`. |
| LangChain4j OpenAI integration | Sends chat requests using an OpenAI-compatible API. |
| Groq API, `openai/gpt-oss-20b` | Model endpoint used by the current AI mode (`https://api.groq.com/openai/v1`). |
| Stanford CoreNLP Simple / OpenIE | Extracts subject–relation–object claim triples in non-AI mode. |
| Apache Lucene 9.12.3 | Indexes source sentences and retrieves passages with BM25. |
| Flexmark | Converts Markdown review output, including tables, for display in Swing. |

The current model and provider are set in `ClaimChecker.java`. The AI mode uses Groq's OpenAI-compatible endpoint; the model can be changed there if the provider or available model changes. The non-AI mode is a retrieval prototype, not a separate claim-verification model.

## Setup and running

### Requirements

- IntelliJ IDEA with Java support and GUI Designer.
- JDK 17.
- Internet access for Maven to download dependencies. Internet access is also needed for AI mode.
- A Groq API key only if you want to use AI mode.

### Open and run the project

1. Clone or download this repository.
2. In IntelliJ IDEA, choose **Open** and select the project folder containing `pom.xml`.
3. Accept the prompt to load the project as a Maven project and wait for dependency import to finish. Set the project and Maven JDK to 17 if IntelliJ asks.
4. Open `src/main/java/ac/uk/iisa/claimcheck/Main.java` and run `Main`.
5. To use AI mode, open **Run | Edit Configurations**, select the `Main` run configuration, and add `AI_API_KEY` under **Environment variables**. Keep the key local; do not commit it. The non-AI mode can be run without this variable.
6. Paste the AI response and source passages into the interface, choose the AI option, and click **Check**.

The project uses an IntelliJ `.form` file for its Swing interface. Run it from IntelliJ, which handles the GUI Designer form as part of the IDE build. A plain Maven command-line build may not compile the form without additional GUI Designer build configuration.

## Development record

Significant changes are linked to the repository's commit history:

- [Initial project](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/72cbb25) — created the project.
- [Basic Swing interface](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/0175d1c) — added the first interface to try the idea.
- [UI polish](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/c0bb695) — refined the interface for a clearer, more consistent prototype.
- [Maven and ClaimChecker](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/f14c62d) — added Maven dependencies and separated checking logic into a service class.
- [Groq integration](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/5a9749f) — added the AI-assisted review path.
- [Non-AI path](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/efa4cbe) — began a local claim extraction and retrieval alternative.
- [BM25 retrieval](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/7723640) — indexed source passages and ranked them for extracted claims.
- [Markdown results and passages](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/02650f1) — displayed retrieved passages and rendered Markdown in the review area.
- [Non-AI mode without an API key](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/5069e69) — allowed the local path to run without configuring Groq.
- [Checking-state feedback](https://github.com/SoldierZoom/Intelligent-Interactive-Systems-A2/commit/7afd5e2) — updated the checking flow so the interface can show progress while a review is running.

Development focused first on a usable Swing prototype, then added the AI path, a non-AI comparison path, passage retrieval, and clearer review output. Manual checks during development included running the interface and trying the response review flow. No automated tests are currently included.

## AI assistance

OpenAI Codex (ChatGPT) was used as a development assistant to discuss project structure, explain Java and library concepts, suggest approaches for Groq/LangChain4j, CoreNLP OpenIE and Lucene BM25, and help refine the Swing interface and Markdown output. It was also used to help interpret errors and plan the system diagram and documentation.

Suggestions were treated as guidance: code and dependencies were adapted to the project, and the resulting interface and output were checked manually while developing. AI-generated suggestions were not accepted as proof that the claim-checking results are correct; the limitations above still apply.

## Additional files

None
