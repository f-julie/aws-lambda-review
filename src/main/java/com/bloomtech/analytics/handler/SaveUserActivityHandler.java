package com.bloomtech.analytics.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.bloomtech.analytics.db.UserActivity;
import com.bloomtech.analytics.response.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bloomtech.analytics.response.ApiGatewayResponse;
import org.apache.log4j.Logger;
import java.util.Collections;
import java.util.Map;

public class SaveUserActivityHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private final Logger logger = Logger.getLogger(this.getClass());
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

		try {
			logger.info("Event received from client: "+ gson.toJson(input));

			// use it to identify request type from the gateway
			String methodType = (String)input.get("method");

			//get the body from the request
			JsonNode body = new ObjectMapper().convertValue(input.get("body"), JsonNode.class);

			// create the UserActivity object for post
			UserActivity userActivity = new UserActivity();
			userActivity.setEventId(body.get("eventId").asText());
			userActivity.setEventType(body.get("eventType").asText());
			userActivity.setTimestamp(body.get("timestamp").asLong());
			userActivity.setEventValue(body.get("value").asText());
			userActivity.setScreenName(body.get("screen").asText());
			userActivity.setUserId(body.get("userId").asText());
			userActivity.save(userActivity);

			// send the response back
			return ApiGatewayResponse.builder()
					.setStatusCode(200)
					.setObjectBody(userActivity)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda and Bloomtech"))
					.build();

		} catch (Exception ex) {
			logger.error("Error in saving product: " + ex);
			logger.error("Error details: " + ex.getMessage());


			// send the error response back
            // remove the Response class and send back the body
			Response responseBody = new Response("Error in saving product: ", input);
			return ApiGatewayResponse.builder()
					.setStatusCode(500)
					.setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda and Bloomtech"))
					.build();
		}
	}
}
