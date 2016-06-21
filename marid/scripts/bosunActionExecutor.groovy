import com.ifountain.opsgenie.client.http.OpsGenieHttpClient
import com.ifountain.opsgenie.client.util.ClientConfiguration
import com.ifountain.opsgenie.client.util.JsonUtils
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpHeaders

import java.text.SimpleDateFormat

LOG_PREFIX = "[${action}]:";
logger.warn("${LOG_PREFIX} Will execute action for alertId ${alert.alertId}");

CONF_PREFIX = "bosun.";
alertFromOpsgenie = opsgenie.getAlert(alertId: alert.alertId)
if (alertFromOpsgenie.size() > 0) {
    def contentMap = [:]
    def urlPath = ""

    boolean discardAction = false;

    def apiUrl = _conf("api_url", false);

    if (!apiUrl) {
        logger.warn("Ignoring action ${action}, because ${CONF_PREFIX}api_url does not exist in conf file, alert: ${alert.message}");
        return;
    }

    HTTP_CLIENT = createHttpClient();
    try {
        if (action == "Acknowledge") {
            urlPath = "/api/action"
            contentMap.put("Type", "ack")
            contentMap.put("User", "OpsGenie")
            contentMap.put("Message", String.valueOf("Acknowledged by ${alert.username} via OpsGenie"))
            contentMap.put("Ids", [ alertFromOpsgenie.alias.toInteger() ])
            contentMap.put("Notify", true)
        }

        if (!discardAction) {
            postToBosunApi(urlPath, contentMap);
        }
    }
    finally {
        HTTP_CLIENT.close()
    }
} else {
    logger.warn("${LOG_PREFIX} Alert with id [${alert.alertId}] does not exist in OpsGenie. It is probably deleted.")
}

def _conf(confKey, boolean isMandatory){
    def confVal = conf[CONF_PREFIX+confKey]
    logger.debug ("confVal ${CONF_PREFIX+confKey} from file is ${confVal}");
    if(isMandatory && confVal == null){
        def errorMessage = "${LOG_PREFIX} Skipping action, Mandatory Conf item ${CONF_PREFIX+confKey} is missing. Check your marid conf file.";
        logger.warn(errorMessage);
        throw new Exception(errorMessage);
    }
    return confVal
}

def createHttpClient() {
    def timeout = _conf("http.timeout", false);
    if(timeout == null){
        timeout = 30000;
    }
    else{
        timeout = timeout.toInteger();
    }

    ClientConfiguration clientConfiguration = new ClientConfiguration().setSocketTimeout(timeout)

    return new OpsGenieHttpClient(clientConfiguration)
}

def postToBosunApi(String urlPath, Map contentMap) {
    String url = _conf("api_url", true) + urlPath;
    String jsonContent = JsonUtils.toJson(contentMap);
    logger.debug("${LOG_PREFIX} Posting to Bosun. Url ${url}, content:${jsonContent} , conmap:${contentMap}")
    def httpPost = ((OpsGenieHttpClient) HTTP_CLIENT).preparePostMethod(url, jsonContent, [(HttpHeaders.ACCEPT): "application/json", (HttpHeaders.CONTENT_TYPE): "application/json"], [:])
    def response = ((OpsGenieHttpClient) HTTP_CLIENT).executeHttpMethod(httpPost)
    if (response.getStatusCode() == 200) {
        logger.info("${LOG_PREFIX} Successfully executed at Bosun.");
        logger.debug("${LOG_PREFIX} Bosun response: ${response.getContentAsString()}")
    } else {
        logger.warn("${LOG_PREFIX} Could not execute at Bosun. Bosun Response:${response.getContentAsString()}")
    }
}
