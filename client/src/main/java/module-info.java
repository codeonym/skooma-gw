module client {
    requires javafx.controls;
    requires javafx.fxml;
    requires shared;
    requires org.slf4j;
    requires java.rmi;
    requires java.naming;
    requires java.transaction.xa;
    requires static lombok;
    requires de.jensd.fx.glyphs.fontawesome;
    requires MaterialFX;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.materialdesign2;
    requires java.desktop;
    requires VirtualizedFX;
    requires jdk.localedata;
    requires javafx.graphics;
    requires fr.brouillard.oss.cssfx;
    requires java.logging;

    opens com.m2i.client.gui.controllers to javafx.fxml, shared;
    exports com.m2i.client;
    exports com.m2i.client.gui.controllers.coordinator;
    exports com.m2i.client.gui.controllers.student;
    exports com.m2i.client.gui.controllers.teacher;
    exports com.m2i.client.gui.controllers.common;

    opens com.m2i.client.gui.controllers.common to javafx.fxml, shared;
    opens com.m2i.client.gui.controllers.student to javafx.fxml, shared;
    opens com.m2i.client.gui.controllers.teacher to javafx.fxml, shared;
    opens com.m2i.client.gui.controllers.coordinator to javafx.fxml, shared;
}