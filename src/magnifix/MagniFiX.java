/*
 * Magnifier in JavaFX
 */
import insidefx.undecorator.Undecorator;
import insidefx.undecorator.UndecoratorScene;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author in-sideFX
 */
public class MagniFiX extends Application {

    Stage stage;
    Robot robot = null;

    @FXML
    Circle circleSrc;
    @FXML
    Circle circleDest;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        primaryStage.setTitle("MagniFiX: JavaFX Magnifier");

        // The UI (Client Area) to display
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/magnifix/fxml/ClientArea.fxml"));
        fxmlLoader.setController(this);
        Region root = (Region) fxmlLoader.load();

        // The Undecorator as a Scene
        final UndecoratorScene undecoratorScene = new UndecoratorScene(primaryStage, root);
        // Overrides defaults
        undecoratorScene.addStylesheet("/magnifix/fxml/demoapp.css");
        // Enable fade transition
        undecoratorScene.setFadeInTransition();
        initMagnifier();

        /*
         * Fade out transition on window closing request
         */
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                we.consume();   // Do not hide yet
                undecoratorScene.setFadeOutTransition();
            }
        });

        // Application icons
        Image image = new Image("/demoapp/in-sidefx.png");
        primaryStage.getIcons().addAll(image);

        primaryStage.setScene(undecoratorScene);
        primaryStage.sizeToScene();
        primaryStage.toFront();

        // Set minimum size based on client area's minimum sizes
        Undecorator undecorator = undecoratorScene.getUndecorator();
        primaryStage.setMinWidth(undecorator.getMinWidth());
        primaryStage.setMinHeight(undecorator.getMinHeight());

        primaryStage.show();
    }

    void initMagnifier() {

        try {
            robot = new Robot();
        } catch (AWTException ex) {
        }

        stage.xProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fireMagnifier();
            }
        });
        stage.yProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fireMagnifier();
            }
        });
        circleDest.fillProperty().addListener(new ChangeListener<Paint>() {

            @Override
            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                if (newValue == null) {
                    Bounds b = new BoundingBox(
                            (int) (circleSrc.getCenterX() - circleSrc.getRadius() / 2),
                            (int) (circleSrc.getCenterY() - circleSrc.getRadius() / 2),
                            (int) (circleSrc.getRadius()),
                            (int) (circleSrc.getRadius()));

                    Bounds localToScreen = circleSrc.localToScreen(b);

                    Rectangle r = new Rectangle(
                            (int) localToScreen.getMinX(),
                            (int) localToScreen.getMinY(),
                            (int) localToScreen.getWidth(),
                            (int) localToScreen.getHeight());
                    if (r.width > 0 && r.height > 0) {
                        BufferedImage screenShot = robot.createScreenCapture(r);
                        Image background = SwingFXUtils.toFXImage(screenShot, null);
                        ImagePattern imagePattern = new ImagePattern(background);
                        circleDest.setFill(imagePattern);
                    } else {
                        circleDest.setFill(Color.TRANSPARENT);
                    }
                }
            }
        });
        ReadOnlyDoubleProperty heightProperty = ((Pane) circleDest.getParent()).heightProperty();
        ReadOnlyDoubleProperty widthProperty = ((Pane) circleDest.getParent()).widthProperty();
        circleDest.radiusProperty().bind(Bindings.min(heightProperty.divide(2).subtract(15), widthProperty.divide(3).subtract(30)));
    }

    void fireMagnifier() {
        circleDest.setFill(null);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
