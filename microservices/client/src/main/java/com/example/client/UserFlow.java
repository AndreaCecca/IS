package com.example.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UserFlow {

    private static void request(String method, String targetUrl, String jsonBody) throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setUseCaches(false);
        conn.setDoInput(true);

        if (jsonBody != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] out = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(out.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }
        }

        int code = conn.getResponseCode();
        String message = conn.getResponseMessage();

        System.out.println("=== " + method + " " + targetUrl + " ===");
        System.out.println("Status: " + code + " " + message);
        String body = readBody(conn);
        if (body != null && !body.isBlank()) {
            System.out.println(body);
        }
        System.out.println();

        conn.disconnect();
    }

    private static String readBody(HttpURLConnection conn) {
        InputStream is = null;
        try {
            is = (conn.getResponseCode() >= 400) ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) return "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
                return sb.toString();
            }
        } catch (IOException e) {
            return "";
        }
    }

    public static void main(String[] args) throws Exception {
        String base = "http://localhost:8003";

        // 1) Create user "test"/"pwd"
        String createJson = "{\"username\":\"test\",\"password\":\"pwd\"}";
        request("POST", base + "/users", createJson);

        // 2) Show users
        request("GET", base + "/users", null);

        // 3) Rename to "test1" and change password to "pwd1"
        String updateJson = "{\"newUsername\":\"test1\",\"password\":\"pwd1\"}";
        request("PUT", base + "/users/test", updateJson);

        // 4) Show the updated user
        request("GET", base + "/users/test1", null);
    }
}
