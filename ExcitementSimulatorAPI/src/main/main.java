package main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class main extends Application  {
	String temp1="asdf";//, temp2="qwer";
	String [] browseFileContainer=new String[0];
	engine mainEngine;

    @Override
    public void start(Stage stage) {
    	popWindow(stage);
    	if (browseFileContainer.length<1) return;
    	else {
			ArrayList<ArrayList<Double>>temp=new ArrayList<>();
        	for (int i=0;i<browseFileContainer.length;i++) {
            	mainEngine=new engine(browseFileContainer[i]);
            	mainEngine.single_experimental("topology");
            	temp.add(mainEngine.averageList());
            	generateMetadata(i);
        	}
        	drawGraph(temp);
    	}
//        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
//        Scene scene = new Scene(root);
//        
//        stage.getIcons().add(new Image(main.class.getResourceAsStream("ExSim.png")));
//        stage.setTitle("ExSim - Excitement Simulator");
//        stage.setScene(scene);
//        stage.setResizable(false);
//        stage.show();
    }
    public static void main(String[] args) {
//    	main a=new main();
//    	if (args.length>=1) a.temp1=args[0];
    	//if (args.length>1) a.temp2=args[1];
    	//System.out.println(a.temp1+a.temp2);
    	launch(args);
    }
    public main() {
    }    
    private void configureFileChooser(FileChooser fileChooser) {
    	fileChooser.setTitle("Select Test Variables");
    	fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
    }
    private void popWindow(Stage stage) {
    	final FileChooser fileChooser = new FileChooser();
		configureFileChooser(fileChooser);
		List<File> list= fileChooser.showOpenMultipleDialog(stage);
		String temp="";
		if (list!=null) {
			browseFileContainer=new String[list.size()];
			int i=0;
			for (File file : list) {
				temp+=file.getAbsolutePath()+" ";
				browseFileContainer[i]= file.getAbsolutePath();
				//Do something here with the files
				//openFile(file);
				i++;
			}
		}
    }
    private void generateMetadata(int i) {
    	String data="Simulation \"<"+browseFileContainer[i].substring(browseFileContainer[i].length()-13, browseFileContainer[i].length()-5)+">\" Metadata:\r\n" + 
    			"\r\n" + 
    			"Max average excitement:\r\n" + 
    			"<"+mainEngine.getAverageMax()+">\r\n" + 
    			"Number of simulations that converge at 0\r\n" + 
    			"<"+mainEngine.getZero()+">\r\n" + 
    			"Number of simulations that converge at a non-zero value\r\n" + 
    			"<"+mainEngine.getConvergence()+">\r\n" + 
    			"Number of simulations that oscilated\r\n" + 
    			"<"+mainEngine.getOscilation()+">\r\n" + 
    			"Average Amplitude:\r\n" + 
    			"<"+mainEngine.getAmplitude()+">\r\n" + 
    			"Average Period:\r\n" + 
    			"<"+mainEngine.getPeriod()+">\r\n" + 
    			"Number of Simulations that timed out (1000 iterations)\r\n" + 
    			"<"+mainEngine.getTimeout()+">";     
    	String fileName=browseFileContainer[i].substring(0, browseFileContainer[0].length()-5);
        fileName+="_"+"basic-metadata";
        fileName+=".txt";
        try (PrintWriter out = new PrintWriter(fileName)){
            out.print(data);
        } catch(Exception e) {
        	System.out.println("generateMetadata error?");
        }
        data="Experiment \"<"+browseFileContainer[i].substring(browseFileContainer[i].length()-13, browseFileContainer[i].length()-5)+">\" Raw Data:\r\n" + 
    			"\r\n" +mainEngine.getLog();

    	fileName=browseFileContainer[i].substring(0, browseFileContainer[0].length()-5);
        fileName+="_"+"raw-data";
        fileName+=".txt";
        try (PrintWriter out = new PrintWriter(fileName)){
            out.print(data);
        } catch(Exception e) {
        	System.out.println("generateMetadata error?");
        }
    }
    private void drawGraph(ArrayList<ArrayList<Double>> seriesValue) {
    	// Variables
    	String stageTitle="ExSim Graph", xLabel, yLabel="Excitement", graphTitle="Excitement vs ";
    	xLabel="Iterations";
		 graphTitle+="Iterations";


    	Stage stage=new Stage();
        stage.setTitle(stageTitle);
        final Axis<Number> xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setTitle(graphTitle);
        for(int i=0;i<seriesValue.size();i++) {
	        XYChart.Series<Number, Number> series = new Series<Number, Number>();
	        series.setName(browseFileContainer[i].substring(browseFileContainer[i].length()-13, browseFileContainer[i].length()-5));
	        /** xAxis & yAxis Data */
	        LinkedHashMap<Number, Number> map = new LinkedHashMap<>();
	        for (int j=0;j<seriesValue.get(i).size();j++) {
	        	map.put(j+1, seriesValue.get(i).get(j));
	        }
            System.out.println(seriesValue.get(i).size());
	        for (Entry<Number, Number> entry : map.entrySet()) {
	            series.getData().add(new XYChart.Data<Number, Number>(entry.getKey(), entry.getValue()));
	        }
	        lineChart.getData().add(series);
        }
        lineChart.setAnimated(false);
        Scene scene = new Scene(lineChart, 800, 600);
        stage.setScene(scene);
        stage.show();
        WritableImage snapShot = scene.snapshot(null);
        String fileName=browseFileContainer[0].substring(0, browseFileContainer[0].length()-5);
        if (browseFileContainer.length>1) fileName+="_"+browseFileContainer.length+"nodes";
        fileName+=".png";
        try {
        	ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", new File(fileName));
        } catch (Exception e) {
        	System.out.println("Folder creation/Image creation error.");
        }
    }
}

//Adrian Harminto