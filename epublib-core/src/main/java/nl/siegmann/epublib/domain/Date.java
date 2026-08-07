package nl.siegmann.epublib.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;

import nl.siegmann.epublib.epub.PackageDocumentBase;

/**
 * A Date used by the book's metadata.
 * 
 * @author paul
 *
 */
public class Date implements Serializable {

	private static final long serialVersionUID = 7533866830395120136L;

	public enum Event {
		PUBLICATION("publication"),
		MODIFICATION("modification"),
		CREATION("creation");
		
		private final String value;

		Event(String v) {
			value = v;
		}

		public static Event fromValue(String v) {
			if (v == null) {
				return null;
			}
			return Arrays.stream(Event.values())
					.filter(c -> c.value.equalsIgnoreCase(v))
					.findFirst()
					.orElse(null);
		}
		
		@Override
		public String toString() {
			return value;
		}
	}

	private Event event;
	private String dateString;

	public Date(java.util.Date date) {
		this(date, (Event) null);
	}
	
	public Date(String dateString) {
		this(dateString, (Event) null);
	}
	
	public Date(java.util.Date date, Event event) {
		this((new SimpleDateFormat(PackageDocumentBase.dateFormat)).format(date), event);
	}
	
	public Date(String dateString, Event event) {
		this.dateString = dateString;
		this.event = event;
	}
	
	public Date(java.util.Date date, String event) {
		this((new SimpleDateFormat(PackageDocumentBase.dateFormat)).format(date), event);
	}
	
	public Date(String dateString, String event) {
		this(checkDate(dateString), Event.fromValue(event));
		this.dateString = dateString;
	}

	/**
	 * Creates a Date with the given date string and event type.
	 *
	 * @param dateString the date string (e.g. {@code "2024-01-15"})
	 * @param event      the event type
	 * @return a new Date instance
	 * @since 5.0
	 */
	public static Date of(String dateString, Event event) {
		return new Date(dateString, event);
	}

	/**
	 * Creates a Date with the given date string and event string.
	 * The event string is matched case-insensitively against {@link Event} values.
	 *
	 * @param dateString the date string (e.g. {@code "2024-01-15"})
	 * @param event      the event string (e.g. {@code "publication"})
	 * @return a new Date instance
	 * @since 5.0
	 */
	public static Date of(String dateString, String event) {
		return new Date(dateString, event);
	}


	private static String checkDate(String dateString) {
		if (dateString == null) {
			throw new IllegalArgumentException("Cannot create a date from a blank string");
		}
		return dateString;
	}

	public String getValue() {
		return dateString;
	}

	public Event getEvent() {
		return event;
	}
	
	public void setEvent(Event event) {
		this.event = event;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Date other)) {
			return false;
		}
		return event == other.event && Objects.equals(dateString, other.dateString);
	}

	@Override
	public int hashCode() {
		return Objects.hash(event, dateString);
	}

	@Override
	public String toString() {
		return event == null ? String.valueOf(dateString) : event + ":" + dateString;
	}
}

