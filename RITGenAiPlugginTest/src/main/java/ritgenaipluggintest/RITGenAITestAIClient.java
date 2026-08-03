package ritgenaipluggintest;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class RITGenAITestAIClient {

    private final HttpClient client;

    public RITGenAITestAIClient() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String send(String json)
            throws IOException, InterruptedException {

        HttpRequest request =
                HttpRequest.newBuilder()
                .uri(URI.create(RITGenAiPlugginTestPlugin.AIConfig.getEndpoint()))
                .header("Authorization",
                        "Bearer " +
                        RITGenAiPlugginTestPlugin.AIConfig.getApiKey())
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

        return extractMessage(response.body());
    }
    
  //extracts the message from the json response
  		private String extractMessage(String responseBody) {

  		    String marker = "\"content\":\"";

  		    int start = responseBody.indexOf(marker);

  		    if (start == -1) {
  		        return "Unable to parse AI response.";
  		    }

  		    start += marker.length();

  		    StringBuilder message = new StringBuilder();
  		    boolean escaped = false;

  		    for (int i = start; i < responseBody.length(); i++) {

  		        char c = responseBody.charAt(i);

  		        if (escaped) {

  		            switch (c) {
  		                case 'n':
  		                    message.append('\n');
  		                    break;
  		                case 'r':
  		                    break;
  		                case 't':
  		                    message.append('\t');
  		                    break;
  		                case '"':
  		                    message.append('"');
  		                    break;
  		                case '\\':
  		                    message.append('\\');
  		                    break;
  		                default:
  		                    message.append(c);
  		            }

  		            escaped = false;
  		            continue;
  		        }

  		        if (c == '\\') {
  		            escaped = true;
  		            continue;
  		        }

  		        if (c == '"') {
  		            break;
  		        }

  		        message.append(c);
  		    }

  		    return message.toString();
  		}
}