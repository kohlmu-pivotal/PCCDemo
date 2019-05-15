package io.pivotal.pcc.demo.client.configuration;

import java.util.Map;

import org.json.simple.parser.ParseException;

public interface Configurer {
	Map parseConnectionProperties() throws ParseException;
}
