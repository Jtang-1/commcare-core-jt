package org.commcare.util.screen;
import datadog.trace.api.Trace;

import com.google.common.collect.Multimap;

import org.commcare.cases.util.StringUtils;
import org.commcare.modern.session.SessionWrapper;
import org.commcare.modern.util.Pair;
import org.commcare.session.CommCareSession;
import org.commcare.session.RemoteQuerySessionManager;
import org.commcare.suite.model.QueryPrompt;
import org.commcare.suite.model.RemoteQueryDatum;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.model.instance.ExternalDataInstanceSource;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.util.NoLocalizedTextException;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import static org.commcare.suite.model.QueryPrompt.INPUT_TYPE_ADDRESS;
import static org.commcare.suite.model.QueryPrompt.INPUT_TYPE_DATERANGE;
import static org.commcare.suite.model.QueryPrompt.INPUT_TYPE_SELECT;
import static org.commcare.suite.model.QueryPrompt.INPUT_TYPE_SELECT1;

/**
 * Screen that displays user configurable entry texts and makes
 * a case query to the server with these fields.
 *
 * @author wspride
 */
public class QueryScreen extends Screen {

    private String queryUrl;

    public interface QueryClient {
        public InputStream makeRequest(Request request);
    }

    private class OkHttpQueryClient implements QueryClient {
        @Override
        public InputStream makeRequest(Request request) {
            try {
                Response response = new okhttp3.OkHttpClient().newCall(request).execute();
                return response.body().byteStream();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private RemoteQuerySessionManager remoteQuerySessionManager;
    protected OrderedHashtable<String, QueryPrompt> userInputDisplays;
    private SessionWrapper sessionWrapper;
    private String[] fields;
    private String mTitle;
    private String currentMessage;

    private String domainedUsername;
    private String password;

    private PrintStream out;

    private boolean defaultSearch;
    private QueryClient client = new OkHttpQueryClient();

    public QueryScreen(String domainedUsername, String password, PrintStream out) {
        this.domainedUsername = domainedUsername;
        this.password = password;
        this.out = out;
    }

    @Override
    public void init(SessionWrapper sessionWrapper) throws CommCareSessionException {
        this.sessionWrapper = sessionWrapper;
        remoteQuerySessionManager =
                RemoteQuerySessionManager.buildQuerySessionManager(sessionWrapper,
                        sessionWrapper.getEvaluationContext(), getSupportedPrompts());

        if (remoteQuerySessionManager == null) {
            throw new CommCareSessionException(String.format("QueryManager for case " +
                    "claim screen with id %s cannot be null.", sessionWrapper.getNeededData()));
        }
        userInputDisplays = remoteQuerySessionManager.getNeededUserInputDisplays();

        int count = 0;
        fields = new String[userInputDisplays.keySet().size()];
        for (Map.Entry<String, QueryPrompt> queryPromptEntry : userInputDisplays.entrySet()) {
            fields[count] = queryPromptEntry.getValue().getDisplay().getText().evaluate(sessionWrapper.getEvaluationContext());
        }

        try {
            mTitle = Localization.get("case.search.title");
        } catch (NoLocalizedTextException nlte) {
            mTitle = "Case Claim";
        }
    }

    public void setClient(QueryClient client) {
        this.client = client;
    }

    // Formplayer List of Supported prompts
    private ArrayList<String> getSupportedPrompts() {
        ArrayList<String> supportedPrompts = new ArrayList<>();
        supportedPrompts.add(INPUT_TYPE_SELECT1);
        supportedPrompts.add(INPUT_TYPE_SELECT);
        supportedPrompts.add(INPUT_TYPE_DATERANGE);
        supportedPrompts.add(INPUT_TYPE_ADDRESS);
        return supportedPrompts;
    }

    /**
     * @param skipDefaultPromptValues don't apply the default value expressions for query prompts
     * @return case search url with search prompt values
     */
    private String buildUrl(boolean skipDefaultPromptValues) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(getBaseUrl().toString()).newBuilder();
        Multimap<String, String> queryParams = getQueryParams(skipDefaultPromptValues);
        for (String key : queryParams.keySet()) {
            QueryPrompt prompt = userInputDisplays.get(key);
            for (String value : queryParams.get(key)) {
                if (prompt != null && prompt.isSelect()) {
                    String[] selectedChoices = RemoteQuerySessionManager.extractMultipleChoices(value);
                    for (String selectedChoice : selectedChoices) {
                        urlBuilder.addQueryParameter(key, selectedChoice);
                    }
                } else {
                    urlBuilder.addQueryParameter(key, value);
                }
            }
        }
        return urlBuilder.build().toString();
    }


    private InputStream makeQueryRequestReturnStream() {
        queryUrl = buildUrl(false);
        String credential = Credentials.basic(domainedUsername, password);

        Request request = new Request.Builder()
                .url(queryUrl)
                .header("Authorization", credential)
                .build();
        return client.makeRequest(request);
    }

    public Pair<ExternalDataInstance, String> processResponse(InputStream responseData, String url) {
        if (responseData == null) {
            currentMessage = "Query result null.";
            return new Pair<>(null, currentMessage);
        }
        Pair<ExternalDataInstance, String> instanceOrError;
        try {
            String instanceID = getQueryDatum().getDataId();
            TreeElement root = ExternalDataInstance.parseExternalTree(responseData, instanceID);
            ExternalDataInstanceSource instanceSource = new ExternalDataInstanceSource(instanceID, root, url, getQueryDatum().useCaseTemplate());
            ExternalDataInstance instance = ExternalDataInstance.buildFromRemote(getQueryDatum().getDataId(), instanceSource);
            instanceOrError = new Pair<>(instance, "");
        } catch (InvalidStructureException | IOException
                | XmlPullParserException | UnfullfilledRequirementsException e) {
            instanceOrError = new Pair<>(null, e.getMessage());
        }

        if (instanceOrError.first == null) {
            currentMessage = "Query response format error: " + instanceOrError.second;
        }
        return instanceOrError;
    }

    public void setQueryDatum(ExternalDataInstance dataInstance) {
        if (dataInstance != null) {
            sessionWrapper.setQueryDatum(dataInstance);
        }
    }

    public ExternalDataInstance buildExternalDataInstance(TreeElement root, String url) {
        return remoteQuerySessionManager.buildExternalDataInstance(root, url);
    }

    public void answerPrompts(Hashtable<String, String> answers) {
        for (Enumeration en = userInputDisplays.keys(); en.hasMoreElements(); ) {
            String key = (String)en.nextElement();
            QueryPrompt queryPrompt = userInputDisplays.get(key);
            String answer = answers.get(key);

            // If select question, we should have got an index as the answer which should
            // be converted to the corresponding value
            if (queryPrompt.isSelect() && !StringUtils.isEmpty(answer)) {
                Vector<SelectChoice> selectChoices = queryPrompt.getItemsetBinding().getChoices();
                String[] indicesOfSelectedChoices = RemoteQuerySessionManager.extractMultipleChoices(answer);
                ArrayList<String> selectedChoices = new ArrayList<>(indicesOfSelectedChoices.length);
                for (int i = 0; i < indicesOfSelectedChoices.length; i++) {
                    if (indicesOfSelectedChoices[i].isEmpty()) {
                        selectedChoices.add("");
                    } else {
                        int choiceIndex = Integer.parseInt(indicesOfSelectedChoices[i]);
                        if (choiceIndex < selectChoices.size() && choiceIndex > -1) {
                            selectedChoices.add(selectChoices.get(choiceIndex).getValue());
                        }
                    }
                }
                answer = String.join(RemoteQuerySessionManager.ANSWER_DELIMITER, selectedChoices);
            }
            remoteQuerySessionManager.answerUserPrompt(key, answer);
        }
    }

    public void refreshItemSetChoices() {
        remoteQuerySessionManager.refreshItemSetChoices(remoteQuerySessionManager.getUserAnswers());
    }

    protected URL getBaseUrl() {
        return remoteQuerySessionManager.getBaseUrl();
    }

    /**
     * @param skipDefaultPromptValues don't apply the default value expressions for query prompts
     * @return filters to be applied to case search uri as query params
     */
    protected Multimap<String, String> getQueryParams(boolean skipDefaultPromptValues) {
        return remoteQuerySessionManager.getRawQueryParams(skipDefaultPromptValues);
    }

    public String getScreenTitle() {
        return mTitle;
    }

    @Override
    public boolean prompt(PrintStream out) {
        if (doDefaultSearch()) {
            return false;
        }
        out.println("Enter the search fields as a comma-separated list.");
        for (int i = 0; i < fields.length; i++) {
            out.println(i + ") " + fields[i]);
        }
        return true;
    }

    @Override
    public String[] getOptions() {
        return fields;
    }

    @Trace
    @Override
    public boolean handleInputAndUpdateSession(CommCareSession session, String input, boolean allowAutoLaunch) {
        String[] answers = input.split(",");
        Hashtable<String, String> userAnswers = new Hashtable<>();
        int count = 0;
        for (Map.Entry<String, QueryPrompt> queryPromptEntry : userInputDisplays.entrySet()) {
            userAnswers.put(queryPromptEntry.getKey(), answers[count]);
            count++;
        }
        answerPrompts(userAnswers);
        InputStream response = makeQueryRequestReturnStream();
        Pair<ExternalDataInstance, String> instanceOrError = processResponse(response, queryUrl);
        setQueryDatum(instanceOrError.first);
        if (currentMessage != null) {
            out.println(currentMessage);
        }
        return instanceOrError.first != null;
    }


    public OrderedHashtable<String, QueryPrompt> getUserInputDisplays() {
        return userInputDisplays;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public Hashtable<String, String> getCurrentAnswers() {
        return remoteQuerySessionManager.getUserAnswers();
    }

    public boolean doDefaultSearch() {
        return remoteQuerySessionManager.doDefaultSearch();
    }

    public RemoteQueryDatum getQueryDatum() {
        return remoteQuerySessionManager.getQueryDatum();
    }
}
