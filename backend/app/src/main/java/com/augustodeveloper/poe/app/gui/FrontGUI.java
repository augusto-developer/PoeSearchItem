package com.augustodeveloper.poe.app.gui;

import java.util.List;
import java.util.Map;

import com.augustodeveloper.poe.app.entities.itens.Equipment;
import com.augustodeveloper.poe.app.exec.AccessoryExec;
import com.augustodeveloper.poe.app.services.PoeNinjaService;
import com.augustodeveloper.poe.app.services.PoeTradeService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FrontGUI extends Application {

	private TextField textField;
    private Button tradeButton;
    private PoeNinjaService poeNinjaService;
    private PoeTradeService poeTradeService;
    private AccessoryExec accessoryExec;
    private String apiUrlPoeNinja;
    private String apiUrlPoeTrade;
    private String storedUrl;
    
    private Equipment equipments;
    
    public FrontGUI() {
        this.equipments = new Equipment();
    }
    
	@Override
	public void start(Stage primaryStage) {
		poeNinjaService = new PoeNinjaService();
        poeTradeService = new PoeTradeService();
        
       
        accessoryExec = new AccessoryExec();

        textField = new TextField();
        tradeButton = new Button("Trade");

        tradeButton.setOnAction(e -> {
            String url = textField.getText();
            try {
                apiUrlPoeNinja = poeNinjaService.handleRequest(url);
                System.out.println("API URL Ninja: " + apiUrlPoeNinja);
                apiUrlPoeTrade = poeTradeService.makeRequest(accessoryExec.run());
                System.out.println("API URL Trade: " + apiUrlPoeTrade);            
                storedUrl = apiUrlPoeNinja;
                
                Map<String, List<Equipment>> equipmentInfo = equipments.getEquipmentInfo(storedUrl);
                System.out.println("Equipment Info: " + equipmentInfo);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
       
        
        VBox vbox = new VBox(textField, tradeButton);
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        Scene scene = new Scene(vbox, 300, 200);

        primaryStage.setTitle("FrontGUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
	
    public String getStoredUrl() {
        return storedUrl;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
