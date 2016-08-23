
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI extends Application {
	
	@SuppressWarnings("unchecked")
	public LineChart<String, Number>[]			lineChart	= new LineChart[7];
	private volatile boolean					running		= false;
	private static Processor					process		= new Processor();
	@SuppressWarnings("unchecked")
	private XYChart.Series<String, Number>[]	series1		= new XYChart.Series[7];
	@SuppressWarnings("unchecked")
	private XYChart.Series<String, Number>[]	series2		= new XYChart.Series[7];
	private static TextField					startTF;
	private static TextField					endTF;
	private static Label						errorLabel, isRunning, timeIntErrorLabel;
	private Stage								timeIntStage, imgExportStage;
	
	@Override
	/*
	 * Set up GUI (non-Javadoc)
	 * 
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	public void start(Stage stage) throws Exception {
		// Set application title
		stage.setTitle("Travel Times");
		
		// Layout for whole application
		VBox layout = new VBox();
		
		// Button layout
		VBox buttons = new VBox();
		
		// Labels
		VBox labels = new VBox(5);
		
		// Text field layout
		VBox textFields = new VBox();
		
		// UI control layout
		HBox upper = new HBox(5);
		
		// Building menu
		MenuBar menuBar = new MenuBar();
		Menu file = new Menu("File");
		MenuItem close = new MenuItem("Close");
		close.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				Platform.exit();
			}
		});
		
		// Set time interval menu item
		StackPane timeIntSP = new StackPane();
		VBox timeIntVBox = new VBox();
		HBox timeIntHBox = new HBox();
		Label timeIntL1 = new Label("Number of seconds: ");
		Label timeIntL2 = new Label("Enter how often you would like the application to pull traffic ");
		Label timeIntL3 = new Label("data from Google Maps in seconds (default is 60 seconds)");
		TextField timeIntervalInputTF = new TextField();
		Button timeIntSubmitBtn = new Button("Submit");
		timeIntHBox.getChildren().addAll(timeIntL1, timeIntervalInputTF, timeIntSubmitBtn);
		timeIntErrorLabel = new Label("");
		timeIntVBox.getChildren().addAll(timeIntL2, timeIntL3, timeIntHBox, timeIntErrorLabel);
		timeIntSubmitBtn.setOnAction(e -> setTimeInterval(timeIntervalInputTF.getText()));
		timeIntSP.getChildren().add(timeIntVBox);
		StackPane.setMargin(timeIntVBox, new Insets(12));
		Scene timeIntScene = new Scene(timeIntSP, 350, 100);
		timeIntStage = new Stage();
		timeIntStage.setScene(timeIntScene);
		timeIntStage.initModality(Modality.APPLICATION_MODAL);
		timeIntStage.setTitle("Set Time Interval");
		MenuItem setTimeInterval = new MenuItem("Set Time Interval");
		setTimeInterval.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				timeIntStage.showAndWait();
			}
		});
		
//		// Export graph data as CSV file item
//		MenuItem exportAsCSV = new MenuItem("Export as CSV");
//		exportAsCSV.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent t) {
//				LocalDateTime ldt = LocalDateTime.now();
//				DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE;
//				String date = ldt.format(dtf);
//				FileChooser fileChooser = new FileChooser();
//				fileChooser.setTitle("Save Data");
//				fileChooser.setInitialFileName("TravelTimeData" + date);
//				fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV", "*.csv"));
//				File file = fileChooser.showSaveDialog(new Stage());
//				try {
//					FileWriter writer = new FileWriter(file);
//					System.out.println(getStartAddress());
//					System.out.println(getEndAddress());
//					for (int i = 0; i < lineChart.length; i++) {
//						System.out.println(i);
//						lineChart[i].getData().forEach(data -> data.getData().forEach(actualData -> System.out.println(actualData.getXValue() + ", " + actualData.getYValue())));
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		});
		
		// Load data from CSV file menu item
		
		// Error and isRunning labels
		errorLabel = new Label();
		isRunning = new Label("Not running");
		labels.getChildren().addAll(isRunning, errorLabel);
		
		// Start button
		Button startButton = new Button("Start");
		startButton.setOnAction(e -> startup());
		
		// Stop Button
		Button stopButton = new Button("Stop");
		stopButton.setOnAction(e -> {
			try {
				shutdown();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		// Start Address text field
		Label startLabel = new Label("Address 1: ");
		startTF = new TextField();
		HBox startAddress = new HBox();
		
		startAddress.getChildren().addAll(startLabel, startTF);
		startAddress.setSpacing(10);
		
		// End Address text field
		Label endLabel = new Label("Address 2: ");
		endTF = new TextField();
		HBox endAddress = new HBox();
		
		endAddress.getChildren().addAll(endLabel, endTF);
		endAddress.setSpacing(10);
		
		// Add tabs
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		BorderPane bp = new BorderPane();
		for (int i = 0; i < 7; i++) {
			Tab tab = new Tab();
			tab.setText(DayOfWeek.of(i + 1).toString());
			makeChart(DayOfWeek.of(i + 1));
			HBox hb = new HBox();
			hb.getChildren().add(lineChart[i]);
			tab.setContent(hb);
			tabPane.getTabs().add(tab);
		}
		LocalDateTime date = LocalDateTime.now();
		DayOfWeek dow = date.getDayOfWeek();
		tabPane.getSelectionModel().select(dow.ordinal());
		
		bp.setCenter(tabPane);
		
		// Export graph as image menu item
		StackPane imgExportSP = new StackPane();
		HBox imgExportHBox = new HBox();
		ObservableList<DayOfWeek> options = FXCollections.observableArrayList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
				DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
		ComboBox<DayOfWeek> graphSelect = new ComboBox<>(options);
		graphSelect.getSelectionModel().selectFirst();
		Button imgExportSubmit = new Button("Submit");
		imgExportSubmit.setOnAction(e -> saveAsPng(lineChart[graphSelect.getValue().ordinal()]));
		imgExportHBox.getChildren().addAll(graphSelect, imgExportSubmit);
		StackPane.setMargin(imgExportHBox, new Insets(12));
		imgExportSP.getChildren().add(imgExportHBox);
		Scene imgExportScene = new Scene(imgExportSP, 250, 100);
		imgExportStage = new Stage();
		imgExportStage.setScene(imgExportScene);
		imgExportStage.initModality(Modality.APPLICATION_MODAL);
		imgExportStage.setTitle("Export graph as image");
		MenuItem imgExport = new MenuItem("Export graph as image");
		imgExport.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent t) {
				imgExportStage.showAndWait();
			}
		});
		
		// Add items to menu bar
		file.getItems().addAll(setTimeInterval, imgExport, close);
		menuBar.getMenus().add(file);
		
		// Add buttons to their VBox
		buttons.getChildren().addAll(startButton, stopButton);
		
		// Add text fields to their VBox
		textFields.getChildren().addAll(startAddress, endAddress);
		
		// Add all UI controls to HBox
		upper.getChildren().addAll(textFields, buttons, labels);
		
		// Add buttons and chart to layout
		layout.getChildren().addAll(menuBar, upper, bp);
		
		// Add layout to scene
		Scene scene = new Scene(layout, 550, 500);
		
		// Add scene to stage
		stage.setScene(scene);
		
		// Set icon
		stage.getIcons().add(new Image("TT-icon.png"));
		
		// Show stage
		stage.show();
	}
	
	public void setTimeInterval(String seconds) {
		if (!seconds.equals("")) {
			int timeInt = -1;
			try {
				timeInt = Integer.parseInt(seconds);
				if (timeInt < 1 || timeInt > 3600) {
					timeIntErrorLabel.setText("Please enter an integer value between 1 and 3600");
				} else {
					Processor.setTimeInterval(timeInt);
					timeIntStage.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				timeIntErrorLabel.setText("Please enter an integer value between 1 and 3600");
			}
		}
	}
	
	public static String getStartAddress() {
		return startTF.getText();
	}
	
	public static String getEndAddress() {
		return endTF.getText();
	}
	
	/*
	 * Action for start button to take, start thread in process and start thread
	 * to update chart
	 */
	public void startup() {
		if (!startTF.getText().isEmpty() && !endTF.getText().isEmpty() && !running) {
			isRunning.setText("Running");
			errorLabel.setText("");
			process = new Processor();
			running = true;
			process.start();
			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					while (running) {
						if (!process.h2wQ.isEmpty() || !process.w2hQ.isEmpty()) {
							try {
								updateChart();
								Thread.sleep(1000);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						} else {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
					}
					
				}
			});
			t1.start();
		} else {
			if (running) {
				errorLabel.setText("Application already running");
			} else if (startTF.getText().isEmpty() || endTF.getText().isEmpty())
				errorLabel.setText("You must enter an address in both fields");
		}
	}
	
	/*
	 * Action for stop button, stop both threads (not main thread)
	 */
	public void shutdown() throws Exception {
		if (running) {
			isRunning.setText("Not running");
			process.shutdown();
			running = false;
		}
	}
	
	public static void updateErrorMessage(String error) {
		errorLabel.setText(error);
	}
	
	/*
	 * Set up the chart when the GUI first starts
	 */
	@SuppressWarnings("unchecked")
	public void makeChart(DayOfWeek dow) throws Exception {
		final CategoryAxis xAxis = new CategoryAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Time of Day");
		yAxis.setLabel("Time (mins)");
		lineChart[dow.ordinal()] = new LineChart<>(xAxis, yAxis);
		lineChart[dow.ordinal()].setTitle(dow.toString());
		series1[dow.ordinal()] = new XYChart.Series<>();
		series1[dow.ordinal()].setName("1 to 2");
		series2[dow.ordinal()] = new XYChart.Series<>();
		series2[dow.ordinal()].setName("2 to 1");
		lineChart[dow.ordinal()].getData().addAll(series1[dow.ordinal()], series2[dow.ordinal()]);
	}
	
	/*
	 * Run in the background and update the chart when new data is added to the
	 * Queue
	 */
	public void updateChart() throws Exception {
		while (!process.h2wQ.isEmpty()) {
			TravelData temp = process.h2wQ.poll();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					series1[temp.getDayOfWeekEnum().ordinal()].getData()
							.add(new XYChart.Data<String, Number>(temp.getTime(), temp.getTimeData().getTotalMins()));
				}
			});
		}
		while (!process.w2hQ.isEmpty()) {
			TravelData temp = process.w2hQ.poll();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					series2[temp.getDayOfWeekEnum().ordinal()].getData()
							.add(new XYChart.Data<String, Number>(temp.getTime(), temp.getTimeData().getTotalMins()));
				}
			});
		}
	}
	
	/*
	 * Saves a picture of the graph as a .png image file
	 */
	public void saveAsPng(LineChart<String, Number> lineChart) {
		LocalDateTime ldt = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE;
		String date = ldt.format(dtf);
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		fileChooser.setInitialFileName(lineChart.getTitle() + date);
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Image", "*.png"));
		File file = fileChooser.showSaveDialog(new Stage());
		WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
		} catch (Exception e) {
			imgExportStage.close();
		}
		imgExportStage.close();
	}
	
	/*
	 * Starts the program
	 */
	public static void startProgram() {
		launch();
	}
	
}
