package io.pivotal.pcc.demo.client.security;

import java.util.Properties;

import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

public class ClientAuthentication implements AuthInitialize {

	@Override public Properties getCredentials(Properties properties, DistributedMember distributedMember, boolean b)
		throws AuthenticationFailedException {
		return properties;
	}
}
