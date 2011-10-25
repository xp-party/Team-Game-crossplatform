package com.xpparty.teamgame.core.WebSockets;

public interface WebSocketEventsListener {
    public void onMessage(final String msg);
	public void onOpen();
	public void onClose();
	public void onError(final Throwable t);
}
