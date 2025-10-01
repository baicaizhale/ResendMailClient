package org.resend.mailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Resend邮件客户端应用程序入口类
 */
public class ResendMailClient extends Application {
    private static final Logger logger = LogManager.getLogger(ResendMailClient.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载主界面FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            Parent root = loader.load();
            
            // 设置场景
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            
            // 配置主舞台
            primaryStage.setTitle("Resend邮件客户端");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            logger.info("应用程序启动成功");
        } catch (IOException e) {
            logger.error("应用程序启动失败", e);
            showErrorAlert("应用程序启动失败", e.getMessage());
        }
    }

    /**
     * 显示错误提示框
     * 
     * @param header 错误标题
     * @param content 错误内容
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 应用程序主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        launch(args);
    }
}