
public class TimeData {
	
	private int days;
	private int hours;
	private int minutes;
	private int totalMins;


	TimeData(int inDays, int inHours, int inMinutes) {
		days = inDays;
		hours = inHours;
		minutes = inMinutes;
		totalMins = (days*1440) + (hours*60) + minutes;
	}

	TimeData(int inHours, int inMinutes) {
		days = 0;
		hours = inHours;
		minutes = inMinutes;
		totalMins = (hours*60) + minutes;
	}

	TimeData(int inMinutes) {
		days = 0;
		hours = 0;
		minutes = inMinutes;
		totalMins = inMinutes;
	}

	int getTotalMins() {
		return totalMins;
	}
	
	@Override
	public String toString(){
		if(days != 0){
			return days + " days " + hours + " hours " + minutes + " minutes";
		}
		else if(hours != 0){
			return hours + " hours " + minutes + " minutes";
		}
		else return minutes + " minutes";
	}
}
