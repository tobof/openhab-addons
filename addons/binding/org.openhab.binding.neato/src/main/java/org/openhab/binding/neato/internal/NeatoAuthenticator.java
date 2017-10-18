package org.openhab.binding.neato.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Properties;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.neato.internal.classes.BeehiveAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class NeatoAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(NeatoAuthenticator.class);
    
    private String accessToken;
    
    private SecureRandom random = new SecureRandom();
    
    private String username;
    private String password;
    
    public NeatoAuthenticator(String email, String password) {
        this.username = email;
        this.password = password;
        logger.debug(username);
    }
    
    public String sendAuthRequestToNeato(String data) {

        Properties headers = new Properties();
        headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

        if (this.accessToken != null) {
            headers.setProperty("Token token", this.accessToken);
        }

        String resultString = "";

        try {

            InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

            resultString = HttpUtil.executeUrl("POST", "https://beehive.neatocloud.com/sessions", headers, stream,
                    "application/json", 20000);

            logger.info(resultString);

        } catch (UnsupportedEncodingException e) {
            logger.error("Error when sending Authentication request to Neato. Error: {}", e.getMessage());

        } catch (IOException e) {
            logger.error("Error when sending Authentication request to Neato. Error: {}", e.getMessage());

        }
        return resultString;
    }

    public Boolean authenticate() {

        String authenticationString;
        try {
            authenticationString = "{\"email\": \"" + username + "\", \"password\": \"" + password
                    + "\", \"os\": \"ios\", \"token\": \"" + new BigInteger(130, random).toString(64).getBytes("UTF-8")
                    + "\"}";
        } catch (UnsupportedEncodingException e) {
            logger.error("Error when during Authentication procedure. Error: {}", e.getMessage());

            return false;
        }

        String authenticationResponse = sendAuthRequestToNeato(authenticationString);

        logger.info("Authentication Response: {}", authenticationResponse);

        Gson gson = new Gson();

        BeehiveAuthentication authenticationObject = gson.fromJson(authenticationResponse, BeehiveAuthentication.class);
        this.accessToken = authenticationObject.getAccessToken();

        return true;
    }

    public String getAccessToken() {
        if(this.accessToken == null) 
            authenticate();
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    
}