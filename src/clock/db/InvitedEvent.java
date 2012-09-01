package clock.db;

public class InvitedEvent extends Event {
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.channel + ": " + super.toString();
	}

	private String channel;
	
	private InvitedEvent() { }

	/**
	 * Returns a new "empty" instance of event.
	 * @return
	 */
	public static InvitedEvent createNewInstance()
	{
		InvitedEvent event = new InvitedEvent();
		event.setLocation("");
		event.setDetails("");
		return event;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	
}
