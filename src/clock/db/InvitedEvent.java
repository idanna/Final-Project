package clock.db;

public class InvitedEvent extends Event {
	
	private String channel;
	private long originalId;
	private String senderUserName;
	
	private InvitedEvent() { }

	@Override
	public String toString() {
		return this.senderUserName + ": " + super.toString();
	}

	/**
	 * adds |channel|originalId
	 */
	@Override
	public String encodeToString() {
		return super.encodeToString() + "|" + channel + "|" + originalId + "|" + senderUserName;
	}
	
	public static InvitedEvent createFromString(String eventStr) {
		String[] prop = eventStr.split("\\|");
		InvitedEvent retEvent = new InvitedEvent();
		setInitFieldsFromStr(retEvent, eventStr);
		retEvent.setChannel(prop[8]);
		retEvent.setOriginalId(Integer.parseInt(prop[9]));
		retEvent.setSenderUserName(prop[10]);
		return retEvent;
	}
	
	public String getSenderUserName() {
		return senderUserName;
	}

	public void setSenderUserName(String senderUserName) {
		this.senderUserName = senderUserName;
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

	/**
	 * Returns an invited event instance cloned from the regular event
	 * and sets the user name and channel fields.
	 * @param regularEvent - event to create from
	 * @param userName - user name for the invited event (the sender user name)
	 * @param channelName - the sender channel.
	 * @return
	 */
	public static InvitedEvent newInstanceFromEvent(Event regularEvent, String userName, String channelName) 
	{
		InvitedEvent retEvent = new InvitedEvent();		
		setInitFieldsFromStr(retEvent, regularEvent.encodeToString());
		retEvent.setChannel(channelName);
		retEvent.setSenderUserName(userName);
		return retEvent;
	}
	
	
}
