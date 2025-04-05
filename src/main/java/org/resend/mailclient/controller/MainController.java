package org.resend.mailclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import org.resend.mailclient.service.ConfigManager;
import org.resend.mailclient.service.ResendService;

public class MainController {
    // UI组件
    @FXML private TextField apiKeyField;
    @FXML private TextField senderNameField;
    @FXML private TextField senderEmailField;
    @FXML private TextField recipientField;
    @FXML private TextField subjectField;
    @FXML private HTMLEditor htmlEditor;
    @FXML private Label statusLabel;

    private final ResendService resendService = new ResendService();

    @FXML
    public void initialize() {
        loadConfig();
    }

    private void loadConfig() {
        apiKeyField.setText(ConfigManager.get("api.key"));
        senderNameField.setText(ConfigManager.get("sender.name"));
        senderEmailField.setText(ConfigManager.get("sender.email"));
    }

    @FXML
    private void handleSaveConfig() {
        ConfigManager.save("api.key", apiKeyField.getText().trim());
        ConfigManager.save("sender.name", senderNameField.getText().trim());
        ConfigManager.save("sender.email", senderEmailField.getText().trim());
        statusLabel.setText("配置已保存！");
    }

    @FXML
    private void handleSendEmail() {
        if (apiKeyField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "必须填写API密钥");
            return;
        }

        String fromName = senderNameField.getText();
        String fromEmail = senderEmailField.getText();
        String to = recipientField.getText();
        String subject = subjectField.getText();
        String html = htmlEditor.getHtmlText();

        new Thread(() -> {
            try {
                resendService.sendEmail(fromName, fromEmail, to, subject, html);
                updateStatus("发送成功！");
            } catch (Exception e) {
                updateStatus("发送失败: " + e.getMessage());
            }
        }).start();
    }

    private void updateStatus(String message) {
        javafx.application.Platform.runLater(() -> statusLabel.setText(message));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}