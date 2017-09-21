import java.time.DayOfWeek;

/*
 * This class defines a travel data object
 */
public class TravelData {
	private String		day;
	private String		time;
	private TimeData	travel;


	TravelData(String inDay, String inTime, TimeData inTravel) {
		day = inDay;
		time = inTime;
		travel = inTravel;
	}
	
	@Override
	public String toString() {
		return day + " " + time + " " + travel.toString();
	}

	DayOfWeek getDayOfWeekEnum() {
		switch(day){
			case "Mon":
				return DayOfWeek.MONDAY;
			case "Tue":
				return DayOfWeek.TUESDAY;
			case "Wed":
				return DayOfWeek.WEDNESDAY;
			case "Thu":
				return DayOfWeek.THURSDAY;
			case "Fri":
				return DayOfWeek.FRIDAY;
			case "Sat":
				return DayOfWeek.SATURDAY;
			case "Sun":
				return DayOfWeek.SUNDAY;
			default:
				return DayOfWeek.MONDAY;
		}
	}

	String getTime() {
		return time;
	}

	TimeData getTimeData() {
		return travel;
	}
}
