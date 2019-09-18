package com.example.contactsyncdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;

@RestController
public class controller {

	@GetMapping("/aouth")
	private void contactUtility() throws IOException, URISyntaxException {

		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();

		String clientId = "client_id";
		String clientSecret = "client_secret";

		// Or your redirect URL for web based applications.
		String redirectUrl = "http://localhost/mycode";
		List<String> scope = new ArrayList<String>();
		scope.add("https://www.googleapis.com/auth/contacts");
		scope.add("https://www.googleapis.com/auth/user.emails.read");
		scope.add("https://www.googleapis.com/auth/userinfo.email");
		scope.add("https://www.googleapis.com/auth/userinfo.profile");

		List<String> responseType = new ArrayList<String>();

		responseType.add("code");

//		String scope = "https://www.googleapis.com/auth/user.emails.read";

		// Step 1: Authorize -->
		String authorizationUrl = new GoogleBrowserClientRequestUrl(clientId, redirectUrl, scope)
				.setResponseTypes(responseType).build();

		// Point or redirect your user to the authorizationUrl.
		System.out.println("Go to the following link in your browser:");
		System.out.println(authorizationUrl);

		URI url = new URI(authorizationUrl);
		System.out.println(url);
		System.out.println(url.getQuery());
		
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
	    String query = url.getQuery();
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    
	    System.out.println(query_pairs);
	    
		// Read the authorization code from the standard input stream.
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("What is the authorization code?");
		String code = in.readLine();
		// End of Step 1 <--

		// Step 2: Exchange -->
		GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
				clientId, clientSecret, code, redirectUrl).execute();
		// End of Step 2 <--

		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(jsonFactory).setClientSecrets(clientId, clientSecret).build()
				.setFromTokenResponse(tokenResponse);

		PeopleService peopleService = new PeopleService.Builder(httpTransport, jsonFactory, credential).build();

		ListConnectionsResponse response = peopleService.people().connections().list("people/me")
//				.setRequestMaskIncludeField("person.names,person.emailAddresses,person.phoneNumbers").execute();
				.setPersonFields("names,emailAddresses").execute();
		List<Person> connections = response.getConnections();
		connections.forEach(c -> {
			System.out.println(c.getEmailAddresses());
			System.out.println(c.getNames());
			try {
				Person profile = peopleService.people().get(c.getResourceName()).setPersonFields("emailAddresses")
						.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
}
