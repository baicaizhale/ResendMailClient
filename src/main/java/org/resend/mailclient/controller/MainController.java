package org.resend.mailclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import org.resend.mailclient.service.ResendService;
import java.io.InputStream;
import java.util.Properties;

public class MainController {
    @FXML private TextField senderNameField;
    @FXML private TextField senderField;
    @FXML private TextField recipientField;
    @FXML private TextField subjectField;
    @FXML private HTMLEditor htmlEditor;
    @FXML private Label statusLabel;

    private final ResendService resendService = new ResendService();

    @FXML
    private void initialize() {
        // 从配置文件加载默认值
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            senderNameField.setText(prop.getProperty("default.sender.name", ""));
            senderField.setText(prop.getProperty("default.from", ""));

            // 验证关键配置
            if (senderField.getText().isEmpty()) {
                showErrorAlert("配置错误", "未配置默认发件邮箱(default.from)");
            }

        } catch (Exception e) {
            System.err.println("加载配置失败: " + e.getMessage());
            senderNameField.setText("我的应用");
            senderField.setText("no-reply@example.com");
        }
    }

    @FXML
    private void sendEmail() {
        String fromName = senderNameField.getText().trim();
        String fromEmail = senderField.getText().trim();
        String to = recipientField.getText().trim();
        String subject = subjectField.getText().trim();
        String htmlContent = htmlEditor.getHtmlText();

        // 输入验证
        if (fromName.isEmpty() || fromEmail.isEmpty() || to.isEmpty() || subject.isEmpty() || htmlContent.isEmpty()) {
            showErrorAlert("输入错误", "所有带*字段必须填写");
            return;
        }

        if (!fromEmail.contains("@")) {
            showErrorAlert("格式错误", "发件邮箱格式不正确");
            return;
        }

        statusLabel.setText("发送中...");

        new Thread(() -> {
            try {
                resendService.sendEmail(fromName, fromEmail, to, subject, htmlContent);

                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("发送成功！");
                    clearForm();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("发送失败");
                    showErrorAlert("发送错误", "原因: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void clearForm() {
        recipientField.clear();
        subjectField.clear();
        htmlEditor.setHtmlText("");
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}