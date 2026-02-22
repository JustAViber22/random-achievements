package com.randomachievements.client;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientProgress {
	private static final Set<String> COMPLETED = ConcurrentHashMap.newKeySet();

	public static Set<String> getCompleted() {
		return Collections.unmodifiableSet(COMPLETED);
	}

	public static void setCompleted(java.util.Collection<String> ids) {
		COMPLETED.clear();
		COMPLETED.addAll(ids);
	}
}
