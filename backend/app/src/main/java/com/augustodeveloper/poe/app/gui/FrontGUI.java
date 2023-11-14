package com.augustodeveloper.poe.app.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.augustodeveloper.poe.app.controllers.PoeNinjaController;
import com.augustodeveloper.poe.app.controllers.PoeTradeController;
import com.augustodeveloper.poe.app.services.PoeNinjaService;
import com.augustodeveloper.poe.app.services.PoeTradeService;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FrontGUI extends Application {

	private TextField textField;
	private Button tradeButton;
	private ListView<Node> listView;
	private PoeNinjaController poeNinjaController;
	private PoeTradeController poeTradeController;
	private PoeTradeService poeTradeService;
	private String apiUrlPoeNinja;
	private String apiUrlPoeTrade;
	private ProgressBar loadingProgressBar;
	private Label statusLabel;
	private CheckBox equipmentCheckBox;
	private CheckBox flasksCheckBox;
	private CheckBox jewelsCheckBox;
	private CheckBox gemsCheckBox;

	private PoeNinjaService equipments;

	public FrontGUI() {
		this.equipments = new PoeNinjaService(new PoeNinjaController());
		this.statusLabel = new Label();
		this.equipmentCheckBox = new CheckBox("Equipment");
		this.flasksCheckBox = new CheckBox("Flasks");
		this.jewelsCheckBox = new CheckBox("Jewels");
		this.gemsCheckBox = new CheckBox("Gems");
	}

	@Override
	public void start(Stage primaryStage) {
		listView = new ListView<>();
		listView.getStyleClass().add("meuEstilo");


		poeNinjaController = new PoeNinjaController();
		poeTradeController = new PoeTradeController();

		textField = new TextField();
		textField.getStyleClass().add("meuEstilo");
		
		tradeButton = new Button("Trade");
		tradeButton.getStyleClass().add("meuBotao");
		tradeButton.setPrefWidth(100);
		

		tradeButton.setOnAction(e -> handleButtonClick());

		loadingProgressBar = new ProgressBar();
		loadingProgressBar.setProgress(0);
		loadingProgressBar.setPrefSize(500, 50);
		loadingProgressBar.setPadding(new Insets(0, 0, 0, 0));

		statusLabel = new Label();
		statusLabel.setPadding(new Insets(0, 0, 0, 0));
		statusLabel.setVisible(false);

		HBox hboxCheckboxes = new HBox(equipmentCheckBox, flasksCheckBox, jewelsCheckBox, gemsCheckBox);
	    hboxCheckboxes.setSpacing(10);
	    hboxCheckboxes.setAlignment(Pos.CENTER);

	    CheckBox[] checkboxes = {equipmentCheckBox, flasksCheckBox, jewelsCheckBox, gemsCheckBox};
	    setupCheckBoxes(checkboxes);
	    
	    GridPane gridPane = new GridPane();
	    gridPane.setHgap(100); // Define o espaçamento horizontal entre as colunas

	    gridPane.add(tradeButton, 0, 0); // Adiciona o tradeButton à primeira coluna e primeira linha
	    gridPane.add(hboxCheckboxes, 1, 0); // Adiciona o hboxCheckboxes à segunda coluna e primeira linha
		
		BorderPane borderPane = new BorderPane();
		borderPane.setBottom(loadingProgressBar);
		borderPane.setCenter(statusLabel);
		

		VBox vbox = new VBox(textField, gridPane, listView, borderPane);
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(5);
		vbox.getStyleClass().add("meuEstilo");


		Scene scene = new Scene(vbox, 500, 565);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		

		
		
		primaryStage.setResizable(false);
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
					if (equipmentCheckBox.isSelected()) {
						poeTradeService.run(link -> {
							Platform.runLater(() -> {

								String[] linkParts = link.split(" - ");
								String equipmentName = linkParts[0]; // Nome do equipamento
								String linkOnly = linkParts[1]; // Link de negociação
								Button linkButton = new Button(equipmentName);
								linkButton.setPadding(new Insets(10, 10, 10, 10));
								linkButton.setPrefWidth(465);
								linkButton.getStyleClass().add("meuBotao");
								
								
								
								
								linkButton.setOnAction(e -> {
									try {
										Desktop.getDesktop().browse(new URI(linkOnly));
									} catch (IOException | URISyntaxException ex) {
										ex.printStackTrace();
									}
								});
								listView.getItems().add(new HBox(linkButton));
								
								
								// Atualize o progresso da barra de progresso
								double progress = listView.getItems().size() / 4.0;
								loadingProgressBar.setProgress(progress);

								// Crie uma nova Timeline para animar a barra de progresso
								Timeline timeline = new Timeline();
								timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000),
										new KeyValue(loadingProgressBar.progressProperty(), progress)));
								timeline.play(); // Inicie a animação
								if (listView.getItems().size() == 4) {
									
									statusLabel.setText("Concluído 🗹");
									
									statusLabel.setPadding(new Insets(50, 0, 0, 0));
									statusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: green;");
									statusLabel.setVisible(true); 
									loadingProgressBar.setVisible(false);
								}

							});
						});
		               }
					

				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 4, TimeUnit.SECONDS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void setupCheckBoxes(CheckBox... checkboxes) {
	    for (CheckBox checkbox : checkboxes) {
	        checkbox.setOnAction(e -> {
	            if (checkbox.isSelected()) {
	                for (CheckBox other : checkboxes) {
	                    if (other != checkbox) {
	                        other.setSelected(false);
	                    }
	                }
	            }
	        });
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
