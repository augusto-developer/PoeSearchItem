package com.augustodeveloper.poe.app.gui;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.augustodeveloper.poe.app.controllers.PoeNinjaController;
import com.augustodeveloper.poe.app.controllers.PoeTradeController;
import com.augustodeveloper.poe.app.services.PoeNinjaService;
import com.augustodeveloper.poe.app.services.PoeTradeService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FrontGUI extends Application {

	private TextField textField;
	private Button tradeButton;
	private ListView<String> listView;
	private ProgressBar progressBar;
	private PoeNinjaController poeNinjaController;
	private PoeTradeController poeTradeController;
	private PoeTradeService poeTradeService;
	private String apiUrlPoeNinja;
	private String apiUrlPoeTrade;

	private PoeNinjaService equipments;

	public FrontGUI() {
		this.equipments = new PoeNinjaService(new PoeNinjaController());

	}

	@Override
	public void start(Stage primaryStage) {
		poeNinjaController = new PoeNinjaController();
		poeTradeController = new PoeTradeController();

		textField = new TextField();
		tradeButton = new Button("Trade");
		listView = new ListView<>();
        progressBar = new ProgressBar();        
        
		tradeButton.setOnAction(e -> handleButtonClick());

		VBox vbox = new VBox(textField, tradeButton, listView, progressBar);
	    vbox.setPadding(new Insets(10));
	    vbox.setSpacing(10);

		Scene scene = new Scene(vbox, 500, 400);
		
		progressBar.setStyle("-fx-accent: red;");
		primaryStage.setTitle("Quickly Trade v1.0 - by: Lopez");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void handleButtonClick() {
		String url = textField.getText();


		 try {
		        apiUrlPoeNinja = poeNinjaController.handleRequest(url);
		        System.out.println("API URL Ninja: " + apiUrlPoeNinja);

		        poeTradeService = new PoeTradeService(apiUrlPoeNinja);

		        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		        executor.schedule(() -> {
		            try {
		                poeTradeService.run();
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		            List<String> tradeLinks = poeTradeService.getTradeLinks();
		            Platform.runLater(() -> listView.getItems().addAll(tradeLinks));
		        }, 20, TimeUnit.SECONDS);
		        executor.scheduleAtFixedRate(() -> {
		            double currentProgress = progressBar.getProgress();
		            if (currentProgress < 1) {
		                Platform.runLater(() -> progressBar.setProgress(currentProgress + 0.01));
		            }
		        }, 0, 100, TimeUnit.MILLISECONDS);
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
	}

	public static void main(String[] args) {
		launch(args);
	}

	public TextField getTextField() {
		return textField;
	}

	public Button getTradeButton() {
		return tradeButton;
	}

	public PoeNinjaController getPoeNinjaController() {
		return poeNinjaController;
	}

	public PoeTradeController getPoeTradeController() {
		return poeTradeController;
	}

	public PoeTradeService getPoeTradeService() {
		return poeTradeService;
	}

	public String getApiUrlPoeNinja() {
		return apiUrlPoeNinja;
	}

	public String getApiUrlPoeTrade() {
		return apiUrlPoeTrade;
	}

	public PoeNinjaService getEquipments() {
		return equipments;
	}
}
