package clock.exceptions;

import clock.db.Event;

public class EventsCollideException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private Event collidedEvent;

	public EventsCollideException(Event collidedEvent) {
		super();
		this.collidedEvent = collidedEvent;
	}

	public Event getCollidedEvent() {
		return collidedEvent;
	}
		
	
}
