package com.raven.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesTest {
    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream("oauth.properties")) {
            props.load(input);
            System.out.println("Properties loaded successfully!");
            System.out.println("All properties:");
            props.forEach((key, value) -> System.out.println(key + " = " + value));
            
            System.out.println("\nSpecific values:");
            System.out.println("Client ID: " + props.getProperty("google.oauth.client.id"));
            System.out.println("Client Secret: " + props.getProperty("google.oauth.client.secret"));
            System.out.println("Redirect URI: " + props.getProperty("google.oauth.redirect.uri"));
        } catch (IOException e) {
            System.out.println("Error loading properties: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
