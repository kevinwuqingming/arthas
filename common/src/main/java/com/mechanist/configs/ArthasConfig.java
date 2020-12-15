package com.mechanist.configs;

import com.aionemu.commons.configuration.Property;

public class ArthasConfig
{
	@Property(key = "arthas.core.jar.path", defaultValue = "")
	public static String ARTHAS_CORE_JAR_PATH;

	@Property(key = "arthas.core.listen.ip", defaultValue = "localhost")
	public static String ARTHAS_CORE_LISTEN_IP;

	@Property(key = "arthas.core.listen.http.port", defaultValue = "8564")
	public static int ARTHAS_CORE_LISTEN_HTTP_PORT;

	@Property(key = "arthas.core.listen.telnet.port", defaultValue = "3659")
	public static int ARTHAS_CORE_LISTEN_TELNET_PORT;

	@Property(key = "arthas.core.tunnel.server.addr", defaultValue = "")
	public static String ARTHAS_CORE_TUNNEL_SERVER_ADDR;

	@Property(key = "arthas.core.agent.id", defaultValue = "")
	public static String ARTHAS_CORE_AGENT_ID;

	@Property(key = "arthas.core.session.timeout", defaultValue = "18000")
	public static long ARTHAS_CORE_SESSION_TIMEOUT;
}
