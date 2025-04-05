package org.resend.mailclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.resend.mailclient.service.ConfigService;

public class ConfigController {
    @FXML private TextField apiKeyField;
    @FXML private TextField defaultFromField;
    @FXML private TextField senderNameField;

    private Stage dialogStage;
    private final ConfigService configService = new ConfigService();

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void initialize() {
        // 加载现有配置
        apiKeyField.setText(configService.getApiKey());
        defaultFromField.setText(configService.getDefaultFrom());
        senderNameField.setText(configService.getSenderName());
    }

    @FXML
    private void handleSave() {
        configService.saveConfig(
                apiKeyField.getText(),
                defaultFromField.getText(),
                senderNameField.getText()
        );
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}