package com.augustodeveloper.poe.app.gui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FrontGUI extends Application {

	private String characterName;
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
	private ScheduledExecutorService executor;
	private Duration globalCooldown = Duration.seconds(10);
	private Timeline globalCooldownTimeline;
	private List<Button> buttons = new ArrayList<>();
	private Button clickedButton;
	private ProgressIndicator progressIndicator;

	private PoeNinjaService equipments;

	public FrontGUI() {
		this.equipments = new PoeNinjaService(new PoeNinjaController());
		this.statusLabel = new Label();
		this.equipmentCheckBox = new CheckBox("Equipment");
		this.flasksCheckBox = new CheckBox("Flasks");
		this.jewelsCheckBox = new CheckBox("Jewels");
		this.gemsCheckBox = new CheckBox("Gems");
		this.globalCooldownTimeline = new Timeline();
		this.progressIndicator = new ProgressIndicator();
	}

	public void start(Stage primaryStage) {
		executor = Executors.newSingleThreadScheduledExecutor();

		listView = new ListView<>();
		listView.getStyleClass().add("meuEstilo");

		poeNinjaController = new PoeNinjaController();
		poeTradeController = new PoeTradeController();

		textField = new TextField();
		textField.getStyleClass().add("meuEstilo");
		textField.setPromptText("Insert Profile PoeNinja link!");
		textField.setAlignment(Pos.CENTER);

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

		progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefSize(25, 25);
		progressIndicator.setPadding(new Insets(0, 5, 0, 5));
		progressIndicator.setVisible(false);

		StackPane stackPane = new StackPane();
		stackPane.getChildren().addAll(listView, progressIndicator);

		HBox hboxCheckboxes = new HBox(equipmentCheckBox, flasksCheckBox, jewelsCheckBox, gemsCheckBox);
		hboxCheckboxes.setSpacing(10);
		hboxCheckboxes.setAlignment(Pos.CENTER);

		CheckBox[] checkboxes = { equipmentCheckBox, flasksCheckBox, jewelsCheckBox, gemsCheckBox };
		setupCheckBoxes(checkboxes);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(100); // Define o espaçamento horizontal entre as colunas

		gridPane.add(tradeButton, 0, 0); // Adiciona o tradeButton à primeira coluna e primeira linha
		gridPane.add(hboxCheckboxes, 1, 0); // Adiciona o hboxCheckboxes à segunda coluna e primeira linha

		BorderPane borderPane = new BorderPane();
		borderPane.setBottom(loadingProgressBar);
		borderPane.setCenter(statusLabel);

		VBox vbox = new VBox(textField, gridPane, stackPane, borderPane);
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(5);
		vbox.getStyleClass().add("meuEstilo");

		Scene scene = new Scene(vbox, 500, 565);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

		primaryStage.setResizable(false);
		primaryStage.setTitle("Quickly Trade v1.0 - by: Lopez");
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/fastbuild32x32.png")));
		primaryStage.show();

	}

	private void handleButtonClick() {
		String url = textField.getText();
		characterName = extractCharacterName(url);

		try {
			apiUrlPoeNinja = poeNinjaController.handleRequest(url);
			System.out.println("API URL Ninja: " + apiUrlPoeNinja);

			poeTradeService = new PoeTradeService(apiUrlPoeNinja);

			// Cria um mapa que associa cada checkbox a uma string que representa o tipo de item
			 Map<CheckBox, String> checkboxMap = new HashMap<>();
			 checkboxMap.put(equipmentCheckBox, "Equipments:");
			 checkboxMap.put(flasksCheckBox, "Flasks:");
			 checkboxMap.put(jewelsCheckBox, "Jewels:");
			 checkboxMap.put(gemsCheckBox, "Gems:");
			
			// Verifica se o arquivo já existe e se contém a lista de cada tipo de item que está sendo gerado
			 File file = new File(characterName + ".txt");
			 if (file.exists()) {
			  try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			      String line;
			      while ((line = br.readLine()) != null) {
			          // Se a linha contém a string que representa o tipo de item, o arquivo já contém a lista desse tipo de item
			          for (Map.Entry<CheckBox, String> entry : checkboxMap.entrySet()) {
			              CheckBox checkbox = entry.getKey();
			              String type = entry.getValue();
			              if (checkbox.isSelected() && line.contains(type)) {
			                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
			                 alert.setTitle("Informação");
			                 alert.setHeaderText(null);
			                 alert.setContentText("O arquivo já contém a lista de " + type);
			                 alert.showAndWait();
			                 return;
			              }
			          }
			      }
			  } catch (IOException e) {
			      e.printStackTrace();
				}
			}

			executor.schedule(() -> {
				try {
					if (equipmentCheckBox.isSelected()) {
						poeTradeService.equipmentsTrade(link -> {
							Platform.runLater(() -> {
								long valueEquipment = 10L;
								checkBoxConfig(link, valueEquipment);
								writeLinksToFile(characterName, link, "Equipments");
							});
						});
					}
					if (gemsCheckBox.isSelected()) {
						poeTradeService.gemTrade(linkAndIdSize -> {
							Platform.runLater(() -> {
								checkBoxConfig(linkAndIdSize.getLink(), Long.valueOf(linkAndIdSize.getIdSize()));
								writeLinksToFile(characterName, linkAndIdSize.getLink(), "Gems");
							});
						});
					}
					if (jewelsCheckBox.isSelected()) {
						poeTradeService.jewelTrade(linkAndIdSize -> {
							Platform.runLater(() -> {
								checkBoxConfig(linkAndIdSize.getLink(), Long.valueOf(linkAndIdSize.getIdSize()));
								writeLinksToFile(characterName, linkAndIdSize.getLink(), "Jewels");
							});
						});
					}
					if (flasksCheckBox.isSelected()) {
						poeTradeService.flaskTrade(linkAndIdSize -> {
							Platform.runLater(() -> {
								checkBoxConfig(linkAndIdSize.getLink(), Long.valueOf(linkAndIdSize.getIdSize()));
								writeLinksToFile(characterName, linkAndIdSize.getLink(), "Flasks");
							});
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 4, TimeUnit.SECONDS);
		} catch (Exception e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Erro");
			alert.setHeaderText(null);
			alert.setContentText("Insert a valid PoeNinja Profile link !");
			alert.showAndWait();
			textField.clear();
		}

		progressIndicator.setVisible(true); // Mostra o ProgressIndicator
		progressIndicator.setProgress(-1); // Inicia a animação
	}

	private String extractCharacterName(String url) {
		String[] parts = url.split("/");
		String characterName = parts[7].split("\\?")[0];
		String timeMachine;
		if(url.contains("&time-machine=")) {
			timeMachine = parts[7].split("&time-machine=")[1];
		} else {
			timeMachine = "";
		}
		return characterName + " - " + timeMachine;
	}

	private void writeLinksToFile(String characterName, String link, String typeEquipment) {
		try {
			characterName = URLDecoder.decode(characterName, StandardCharsets.UTF_8.toString());
			File file = new File(characterName + ".txt");
			boolean isFirstLink = !file.exists();
			try (PrintWriter out = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
				if (isFirstLink || !fileContainsType(file, typeEquipment)) {
					out.println(typeEquipment + ":");
				}
				out.println(link);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean fileContainsType(File file, String type) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(type)) {
					return true;
				}
			}
		}
		return false;
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

	private void checkBoxConfig(String link, Long value) {
		String[] linkParts = link.split(" - ");
		String jewelName = linkParts[0]; // Nome da gema
		String linkOnly = linkParts.length > 1 ? linkParts[1] : null; // Link de negociação

		Button linkButton = new Button(jewelName);
		linkButton.setPadding(new Insets(10, 10, 10, 10));
		linkButton.setPrefWidth(465);
		linkButton.getStyleClass().add("meuBotao");

		// Crie um ObjectProperty para armazenar o link no botão
		ObjectProperty<String> linkProperty = new SimpleObjectProperty<>();
		linkButton.setUserData(linkProperty);

		linkButton.setOnAction(e -> {
			clickedButton = linkButton;
			startGlobalCooldown();
			try {
				Desktop.getDesktop().browse(new URI(linkProperty.get()));
			} catch (IOException | URISyntaxException ex) {
				ex.printStackTrace();
			}
		});

		// Inicie o cooldown no botão assim que ele é gerado, exceto para o primeiro
		// botão
		if (buttons.size() > 0 && buttons.get(buttons.size() - 1).isDisabled()) {
			linkButton.setDisable(true);
		}

		buttons.add(linkButton);

		// Atualize o ObjectProperty com o link
		linkProperty.set(linkOnly);

		listView.getItems().add(new HBox(linkButton));

		// Atualize o progresso da barra de progresso
		double progress = listView.getItems().size() / (double) value;
		loadingProgressBar.setProgress(progress);

		// Crie uma nova Timeline para animar a barra de progresso
		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.millis(1000), new KeyValue(loadingProgressBar.progressProperty(), progress)));
		timeline.play(); // Inicie a animação
		if (listView.getItems().size() == value) {
			statusLabel.setText("Done !");
			statusLabel.setPadding(new Insets(50, 0, 0, 0));
			statusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: green;");
			statusLabel.setVisible(true);
			loadingProgressBar.setVisible(false);

			progressIndicator.setVisible(false);
			progressIndicator.setProgress(0);
		}
	}

	private void startGlobalCooldown() {
		// Desabilite o botão que foi clicado e todos os botões que foram gerados após
		// ele
		for (Button button : buttons) {
			button.setDisable(true);
		}

		// Inicie o cooldown global
		globalCooldownTimeline = new Timeline(new KeyFrame(globalCooldown, event -> {
			// Reabilite todos os botões que foram gerados após o botão que foi clicado
			for (Button button : buttons) {
				button.setDisable(false);

				// Adicione uma cor de fundo verde ao botão que foi clicado quando ele voltar a
				// ficar disponível
				if (button == clickedButton) {
					button.setStyle("-fx-background-color: #90ee90;");
				}
			}
		}));
		globalCooldownTimeline.play();
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
