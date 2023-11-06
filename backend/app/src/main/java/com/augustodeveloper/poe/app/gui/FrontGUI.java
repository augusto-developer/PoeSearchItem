package com.augustodeveloper.poe.app.gui;

import java.util.Optional;

import com.augustodeveloper.poe.app.exec.AccessoryExec;
import com.augustodeveloper.poe.app.services.PoeTradeService;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FrontGUI extends Application{
	
	private PoeTradeService service;
    private AccessoryExec accessoryExec;

    public static void main(String[] args) {
        Application.launch(FrontGUI.class, args);
    }
    
    private Label labelResult;
    
    @Override
    public void start(Stage primaryStage) {
        service = new PoeTradeService();
        String[] args = null;
        accessoryExec = new AccessoryExec(args);

        Button buttonAcessorios = new Button("Accessory");
        labelResult = new Label();
        Button buttonCopy = new Button("Copy URL");

        buttonAcessorios.setOnAction(e -> {
            TextArea textArea = new TextArea();
            textArea.setPrefSize(300, 100);

            ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
            Dialog<String> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
            dialog.getDialogPane().setContent(textArea);
            dialog.setTitle("Input Dialog");

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonTypeOk) {
                    return textArea.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                
                try {
                	accessoryExec.run(name);
                    String searchUrl = service.makeRequest(accessoryExec.getJson().toString());
                    System.out.println(searchUrl);

                    // Crie um novo Stage para a janela de resultados
                    Stage resultStage = new Stage();
                    resultStage.setTitle("Result");

                    labelResult.setText(searchUrl);
                    buttonCopy.setOnAction(ee -> {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(labelResult.getText());
                        clipboard.setContent(content);
                    });

                    VBox vbox = new VBox(labelResult, buttonCopy);
                    Scene scene = new Scene(vbox, 400, 400);
                    resultStage.setScene(scene);
                    resultStage.show();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        VBox vbox = new VBox(buttonAcessorios, buttonCopy);
        Scene scene = new Scene(vbox, 400, 400);

        primaryStage.setTitle("Aplicação");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
