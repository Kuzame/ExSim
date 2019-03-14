package main;

import java.io.File;
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


public class FXMLDocumentController implements Initializable {
	engine mainEngine;
    @FXML private TextField browseFileText;
    @FXML private Button browseFileButton, browseFileButtonSolo;
    @FXML private Spinner <Double> spinnerRetention, spinnerTransmission, spinnerIteration;
    @FXML private Slider sliderRetention, sliderTransmission, sliderIteration;
    @FXML private CheckBox checkboxTopology, checkboxRetention, checkboxTransmission, checkboxIteration;
    @FXML private RadioButton radioTopology, radioRetention, radioTransmission, radioIteration;
    @FXML private Label errorTopology, errorRetention, errorTransmission, errorIteration;
    
    String [] browseFileContainer=new String[0];
    
    Double minRetention=0.0, maxRetention=1.0;
    Double minTransmission=0.0, maxTransmission=1.0;
    Double minIteration=0.0, maxIteration=1.0, stepIteration=0.01;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	final ToggleGroup group=new ToggleGroup();
    	radioTopology.setToggleGroup(group); radioRetention.setToggleGroup(group); radioTransmission.setToggleGroup(group); 
//    	radioIteration.setToggleGroup(group);
    	radioTopology.setSelected(true);
    	
    	//For Topology
    	errorTopology.setVisible(false);
    	browseFileButtonSolo.setVisible(false);

    	//For Retention
    	variablesConfig(sliderRetention, minRetention, maxRetention, spinnerRetention, radioRetention, errorRetention, checkboxRetention);
    	//For Transmission
    	variablesConfig(sliderTransmission, minTransmission, maxTransmission, spinnerTransmission, radioTransmission, errorTransmission, checkboxTransmission);
//    	//For Iteration
//    	variablesConfig(sliderIteration, minIteration, maxIteration, spinnerIteration, radioIteration, errorIteration, checkboxIteration);
    	
    	//For Browse File(s) button
    	browseButtonConfig(browseFileButtonSolo, browseFileButton);
    }  
    
    public FXMLDocumentController() {
    }
    
    private void browseButtonConfig(Button buttonSolo, Button buttonMulti) {
    	final FileChooser fileChooser = new FileChooser();
    	buttonSolo.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(new Stage());
					String temp="";
                    if (file != null) {
                    	browseFileContainer=new String[1];
    					temp=file.getAbsolutePath();
                    	browseFileContainer[0]=temp;
                    }
					browseFileText.setText(temp);
					errorTopology.setVisible(false);
                }
            });
    	buttonMulti.setOnAction(
			new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent e) {
					configureFileChooser(fileChooser);
					List<File> list= fileChooser.showOpenMultipleDialog(new Stage());
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
						browseFileText.setText(temp);
						errorTopology.setVisible(false);
					}
				}
			}
		);

        radioTopology.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
               browseFileButton.setVisible(newValue);
               browseFileButtonSolo.setVisible(!newValue);
               //System.out.println("New value: " + newValue);
            }
         });
        
    }
    
    private void configureFileChooser(FileChooser fileChooser) {
    	fileChooser.setTitle("Select Test Variables");
    	fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    	fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
    }
    
    private void variablesConfig(Slider slider, Double minValue, Double maxValue, Spinner <Double> spinner, RadioButton radio, Label error, CheckBox checkbox) {
    	error.setVisible(false);
    	// ----------------------------------- FOR SLIDER ---------------------------------------------------------
    	slider.setShowTickMarks(true); slider.setShowTickLabels(true);
    	slider.setMin(minValue);
    	slider.setMax(maxValue);
    	slider.setValue(0.5);
    	slider.setBlockIncrement(maxValue/10);
    	slider.setMajorTickUnit(2*maxValue/10);
    	
    	slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
               setSpinnerValue(spinner, (double)newValue);
               //System.out.println("New value: " + (int)(double)newValue);
            }
         });
    	
    	// ----------------------------------- FOR SPINNER --------------------------------------------------------
    	spinner.setEditable(true);
        final Spinner<Double> items = new Spinner<Double>(minValue, maxValue, 0, stepIteration);
        SpinnerValueFactory<Double> valueFactory = items.getValueFactory();
        MyConverter converter = new MyConverter();
        valueFactory.setConverter(converter);
        valueFactory.setValue(0.5);
  
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String text = spinner.getEditor().getText();
//                System.out.println("handle: ~" + text+"~");
//                SpinnerValueFactory.ListSpinnerValueFactory<Double> valueFactory1 = (ListSpinnerValueFactory<Double>) spinner.getValueFactory();
  
                StringConverter<Double> converter = valueFactory.getConverter();
                try {
                    Double enterValue = converter.fromString(text);
//                  System.out.println("enterValue: "+ enterValue);
                    valueFactory.setValue(enterValue);
                    error.setVisible(false);
                } catch (Exception e) {
                	error.setVisible(true);
                }
            }
        });
        spinner.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            	setSliderValue(slider, (Double)newValue);
            }
         });
        
        
        // ------------------------------------ FOR RADIO BUTTON -----------------------------------------------------
        radio.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
               slider.setDisable(newValue);
               spinner.setDisable(newValue);
               checkbox.setDisable(newValue);
               //System.out.println("New value: " + newValue);
            }
         });
        

        // ------------------------------------ FOR CHECKBOX -----------------------------------------------------
        checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				slider.setDisable(newValue);
				spinner.setDisable(newValue);
                radio.setDisable(newValue);
               //System.out.println("New value: " + newValue);
            }
         });
    }
    
//    @FXML
//    private void addNumber(ActionEvent event) {
//        //System.out.println("Clicked. !");
//        String str = textone.getText();
//        String str2 = texttwo.getText();
//        Integer x = (Integer.parseInt(str)+Integer.parseInt(str2));
//        result.setText(x.toString());
//        setSpinnerValue(spinnerRetention, x);
//    }
    
    @FXML
    private void startButton(ActionEvent event) {
    	if(browseFileContainer.length==0) {errorTopology.setVisible(true);}
    	else {
    		errorTopology.setVisible(false);
//        	System.out.println("~"+browseFileText.getText()+"~");
    		if (radioTopology.isSelected()) {
    			ArrayList<ArrayList<Double>>temp=new ArrayList<>();
	        	for (int i=0;i<browseFileContainer.length;i++) {
	            	if(checkboxRetention.isSelected()) mainEngine=new engine(browseFileContainer[i], "", spinnerTransmission.getValue());
	            	else if (checkboxTransmission.isSelected())mainEngine=new engine(browseFileContainer[i], spinnerRetention.getValue(), "");
	            	else mainEngine=new engine(browseFileContainer[i], spinnerRetention.getValue(), spinnerTransmission.getValue());
	            	mainEngine.single_experimental("topology");
	            	temp.add(mainEngine.averageList());
	            	generateMetadata(i);
	        	}
            	drawGraph(temp);
    		}
    		else if (radioRetention.isSelected()) {
    			if (checkboxTransmission.isSelected())mainEngine=new engine(browseFileContainer[0], spinnerRetention.getValue(), "");
            	else mainEngine=new engine(browseFileContainer[0], spinnerRetention.getValue(), spinnerTransmission.getValue());
            	mainEngine.experiment("retention");
            	ArrayList<ArrayList<Double>>temp=new ArrayList<>();
            	temp.add(mainEngine.averageList);
            	generateMetadata(0);
            	drawGraph(temp);
    		}
    		else if (radioTransmission.isSelected()) {
            	if(checkboxRetention.isSelected()) mainEngine=new engine(browseFileContainer[0], "", spinnerTransmission.getValue());
            	else mainEngine=new engine(browseFileContainer[0], spinnerRetention.getValue(), spinnerTransmission.getValue());
            	mainEngine.experiment("transmission");
            	ArrayList<ArrayList<Double>>temp=new ArrayList<>();
            	temp.add(mainEngine.averageList);
            	generateMetadata(0);
            	drawGraph(temp);
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
    	if (radioTopology.isSelected()) {
    		xLabel="Iterations"; graphTitle+="Iterations";
		}
    	else {
    		xLabel="Test Variable"; graphTitle+="Test Variable";
    		if(radioRetention.isSelected()) xLabel+=" (Retention)"; 
    		else if (radioTransmission.isSelected())xLabel+=" (Transmission)";
		}
    	graphTitle+="\n[";
    	if (!radioTransmission.isSelected()) {
    		graphTitle+="Transmission: "+spinnerTransmission.getValue();if(!radioRetention.isSelected())graphTitle+=", ";
		}
    	
    	if(!radioRetention.isSelected()) graphTitle+="Retention: "+spinnerRetention.getValue();
    	graphTitle+="]";

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
    
    private void setSpinnerValue(Spinner <Double> spinner, Double x) {
    	spinner.getValueFactory().setValue(BigDecimal.valueOf(x)
    		    .setScale(2, RoundingMode.HALF_UP)
    		    .doubleValue());
    }
    private void setSliderValue(Slider slider, Double x) {
    	slider.setValue(x);
    }
    class MyConverter extends StringConverter<Double> {
        @Override
        public String toString(Double object) {
            return object + "";
        }
        @Override
        public Double fromString(String string) {
            return Double.parseDouble(string);
        }
  
    }
}

// Adrian Harminto