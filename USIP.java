import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class KnnSampling {
	
	public static int SHOT_USE_LIMIT = -1;
	public static final int RESULT_NUM = 1000;
	public static final int SAMPLE_NUM = 50000;
	public static final int NUM_FOR_AVG = 3;
	public static final String CONCEPT_FILE = "All_concept_list2.csv";
	public static final String ANNOTATION_M_FILE = "ann_M.csv";
	public static final String ANNOTATION_S_FILE = "ann_S.csv";
	
	private int conceptNo = -1;
	private ArrayList<Data> datas = null;
	private ArrayList<Pos> positives = null;
	
	public KnnSampling(int conceptNo){
		this.conceptNo = conceptNo;
		datas = new ArrayList<Data>();
		positives = new ArrayList<Pos>();
	}
	
	private void readData(){
		if( conceptNo < 0 ){
			System.err.println("[ERROR] CONCEPT NO IS INVALID : "+this.conceptNo);
			System.exit(-1);
		}
		ArrayList<String> concepts = new ArrayList<String>();
		BufferedReader br = null;
		// read concepts
		System.out.print("Reading concepts ...");
		try {
			br = new BufferedReader(new FileReader("All_concept_list2.csv"));
			String line = null;
			while( (line = br.readLine()) != null ){
				String[] sp = line.split(",");
				concepts.add(sp[1]);
			}
	        br.close();
		} catch (Exception e){
			System.err.println("[ERROR] Cannot read : "+CONCEPT_FILE );
			System.exit(-1);
		}
        // read features
		System.out.print("\rReading features ...");
        try {
                br = new BufferedReader(new FileReader(ANNOTATION_M_FILE));
                String line = null;
                int flag = 0;
                while( (line = br.readLine()) != null ){
                        String[] sp = line.split(",");
                        datas.add(new Data(null, Double.parseDouble(sp[conceptNo]), 1.0, null, null));
                        if( Double.parseDouble(sp[conceptNo]) != 0 )
                                flag = 1;
                }
                br.close();
                if( flag == 0 ){
                        System.err.println("ALL ann_M data is ZERO!!\nDo you have the right concept number??");
                        System.exit(-1);
                }
        } catch (Exception e) {
                System.err.println("[ERROR] during read file : "+ANNOTATION_M_FILE);
                e.printStackTrace();
                System.exit(-1);
        }
        // read shot IDs
        System.out.print("\rReading shot IDs ...");
        try {
                br = new BufferedReader(new FileReader(ANNOTATION_S_FILE));
                String line = null;
                int cnt = 0;
                while( (line = br.readLine()) != null ){
                        String[] sp = line.split(",");
                        int s = 0;
                        for(int i=0;i<sp.length;i++){
                                if( i+1 == sp.length || sp[i+1].equals("B") ){
                                        String shot = "";
                                        for(int j=s;j<=i;j++)
                                                shot += sp[j];
                                        datas.get(cnt).setName(shot);
                                        cnt++;
                                        s = i+1;
                                }
                        }
                }
                br.close();
        } catch (Exception e) {
                System.err.println("[ERROR] during read file : "+ANNOTATION_S_FILE);
                e.printStackTrace();
                System.exit(-1);
        }
        // read positive annotation data
        System.out.print("\rReading positive annotation datas ...");
        try {
            br = new BufferedReader(new FileReader("annGetOutExP/"+concepts.get(conceptNo)+".txt"));
            String line = null;
            br.readLine();
            while( (line = br.readLine()) != null ){
                    String[] sp = line.split("\\.");
                    if( sp.length == 1 )
                            sp = line.split(" ");
                    String name = "BG_"+sp[0]+"-"+sp[1]+"-2";
                    for(int j=0; j<datas.size(); j++){
                            if( datas.get(j).getName().equals(name) ){
                                    positives.add(new Pos(datas.get(j).getFeat(), 0));
                                    datas.remove(j);
                                    break;
                            }
                    }
            }
            br.close();
	    } catch (Exception e) {
	            System.err.println("[ERROR] during read file : annGetOutExP/"+concepts.get(conceptNo)+".txt");
	            e.printStackTrace();
	            System.exit(-1);
	    }
        // read negative annotation data
        System.out.print("\rReading negative annotation datas ...");
        try {
                br = new BufferedReader(new FileReader("annGetOutExN/"+concepts.get(conceptNo)+".txt"));
                String line = null;
                br.readLine();
                while( (line = br.readLine()) != null ){
                        String[] sp = line.split("\\.");
                        if( sp.length == 1 )
                                sp = line.split(" ");
                        String name = "BG_"+sp[0]+"-"+sp[1]+"-2";
                        for(int j=0; j<datas.size(); j++){
                                if( datas.get(j).getName().equals(name) ){
                                        datas.remove(j);
                                        break;
                                }
                        }
                }
                br.close();
        } catch (Exception e) {
                System.err.println("[ERROR] during read file : annGetOutExN/"+concepts.get(conceptNo)+".txt");
                e.printStackTrace();
                System.exit(-1);
        }
        
        // random sampling deal with heap error
        System.out.print("\rRandom sampling from datas dealing with heap error ...");
        ArrayList<Data> samples = new ArrayList<Data>();
        Random rnd = new Random();
        for(int i=0; i<SAMPLE_NUM; i++){
        	samples.add(datas.remove(rnd.nextInt(datas.size())));
        }
        datas = samples;

		// optimize SHOT_USE_LIMIT
		if( this.SHOT_USE_LIMIT == -1 ){
			this.SHOT_USE_LIMIT = (int)(datas.size()*this.NUM_FOR_AVG/positives.size())+1;
		}

        // debug
        //for(int i=0; i<datas.size(); i++) System.out.println("shot : "+datas.get(i).getName()+"\tfeature : "+datas.get(i).getFeat()+"\tavgDist : "+datas.get(i).getAvgDist());
        //for(int i=0; i<positives.size(); i++) System.out.println("feature : "+positives.get(i).getFeat()+"\tuseCnt : "+positives.get(i).getUseCnt());

        // output to stdo
        System.out.println("\n--------------------------------");
        System.out.println("- CONCEPT NUMBER     : "+this.conceptNo);
        System.out.println("- CONCEPT NAME       : "+concepts.get(conceptNo));
        System.out.println("- length of datas    : "+datas.size());
        System.out.println("- length of postives : "+positives.size());
		System.out.println("- num of USE_LIMIT   : "+this.SHOT_USE_LIMIT);
        System.out.println("--------------------------------");
		 
        // free temporal valiables
        concepts = null;
	}
	
    private void run(int algorithmNo){
        switch(algorithmNo){
                case 1 :
                        nearMiss1();
                        break;
                case 2 :
                        nearMiss2();
                        break;
                case 3 :
                        nearMiss3();
                        break;
                case 4 :
                        mostDistance();
                        break;
                default :
                        break;
        }
    }
    
    private void nearMiss1(){
    	int useTopNum4Avg = this.NUM_FOR_AVG;
    	int dataNum = datas.size();
    	System.out.println("Calculating all distance of datas and positives.");
    	for(int i=0; i<datas.size(); i++ ){
    		System.out.print("\r## processing "+(i+1)+" of "+dataNum+" datas...");
    		ArrayList<Tmp> tmp_diffs = new ArrayList<Tmp>();
    		for(int j=0; j<positives.size(); j++ ){
    			tmp_diffs.add(new Tmp(j,Math.abs(datas.get(i).getFeat()-positives.get(j).getFeat())));
    		}
    		Collections.sort(tmp_diffs, new distComparator());
    		double avgDist = 0.0;
    		int[] indexes = new int[3];
    		for(int k=0; k<useTopNum4Avg; k++){
    			avgDist += tmp_diffs.get(k).getDist();
    			indexes[k] = tmp_diffs.get(k).getPosIndex();
    		}
    		datas.get(i).setAvgDist(avgDist);
    		datas.get(i).setIndexes(indexes);
    		datas.get(i).setNears(tmp_diffs);
    	}
    	Collections.sort(datas, new avgDistComparator());
    	
    	System.out.println("\nCalculating avgDist considering SHOT_USE_LIMIT.");
    	for(int i=0; i<datas.size(); i++ ){
    		System.out.print("\r### processing "+(i+1)+" of "+dataNum+" datas...");
    		Data turnData = datas.get(i);
    		for(int j=0; j<useTopNum4Avg; j++ ){
    			if( positives.get((turnData.getIndexes())[j]).getUseCnt() >= SHOT_USE_LIMIT ){
					int flag = 0;
    				ArrayList<Tmp> turnTmp = turnData.getNears();
    				for(int k=useTopNum4Avg; k<turnTmp.size(); k++ ){
    					if( positives.get(turnTmp.get(k).getPosIndex()).getUseCnt() < SHOT_USE_LIMIT ){
    						(turnData.getIndexes())[j] = turnTmp.get(k).getPosIndex();
    						positives.get(turnTmp.get(k).getPosIndex()).addUseCnt();
    						flag = 1;
    						break;
    					}
    				}
    				if( flag == 0 ){
    					System.err.println("\n[ERROR] Limit is too tight!!");
    					System.exit(-1);
    				}
    			}else{
    				positives.get((turnData.getIndexes())[j]).addUseCnt();
    			}
    		}
    		double newAvgDist = 0.0;
    		for(int l=0; l<useTopNum4Avg; l++ ){
    			newAvgDist += Math.abs(datas.get(i).getFeat()-positives.get(datas.get(i).getIndexes()[l]).getFeat());
    		}
    		datas.get(i).setAvgDist(newAvgDist);
    	}
    	Collections.sort(datas, new avgDistComparator());
    }
	
    
    private void nearMiss2(){

    }

    private void nearMiss3(){

    }

    private void mostDistance(){

    }
	
    
    /*
     * print result and output file
     */
    private void show(){
            BufferedWriter bw_m, bw_s, bw_a;
            try{
                    bw_m = new BufferedWriter(new FileWriter("OUTPUT/"+this.conceptNo+".feat"));
                    bw_s = new BufferedWriter(new FileWriter("OUTPUT/"+this.conceptNo+".shot"));
					bw_a = new BufferedWriter(new FileWriter("OUTPUT/"+this.conceptNo+".4ann"));
                    int rankNum = Math.min(RESULT_NUM, datas.size());
                    for(int i=0; i<rankNum; i++ ){
                            //System.out.println(i+"-th nearest shot : "+avgDist.get(i).getName()+"\tdist : "+avgDist.get(i).getDistance()+"\tfeature : "+avgDist.get(i).getFeature());
                            bw_m.write(datas.get(i).getFeat()+"\n");
                            bw_s.write(datas.get(i).getName()+"\n");
							bw_a.write(datas.get(i).getName()+" "+datas.get(i).getFeat()+" "+"-1\n");
                    }
                    bw_m.close();
                    bw_s.close();
					bw_a.close();
            }catch( Exception e){
                    System.err.println("[ERROR] during result output!!\n");
					e.printStackTrace();
                    System.exit(-1);
            }
			this.SHOT_USE_LIMIT = -1;
            System.out.println("\nComplete!! Results output into OUTPUT_DIR. ===");
    }
	
	public static void main(String[] args){
        
		if( args.length == 0 ){
            System.err.println("### CONCEPT No as parameter is required! ###");
            System.exit(-1);
        }
        
        try{
        	for( int i=0; i<args.length; i++ ){
				KnnSampling ins = new KnnSampling(Integer.parseInt(args[i]));
				ins.readData();
				ins.run(1);
				ins.show();
				ins = null;
        	}
        }catch(NumberFormatException e){
        	 System.err.println("### [ERROR] : Invalid parameters!! ###");
             System.exit(-1);
        }
       
		/*
		// debug
		KnnSampling ins = new KnnSampling(17);
		ins.readData();
		ins.run(1);
		ins.show();
		ins = null;
		*/	
	}
	
}

class Data {
	
	private String name;
	private double feat;
	private double avgDist;
	private int[] indexes;
	private ArrayList<Tmp> nears;
	
	public Data(String name, double feat, double avgDist, int[] indexes, ArrayList<Tmp> near){
		this.name = name;
		this.feat = feat;
		this.avgDist = avgDist;
		this.indexes = indexes;
		this.nears = near;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setFeat(double feat){
		this.feat = feat;
	}
	
	public void setAvgDist(double avgDist){
		this.avgDist = avgDist;
	}
	
	public void setIndexes(int[] indexes){
		this.indexes = indexes;
	}
	
	public void setNears(ArrayList<Tmp> nears){
		this.nears = nears;
	}
	
	public String getName(){
		return this.name;
	}
	
	public double getFeat(){
		return this.feat;
	}
	
	public double getAvgDist(){
		return this.avgDist;
	}
	
	public int[] getIndexes(){
		return this.indexes;
	}
	
	public ArrayList<Tmp> getNears(){
		return this.nears;
	}
	
}

class Pos {
	
	private double feat;
	private int useCnt;
	
	public Pos(double feat, int useCnt){
		this.feat = feat;
		this.useCnt = useCnt;
	}
	
	public void setFeat(double feat){
		this.feat = feat;
	}
	
	public void setUseCnt(int useCnt){
		this.useCnt = useCnt;
	}
	
	public void addUseCnt(){
		this.useCnt++;
	}
	
	public double getFeat(){
		return this.feat;
	}
	
	public int getUseCnt(){
		return this.useCnt;
	}
	
}

class Tmp {
	
	private int posIndex;
	private double dist;
	
	public Tmp(int posIndex, double dist){
		this.posIndex = posIndex;
		this.dist = dist;
	}
	
	public void setPosIndex(int posIndex){
		this.posIndex = posIndex;
	}
	
	public void setDist(double dist){
		this.dist = dist;
	}
	
	public int getPosIndex(){
		return this.posIndex;
	}
	
	public double getDist(){
		return this.dist;
	}
	
}

class distComparator implements Comparator<Object> {
	@Override
	public int compare(Object o1, Object o2) {
		if( ((Tmp)o1).getDist() - ((Tmp)o2).getDist() == 0)
			return 0;
		else if( ((Tmp)o1).getDist() - ((Tmp)o2).getDist() > 0 )
			return 1;
		else
			return -1;
	}
}

class avgDistComparator implements Comparator<Object> {
	@Override
	public int compare(Object o1, Object o2) {
		if( ((Data)o1).getAvgDist() - ((Data)o2).getAvgDist() == 0 )
			return 0;
		else if( ((Data)o1).getAvgDist() - ((Data)o2).getAvgDist() > 0 )
			return 1;
		else
			return -1;
	}
}


