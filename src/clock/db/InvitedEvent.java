package clock.db;

public class InvitedEvent extends Event {
	
	private String channel;
	private long originalId;
	
	private InvitedEvent() { }

	@Override
	public String toString() {
		return this.channel + ": " + super.toString();
	}


	
	/**
	 * adds |channel|originalId
	 */
	@Override
	public String encodeToString() {
		return super.encodeToString() + "|" + this.getChannel() + "|" + this.getOriginalId();
	}
	
	public static InvitedEvent createFromString(String eventStr) {
		String[] prop = eventStr.split("\\|");
		InvitedEvent retEvent = new InvitedEvent();
		setInitFieldsFromStr(retEvent, eventStr);
		retEvent.setChannel(prop[8]);
		retEvent.setOriginalId(Integer.parseInt(prop[9]));
		return retEvent;
	}
	
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

	public long getOriginalId() {
		return originalId;
	}

	public void setOriginalId(long originalId) {
		this.originalId = originalId;
	}
	
	
}
