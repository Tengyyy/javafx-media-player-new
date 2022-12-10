package hans.Menu;

import hans.*;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ActiveBox extends StackPane {

    double animationSpeed = 200;
    MenuController menuController;

    ActiveBox(MenuController menuController){
        this.menuController = menuController;
        this.setAlignment(Pos.BOTTOM_CENTER);
        this.setMinHeight(90);
        this.setPrefHeight(90);
        this.setMaxHeight(90);
    }

    public void clear(){
        if(this.getChildren().isEmpty()) return;

        menuController.captionsController.clearCaptions();

        menuController.activeItem = null;

        if(menuController.mainController.miniplayerActive){
            menuController.mainController.miniplayerActiveText.setVisible(false);
        }

        ActiveItem child = (ActiveItem) this.getChildren().get(0);
        FadeTransition fadeTransition = AnimationsClass.fadeOut(child);
        fadeTransition.setOnFinished(e -> {
            this.getChildren().clear();
            menuController.animationsInProgress.remove(fadeTransition);
        });

        menuController.animationsInProgress.add(fadeTransition);
        fadeTransition.playFromStart();
    }


    public void set(ActiveItem activeItem, boolean pause){

        menuController.captionsController.clearCaptions();
        menuController.activeItem = activeItem;

        if(this.getChildren().isEmpty()){
            // add new item

            // pause transitions are necessary to line up this animation with queue and history animations which are longer
            PauseTransition pause1 = new PauseTransition();
            if(pause) pause1.setDuration(Duration.millis(animationSpeed));
            else pause1.setDuration(Duration.ZERO);

            PauseTransition pause2 = new PauseTransition();
            if(pause) pause2.setDuration(Duration.millis(animationSpeed));
            else pause2.setDuration(Duration.ZERO);


            pause1.setOnFinished(l -> {
                menuController.animationsInProgress.remove(pause1);
                menuController.animationsInProgress.add(pause2);
                pause2.playFromStart();
            });

            pause2.setOnFinished(e -> {
                this.getChildren().add(activeItem);
                initialize(activeItem);
                FadeTransition fadeTransition = AnimationsClass.fadeIn(activeItem);

                menuController.animationsInProgress.remove(pause2);
                fadeTransition.playFromStart();
            });

            menuController.animationsInProgress.add(pause1);
            pause1.playFromStart();
        }
        else {
            // remove old, add new item

            ActiveItem oldChild = (ActiveItem) this.getChildren().get(0);
            FadeTransition fadeOut = AnimationsClass.fadeOut(oldChild);
            fadeOut.setOnFinished(e -> {
                PauseTransition pauseTransition = new PauseTransition();
                if(pause) pauseTransition.setDuration(Duration.millis(animationSpeed));
                else pauseTransition.setDuration(Duration.ZERO);

                pauseTransition.setOnFinished(h -> {
                    getChildren().set(0, activeItem);
                    initialize(activeItem);
                    FadeTransition fadeIn = AnimationsClass.fadeIn(activeItem);

                    menuController.animationsInProgress.remove(pauseTransition);
                    fadeIn.playFromStart();
                });

                menuController.animationsInProgress.remove(fadeOut);
                menuController.animationsInProgress.add(pauseTransition);
                pauseTransition.playFromStart();
            });

            menuController.animationsInProgress.add(fadeOut);
            fadeOut.playFromStart();
        }
    }


    public void initialize(ActiveItem activeItem){
        Platform.runLater(() -> {
            activeItem.activeItemContextMenu = new ActiveItemContextMenu(activeItem, menuController.captionsController);
            activeItem.playButtonTooltip = new ControlTooltip(menuController.mainController, "Play video", activeItem.playButton, 1000);
            activeItem.removeButtonTooltip = new ControlTooltip(menuController.mainController, "Remove video", activeItem.removeButton, 1000);
            activeItem.optionsButtonTooltip = new ControlTooltip(menuController.mainController, "Options", activeItem.optionsButton, 1000);

            menuController.mediaInterface.createMedia(activeItem);
        });
    }

}
