/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ids;

import com.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Dan
 */
public class Clustering {

    /**
     * @param args the command line arguments
     */
    //ch = count lables, ai = count protocol, probaR = probability in rule dataset (probaR = ch/ai)
    private static double delta, xi, xj, aimin, aimax = 0.0d;
    static double sim = 0.0d, simdelta = 0.7d; //sim = similarity, simdelta = Similarity threshold
    static double someAcc = 0.0d;

    public static void main(String[] args) {
        try {

            /*
             ******************************************************
             * Calculating Weights                                  *             
             ******************************************************                                                           
             */
            // Read weight dataset csv file
            CSVReader wData = new CSVReader(new FileReader("replacewithfile.cvs"), ',', '\"');
            List<String[]> nextLineW = wData.readAll();

            // Convert to 2D array of Weight dataset
            String[][] dataArrW;
            dataArrW = new String[nextLineW.size()][];
            dataArrW = nextLineW.toArray(dataArrW);

            //Lsame and Ldiff cases - c
            int lsameRows = 0, ldiffRows = 0;

            for (int row = 0; row < dataArrW.length; row++) {
                //Lsame case - count number of rows to build array
                if (dataArrW[row][dataArrW[0].length - 1].equalsIgnoreCase(dataArrW[0][dataArrW[0].length - 1])) { //41
                    lsameRows++;
                } else {
                    //Ldiff case - count number of rows to build array
                    ldiffRows++;
                }

            }

            //System.out.println(lsameRows);
            //System.out.println(ldiffRows);
            String lsameData[][] = new String[lsameRows][dataArrW[0].length]; //42
            String ldiffData[][] = new String[ldiffRows][dataArrW[0].length]; //42
            int i = 0, j = 0;

            // populating the arrays
            for (int row = 0; row < dataArrW.length; row++) {
                //Lsame case - count number of rows
                if (dataArrW[row][dataArrW[0].length - 1].equalsIgnoreCase(dataArrW[0][dataArrW[0].length - 1])) { //41
                    for (int col = 0; col < dataArrW[0].length; col++) {
                        lsameData[i][col] = dataArrW[row][col];
                        //i++;
                    }
                    i++;
                } else {
                    //Ldiff case - count number of rows
                    for (int col = 0; col < dataArrW[row].length; col++) {
                        ldiffData[j][col] = dataArrW[row][col];
                        //j++;
                    }
                    j++;
                }
            }

            // lsame-k-neigbhor and ldiff-k-neigbor final
            // k = 60% of total dataset (lsame or ldiff)
            int kValue = 0;
            if(lsameRows > ldiffRows){
                kValue = (int) (ldiffRows * 0.6);
            }else{
                kValue = (int)(lsameRows * 0.6);
            }
            
            
            String[][] lsameKData = calDistance(lsameData, kValue); // second parameter is k-neighbor
            String[][] ldiffKData = calDistance(ldiffData, kValue); // second parameter is k-neighbor

            System.out.println("Lsame-k=" + lsameKData.length + "-neigbhour");
            for (int row = 0; row < lsameKData.length; row++) {
                for (int col = 0; col < lsameKData[row].length; col++) {
                    System.out.print(lsameKData[row][col] + "\t");
                }
                System.out.println();
            }

            System.out.println();

            System.out.println("Ldiff-k=" + ldiffKData.length + "-neigbhour");
            for (int row = 0; row < ldiffKData.length; row++) {
                for (int col = 0; col < ldiffKData[row].length; col++) {
                    System.out.print(ldiffKData[row][col] + "\t");
                }
                System.out.println();
            }

            double deltaValue = 0.01d; // setting delta value change
            double iniWei = 1.0d; //Initially weight is  1 -  the same for all attributes
            double finalWei = 0, numValue;
            List<Double> weiAi = new ArrayList<>();
            for (int f = 0; f < dataArrW[0].length - 1; f++) { //41
                for (int l = 0; l < lsameKData.length; l++) {
                    /*
                     *******************************************************
                     *   Character case                                    *
                     *******************************************************
                     */
                    if (f == 1) {
                        if ((lsameKData[l][f]).equalsIgnoreCase(lsameKData[0][f])) {
                            finalWei = iniWei;
                        } else {
                            iniWei = iniWei - deltaValue;
                            finalWei = iniWei;
                        }
                    } else if (f == 2) {
                        if ((lsameKData[l][f]).equalsIgnoreCase(lsameKData[0][f])) {
                            finalWei = iniWei;
                        } else {
                            iniWei = iniWei - deltaValue;
                            finalWei = iniWei;
                        }

                    } else if (f == 3) {
                        if ((lsameKData[l][f]).equalsIgnoreCase(lsameKData[0][f])) {
                            finalWei = iniWei;
                        } else {
                            iniWei = iniWei - deltaValue;
                            finalWei = iniWei;
                        }
                    } else {
                        /*
                         *******************************************************
                         *   Numerical case                                    *
                         *******************************************************
                         */
                        numValue = Double.parseDouble(lsameKData[l][f]);
                        if (numValue > 0.5) {
                            iniWei = iniWei - deltaValue;
                            finalWei = iniWei;
                        } else {
                            finalWei = iniWei;
                        }
                    }

                }
                weiAi.add(finalWei);
                // reset all variable to be used in next attribution weight calculation
                iniWei = 1.0d;
                finalWei = 0;
            }

//            System.out.println();
//
//            for (int g = 0; g < weiAi.size(); g++) {
//                System.out.println((g + 1) + "\t\t" + weiAi.get(g));
//            }
            // ---- ldiff ------------------------------------//
            for (int f = 0; f < dataArrW[0].length - 1; f++) { //41
                for (int l = 0; l < ldiffKData.length; l++) {
                    /*
                     *******************************************************
                     *   Character Value                                   *
                     *******************************************************
                     */
                    if (f == 1) {
                        if ((ldiffKData[l][f]).equalsIgnoreCase(ldiffKData[0][f])) {
                            weiAi.set(f, weiAi.get(f));
                        } else {
                            weiAi.set(f, (weiAi.get(f) + deltaValue));
                        }
                    } else if (f == 2) {
                        if ((ldiffKData[l][f]).equalsIgnoreCase(ldiffKData[0][f])) {
                            weiAi.set(f, weiAi.get(f));
                        } else {
                            weiAi.set(f, (weiAi.get(f) + deltaValue));
                        }
                    } else if (f == 3) {
                        if ((ldiffKData[l][f]).equalsIgnoreCase(ldiffKData[0][f])) {
                            weiAi.set(f, weiAi.get(f));
                        } else {
                            weiAi.set(f, (weiAi.get(f) + deltaValue));
                        }
                    } else {
                        /*
                         *******************************************************
                         *   Numerical value                                   *
                         *******************************************************
                         */
                        numValue = Double.parseDouble(ldiffKData[l][f]);
                        if (numValue > 0.5) {
                            weiAi.set(f, (weiAi.get(f) + deltaValue));
                        } else {
                            weiAi.set(f, weiAi.get(f));
                        }
                    }

                }
            }

//            System.out.println();
//
//            for (int g = 0; g < weiAi.size(); g++) {
//                System.out.println((g + 1) + "\t\t" + weiAi.get(g));
//            }
            System.out.println();

            //--------------------------------------------------------//
            //             Weight Generalization                      //
            //--------------------------------------------------------//
            Double weiAiSame = 0.0d;
            for (int g = 0; g < weiAi.size(); g++) {
                //System.out.println((g + 1) + "\t\t" + weiAi.get(g));
                weiAiSame = weiAiSame + weiAi.get(g);
            }
            for (int g = 0; g < weiAi.size(); g++) {
                weiAi.set(g, (weiAi.get(g) / weiAiSame));
            }
            for (int g = 0; g < weiAi.size(); g++) {
                System.out.println("w" + (g + 1) + "\t\t" + weiAi.get(g));
            }

            //-----------------------------------------------------------//
            // Start building clusters                                   //
            //-----------------------------------------------------------//
            ArrayList<String> tuple = new ArrayList<>();
            int clusterID = 0, counter = 0; // keeps track of number clusters created
            ArrayList<Double> sim_result = new ArrayList<>();
            //TreeMap<Integer, Double> map2 = new TreeMap<>(); //sim data and clusterID
            //SortedSet<Map.Entry<Integer, Double>> sorteMap2;

            for (int row = 0; row < dataArrW.length; row++) {
                for (int col = 0; col < dataArrW[row].length; col++) {
                    // read the tuples and create a cluster immediately 
                    tuple.add(dataArrW[row][col]);
                }

                // for first tuple, just create new cluster
                if (row == 0) {
                    clusterID = clusterID + 1; //Number of clusters increases by one everytime a new cluster is created
                    createCluster(clusterID, tuple);
                } else {

                    for (int cid = 1; cid <= clusterID; cid++) {
                        // Read cluster dataset csv file
                        CSVReader clusterData = new CSVReader(new FileReader("c" + cid + ".csv"), ',', '\"');
                        List<String[]> nextLineC = clusterData.readAll();

                        // Convert to 2D array of cluster dataset
                        String[][] dataArrC;
                        dataArrC = new String[nextLineC.size()][];
                        dataArrC = nextLineC.toArray(dataArrC);

                        for (int cols = 0; cols < dataArrC[0].length - 1; cols++) {
                            for (int rows = 0; rows < dataArrC.length; rows++) {
                                if (dataArrC[rows][cols].equalsIgnoreCase(tuple.get(cols))) {
                                    counter = counter + 1;
                                }
                            }
                            sim = sim + (counter * weiAi.get(cols));
                            counter = 0;
                        }
                        sim = sim / dataArrC.length;

                        sim_result.add(sim);
                        sim = 0.0d;
                        //map2.put(cid, sim); //mapping cluster Id and similarity value

                    }
                    //sorteMap2 = entriesSortedByValues(map2);
                    //System.out.println();
                    //System.out.println(sim_result);

                    //get max sim and id.
                    double maxSim = Collections.max(sim_result);
                    //int maxSimCount = 0;
                    ArrayList<Integer> maxNum = new ArrayList<>();
                    ArrayList<Integer> maxCluster = new ArrayList<>();
                    ArrayList<Integer> maxClusterId = new ArrayList<>();

                    for (int m = 0; m < sim_result.size(); m++) {
                        if (maxSim == sim_result.get(m)) {
                            //maxSimCount++;
                            maxNum.add(m + 1);
                        }
                    }
                    //check if number of max similarity > 1
                    if (maxNum.size() > 1) {
                        //get max cluster by getting length or size property of the array
                        for (int maxi = 0; maxi < maxNum.size(); maxi++) {
                            // Read cluster dataset csv file
                            CSVReader cdata = new CSVReader(new FileReader("c" + maxNum.get(maxi) + ".csv"), ',', '\"');
                            List<String[]> nextLineC = cdata.readAll();

                            // Convert to 2D array of cluster dataset
                            String[][] dataArrC;
                            dataArrC = new String[nextLineC.size()][];
                            dataArrC = nextLineC.toArray(dataArrC);

                            maxCluster.add(dataArrC.length);
                            maxClusterId.add(maxNum.get(maxi));
                        }
                        int maxClustering = Collections.max(maxCluster);
                        int cMaxcid = 0;
                        for (int m = 0; m < maxCluster.size(); m++) {
                            if (maxClustering == maxCluster.get(m)) {
                                cMaxcid = maxClusterId.get(m);
                            }
                        }
                        //after getting max cluster, we test it against delta - threshold
                        //you might accually ignore the if condition
                        if (maxSim >= simdelta) {
                            //add tuple in any max cluster
                            createCluster(cMaxcid, tuple);
                        }

                    } else {
                        //Check it against simdelta - Threshold
                        if (maxSim >= simdelta) {
                            // add to that existing cluster maxNum.get(id)
                            createCluster(maxNum.get(0), tuple);
                        } else {
                            //call create new cluster method
                            clusterID = clusterID + 1; //Number of clusters increases by one everytime a new cluster is created
                            createCluster(clusterID, tuple);
                        }

                    }
                    maxNum.clear();
                    maxCluster.clear();
                    maxClusterId.clear();
                }

                // reset the output array and other variables 
                counter = 0;
                tuple.clear();
                sim_result.clear();
            }

            System.out.println("No of Clusters : " + clusterID);
            /////////////////////////////////////////////////////////////////
            // Calculating cluster accuracy module                             //
            /////////////////////////////////////////////////////////////////
            clusterAccuracy(clusterID);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(IdsBuider.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IdsBuider.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void clusterAccuracy(int numOfCluster) throws IOException {
        for (int cid = 1; cid <= numOfCluster; cid++) {
            CSVReader clusterData = new CSVReader(new FileReader("c" + cid + ".csv"), ',', '\"');

            List<String[]> nextLineC = clusterData.readAll();
            // Convert to 2D array of Weight dataset
            String[][] dataArrC;
            dataArrC = new String[nextLineC.size()][];
            dataArrC = nextLineC.toArray(dataArrC);

            List<String> lableT = new ArrayList<>();
            List<Integer> lableCount = new ArrayList<>();

            List<String> lableDistinct = new ArrayList<>();
            for (int row = 0; row < dataArrC.length; row++) {
                lableT.add(dataArrC[row][dataArrC[0].length - 1]);
            }

            lableDistinct = lableT.stream().distinct().collect(Collectors.toList());

            for (int i = 0; i < lableT.size(); i++) {
                // System.out.println(lableT.get(i));
            }

            System.out.println();
            System.out.println();

            for (int i = 0; i < lableDistinct.size(); i++) {
                // System.out.println(lableDistinct.get(i));
            }
            int counter = 0;
            for (int c = 0; c < lableDistinct.size(); c++) {
                for (int a = 0; a < lableT.size(); a++) {
                    if (lableDistinct.get(c).equalsIgnoreCase(lableT.get(a))) {
                        counter++;
                    }
                }
                lableCount.add(counter);
                System.out.println(lableDistinct.get(c) + " \t " + counter);
                counter = 0;
            }
            Collections.sort(lableCount, Collections.reverseOrder());
            System.out.println(lableCount);

            double lableCount1 = lableCount.get(0);
            double lableCount2 = dataArrC.length;

            double Acc = lableCount1 / lableCount2;
            System.out.println("Cluster " + cid + " Acc " + Acc);
            System.out.println();

            someAcc += Acc;
            lableCount.clear();

        }

        System.out.println("Total Acc = " + someAcc);
    }

    /////////////////////////////////////////////////////////////////////////////
    // Create New Clustering                                                     //
    ////////////////////////////////////////////////////////////////////////////
    private static void createCluster(int clusterID, ArrayList<String> tuple) {
        BufferedWriter bw = null;

        try {
            // APPEND MODE SET HERE
            bw = new BufferedWriter(new FileWriter("c" + clusterID + ".csv", true));
            String collect = tuple.stream().collect(Collectors.joining(","));
            bw.write(collect);
            bw.newLine();
            bw.flush();
        } catch (IOException ioe) {
        } finally {                       // always close the file
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ioe2) {
                    // just ignore it
                }
            }
        } // end try/catch/finally
    }

    /////////////////////////////////////////////////////////////////////////////
    // Calculate distance                                                      //
    ////////////////////////////////////////////////////////////////////////////
    private static String[][] calDistance(String[][] lDataArray, int k) throws NumberFormatException {
        /*
         *******************************************************
         *   Core System logic. distance  calculation          *
         *******************************************************
         */
        // Getting aimin and aimax
        List<Double> minmax = new ArrayList<>();
        double diffx, diffa;
        TreeMap<Integer, Double> map1 = new TreeMap<>();
        for (int l = 0; l < lDataArray.length; l++) {
            for (int f = 0; f < lDataArray[0].length - 1; f++) { //41

                /*
                 *******************************************************
                 *   Character Value                                   *
                 *******************************************************
                 */
                if (f == 1) {
                    if ((lDataArray[0][f]).equalsIgnoreCase(lDataArray[l][f])) {
                        delta = delta + 0;
                    } else {
                        delta = delta + 1;
                    }
                } else if (f == 2) {
                    if ((lDataArray[0][f]).equalsIgnoreCase(lDataArray[l][f])) {
                        delta = delta + 0;
                    } else {
                        delta = delta + 1;
                    }

                } else if (f == 3) {
                    if ((lDataArray[0][f]).equalsIgnoreCase(lDataArray[l][f])) {
                        delta = delta + 0;
                    } else {
                        delta = delta + 1;
                    }

                } else {

                    /*
                     *******************************************************
                     *   Numerical value                                   *
                     *******************************************************
                     */
                    // get aimin and aimax
                    for (int m = 0; m < lDataArray.length; m++) {
                        minmax.add(Double.parseDouble(lDataArray[m][f]));
                    }
                    //sort to determine min and max value in the array
                    Collections.sort(minmax);

                    aimin = minmax.get(0);
                    aimax = minmax.get(minmax.size() - 1);
                    diffa = aimax - aimin;

                    xi = Double.parseDouble(lDataArray[l][f]);
                    xj = Double.parseDouble(lDataArray[0][f]); //first row
                    diffx = Math.abs(xi - xj);

                    if (diffa == 0.0) {         // if denominator is 0
                        delta = delta + 0.0d;
                    } else {
                        delta = delta + (diffx / diffa);
                    }

                }
                minmax.clear();
            }
            map1.put(l, delta);
            delta = 0.0d;

        }

        SortedSet<Map.Entry<Integer, Double>> sorteMap1 = entriesSortedByValues(map1);
        Iterator j = sorteMap1.iterator();
        String str1;
        String part1[];
        ArrayList<Integer> lsamePart1 = new ArrayList<>();
        for (int v = 0; v < k; v++) {
            str1 = j.next().toString();
            part1 = str1.split("=");
            lsamePart1.add(Integer.parseInt(part1[0]));
        }

        // setting lsameK-22-Data
        String[][] lKData = new String[lsamePart1.size()][lDataArray[0].length]; //42
        for (int row = 0; row < lsamePart1.size(); row++) {
            for (int col = 0; col < lDataArray[0].length; col++) {          //42
                lKData[row][col] = lDataArray[lsamePart1.get(row)][col];
            }
        }

        return lKData;
    }

    //------------------------------------------------------------------//
    // sorting the map by value                                         //
    //------------------------------------------------------------------//
    static <K, V extends Comparable<? super V>>
            SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
