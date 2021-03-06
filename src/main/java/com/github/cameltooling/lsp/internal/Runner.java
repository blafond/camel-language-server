/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cameltooling.lsp.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import com.github.cameltooling.lsp.internal.websocket.WebSocketRunner;

/**
 * @author lhein
 */
public class Runner {

	/**
	 * For test only
	 */
	static CamelLanguageServer server;
	static WebSocketRunner webSocketRunner;
	
	private static final String WEBSOCKET_PARAMETER = "--websocket";
	private static final String PORT_PARAMETER = "--port=";
	private static final String HOSTNAME_PARAMETER = "--hostname=";
	private static final String CONTEXTPATH_PARAMETER = "--contextPath=";
	static final String HELP_PARAMETER = "--help";
	
	static final String HELP_MESSAGE =
			"`--help` to display this message\n\n"
			+ "When launching without parameter, the Language Client can use Standard Input and Ouput to connect to the Camel Language Server.\n\n"
			+ "To use a websocket connection, the parameter `--websocket` must be provided.\n"
			+ "If you are using a websocket connection, 3 other parameters are available:\n"
			+ "  `--port=<port>` default value is `8025`\n"
			+ "  `--hostname=<hostname>`, default value `localhost`\n"
			+ "  `--contextPath=<contextPath>`, default value `/`. It must start with a `/`.";

	public static void main(String[] args) {
		List<String> arguments = Arrays.asList(args);
		if (arguments.contains(HELP_PARAMETER)) {
			System.out.println(HELP_MESSAGE);
		} else if (arguments.contains(WEBSOCKET_PARAMETER)) {
			int port = extractPort(arguments);
			String hostname = extractHostname(arguments);
			webSocketRunner = new WebSocketRunner();
			String contextPath = extractContextPath(arguments);
			webSocketRunner.runWebSocketServer(hostname, port, contextPath);
		} else {
			server = new CamelLanguageServer();
			Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
			server.connect(launcher.getRemoteProxy());
			launcher.startListening();
		}
	}

	private static String extractContextPath(List<String> arguments) {
		return extractParameterValue(arguments, CONTEXTPATH_PARAMETER);
	}

	private static String extractHostname(List<String> arguments) {
		return extractParameterValue(arguments, HOSTNAME_PARAMETER);
	}

	private static String extractParameterValue(List<String> arguments, String parameterToExtract) {
		for (String argument : arguments) {
			if (argument.startsWith(parameterToExtract)) {
				return argument.substring(parameterToExtract.length());
			}
		}
		return null;
	}

	private static int extractPort(List<String> arguments) {
		for (String argument : arguments) {
			if (argument.startsWith(PORT_PARAMETER)) {
				String providedPort = argument.substring(PORT_PARAMETER.length());
				try {
					return Integer.parseInt(providedPort);
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException("The provided port is invalid.", nfe);
				}
			}
		}
		return -1;
	}
}
