module org.resend.mailclient {
    // 基础模块
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web; // 明确声明web模块

    // 第三方依赖
    requires resend.java;

    // 开放控制器包权限
    opens org.resend.mailclient.controller to javafx.fxml;

    // 开放主包权限
    opens org.resend.mailclient to javafx.fxml;

    // 导出主包
    exports org.resend.mailclient;
}