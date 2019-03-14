package main;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class engine 
{
	Integer[][] neighbors;
	//Sj
	double[] excitement;
	//Mj
	double[] weights;
	//hj
	double transmission;
	//Deltaj
	double retention;
	
	// keep for resetting the excitement for future loops
	double[] save_excitement;
	//
	
	//previous excitement used for checking convergence
	double memory = 0.0;
	//total excitement
	double average = 0.0;
	// Maximum value of average
	double averageMax=0.0;
	//Some memory device for detecting oscillation
	double min[] = new double[4];
	double max[] = new double[4];
	int min_flg = 0;
	int max_flg = 0;
	//measures half of the period assuming that the period is an exact double
	int period = 0;
	
	int check=0;
	ArrayList<Double>averageList=new ArrayList<>();
	String mode="topology"; //topology or nontopology (retention & transmission)
	String log=""; 
	
	int z_count = 0; //Zero
    int c_count = 0; //Convergence
    int o_count = 0; //Oscillation
    int t_count = 0; //Timeout
	
	private void update() 
	{
		int size = excitement.length;
		double[] excitement_cpy = new double[size];
		//Deep copy
		for (int i = 0; i < size; i++) {
			excitement_cpy[i] = excitement[i];
		}
		//update
		for (int i = 0; i < size; i++) {
			int in_size = neighbors[i].length;
			
			double sum = 0;
			if (neighbors[i] == null) {
				sum = 0;
				continue;
			}
			else {
				//sum excitement of neighbors
				sum = 0;
				for (int j = 0; j < in_size; j++) {
					//Index of the neighbor (adjusted for 0 index start)
					int index = neighbors[i][j] - 1;
					//System.out.println("I'm in");
					//1 - delta
					double minus_delta = 1 - retention;
					//Mj - Sj
					double scaled_ex = weights[index] - excitement_cpy[index];
					//(1- delta)*hj*sj*(Mj- sj)
					double temp = minus_delta * transmission * excitement_cpy[index] * scaled_ex;
					// ^ / Mj
					double last = temp/weights[index];
					sum = sum + last;
				}
			}
//			System.out.println(excitement_cpy[i]);
//			System.out.println(excitement[i]);
			//Delta * Si
			excitement[i] = (retention * excitement_cpy[i]) + sum;
//			System.out.println(excitement_cpy[i]);
//			System.out.println(excitement[i]);
		}
		memory = average;
		average = 0.0;
		for (double i : excitement)
		{
			average += i;
		}
		average = average / size; // gives average excitement
		if (mode=="topology") averageList.add(average);
		if (averageMax<average) averageMax=average;
	};
	//
	private void reset() //resets all of the simulated values
	{
		int size = excitement.length;
		System.out.println("Reseting");
		for (int i = 0; i < size; i++) {
//			System.out.println(excitement[i]);
//			System.out.println(save_excitement[i]);
			excitement[i] = save_excitement[i];
//			System.out.println(excitement[i]);
		}
		memory = 0.0;
		average = 0.0;
		for (int i = 0; i < 4; i++) {
			min[i] = 0.0;
			max[i] = 0.0;
		}
		min_flg = 0;
		max_flg = 0;
		period = 0;
	}
	
	private boolean check_convergence()
	{
		double temp = memory - average;
		if (temp < 0) {
			temp = -temp;
		}
		if (temp < .0000001) //might have to be smaller
		{
			return true;
		}
		else
		{
			return false;
		}
	};
	
	private boolean check_zero()
	{
		if (average < .000001) //might have to be smaller 
		{
			return true;
		}
		else 
		{
			return false;
		}
	};
	
	private boolean check_oscillation() {
		double[] diff_min = new double[4];
		double[] diff_max = new double[4];
		double temp_min = 0.0;
		double temp_max = 0.0;
		for (int i = 0; i < 4; i++) {
			temp_min = min[i] - memory;
			temp_max = max[i] - memory;
			if (temp_min < 0)  {
				diff_min[i] = -temp_min;
			}
			else  {
				diff_min[i] = temp_min;
			}
			if (temp_max < 0)  {
				diff_max[i] = -temp_max;
			}
			else  {
				diff_max[i] = temp_max;
			}
		}
		
		if (memory < average) {
			 if (min_flg == 1) {
				//checks if four mains in a row are the same
				 if (diff_min[0] < .000001 && diff_min[1] < .000001 && diff_min[2] < .000001 && diff_min[3] < .000001) {
					 return true;
				 }
				 min[3] = min[2];
				 min[2] = min[1];
				 min[1] = min[0];
				 min[0] = memory;
				 period = 0;
			 }
			 period++;
			 min_flg = 0;
			 max_flg = 1;
		}
		else if (memory > average) {
			 if (max_flg == 1) {
				 //checks if four maxes in a row are the same
				 if (diff_max[0] < .000001 && diff_max[1] < .000001 && diff_max[2] < .000001 && diff_max[3] < .000001) {
					 return true;
				 }
				 max[3] = max[2];
				 max[2] = max[1];
				 max[1] = max[0];
				 max[0] = memory;
				 period = 0;
			 }
			period++;
			max_flg = 0;
			min_flg = 1;
			
		}
		else
		{
			return false;
		}
		return false;
	};
	
	private void display()
	{
//		System.out.println("Excitement:");
//		for(int i = 0; i < excitement.length; i++) {
//			System.out.println(excitement[i]);
//		}
		System.out.println("Memory");
		System.out.println(memory);
		System.out.println("Average:");
		System.out.println(average);
		System.out.println("");
	};
	
	public engine (String jsonPath) {
		JSONObject jsonFile = parseFile(jsonPath);
		updateVar(jsonFile);
		this.check=checkSizeMatch();
	}
	
	public engine (String jsonPath, double retention, double transmission) {
		JSONObject jsonFile = parseFile(jsonPath);
		updateVarGUI(jsonFile, retention, transmission);
		this.check=checkSizeMatch();
	}

	public engine (String jsonPath, String noRetention, double transmission) {
		JSONObject jsonFile = parseFile(jsonPath);
		updateVarGUI(jsonFile, -99, transmission);
		this.check=checkSizeMatch();
	}
	public engine (String jsonPath, double retention, String noTransmission) {
		JSONObject jsonFile = parseFile(jsonPath);
		updateVarGUI(jsonFile, retention, -99);
		this.check=checkSizeMatch();
	}
	
	public engine() 
	{

	};
	
	public int checkSizeMatch() {
    	if ((neighbors.length != excitement.length) || (neighbors.length != weights.length))
    	{
    		System.out.println("Array Size mismatch");
    		return -1;
    	}
		return 0;
	}
	
	public int initPrint() 
	{
		if (checkSizeMatch()==-1) return checkSizeMatch();
    	//Initial Print
//    	for (int i = 0; i < neighbors.length; i++) {
//    		for (int j = 0; j < neighbors[i].length; j++) {
//    			System.out.println(neighbors[i][j]);
//    		}
//    	}
//    	System.out.println("Weights:");
//    	for(int i = 0; i < weights.length; i++) 
//    	{
//    		System.out.println(weights[i]);
//    	}
    	System.out.println("Transmission:");this.log+="Transmission:"+"\n";
    	System.out.println(transmission);this.log+=transmission+"\n";
    	System.out.println("Retention");this.log+="Retention:"+"\n";
    	System.out.println(retention);this.log+=retention+"\n\n";
    	System.out.println("");
    	
    	System.out.println("Initial Excitement:");this.log+="Initial Excitement:"+"\n";
    	for(int i = 0; i < excitement.length; i++) 
    	{
    		System.out.println(excitement[i]); this.log+=excitement[i]+"\n";
    	}
		return 0;
	};
	
	public int simulate() 
	{
		int flg = 0;
    	//simulation
    	for( int i = 0; i < 1000; i++) 
    	{
    		//System.out.println(i);
    		update();
    		//display();
        	// check for oscillation
    		if (check_oscillation())
    		{
    			System.out.println("Oscillation");this.log+="Oscillation"+"\n";
    			flg = 1;
    			return 2; //means oscillation
    		}
        	// check for 0 (only true if oscillation is not true)
    		else if (check_zero()) 
    		{
    			System.out.println("Zero");this.log+="Zero"+"\n";
    			System.out.println(average);this.log+=average+"\n";
    			flg = 1;
    			return 0; //means zero
    		}
    		//check for convergence
    		else if (check_convergence()) 
    		{
    			System.out.println("Converged");this.log+="Converged"+"\n";
    			System.out.println(average);this.log+=average+"\n";
    			flg = 1;
    			return 1; //means convergence
    		}
    	}
    	if (flg == 0)
    	{
    		//End of loop
    		System.out.println("Timeout");this.log+="Timeout"+"\n";
    		System.out.println(average);this.log+=average+"\n";
    		return -1;
    	}
    	return -2;
	};
	public JSONObject parseFile(String f) {
    	JSONParser parser=new JSONParser();
    	JSONObject jsonObject=null;
//		URL path = engine.class.getResource(jsonPath);
//		File f = new File(path.getFile());
		Object obj;
		try {
    		obj = parser.parse(new FileReader(f));
			jsonObject=(JSONObject) obj;
		}
		catch (Exception e) {
			System.out.println("Parsing file error"); 
		}
		return jsonObject;
	};
	private double[] convertWeight(Object weightObject) {
		//Converting from List<Long> to double[]
		ArrayList<Double> w=(ArrayList<Double>)weightObject;
		double[]weights=new double[w.size()];
		for(int i=0;i<w.size();i++) {
			try {
				weights[i]=w.get(i);
			} catch (Exception e) {
				System.out.println("Please include decimals!! E.g.: 5.0");
			}
		}
		return weights;
	};
	private double []convertExcitement(Object excitementObject) {
		//Converting from List<Double> to double[]
		ArrayList<Double> e=(ArrayList<Double>)excitementObject;
		double[]excitement=new double[e.size()];
		for(int i=0;i<e.size();i++) {
			excitement[i]=e.get(i);
		}
		
		return excitement;
	};
	private Integer[] []convertNeighbors(Object neighborsObject) {
		//Converting from List<List<Long>> to Integer [][]
		List<ArrayList<Long>> n=(ArrayList<ArrayList<Long>>)neighborsObject;
		Integer[][]neighbors=new Integer[n.size()][];
		for(int i=0;i<n.size();i++) {
			neighbors[i]=new Integer[n.get(i).size()];
    		for (int j=0; j<n.get(i).size();j++) {
    			neighbors[i][j]=n.get(i).get(j).intValue();
    			//System.out.print(neighbors[i][j]);
    		}
    		//System.out.println();
		}
		return neighbors;
	};
	
	//update variable function for API (through cmd line)
	public void updateVar(JSONObject jsonObject) {
    	Integer[][]neighbors=convertNeighbors(jsonObject.get("neighbors"));
		double[]excitement=convertExcitement(jsonObject.get("excitement")), save_excitement=convertExcitement(jsonObject.get("excitement")), weights=convertWeight(jsonObject.get("weights"));
		double retention=(Double)jsonObject.get("retention");
		double transmission=(Double)jsonObject.get("transmission");
		
		this.neighbors=neighbors;
		this.excitement=excitement;
		this.save_excitement=save_excitement;
		this.weights=weights;
		this.retention=retention;
		this.transmission=transmission;
		//
		int sz = this.excitement.length;
		this.save_excitement= new double[sz];
		for (int i = 0; i < sz; i++)
		{
			this.save_excitement[i] = this.excitement[i];
		}
		//
	}

	//update variable function for GUI
	public void updateVarGUI(JSONObject jsonObject, double retention, double transmission) {
    	Integer[][]neighbors=convertNeighbors(jsonObject.get("neighbors"));
		double[]excitement=convertExcitement(jsonObject.get("excitement")), save_excitement=convertExcitement(jsonObject.get("excitement")), weights=convertWeight(jsonObject.get("weights"));
		
		this.neighbors=neighbors;
		this.excitement=excitement;
		this.save_excitement=save_excitement;
		this.weights=weights;
		if (retention==-99) this.retention=(Double)jsonObject.get("retention");
		else this.retention=retention;
		if (transmission==-99) this.transmission=(Double)jsonObject.get("transmission");
		else this.transmission=transmission;
	}
	
	public void experiment(String flag) {
		if (this.check==-1) {System.out.println("Check = -1, Exiting.");return;}
        for (double i = 0; i <= 1; i = i + 0.01)
        {
        	if (flag=="transmission")
        	this.transmission = i;
        	else if (flag=="retention") this.retention=i;
	        	
	        	System.out.println("Transmission:");this.log+="Transmission:"+"\n";
	        	System.out.println(this.transmission);this.log+=this.transmission+"\n";
	        	System.out.println("Retention");this.log+="Retention"+"\n";
	        	System.out.println(this.retention);this.log+=this.retention+"\n\n";
	        	System.out.println("");
	        	single_experimental("nontopology");

        }
	}
	
	public void single_experimental(String mode) {
    	int result = 0; // tells us what the function did
    	result = this.simulate();
    	this.mode=mode;
    	switch (result)
    	{
			case 0:
				System.out.println("Zeros");this.log+="Zeros"+"\n";
				z_count++;
				break;
			case 1:
				System.out.println("Converges");this.log+="Converges"+"\n";
				c_count++;
				break;
			case 2:
				System.out.println("Oscilattes");this.log+="Oscilattes"+"\n";
				o_count++;
				break;
			case -1:
				System.out.println("Timeout");this.log+="Timeout"+"\n";
				t_count++;
				break;
			case -2:
				System.out.println("Error");this.log+="Error"+"\n";
				break;
			default:
				System.out.println("Error Default");this.log+="Error Default"+"\n";
    	}
    	if (this.mode=="nontopology") averageList.add(average);
    	reset();
	}
	
	public int getZero() {
		return z_count;
	}
	public int getConvergence() {
		return c_count;
	}
	public int getOscilation() {
		return o_count;
	}
	public int getTimeout() {
		return t_count;
	}
	public double getAverageMax() {
		return averageMax;
	}
	public double getAmplitude() {
		return max[0]-min[0];
	}
	public int getPeriod() {
		return period;
	}
	public ArrayList<Double>averageList() {
		return averageList;
	}
	
	public String getLog() {
		return this.log;
	}
	
    public static void main(String[] args)
    {
//    		for(int i=0;i<neighbors.length;i++) {
//        		for (int j=0; j<neighbors[i].length;j++) {
//        			if (neighbors[i][j]!=null)
//        			System.out.print(neighbors[i][j]);
//        		}
//        		System.out.println();
//    		}
		//System.out.println(test);
		//System.out.println(a+b+c+d+e);

    	engine main = new engine("C:\\Users\\AdrianH\\test\\config01.json");
    	//engine main = new engine(neighbors, excitement, weights, transmission, retention);
    	int check = main.initPrint();
    	if (check == -1) {
    		System.out.println("ABORT");
    		return;
    	}
    	main.experiment("retention");
    	System.out.println(main.getZero());
    	System.out.println(main.getConvergence());
    	System.out.println(main.getOscilation());
    	System.out.println(main.getTimeout());
    }
};
