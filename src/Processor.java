import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
 * Processor class interacts with Google API to get data and add items to queue
 */
public class Processor extends Thread {
	BlockingQueue<TravelData> h2wQ = new ArrayBlockingQueue<>(10);
	BlockingQueue<TravelData> w2hQ = new ArrayBlockingQueue<>(10);
	private volatile boolean			running			= true;
	private final String				ApiKey			= "AIzaSyAWWXK1q-j7KCpSXmjCuyLxQ5LpDII2LlQ";
	private static long					timeInterval	= 5000;
	
	/*
	 * Defines action processor thread should take (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E HH:mm");
		
		while (running) {
			try {
				
				LocalDateTime dt = LocalDateTime.now();
				String date = dt.format(formatter);
				String day = date.substring(0, 3);
				String time = date.substring(4);
				String startAddress = GUI.getStartAddress(); // home
				String endAddress = GUI.getEndAddress(); // work
				TimeData[] times = calculateTime(startAddress, endAddress);
				
				TravelData h2wItem = new TravelData(day, time, times[0]);
				TravelData w2hItem = new TravelData(day, time, times[1]);
				
				h2wQ.put(h2wItem);
				w2hQ.put(w2hItem);
				
				Thread.sleep(timeInterval);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static void setTimeInterval(int seconds) {
		timeInterval = seconds * 1000;
	}

	public void startup() {
		running = true;
	}
	
	/*
	 * Stop the processor thread
	 */
	void shutdown() throws InterruptedException {
		running = false;
		this.join();
	}
	
	/*
	 * Get travel time between two addresses from Google's API. Returns an
	 * integer array. The first element in the array is the time between addr1
	 * and addr2 in minutes. The second element in the array is the time between
	 * addr2 and addr1 in minutes.
	 */
	private TimeData[] calculateTime(String addr1, String addr2) throws Exception {
		TimeData[] retVal = new TimeData[2];
		TimeData t1, t2;
		String returnedTime1, returnedTime2;
		String[] splitString1, splitString2;
		GeoApiContext context = new GeoApiContext.Builder().apiKey(ApiKey).build();
		String[] origin = {addr1, addr2};
		String[] destination = {addr2, addr1};
		try {
			DistanceMatrix matrix = DistanceMatrixApi.getDistanceMatrix(context, origin, destination)
					.mode(TravelMode.DRIVING).departureTime(DateTime.now()).await();
			returnedTime1 = matrix.rows[0].elements[0].durationInTraffic.humanReadable;
			splitString1 = returnedTime1.split("\\s");
			returnedTime2 = matrix.rows[1].elements[1].durationInTraffic.humanReadable;
			splitString2 = returnedTime2.split("\\s");
			if (returnedTime1.contains("day")) {
				t1 = new TimeData(Integer.parseInt(splitString1[0]), Integer.parseInt(splitString1[2]),
						Integer.parseInt(splitString1[4]));
			} else if (returnedTime1.contains("hour")) {
				t1 = new TimeData(Integer.parseInt(splitString1[0]), Integer.parseInt(splitString1[2]));
			} else {
				t1 = new TimeData(Integer.parseInt(splitString1[0]));
			}
			retVal[0] = t1;

			if (returnedTime2.contains("day")) {
				t2 = new TimeData(Integer.parseInt(splitString2[0]), Integer.parseInt(splitString2[2]),
						Integer.parseInt(splitString2[4]));
			} else if (returnedTime2.contains("hour")) {
				t2 = new TimeData(Integer.parseInt(splitString2[0]), Integer.parseInt(splitString2[2]));
			} else {
				t2 = new TimeData(Integer.parseInt(splitString2[0]));
			}
			retVal[1] = t2;
		} catch (Exception e) {
			GUI.updateErrorMessage();
			System.out.println(e.getMessage());
		}

		return retVal;
	}
	
	public static long getTimeInterval() {
		return timeInterval;
	}
	
}