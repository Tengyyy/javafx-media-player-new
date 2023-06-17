package tengy.Windows.OpenSubtitles;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import tengy.ClearableTextFieldSkin;
import tengy.VisiblePasswordFieldSkin;
import tengy.SVG;
import tengy.Utilities;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static tengy.Utilities.keyboardFocusOff;
import static tengy.Utilities.keyboardFocusOn;

public class ConnectionPage extends VBox implements Page{

    public ScrollPane scrollPane;

    VBox content = new VBox();

    StackPane titleContainer = new StackPane();
    Label title = new Label("Connection");

    Button backButton = new Button();

    HBox usernameBox = new HBox();
    Label usernameLabel = new Label("Username:");
    public TextField usernameField = new TextField();

    HBox passwordBox = new HBox();
    Label passwordLabel = new Label("Password:");
    public PasswordField passwordField = new PasswordField();

    StackPane buttonPane = new StackPane();
    Button createAccountButton = new Button();
    public Button saveButton = new Button();

    BooleanProperty credentialsChanged = new SimpleBooleanProperty(false);
    public String username = "";
    public String password = "";

    OpenSubtitlesWindow openSubtitlesWindow;

    IntegerProperty focus = new SimpleIntegerProperty(-1);
    List<Node> focusNodes = new ArrayList<>();

    ConnectionPage(OpenSubtitlesWindow openSubtitlesWindow){
        this.openSubtitlesWindow = openSubtitlesWindow;
        this.setOpacity(0);

        titleContainer.setPadding(new Insets(15, 20, 15, 0));
        titleContainer.setOnMouseClicked(e -> openSubtitlesWindow.window.requestFocus());

        StackPane.setAlignment(title, Pos.CENTER_LEFT);
        StackPane.setMargin(title, new Insets(0, 0, 0, 50));
        title.getStyleClass().add("popupWindowTitle");

        titleContainer.getChildren().addAll(backButton, title);


        SVGPath backSVG = new SVGPath();
        backSVG.setContent(SVG.ARROW_LEFT.getContent());
        Region backIcon = new Region();
        backIcon.setShape(backSVG);
        backIcon.setPrefSize(20, 20);
        backIcon.setMaxSize(20, 20);
        backIcon.setMouseTransparent(true);
        backIcon.getStyleClass().add("graphic");

        backButton.setPrefWidth(25);
        backButton.setPrefHeight(25);

        backButton.getStyleClass().addAll("transparentButton", "popupWindowCloseButton");
        backButton.setFocusTraversable(false);
        backButton.setGraphic(backIcon);
        backButton.setOnAction(e -> openSubtitlesWindow.openSearchPage());
        backButton.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue){
                focus.set(0);
                openSubtitlesWindow.focus.set(0);
            }
            else{
                keyboardFocusOff(backButton);
                focus.set(-1);
                openSubtitlesWindow.focus.set(-1);
            }
        });

        backButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if(e.getCode() != KeyCode.SPACE) return;
            backButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        });

        backButton.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if(e.getCode() != KeyCode.SPACE) return;
            backButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
        });

        StackPane.setAlignment(backButton, Pos.CENTER_LEFT);
        StackPane.setMargin(backButton, new Insets(0, 0, 0, 10));




        scrollPane = new ScrollPane() {
            ScrollBar vertical;

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                if (vertical == null) {
                    vertical = (ScrollBar) lookup(".scroll-bar:vertical");
                    vertical.visibleProperty().addListener((obs, old, val) -> updatePadding(val));
                }
            }
        };

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("menuScroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.setContent(content);

        content.setPadding(new Insets(15, 20, 15, 15));
        content.setSpacing(15);
        content.getChildren().addAll(usernameBox, passwordBox, buttonPane);
        
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        usernameBox.setAlignment(Pos.CENTER);

        usernameLabel.setPrefWidth(170);
        usernameLabel.getStyleClass().add("settingsText");

        usernameField.setSkin(new ClearableTextFieldSkin(usernameField));
        usernameField.setPrefWidth(320);
        usernameField.getStyleClass().add("customTextField");
        usernameField.setPrefHeight(36);
        usernameField.setMinHeight(36);
        usernameField.setMaxHeight(36);
        usernameField.setFocusTraversable(false);
        usernameField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue){

                focus.set(1);
                openSubtitlesWindow.focus.set(0);
            }
            else {
                focus.set(-1);
                openSubtitlesWindow.focus.set(-1);
                keyboardFocusOff(usernameField);
            }
        });

        usernameField.textProperty().addListener((observableValue, oldValue, newValue) -> credentialsChanged.set(true));

        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        passwordBox.setAlignment(Pos.CENTER);

        passwordLabel.setPrefWidth(170);
        passwordLabel.getStyleClass().add("settingsText");

        passwordField.setSkin(new VisiblePasswordFieldSkin(passwordField));
        passwordField.setPrefWidth(320);
        passwordField.getStyleClass().add("customTextField");
        passwordField.setPrefHeight(36);
        passwordField.setMinHeight(36);
        passwordField.setMaxHeight(36);
        passwordField.setFocusTraversable(false);

        passwordField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue){

                focus.set(2);
                openSubtitlesWindow.focus.set(0);
            }
            else {
                focus.set(-1);
                openSubtitlesWindow.focus.set(-1);
                keyboardFocusOff(passwordField);
            }
        });

        passwordField.textProperty().addListener((observableValue, oldValue, newValue) -> credentialsChanged.set(true));

        buttonPane.getChildren().addAll(createAccountButton, saveButton);
        buttonPane.setPadding(new Insets(25, 40, 0, 35));

        StackPane.setAlignment(createAccountButton, Pos.CENTER_LEFT);
        createAccountButton.setText("Create account");
        createAccountButton.setPrefWidth(130);
        createAccountButton.getStyleClass().add("menuButton");
        createAccountButton.setOnAction(e -> {
            createAccountButton.requestFocus();

            // open opensubtitles account creation page in web browser
            Utilities.openBrowser("https://www.opensubtitles.org/en/newuser");
        });

        createAccountButton.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue){
                focus.set(3);
                openSubtitlesWindow.focus.set(0);
            }
            else{
                keyboardFocusOff(createAccountButton);
                focus.set(-1);
                openSubtitlesWindow.focus.set(-1);
            }
        });

        createAccountButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if(e.getCode() != KeyCode.SPACE) return;
            createAccountButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        });

        createAccountButton.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if(e.getCode() != KeyCode.SPACE) return;
            createAccountButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
        });


        StackPane.setAlignment(saveButton, Pos.CENTER_RIGHT);
        saveButton.setText("Save");
        saveButton.setPrefWidth(130);
        saveButton.getStyleClass().add("mainButton");
        saveButton.disableProperty().bind(credentialsChanged.not());
        saveButton.disabledProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue) focusNodes.remove(saveButton);
            else if(!focusNodes.contains(saveButton)) focusNodes.add(saveButton);
        });

        saveButton.setOnAction(e -> {
            saveButton.requestFocus();

            try {
                saveCredentials();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        saveButton.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue){
                focus.set(4);
                openSubtitlesWindow.focus.set(0);
            }
            else{
                keyboardFocusOff(saveButton);
                focus.set(-1);
                openSubtitlesWindow.focus.set(-1);
            }
        });

        saveButton.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if(e.getCode() != KeyCode.SPACE) return;
            saveButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);
        });

        saveButton.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if(e.getCode() != KeyCode.SPACE) return;
            saveButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
        });

        focusNodes.add(backButton);
        focusNodes.add(usernameField);
        focusNodes.add(passwordField);
        focusNodes.add(createAccountButton);

        this.setVisible(false);
        this.getChildren().addAll(titleContainer, scrollPane);
        StackPane.setMargin(this, new Insets(0, 0, 70, 0));
    }

    private void saveCredentials() throws IOException {

        this.username = usernameField.getText();
        this.password = passwordField.getText();

        File file = new File(new File(System.getProperty("user.home"), "FXPlayer"), "OpenSubtitles.txt");

        Files.writeString(file.toPath(), username + "\n" + password, StandardCharsets.UTF_8);

        credentialsChanged.set(false);
    }


    public void readCredentials(){
        File file = new File(new File(System.getProperty("user.home"), "FXPlayer"), "OpenSubtitles.txt");
        if(file.exists() && file.canRead()){
            try {
                List<String> lines = Files.readAllLines(Path.of(file.toURI()), StandardCharsets.UTF_8);
                if(lines.size() >= 2){
                    usernameField.setText(lines.get(0));
                    passwordField.setText(lines.get(1));

                    this.username = lines.get(0);
                    this.password = lines.get(1);

                    credentialsChanged.set(false);
                }

            } catch (IOException ignored){}
        }
    }

    public void reset(){
        this.setOpacity(0);
        this.setVisible(false);
        scrollPane.setVvalue(0);
        openSubtitlesWindow.connectionButton.setDisable(false);
        usernameField.setText(username);
        passwordField.setText(password);
        credentialsChanged.set(false);
    }

    @Override
    public boolean focusForward(){
        if(focus.get() == focusNodes.size() -1) return true;

        keyboardFocusOn(focusNodes.get(focus.get() + 1));

        return false;
    }

    @Override
    public boolean focusBackward(){
        if(focus.get() == 0) return true;

        if(focus.get() == -1) keyboardFocusOn(focusNodes.get(focusNodes.size() - 1));
        else keyboardFocusOn(focusNodes.get(focus.get() - 1));

        return false;
    }


    private void updatePadding(boolean value){
        if(value) content.setPadding(new Insets(15, 8, 15, 15));
        else      content.setPadding(new Insets(15, 20, 15, 15));
    }
}
