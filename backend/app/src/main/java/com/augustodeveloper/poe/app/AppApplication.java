package com.augustodeveloper.poe.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.augustodeveloper.poe.app.gui.FrontGUI;

import javafx.application.Application;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		Application.launch(FrontGUI.class, args);
	}

}
