package com.lilithsthrone.controller;

/**
 * What happens when the player activates an element. Toolkit-neutral: the desktop front-end wraps
 * one of these in a DOM {@code EventListener}, the HTTP front-end runs it when the matching id is
 * posted to {@code /action}.
 */
@FunctionalInterface
public interface ClickAction {

	void run();
}
