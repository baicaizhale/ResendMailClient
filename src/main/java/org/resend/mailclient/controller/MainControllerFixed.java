package org.resend.mailclient.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.HTMLEditor;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.resend.mailclient.model.Email;
import org.resend.mailclient.model.EmailConfig;
import org.resend.mailclient.model.EmailTemplate;
import org.resend.mailclient.service.ConfigManager;
import org.resend.mailclient.service.ResendServiceNew;
import org.resend.mailclient.service.events.EmailSentEvent;
import org.resend.mailclient.service.events.StatusUpdateEvent;
import org.resend.mailclient.service.events.TemplateLoadedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 主控制器类，处理邮件客户端的主要功能
 */
public class MainControllerFixed {
    private static final Logger logger = LogManager.getLogger(MainControllerFixed.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // UI组件 - 邮件配置
    @FXML private PasswordField apiKeyField;
    @FXML private TextField senderNameField;
    @FXML private TextField senderEmailField;
    @FXML private TextField defaultRecipientField;

    // UI组件 - 邮件内容
    @FXML private TextField recipientField;
    @FXML private TextField subjectField;
    @FXML private HTMLEditor htmlEditor;

    // UI组件 - 状态栏
    @FXML private Label statusLabel;

    // UI组件 - 邮件历史
    @FXML private TableView<Email> emailHistoryTable;
    @FXML private TableColumn<Email, String> statusColumn;
    @FXML private TableColumn<Email, String> subjectColumn;
    @FXML private TableColumn<Email, String> recipientColumn;
    @FXML private TableColumn<Email, String> dateColumn;
    @FXML private TableColumn<Email, String> actionColumn;

    // UI组件 - 模板管理
    @FXML private ListView<EmailTemplate> templateListView;
    @FXML private TextField templateNameField;
    @FXML private TextField templateSubjectField;
    @FXML private HTMLEditor templateHtmlEditor;

    // 数据模型
    private ObservableList<Email> emailHistory = FXCollections.observableArrayList();
    private ObservableList<EmailTemplate> templates = FXCollections.observableArrayList();
    private EmailConfig emailConfig;

    // 服务类
    private final ResendServiceNew resendService = new ResendServiceNew();

    /**
     * 初始化控制器
     */
    @FXML
    public void initialize() {
        // 注册事件总线
        EventBus.getDefault().register(this);

        // 加载配置
        loadConfig();

        // 初始化邮件历史表格
        setupEmailHistoryTable();

        // 初始化模板列表
        setupTemplateListView();

        // 加载模板数据
        loadTemplates();

        // 设置默认值
        setDefaultValues();

        logger.info("主控制器初始化完成");
    }

    /**
     * 设置邮件历史表格
     */
    private void setupEmailHistoryTable() {
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        recipientColumn.setCellValueFactory(new PropertyValueFactory<>("to"));
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getSentAt();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });

        actionColumn.setCellFactory(column -> {
            TableCell<Email, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Button viewButton = new Button("查看");
                        viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        viewButton.setOnAction(event -> viewEmail(getTableView().getItems().get(getIndex())));
                        setGraphic(viewButton);
                        setText(null);
                    }
                }
            };
            return cell;
        });

        emailHistoryTable.setItems(emailHistory);
    }

    /**
     * 设置模板列表视图
     */
    private void setupTemplateListView() {
        templateListView.setItems(templates);
        templateListView.setCellFactory(param -> new ListCell<EmailTemplate>() {
            @Override
            protected void updateItem(EmailTemplate template, boolean empty) {
                super.updateItem(template, empty);
                if (empty || template == null) {
                    setText(null);
                } else {
                    setText(template.getName());
                }
            }
        });

        templateListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> selectTemplate(newValue));
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        emailConfig = new EmailConfig();
        emailConfig.setApiKey(ConfigManager.get("api.key"));
        emailConfig.setSenderName(ConfigManager.get("sender.name"));
        emailConfig.setSenderEmail(ConfigManager.get("sender.email"));
        emailConfig.setDefaultRecipient(ConfigManager.get("default.recipient"));

        // 更新UI
        Platform.runLater(() -> {
            apiKeyField.setText(emailConfig.getApiKey());
            senderNameField.setText(emailConfig.getSenderName());
            senderEmailField.setText(emailConfig.getSenderEmail());
            defaultRecipientField.setText(emailConfig.getDefaultRecipient());
        });
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues() {
        if (recipientField.getText().isEmpty() && emailConfig.getDefaultRecipient() != null) {
            recipientField.setText(emailConfig.getDefaultRecipient());
        }
    }

    /**
     * 加载模板数据
     */
    private void loadTemplates() {
        // 这里应该从文件或数据库加载模板
        // 目前使用示例数据
        EmailTemplate template1 = new EmailTemplate("欢迎邮件", "欢迎加入我们", "<h1>欢迎加入我们的平台</h1><p>尊敬的用户，感谢您的注册！</p>");
        EmailTemplate template2 = new EmailTemplate("订单确认", "您的订单已确认", "<h1>订单确认</h1><p>您的订单已确认，订单号：{orderNumber}</p>");

        templates.addAll(template1, template2);
    }

    /**
     * 选择模板
     */
    private void selectTemplate(EmailTemplate template) {
        if (template != null) {
            templateNameField.setText(template.getName());
            templateSubjectField.setText(template.getSubject());
            templateHtmlEditor.setHtmlText(template.getHtmlContent());

            EventBus.getDefault().post(new TemplateLoadedEvent(template));
        }
    }

    // ========== 事件处理方法 ==========

    /**
     * 保存配置
     */
    @FXML
    private void handleSaveConfig() {
        emailConfig.setApiKey(apiKeyField.getText().trim());
        emailConfig.setSenderName(senderNameField.getText().trim());
        emailConfig.setSenderEmail(senderEmailField.getText().trim());
        emailConfig.setDefaultRecipient(defaultRecipientField.getText().trim());

        // 保存到配置文件
        ConfigManager.save("api.key", emailConfig.getApiKey());
        ConfigManager.save("sender.name", emailConfig.getSenderName());
        ConfigManager.save("sender.email", emailConfig.getSenderEmail());
        ConfigManager.save("default.recipient", emailConfig.getDefaultRecipient());

        updateStatus("配置已保存！");
        logger.info("配置已保存: {}", emailConfig);
    }

    /**
     * 验证API密钥
     */
    @FXML
    private void handleVerifyApiKey() {
        String apiKey = apiKeyField.getText().trim();
        if (apiKey.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "API密钥不能为空");
            return;
        }

        new Thread(() -> {
            try {
                boolean isValid = resendService.verifyApiKey(apiKey);
                Platform.runLater(() -> {
                    if (isValid) {
                        updateStatus("API密钥验证成功！");
                        showAlert(Alert.AlertType.INFORMATION, "成功", "API密钥有效");
                    } else {
                        updateStatus("API密钥验证失败！");
                        showAlert(Alert.AlertType.ERROR, "错误", "API密钥无效");
                    }
                });
            } catch (Exception e) {
                logger.error("API密钥验证失败", e);
                Platform.runLater(() -> {
                    updateStatus("API密钥验证出错: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "错误", "验证API密钥时出错: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 发送邮件
     */
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

        if (to.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "必须填写收件人");
            return;
        }

        if (subject.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "必须填写主题");
            return;
        }

        if (html.isEmpty() || html.equals("<html dir="ltr"><head></head><body contenteditable="true"></body></html>")) {
            showAlert(Alert.AlertType.ERROR, "错误", "邮件内容不能为空");
            return;
        }

        // 创建邮件对象
        Email email = new Email(fromName, fromEmail, List.of(to.split(";")), subject, html);
        emailHistory.add(0, email); // 添加到历史记录顶部

        // 在新线程中发送邮件
        new Thread(() -> {
            try {
                updateStatus("正在发送邮件...");
                String emailId = resendService.sendEmail(fromName, fromEmail, to, subject, html);

                // 更新邮件状态
                email.setId(emailId);
                email.setStatus("SENT");
                email.setSentAt(LocalDateTime.now());

                Platform.runLater(() -> {
                    updateStatus("发送成功！ID: " + emailId);
                    showAlert(Alert.AlertType.INFORMATION, "成功", "邮件发送成功");
                });

                // 发送事件
                EventBus.getDefault().post(new EmailSentEvent(email, true));

            } catch (Exception e) {
                email.setStatus("FAILED");
                email.setErrorMessage(e.getMessage());

                logger.error("邮件发送失败", e);
                Platform.runLater(() -> {
                    updateStatus("发送失败: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "错误", "邮件发送失败: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 保存草稿
     */
    @FXML
    private void handleSaveDraft() {
        try {
            // 创建邮件对象
            String fromName = senderNameField.getText();
            String fromEmail = senderEmailField.getText();
            String to = recipientField.getText();
            String subject = subjectField.getText();
            String html = htmlEditor.getHtmlText();

            // 验证必填字段
            if (fromName.isEmpty() || fromEmail.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "必须填写发件人信息");
                return;
            }

            // 创建邮件对象并保存
            Email email = new Email(fromName, fromEmail, to.isEmpty() ? new ArrayList<>() : List.of(to.split(";")), 
                                  subject.isEmpty() ? "无主题" : subject, html);
            email.setStatus("DRAFT");

            // 保存草稿
            resendService.saveDraft(email);

            updateStatus("草稿已保存");
            logger.info("草稿已保存: {}", subject.isEmpty() ? "无主题" : subject);
            showAlert(Alert.AlertType.INFORMATION, "成功", "草稿已保存");
        } catch (Exception e) {
            logger.error("保存草稿失败", e);
            updateStatus("保存草稿失败: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "错误", "保存草稿失败: " + e.getMessage());
        }
    }

    /**
     * 预览邮件
     */
    @FXML
    private void handlePreviewEmail() {
        try {
            // 获取邮件内容
            String subject = subjectField.getText();
            String html = htmlEditor.getHtmlText();

            if (html.isEmpty() || html.equals("<html dir="ltr"><head></head><body contenteditable="true"></body></html>")) {
                showAlert(Alert.AlertType.WARNING, "提示", "邮件内容为空，无法预览");
                return;
            }

            // 创建预览窗口
            Alert previewAlert = new Alert(Alert.AlertType.INFORMATION);
            previewAlert.setTitle("邮件预览");
            previewAlert.setHeaderText("主题: " + (subject.isEmpty() ? "无主题" : subject));

            // 创建WebView用于预览HTML内容
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.getEngine().loadContent(html);
            webView.setPrefSize(600, 400);

            // 将WebView添加到对话框
            previewAlert.getDialogPane().setContent(webView);
            previewAlert.getButtonTypes().setAll(ButtonType.OK);

            // 显示预览
            previewAlert.showAndWait();
            updateStatus("邮件预览已关闭");
        } catch (Exception e) {
            logger.error("预览邮件失败", e);
            updateStatus("预览邮件失败: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "错误", "预览邮件失败: " + e.getMessage());
        }
    }

    /**
     * 从模板添加内容
     */
    @FXML
    private void handleAddFromTemplate() {
        if (templateListView.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "提示", "请先选择一个模板");
            return;
        }

        EmailTemplate template = templateListView.getSelectionModel().getSelectedItem();
        String currentContent = htmlEditor.getHtmlText();

        // 将模板内容添加到当前内容
        // 更安全的处理方式：在body标签内的最后添加模板内容
        String templateContent = template.getHtmlContent();
        String bodyEndTag = "</body>";

        if (currentContent.contains(bodyEndTag)) {
            String newContent = currentContent.replace(bodyEndTag, templateContent + bodyEndTag);
            htmlEditor.setHtmlText(newContent);
            updateStatus("已添加模板内容");
        } else {
            // 如果找不到body结束标签，直接追加内容
            htmlEditor.setHtmlText(currentContent + templateContent);
            updateStatus("已添加模板内容（追加模式）");
        }
    }

    /**
     * 应用模板
     */
    @FXML
    private void handleApplyTemplate() {
        if (templateListView.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "提示", "请先选择一个模板");
            return;
        }

        EmailTemplate template = templateListView.getSelectionModel().getSelectedItem();
        subjectField.setText(template.getSubject());
        htmlEditor.setHtmlText(template.getHtmlContent());
        updateStatus("已应用模板: " + template.getName());
    }

    /**
     * 新建模板
     */
    @FXML
    private void handleNewTemplate() {
        templateNameField.clear();
        templateSubjectField.clear();
        templateHtmlEditor.setHtmlText("");
        templateListView.getSelectionModel().clearSelection();
        updateStatus("已创建新模板");
    }

    /**
     * 保存模板
     */
    @FXML
    private void handleSaveTemplate() {
        try {
            String name = templateNameField.getText();
            String subject = templateSubjectField.getText();
            String html = templateHtmlEditor.getHtmlText();

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "模板名称不能为空");
                return;
            }

            if (html.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "错误", "模板内容不能为空");
                return;
            }

            // 创建模板对象
            EmailTemplate template = new EmailTemplate(name, subject, html);

            // 保存模板
            resendService.saveTemplate(template);

            // 刷新模板列表
            loadTemplates();

            // 选择新保存的模板
            for (int i = 0; i < templates.size(); i++) {
                if (templates.get(i).getName().equals(name)) {
                    templateListView.getSelectionModel().select(i);
                    break;
                }
            }

            updateStatus("模板已保存: " + name);
            logger.info("模板已保存: {}", name);
            showAlert(Alert.AlertType.INFORMATION, "成功", "模板已保存");
        } catch (Exception e) {
            logger.error("保存模板失败", e);
            updateStatus("保存模板失败: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "错误", "保存模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @FXML
    private void handleDeleteTemplate() {
        EmailTemplate selectedTemplate = templateListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "提示", "请先选择要删除的模板");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确定要删除模板 '" + selectedTemplate.getName() + "' 吗？");
        confirmAlert.setContentText("此操作不可撤销。");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                // 删除模板
                resendService.deleteTemplate(selectedTemplate.getId());

                // 刷新模板列表
                loadTemplates();

                updateStatus("模板已删除: " + selectedTemplate.getName());
                logger.info("模板已删除: {}", selectedTemplate.getName());
                showAlert(Alert.AlertType.INFORMATION, "成功", "模板已删除");
            } catch (Exception e) {
                logger.error("删除模板失败", e);
                updateStatus("删除模板失败: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "错误", "删除模板失败: " + e.getMessage());
            }
        }
    }

    /**
     * 刷新邮件历史
     */
    @FXML
    private void handleRefreshHistory() {
        try {
            // 获取最新的邮件历史
            List<Email> latestHistory = resendService.getEmailHistory();
            emailHistory.clear();
            emailHistory.addAll(latestHistory);

            updateStatus("邮件历史已刷新");
            logger.info("邮件历史已刷新，共 {} 条记录", latestHistory.size());
        } catch (Exception e) {
            logger.error("刷新邮件历史失败", e);
            updateStatus("刷新邮件历史失败: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "错误", "刷新邮件历史失败: " + e.getMessage());
        }
    }

    /**
     * 清空邮件历史
     */
    @FXML
    private void handleClearHistory() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认清空");
        confirmAlert.setHeaderText("确定要清空所有邮件历史记录吗？");
        confirmAlert.setContentText("此操作不可撤销。");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            try {
                // 清空邮件历史
                resendService.clearEmailHistory();

                // 刷新表格
                emailHistory.clear();

                updateStatus("邮件历史已清空");
                logger.info("邮件历史已清空");
                showAlert(Alert.AlertType.INFORMATION, "成功", "邮件历史已清空");
            } catch (Exception e) {
                logger.error("清空邮件历史失败", e);
                updateStatus("清空邮件历史失败: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "错误", "清空邮件历史失败: " + e.getMessage());
            }
        }
    }

    /**
     * 刷新模板列表
     */
    @FXML
    private void handleRefreshTemplates() {
        try {
            // 刷新模板列表
            loadTemplates();

            updateStatus("模板列表已刷新");
            logger.info("模板列表已刷新，共 {} 个模板", templates.size());
        } catch (Exception e) {
            logger.error("刷新模板列表失败", e);
            updateStatus("刷新模板列表失败: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "错误", "刷新模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 查看邮件
     */
    private void viewEmail(Email email) {
        try {
            // 创建预览窗口
            Alert viewAlert = new Alert(Alert.AlertType.INFORMATION);
            viewAlert.setTitle("查看邮件");
            viewAlert.setHeaderText("主题: " + email.getSubject());

            // 创建WebView用于预览HTML内容
            javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
            webView.getEngine().loadContent(email.getHtmlContent());
            webView.setPrefSize(600, 400);

            // 添加邮件信息
            VBox contentBox = new VBox(10);
            contentBox.getChildren().addAll(
                new Label("收件人: " + String.join(", ", email.getTo())),
                new Label("发送时间: " + email.getSentAt().format(DATE_FORMATTER)),
                new Label("状态: " + email.getStatus()),
                webView
            );

            viewAlert.getDialogPane().setContent(contentBox);
            viewAlert.getButtonTypes().setAll(ButtonType.OK);

            // 显示预览
            viewAlert.showAndWait();
            updateStatus("邮件预览已关闭");
        } catch (Exception e) {
            logger.error("查看邮件失败", e);
            updateStatus("查看邮件失败: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "错误", "查看邮件失败: " + e.getMessage());
        }
    }

    // ========== 事件处理 ==========

    /**
     * 处理状态更新事件
     */
    @Subscribe
    public void onStatusUpdate(StatusUpdateEvent event) {
        Platform.runLater(() -> {
            updateStatus(event.getMessage());
            if (event.isError()) {
                showAlert(Alert.AlertType.ERROR, "错误", event.getMessage());
            }
        });
    }

    /**
     * 处理邮件发送事件
     */
    @Subscribe
    public void onEmailSent(EmailSentEvent event) {
        Platform.runLater(() -> {
            if (event.isSuccess()) {
                updateStatus("邮件发送成功: " + event.getEmail().getId());
                showAlert(Alert.AlertType.INFORMATION, "成功", "邮件发送成功");
            } else {
                updateStatus("邮件发送失败: " + event.getErrorMessage());
                showAlert(Alert.AlertType.ERROR, "错误", "邮件发送失败: " + event.getErrorMessage());
            }
        });
    }

    /**
     * 处理模板加载事件
     */
    @Subscribe
    public void onTemplateLoaded(TemplateLoadedEvent event) {
        Platform.runLater(() -> {
            updateStatus("已加载模板: " + event.getTemplate().getName());
        });
    }

    // ========== 辅助方法 ==========

    /**
     * 更新状态栏
     */
    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
