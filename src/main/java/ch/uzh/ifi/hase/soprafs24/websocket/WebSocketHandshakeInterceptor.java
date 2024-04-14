/*
package ch.uzh.ifi.hase.soprafs24.websocket;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpResponse servletResponse = (ServletServerHttpResponse) response;
            HttpServletResponse httpServletResponse = servletResponse.getServletResponse();
            httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // Do nothing
    }
    
}

 */
