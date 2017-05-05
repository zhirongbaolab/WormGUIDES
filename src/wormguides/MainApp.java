/*
 * Bao Lab 2017
 */

package wormguides;

import java.io.IOException;
import java.time.Instant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import wormguides.controllers.RootLayoutController;
import wormguides.resources.NucleiMgrAdapterResource;

import static java.time.Duration.between;
import static java.time.Instant.now;

import static javafx.application.Platform.setImplicitExit;

import static wormguides.loaders.ImageLoader.loadImages;

/**
 * Driver class for the WormGUIDES desktop application
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    private static NucleiMgrAdapterResource nucleiMgrAdapterResource;

    private Scene scene;

    private BorderPane rootLayout;

    private RootLayoutController controller;

    public static void startProgramatically(final String[] args, final NucleiMgrAdapterResource nmar) {
        nucleiMgrAdapterResource = nmar;
        launch(args);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        System.out.println("Starting WormGUIDES JavaFX application");

        loadImages();

        MainApp.primaryStage = primaryStage;
        MainApp.primaryStage.setTitle("WormGUIDES");

        final Instant start = now();
        initRootLayout();
        final Instant end = now();
        System.out.println("Root layout initialized in "
                + between(start, end).toMillis()
                + "ms");

        primaryStage.setResizable(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            if (controller != null) {
                controller.initCloseApplication();
            }
        });
    }

    public void initRootLayout() {
        // Load root layout from FXML file.
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("view/layouts/RootLayout.fxml"));

        if (nucleiMgrAdapterResource != null) {
            loader.setResources(nucleiMgrAdapterResource);
            setImplicitExit(false);
        }

        controller = new RootLayoutController();
        controller.setStage(primaryStage);
        loader.setController(controller);
        loader.setRoot(controller);

        try {
            rootLayout = loader.load();

            scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();

            final Parent root = scene.getRoot();
            for (Node node : root.getChildrenUnmodifiable()) {
                node.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
            }

        } catch (IOException e) {
            System.out.println("Could not initialize root layout");
            e.printStackTrace();
        }
    }
}