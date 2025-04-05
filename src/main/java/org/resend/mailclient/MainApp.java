package org.resend.mailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // 1. 验证FXML文件是否存在
            URL fxmlUrl = getClass().getResource("/view/main.fxml");
            if (fxmlUrl == null) {
                throw new FileNotFoundException("无法找到FXML文件，请检查路径: /view/main.fxml");
            }

            // 2. 加载FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // 3. 设置场景
            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("Resend邮件客户端");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            // 4. 错误处理
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("致命错误");
            alert.setHeaderText("应用程序启动失败");
            alert.setContentText("原因: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}