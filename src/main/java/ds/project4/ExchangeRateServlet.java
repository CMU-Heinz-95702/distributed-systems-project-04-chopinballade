package ds.project4;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet implementation class ExchangeRateServlet
 * Handles requests to fetch exchange rates from the ExchangeRate-API
 */
@WebServlet("/getExchangeRate")
public class ExchangeRateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Replace with your actual API key
    private static final String API_KEY = "b5ca40ce39f488fc2ce3fac5";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/";

    private OkHttpClient httpClient;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        httpClient = new OkHttpClient();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Set response content type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Retrieve request parameters
        String baseCurrency = request.getParameter("baseCurrency");
        String targetCurrency = request.getParameter("targetCurrency");

        // Basic input validation
        if (baseCurrency == null || targetCurrency == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Missing parameters. Please provide baseCurrency and targetCurrency.");
            out.print(gson.toJson(errorResponse));
            out.flush();
            return;
        }

        // Construct the ExchangeRate-API request URL
        String apiRequestUrl = API_URL + baseCurrency.toUpperCase();

        // Build the HTTP request to ExchangeRate-API
        Request apiRequest = new Request.Builder()
                .url(apiRequestUrl)
                .build();

        try (Response apiResponse = httpClient.newCall(apiRequest).execute()) {
            if (!apiResponse.isSuccessful()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter out = response.getWriter();
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Failed to fetch data from ExchangeRate-API.");
                out.print(gson.toJson(errorResponse));
                out.flush();
                return;
            }

            // Parse the JSON response from ExchangeRate-API
            String responseBody = apiResponse.body().string();
            JsonObject apiData = JsonParser.parseString(responseBody).getAsJsonObject();

            // Check if the API returned success
            if (!"success".equalsIgnoreCase(apiData.get("result").getAsString())) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter out = response.getWriter();
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "ExchangeRate-API returned an error.");
                out.print(gson.toJson(errorResponse));
                out.flush();
                return;
            }

            // Extract the conversion rates
            JsonObject conversionRates = apiData.getAsJsonObject("conversion_rates");
            if (!conversionRates.has(targetCurrency.toUpperCase())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter out = response.getWriter();
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Invalid targetCurrency provided.");
                out.print(gson.toJson(errorResponse));
                out.flush();
                return;
            }

            double rate = conversionRates.get(targetCurrency.toUpperCase()).getAsDouble();

            // Create the response JSON
            JsonObject exchangeRateResponse = new JsonObject();
            exchangeRateResponse.addProperty("result", "success");
            exchangeRateResponse.addProperty("baseCurrency", baseCurrency.toUpperCase());
            exchangeRateResponse.addProperty("targetCurrency", targetCurrency.toUpperCase());
            exchangeRateResponse.addProperty("rate", rate);

            // Send the response back to the client (Android app)
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(exchangeRateResponse));
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "An error occurred while processing the request.");
            out.print(gson.toJson(errorResponse));
            out.flush();
        }
    }
}
