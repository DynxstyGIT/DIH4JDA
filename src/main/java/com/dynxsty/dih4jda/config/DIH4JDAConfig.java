package com.dynxsty.dih4jda.config;

import com.dynxsty.dih4jda.DIH4JDALogger;
import net.dv8tion.jda.api.JDA;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Simple data class which represents the config of a single {@link com.dynxsty.dih4jda.DIH4JDA}
 * instance.
 */
public class DIH4JDAConfig {
	private JDA jda;
	private String commandsPackage;
	private Set<DIH4JDALogger.Type> blockedLogTypes = new HashSet<>();
	private boolean registerOnReady = true;
	private boolean globalSmartQueue = true;
	private boolean guildSmartQueue = true;
	private boolean deleteUnknownCommands = true;
	private boolean throwUnregisteredException = true;
	private Executor executor = ForkJoinPool.commonPool();

	public JDA getJDA() {
		return jda;
	}

	public void setJDA(JDA jda) {
		this.jda = jda;
	}

	public String getCommandsPackage() {
		return commandsPackage;
	}

	public void setCommandsPackage(String commandsPackage) {
		this.commandsPackage = commandsPackage;
	}

	public Set<DIH4JDALogger.Type> getBlockedLogTypes() {
		return blockedLogTypes;
	}

	public void setBlockedLogTypes(Set<DIH4JDALogger.Type> blockedLogTypes) {
		this.blockedLogTypes = blockedLogTypes;
	}

	public boolean isRegisterOnReady() {
		return registerOnReady;
	}

	public void setRegisterOnReady(boolean registerOnReady) {
		this.registerOnReady = registerOnReady;
	}

	public boolean isGlobalSmartQueue() {
		return globalSmartQueue;
	}

	public void setGlobalSmartQueue(boolean globalSmartQueue) {
		this.globalSmartQueue = globalSmartQueue;
	}

	public boolean isGuildSmartQueue() {
		return guildSmartQueue;
	}

	public void setGuildSmartQueue(boolean guildSmartQueue) {
		this.guildSmartQueue = guildSmartQueue;
	}

	public boolean isDeleteUnknownCommands() {
		return deleteUnknownCommands;
	}

	public void setDeleteUnknownCommands(boolean deleteUnknownCommands) {
		this.deleteUnknownCommands = deleteUnknownCommands;
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public boolean isThrowUnregisteredException() {
		return throwUnregisteredException;
	}

	public void setThrowUnregisteredException(boolean throwUnregisteredException) {
		this.throwUnregisteredException = throwUnregisteredException;
	}
}
