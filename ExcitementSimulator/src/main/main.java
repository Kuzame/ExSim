package main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class main extends Application {
	String temp1="asdf";//, temp2="qwer";

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        
        stage.getIcons().add(new Image(main.class.getResourceAsStream("ExSim.png")));
        stage.setTitle("ExSim - Excitement Simulator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    public static void main(String[] args) {
    	main a=new main();
    	if (args.length>=1) a.temp1=args[0];
    	//if (args.length>1) a.temp2=args[1];
    	//System.out.println(a.temp1+a.temp2);
        launch(args);
    }
    public main() {
    }

}

//Adrian Harminto