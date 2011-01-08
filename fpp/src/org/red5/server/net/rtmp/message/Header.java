package org.red5.server.net.rtmp.message;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * RTMP packet header
 */
public class Header implements Constants, Externalizable {
	private static final long serialVersionUID = 8982665579411495024L;
    /**
     * Channel
     */
	private int channelId;
    /**
     * Timer
     */
	private int timer;
    /**
     * Header size
     */
	private int size;
    /**
     * Type of data
     */
	private byte dataType;
    /**
     * Stream id
     */
	private int streamId;
    /**
     * Whether timer value is relative
     */
	private boolean timerRelative = true;

	/**
     * Getter for channel id
     *
     * @return  Channel id
     */
    public int getChannelId() {
		return channelId;
	}

	/**
     * Setter for channel id
     *
     * @param channelId  Header channel id
     */
    public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	/**
     * Getter for data type
     *
     * @return  Data type
     */
    public byte getDataType() {
		return dataType;
	}

	/**
     * Setter for data type
     *
     * @param dataType  Data type
     */
    public void setDataType(byte dataType) {
		this.dataType = dataType;
	}

	/**
     * Getter for size.
     *
     * @return  Header size
     */
    public int getSize() {
		return size;
	}

	/**
     * Setter for size
     *
     * @param size  Header size
     */
    public void setSize(int size) {
		this.size = size;
	}

	/**
     * Getter for stream id
     *
     * @return  Stream id
     */
    public int getStreamId() {
		return streamId;
	}

	/**
     * Setter for stream id
     *
     * @param streamId  Stream id
     */
    public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	/**
     * Getter for timer
     *
     * @return  Timer
     */
    public int getTimer() {
		return timer;
	}

	/**
     * Setter for timer
     *
     * @param timer  Timer
     */
    public void setTimer(int timer) {
		this.timer = timer;
	}

	/**
     * Getter for timer relative flag
     *
     * @return  <code>true</code> if timer value is relative, <code>false</code> otherwise
     */
    public boolean isTimerRelative() {
		return timerRelative;
	}

	/**
     * Setter for timer relative flag
     *
     * @param timerRelative <code>true</code> if timer values are relative, <code>false</code> otherwise
     */
    public void setTimerRelative(boolean timerRelative) {
		this.timerRelative = timerRelative;
	}

	/** {@inheritDoc} */
    @Override
	public boolean equals(Object other) {
		if (!(other instanceof Header)) {
			return false;
		}
		final Header header = (Header) other;
		return (header.getChannelId() == channelId
				&& header.getDataType() == dataType && header.getSize() == size
				&& header.getTimer() == timer && header.getStreamId() == streamId);
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ChannelId: ").append(channelId).append(", ");
		sb.append("Timer: ").append(timer).append(" (" + (timerRelative ? "relative" : "absolute") + ')').append(", ");
		sb.append("Size: ").append(size).append(", ");
		sb.append("DataType: ").append(dataType).append(", ");
		sb.append("StreamId: ").append(streamId);
		return sb.toString();
	}

	/** {@inheritDoc} */
    @Override
	public Object clone() {
		final Header header = new Header();
		header.setChannelId(channelId);
		header.setTimer(timer);
		header.setSize(size);
		header.setDataType(dataType);
		header.setStreamId(streamId);
		return header;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		dataType = in.readByte();
		channelId = in.readInt();
		size = in.readInt();
		streamId = in.readInt();
		timer = in.readInt();
		timerRelative = in.readBoolean();
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(dataType);
		out.writeInt(channelId);
		out.writeInt(size);
		out.writeInt(streamId);
		out.writeInt(timer);
		out.writeBoolean(timerRelative);
	}
}
