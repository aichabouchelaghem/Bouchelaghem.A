package HLRP;

import java.io.*;
import java.util.*;

class TSPData {
    String name;
    String type;
    String comment;
    int dimension;
    String edgeWeightType;
    String edgeWeightFormat;
    String displayDataType;
   Vector <Node> nodeCoordinates = new Vector<>();
    double[][] distanceMatrix;

    static class Node {
        int id;
        double x, y;

        Node(int id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }
    
 // Method to calculate Euclidean distance
    private double euclideanDistance(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

   // Method to calculate ATT (pseudo-Euclidean) distance
    private double attDistance(Node a, Node b) {
    	double value ;
        value = Math.sqrt((Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)) / 10.0);
        double distance = Math.round(value);
        if(distance < value){
            distance += 1;
        }
        return distance;
    }

    // Method to calculate GEO (geographical distance"law of sines")
    private double GEODistance(Node a, Node b) {
        final int R = 6371; // Radius of the earth in km
        double lat1 = Math.toRadians(a.x);
        double lon1 = Math.toRadians(a.y);
        double lat2 = Math.toRadians(b.x);
        double lon2 = Math.toRadians(b.y);
        double distance = R * Math.acos( Math.sin(lat1) * Math.sin(lat2) +  Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));
        return distance;
    }
    // Method to calculate distances and fill the distance matrix
    void calculateDistanceMatrix() {
        distanceMatrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i + 1; j < dimension; j++) {
                double distance = 0;
                Node nodeA = nodeCoordinates.get(i);
                Node nodeB = nodeCoordinates.get(j);

                switch (edgeWeightType) {
                    case "EUC_2D":
                        distance = euclideanDistance(nodeA, nodeB);
                        break;
                   case "ATT":
                        distance = attDistance(nodeA, nodeB);
                        break;
                    case "GEO":
                        distance = GEODistance(nodeA, nodeB);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported EDGE_WEIGHT_TYPE: " + edgeWeightType);
                }

                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance; 
            }
        }
    }
}


class Movement {
	String type;
	int vector_Index1;
    int vector_Index2;
    int node_Index1;
    int node_Index2;
    double fitness;

	public Movement(String type, int vector_Index1, int node_Index1, int vector_Index2, int node_Index2, double fitness) {
		this.type = type;
		this.vector_Index1 = vector_Index1;
		this.node_Index1 = node_Index1;
		this.vector_Index2 = vector_Index2;
		this.node_Index2 = node_Index2;
		this.fitness = fitness;
	}

	public String toString() {
		return "Movement{" + "type='" + type + '\'' + ", vector1=" + vector_Index1 + ", index1=" + node_Index1 + ", vector2="
				+ vector_Index2 + ", index2=" + node_Index2 + ", fitness=" + fitness + '}';
	}
}

class solution_fitness {
	Vector<Integer>[] solution ;
	double fitness;
	public solution_fitness(Vector<Integer>[] solution,double fitness) {
		this.solution = solution;
		this.fitness = fitness;
	}

}
public class HLRP{
	
	
	public static TSPData ReadTSPFile(String filePath) throws IOException {
        TSPData tspData = new TSPData();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        boolean readingNodeCoord = false;
        boolean readingEdgeWeight = false;
        int currentRowIndex = 0;
        int colIndex = 0 ;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("NAME")) {
                tspData.name = line.split(":")[1].trim();
            } else if (line.startsWith("TYPE")) {
                tspData.type = line.split(":")[1].trim();
            } else if (line.startsWith("COMMENT")) {
                tspData.comment = line.split(":")[1].trim();
            } else if (line.startsWith("DIMENSION")) {
                tspData.dimension = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("EDGE_WEIGHT_TYPE")) {
                tspData.edgeWeightType = line.split(":")[1].trim();
            } else if (line.startsWith("EDGE_WEIGHT_FORMAT")) {
                tspData.edgeWeightFormat = line.split(":")[1].trim();
                if(tspData.edgeWeightFormat.equals("UPPER_ROW")) {
                	currentRowIndex = 0;
                	colIndex = 0;
                }else if(tspData.edgeWeightFormat.equals("FULL_MATRIX")){
                	currentRowIndex = 0;
                	colIndex = 0;
                }else if(tspData.edgeWeightFormat.equals("LOWER_DIAG_ROW")) {
                	currentRowIndex = 0;
                	colIndex = 0;
                }else  if(tspData.edgeWeightFormat.equals("UPPER_DIAG_ROW")) {
                	currentRowIndex = -1;
                	colIndex = -1;
                }
            } else if (line.startsWith("DISPLAY_DATA_TYPE")) {
                tspData.displayDataType = line.split(":")[1].trim();
            } else if (line.equals("NODE_COORD_SECTION")) {
                readingNodeCoord = true;
            } else if (line.equals("EDGE_WEIGHT_SECTION")) {
                readingEdgeWeight = true;
                tspData.distanceMatrix = new double[tspData.dimension][tspData.dimension];
            } else if (line.startsWith("DISPLAY_DATA_SECTION")||line.startsWith("EOF")) {
                break; 
            } else if (readingNodeCoord) {
                String[] parts = line.split("\\s+");
                int id = Integer.parseInt(parts[0]);
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                tspData.nodeCoordinates.add(new TSPData.Node(id, x, y));
            } else if (readingEdgeWeight) {
               String[] parts = line.split("\\s+");
                if (tspData.edgeWeightFormat.equals("FULL_MATRIX")) {
                    for (int i = 0; i < parts.length; i++) {
                        tspData.distanceMatrix[currentRowIndex][i] = Double.parseDouble(parts[i]);
                    }
                    currentRowIndex++;
                } else if (tspData.edgeWeightFormat.equals("UPPER_ROW")) {
                    
                    for (int i = 0; i < parts.length; i++) {
                        double value = Double.parseDouble(parts[i]);
                        colIndex++;
                        tspData.distanceMatrix[currentRowIndex][colIndex] = value;
                        tspData.distanceMatrix[colIndex][currentRowIndex] = value;
                        if(colIndex == tspData.dimension-1) {
                       	 currentRowIndex++;
                       	 colIndex = currentRowIndex ;
                       }
                    }
                     
               } else if (tspData.edgeWeightFormat.equals("LOWER_DIAG_ROW")) {
                	for (int i = 0; i < parts.length; i++){
                    	double value = Double.parseDouble(parts[i]);
                        tspData.distanceMatrix[currentRowIndex][colIndex] = value;
                        tspData.distanceMatrix[colIndex][currentRowIndex] = value;
                        colIndex++;
                        if(value == 0) {
                        	currentRowIndex++;
                        	colIndex = 0;
                        }
                    }
                } else if (tspData.edgeWeightFormat.equals("UPPER_DIAG_ROW")) {
                    
                	for (int i = 0; i < parts.length; i++) {
                        double value = Double.parseDouble(parts[i]);
                        if(value == 0) {
                        	currentRowIndex++;
                            colIndex = currentRowIndex;
                        }
                        tspData.distanceMatrix[currentRowIndex][colIndex] = value;
                        tspData.distanceMatrix[colIndex][currentRowIndex] = value;
                        colIndex++;
                     }
                   
                }
            }
        }
        reader.close();
        if (!tspData.nodeCoordinates.isEmpty()) {
            tspData.calculateDistanceMatrix();
        }
        return tspData;
    }

	public static Vector<Integer>[] random_solution1(int n, int P, int C) {
		 
		Vector<Integer> candidate = new Vector<>(n);
		Vector<Integer>[] solution = new Vector[P];
        int index_shift, m = n;
        //Initialize candidate list with nodes 1 to n
		for (int i = 0; i < m; i++) {
			candidate.add(i + 1);
		}
		//select p random hubs
		Random rand = new Random();
        for (int i = 0; i < P; i++) {
			index_shift = rand.nextInt(candidate.size());// randomly select a node  index
			int e = candidate.get(index_shift);//get the node
			solution[i] = new Vector<>();//initialize the local route
			solution[i].add(e);// add the hub as the first element in the local route S [i]
			candidate.remove(index_shift);//remove the hub from the candidate list
        }
        //assign remaining nodes (non-hubs) randomly to the local routes
        Random rand2 = new Random();
        while (!candidate.isEmpty()) {
			int chosenHub = rand2.nextInt(P);//select randomly local route
			if (solution[chosenHub].size() < C){//check capacity
				index_shift = rand.nextInt(candidate.size());//choose a node to assign
				solution[chosenHub].add(candidate.get(index_shift));//add to the local route 
				candidate.remove(index_shift);
			}
			
		}
       
        return solution;
	}
	public void to_show(Vector<Integer>[] solution, int p,int scenario,double alpha) {
		if(scenario == 1) {
			System.out.println("ST");		
		}else if(scenario == 2) {
			System.out.println("SL");		
		}else if(scenario == 3) {
			System.out.println("SQ");		
		}
		System.out.println(alpha);
		for (int k = 0; k < p ; k++) {
			for(int i = 0; i < solution[k].size() ; i++) {
				System.out.print(solution[k].get(i) + " ");
			}
			System.out.println();
		}
	}
	public static double Fitness(Vector<Integer>[] solution, double[][] DistanceMatrix, int n, int p, double alpha) {
		double f, distanceInterHubRoutes = 0, distanceLocalRoutes = 0;
		int hub1, hub2, non_hub1, non_hub2;
        //calculate the cost of  transportation in inter-hub route
		for (int k = 0; k < p - 1; k++) {
			hub1 = solution[k].get(0);
			hub2 = solution[k + 1].get(0);
			distanceInterHubRoutes += DistanceMatrix[hub1 - 1][hub2 - 1];
		}
		hub1 = solution[p - 1].get(0);
		hub2 = solution[0].get(0);
		distanceInterHubRoutes += DistanceMatrix[hub1 - 1][hub2 - 1];//add the distance between the first and the last hub in solution
       //calculate the cost of transportation in all local routes
		for (int i = 0; i < p; i++) {
			if (solution[i].size() > 1) {
				for (int j = 0; j < solution[i].size() - 1; j++) {
					non_hub1 = solution[i].get(j);
					non_hub2 = solution[i].get(j + 1);
					distanceLocalRoutes += DistanceMatrix[non_hub1 - 1][non_hub2 - 1];
				}

				hub1 = solution[i].get(0);
				non_hub2 = solution[i].get(solution[i].size() - 1);
				distanceLocalRoutes += DistanceMatrix[hub1 - 1][non_hub2 - 1];//add the distance between the hub and the last non-hub in local route
			}
        }
		f = alpha * distanceInterHubRoutes + distanceLocalRoutes;
		return f;
	}
    
	public static Boolean equals(Vector<Integer>[] S1, Vector<Integer>[] S2, int p) {
		boolean equals;
		int id, id1;
		equals = true;
		for (int i = 0; i < p; i++) {
			if (S2[i].size() == S1[i].size()) {
				for (int j = 0; j < S1[i].size(); j++) {
					id = S1[i].get(j);
					id1 = S2[i].get(j);
					if (id != id1) {
						equals = false;
					}

				}
			} else {
				equals = false;
			}
		}
		return equals;
	}
	public static Vector<Integer>[] copy(Vector<Integer>[] S1, int p) {
		Vector<Integer>[] S2 = new Vector[p] ;
		int id;
		for (int i = 0; i < p; i++) {
			S2[i] = new Vector<>();
			for (int j = 0; j < S1[i].size(); j++) {
				id = S1[i].get(j);
				S2[i].add(id);
			}
		}
		return S2;
	}
	public static Vector<Integer>[] Apply_move(Vector<Integer>[] S, Movement m) {
		String Type;
		int i1, i2, j1, j2, temp;
		Vector<Integer> temp1 = new Vector<>();
		Type = m.type;
		i1 = m.vector_Index1;
		j1 = m.node_Index1;
		i2 = m.vector_Index2;
		j2 = m.node_Index2;
		if (Type == "Swap two non-hub intern" || Type == "Swap two hub" || Type == "Swap hub and non_hub intern"|| Type == "swap hub and non hub extern" || Type == "Swap two non-hub extern") {
			temp = S[i1].get(j1);
			S[i1].set(j1, (S[i2].get(j2)));
			S[i2].set(j2, temp);

		} else if (Type == "Remove/add node") {
			temp = S[i1].get(j1);
			S[i1].remove(j1);
			S[i2].add(j2, temp);
		} else if (Type == "Swap two local route") {
			// Swap vectors in the solution
			temp1 = S[i1];
			S[i1] = S[i2];
			S[i2] = temp1;

		} else if (Type == "remove/add local route") {
			temp1 = S[i1];
			if (i2 > i1) {
				for (int k = i1; k < i2; k++) {
					S[k] = S[k + 1];
				}
			}else {
				for (int k = i1; k > i2; k--) {
					S[k] = S[k - 1];
				}
			}
			S[i2] = temp1;

		}
		return S;

	}
	
	public static double CalculateDelta(String type,int p,double alpha,Vector<Integer>[] solution,double[][] DistanceMatrix,int i, int j,int h, int k) {
		
		int temp;
		Vector<Integer> temp1 = new Vector<>();;
		double delta = 0 ;
		
		if(type == "Swap two non-hub intern" || type == "Swap two non-hub extern") {
			// Subtract the current cost of the edges connected to node non-hub at (i,j)
			if (j != solution[i].size() - 1) {
				delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j + 1) - 1]
						+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1];
			} else { // if the node is at the end of the route (cyclic)
				delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(0) - 1]
						+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1];
			}
			 // Subtract the current cost of the edges connected to node non-hub at (h,k)
			if (k != solution[h].size() - 1) {
				delta -= DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k + 1) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
            } else {// k == solution[h].size()-1
				delta -= DistanceMatrix[solution[h].get(k) - 1][solution[h].get(0) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
			}
			// Perform the swap temporarily
			temp = solution[i].get(j);
			solution[i].set(j, (solution[h].get(k)));
			solution[h].set(k, temp);
			// Add the new cost after the swap for node non-hub at (i,j)
			if (j != solution[i].size() - 1) {
				delta += DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j + 1) - 1]
						+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1];
			} else {
				delta += DistanceMatrix[solution[i].get(j) - 1][solution[i].get(0) - 1]
						+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1];
			}
			// Add the new cost after the swap for node non-hub at (h,k)
			if (k != solution[h].size() - 1) {
				delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k + 1) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
            } else {
				delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(0) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
			}
			// Undo the swap to keep the original solution unchanged
			temp = solution[i].get(j);
			solution[i].set(j, (solution[h].get(k)));
			solution[h].set(k, temp);
			
		}else if(type == "Swap two hub") {
			// Subtract costs related to the non-hub connections of each hub
			if (solution[i].size() > 1) {
				delta -= DistanceMatrix[solution[i].get(0) - 1][solution[i].get(1) - 1]
						+ DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
			}
			if (solution[h].size() > 1) {
				delta -= DistanceMatrix[solution[h].get(0) - 1][solution[h].get(1) - 1]
						+ DistanceMatrix[solution[h].get(0) - 1][solution[h].get(solution[h].size() - 1) - 1];
			}
			// Subtract the edge costs between hub (i,0) and its neighboring hubs,
			// and between hub (h,0) and its neighboring hubs, applying the discount factor alpha.
			if (i == 0) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
			} else {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
			}

			if ((i != p - 1) && (h != p - 1)) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (i == p - 1) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (h == p - 1) { 
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			}
			// Perform the hub swap temporarily
			temp = solution[i].get(0);
			solution[i].set(0, solution[h].get(0));
			solution[h].set(0, temp);
			// add new costs related to the non-hub connections of each hub
			if (solution[i].size() > 1) {
				delta += DistanceMatrix[solution[i].get(0) - 1][solution[i].get(1) - 1]
						+ DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
			}
			if (solution[h].size() > 1) {
				delta += DistanceMatrix[solution[h].get(0) - 1][solution[h].get(1) - 1]
						+ DistanceMatrix[solution[h].get(0) - 1][solution[h].get(solution[h].size() - 1) - 1];
			}
			// After the swap, add the edge costs between hub (i,0) and its neighboring hubs,
			// and between hub (h,0) and its neighboring hubs, applying the discount factor alpha.
			if (i == 0) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
			} else {// (i!=0)
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
			}
			if ((i != p - 1) && (h != p - 1)) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (i == p - 1) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (h == p - 1) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			}
			//Undo the swap to keep the solution unchanged
			temp = solution[i].get(0);
			solution[i].set(0, solution[h].get(0));
			solution[h].set(0, temp);
			
		}else if(type == "Swap hub and non_hub intern" || type == "swap hub and non hub extern") {
			// Subtract costs related to the non-hub connections of each hub
			if (solution[i].size() > 1) {
				delta -= DistanceMatrix[solution[i].get(0) - 1][solution[i].get(1) - 1]
						+ DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
			}
			// Subtract the edge costs between hub (i,0) and its neighboring hubs, applying the discount factor alpha.
			if (i == 0) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
			} else {// (i!=0)
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
			}
			if ((i != p - 1)) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1];
			} else { 
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1];
			}
			// Subtract the current cost of the edges connected to node non-hub at (h,k)
            if (k!= solution[h].size() - 1) {
            	delta -= DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k + 1) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
			} else {// k ==solution[i].size()-1
				delta -= DistanceMatrix[solution[h].get(k) - 1][solution[h].get(0) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
			}
            
         // Perform the swap temporarily
            temp = solution[i].get(0);
			solution[i].set(0, (solution[h].get(k)));
			solution[h].set(k, temp);
			//add costs related to the non-hub connections of each hub
			if (solution[i].size() > 1) {
				delta += DistanceMatrix[solution[i].get(0) - 1][solution[i].get(1) - 1]
						+ DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
			}
			//add the edge costs between hub (i,0) and its neighboring hubs, applying the discount factor alpha.
            if (i == 0) {
            	delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
			
			} else {// (i!=0)
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
			}
			if ((i != p - 1)) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1];
			} else { 
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1];
			}
			// add the new cost of the edges connected to node non-hub at (h,k)
            if (k!= solution[h].size() - 1) {
            	delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k + 1) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
			} else {// k ==solution[j].size()-1
				delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(0) - 1]
						+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];
			}
          //Undo the swap to keep the solution unchanged
            temp = solution[i].get(0);
			solution[i].set(0, (solution[h].get(k)));
			solution[h].set(k, temp);
			
		}else if(type == "Swap two local route") {
			// Subtract the edge costs between hub (i,0) and its neighboring hubs,
			// and between hub (h,0) and its neighboring hubs, applying the discount factor alpha.
			if (i == 0) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
			} else {// (i!=0)
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
			}
			
			if ((i != p - 1) && (h != p - 1)) {
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (i == p - 1) { 
				delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (h == p - 1) { // If hub2 is in the last vector in the solution
				delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			}
			
			 temp1 = solution[i];
	         solution[i] = solution[h];
	         solution[h] = temp1;
	         // add the edge costs between hub (i,0) and its neighboring hubs,
		    // and between hub (h,0) and its neighboring hubs, applying the discount factor alpha.
		    if (i == 0) {
		    	delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
			
			} else {// (i!=0)
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
			}
			if ((i != p - 1) && (h != p - 1)) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (i == p - 1) {
				delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			} else if (h == p - 1) {
				delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0) - 1]
						+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
			}
			temp1 = solution[i];
	        solution[i] = solution[h];
	        solution[h] = temp1;
			
		}else if(type == "remove/add local route"){
			// Subtract the edge costs between hub (i,0) and its neighboring hubs, applying the discount factor alpha.
			 if (i == 0) {
				 delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
				 delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1];
             } else if (i == p - 1) {
            	 delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1];
            	 delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
             }else {
            	 delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
            	 delta -= alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1];
             }

             if (h > i) {// Remove the cost of the edge between (h, 0) and (h + 1, 0)
                 if (h != p - 1) {
                	 delta -= alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1];
                 } else if(i!= 0 ) { //h ==p-1
                	 delta -= alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0) - 1];
                  }
             } else {// Remove the cost of the edge between (h, 0) and (h - 1, 0)
                 if (h != 0) {
                	 delta -= alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
                 } else if(i != p-1) {
                	 delta -= alpha * DistanceMatrix[solution[h].get(0) - 1][solution[p - 1].get(0) - 1];
                 }
             }

			//apply move	
			 temp1= solution[i];
			 if (h>i) {
                 for (int d = i; d < h; d++) {
                    solution[d] = solution[d + 1];
                  }
               } else {
                for (int d = i; d >h; d--) {
                   solution[d] = solution[d - 1];
                }
              }
				solution[h] = temp1;
				
				
		      if (h > i) {// add the cost of the edge between (i, 0) and (i - 1, 0)
                 if (i == 0) {
                 	if(h!=p-1)// Pour éviter de calculer deux fois la distance entre le premier et le dernier élément dans la solution
                 		delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
                 } else {//i!=0
                	 delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1];
                 }
             } else {// add the cost of the edge between (i, 0) and (i + 1, 0)
                 if (i == p - 1) {
                 	if(h!=0)
                 	  delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0) - 1];
                 } else {//i!= p-1
                	 delta += alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1];
                 }
             }
		   // add the edge costs between hub (h,0) and its neighboring hubs, applying the discount factor alpha.
             if (h == 0) {
            	 delta += alpha * DistanceMatrix[solution[h].get(0) - 1][solution[p - 1].get(0) - 1]
                         + alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1];
             } else if (h == p - 1) {
            	 delta += alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0) - 1]
                         + alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
             } else {
            	 delta += alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
                         + alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1];
             }
            //apply move
             temp1= solution[h];
			 if (i>h) {
                 for (int d = h; d < i; d++) {
                    solution[d] = solution[d + 1];
                  }
               }else {
                for (int d = h; d >i; d--) {
                   solution[d] = solution[d - 1];
                }
              }
				solution[i] = temp1;
		}else if(type == "Remove/add node") {
			
			if(i != h) {	// If the removed node at position (i, j) and the insertion position (h, k) are in different local routes  

	             if (j == 0) {// If the removed node is a hub, subtract all routes connected to the hub node
	            	 delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j+1) - 1]
	            			 + DistanceMatrix[solution[i].get(j) - 1][solution[i].get(solution[i].size() - 1) - 1];
	                 if (i == 0) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0)- 1];
					} else if (i == p - 1) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0)- 1];
					}else if (i != p - 1) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0)- 1];
	               } 
	            }else if(j == solution[i].size() - 1){// If the removed node is a non-hub, subtract all routes connected to the non-hub node
					delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1]
							+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(0) - 1];
					
				} else if (j != solution[i].size() - 1 && j != 0){// 0 < j < p-1, 
					delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1]
							+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j + 1) - 1];
				} 

				if (k == 0) {// If the added node is inserted at the hub position
					if(solution[h].size() > 1) delta -= DistanceMatrix[solution[h].get(0) - 1][solution[h].get(solution[h].size() - 1) - 1];

					if (h == 0) {
						delta -= alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[p - 1].get(0)- 1];
					}else if (h == p - 1) {
						delta -= alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0)- 1];
					} else if (h != p - 1) {
						delta -= alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0)- 1];
					} 

				} else if (k != solution[h].size()){// If the added node is inserted at the non-hub position
					delta -= DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1];

				} else if(k ==  solution[h].size()){// If the added node is inserted at the last position in local route h
					delta -= DistanceMatrix[solution[h].get(k - 1) - 1][solution[h].get(0) - 1];
				}

				if (j == 0 && k == 0) {
					//If the removed node is a hub and is inserted at a hub position,
					//and both positions are neighbors in the inter-hub route, correct the double removal
					if ((i - h) == 1 || (i - h) == -1) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[h].get(0) - 1];
					} else if ((i == 0 && h == p - 1) || (i == p - 1 && h == 0))
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[h].get(0) - 1];
				}
				

				// apply move  
				temp = solution[i].get(j);
				solution[i].remove(j);
				solution[h].add(k, temp);

				
				if (j == 0) {// The removed node was a hub ( update inter-hub and local route costs)

				    if (solution[i].size() > 1) {// If route i still contains nodes, update its new local route cost
						delta += DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
					}
	                if (i == 0) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0)
										- 1];
					} else if (i == p - 1) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0)- 1];
					} else if (i != p - 1) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0)- 1];
					} 
	            } else if (solution[i].size() > 1) {//if The removed node was a non-hub
					if (j < solution[i].size()) {
						delta += DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1];
					} else {// j == solution[i].size()
						delta += DistanceMatrix[solution[i].get(j - 1) - 1][solution[i].get(0) - 1];
					}
				}
				if (k == 0) {// If the node is added at the hub position
					delta += DistanceMatrix[solution[h].get(0) - 1][solution[h].get(1) - 1]
							+ DistanceMatrix[solution[h].get(0) - 1][solution[h].get(solution[h].size() - 1) - 1];
					if (h == 0) {
						delta += alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[p - 1].get(0)- 1];
					}else if (h == p - 1) {
						delta += alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0)- 1];
					} else if (h != p - 1) {
						delta += alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0)- 1];
					} 

				} else if (k != solution[h].size() - 1) {// If the node is added at the non-hub position
					delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1]
							+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k + 1) - 1];
				} else {// k == solution[h].size()-1
					delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1]
							+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(0) - 1];
				}

				if (j == 0 && k == 0) {
					// If the removed node is a hub and is inserted at a hub position, 
					// and both positions are neighbors in the inter-hub route, correct the double addition
					if ((i - h) == 1 || (i - h) == -1) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[h].get(0) - 1];

					} else if ((i == 0 && h == p - 1) || (i == p - 1 && h == 0)) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[h].get(0) - 1];
					}
				}
				//apply move
				temp = solution[h].get(k);
				solution[h].remove(k);
				solution[i].add(j, temp);
		
			}else {// If the removed node at position (i, j) and the insertion position (h, k) are in same local routes  
				
				if (j == 0) {// If the removed node is a hub, subtract all routes connected to the hub node
	            	 delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j+1) - 1]
	            			 + DistanceMatrix[solution[i].get(j) - 1][solution[i].get(solution[i].size() - 1) - 1];
	                 if (i == 0) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0)- 1];
					} else if (i == p - 1) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0)- 1];
					}else if (i != p - 1) {
						delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0)- 1];
	               } 
	            }else if(j == solution[i].size() - 1){// If the removed node is a non-hub
					delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1]
							+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(0) - 1];
					
				} else if (j != solution[i].size() - 1 && j != 0){// 0 < j < p-1, 
					delta -= DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1]
							+ DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j + 1) - 1];
				}
				
				if (k == 0) {// If the added node is inserted at the hub position
					delta -= DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
					 if (i == 0) {
							delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
									+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0)- 1];
					} else if (i == p - 1) {
							delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1]
									+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0)- 1];
					}else if (i != p - 1) {
							delta -= alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
									+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0)- 1];
		             } 
				} else if(k ==  solution[i].size()-1){// If the added node is inserted at the non-hub position
					delta -= DistanceMatrix[solution[i].get(k) - 1][solution[i].get(0) - 1];
				}else if (k != solution[i].size()-1) {
					if(k > j ) {delta -= DistanceMatrix[solution[i].get(k) - 1][solution[i].get(k + 1) - 1];}
					if(k < j)  {delta -= DistanceMatrix[solution[i].get(k) - 1][solution[i].get(k - 1) - 1];}
					
                }
				// If the removed node is a hub or is inserted at a hub position,
				// and both positions are adjacent in the same route, correct the double removal
				if ((j == 0 && k == solution[i].size()-1) || (j == solution[i].size()-1 && k == 0)) {
					delta += DistanceMatrix[solution[i].get(j) - 1][solution[h].get(k) - 1];
			    }
				// Modify the solution  
				temp = solution[i].get(j);
				solution[i].remove(j);
				solution[h].add(k, temp);
				
				if (j == 0) {
					delta += DistanceMatrix[solution[i].get(0) - 1][solution[i].get(solution[i].size() - 1) - 1];
					
	                if (i == 0) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[p - 1].get(0) - 1];
					} else if (i == p - 1) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[0].get(0)- 1];
					} else if (i != p - 1) {
						delta += alpha* DistanceMatrix[solution[i].get(0) - 1][solution[i + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[i].get(0) - 1][solution[i - 1].get(0)- 1];
					} 
	            } else if(j == solution[i].size()-1) {
	            	delta += DistanceMatrix[solution[i].get(j) - 1][solution[i].get(0) - 1];
	            }else {
	            	if(k > j ) {delta += DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j - 1) - 1];}
					if(k < j)  {delta += DistanceMatrix[solution[i].get(j) - 1][solution[i].get(j + 1) - 1];}
				}
				if (k == 0) {// if position add the node is position hub
					delta += DistanceMatrix[solution[h].get(0) - 1][solution[h].get(1) - 1]
							+ DistanceMatrix[solution[h].get(0) - 1][solution[h].get(solution[h].size() - 1) - 1];
					if (h == 0) {
						delta += alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[p - 1].get(0)- 1];
					}else if (h == p - 1) {
						delta += alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[0].get(0)- 1];
					} else if (h != p - 1) {
						delta += alpha* DistanceMatrix[solution[h].get(0) - 1][solution[h + 1].get(0) - 1]
								+ alpha * DistanceMatrix[solution[h].get(0) - 1][solution[h - 1].get(0)- 1];
					} 
				}  else if(k == solution[h].size()-1){
					delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1]
							+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(0) - 1];
				}else if (k != solution[h].size() - 1) {// if position not first and not last
					delta += DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k - 1) - 1]
							+ DistanceMatrix[solution[h].get(k) - 1][solution[h].get(k + 1) - 1];
				}
				// If the removed node is a hub or is inserted at a hub position,
				// and both positions are adjacent in the same route, correct the double addition
				if ((j == solution[i].size()-1 && k == 0) || (j == 0  && k == solution[i].size() -1)) {
					delta -= DistanceMatrix[solution[i].get(j) - 1][solution[h].get(k) - 1];
                 }
				
				temp = solution[h].get(k);
				solution[h].remove(k);
				solution[i].add(j, temp);
			}
				
		}
       return delta;
	}
	
	// Swap two non hub intern
	public static Vector<Movement> Neighbors1(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p,int c, double alpha) {
		Vector<Movement> N = new Vector<>();
		String type = "Swap two non-hub intern";
		double delta;
		for (int i = 0; i < p; i++) {
			if (solution[i].size() > 2) {
				for (int j = 1; j < solution[i].size()-1; j++) {
					for (int k = j + 1; k < solution[i].size(); k++) {
					    delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,j,i,k);
			            N.add(new Movement(type, i, j, i, k, (fitness + delta)));
					}
				}
			}
		}
		return N;
	}

	
// Swap two hub
	public static Vector<Movement> Neighbors2(Vector<Integer>[] solution,double fitness ,double[][] DistanceMatrix, int n, int p,int c, double alpha) {
		Vector<Movement> N = new Vector<>();
		String type = "Swap two hub";
		double delta ;
		for (int i = 0; i < p-1; i++) {
			for (int h = (i + 1); h < p; h++) {
				delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,0,h,0);
				N.add(new Movement(type, i,0, h, 0, (fitness + delta)));
			}

		}
		
		return N;

	}

//swap hub /non hub intern
	public static Vector<Movement> Neighbors3(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p,int c, double alpha) {
		Vector<Movement> N = new Vector<>();
		double delta;
		String type = "Swap hub and non_hub intern";
		for (int i = 0; i < p; i++) {
			if (solution[i].size() > 2) {
				for (int k = 1; k < solution[i].size(); k++) {
					delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,0,i,k);
					N.add(new Movement(type, i,0, i, k, (fitness + delta)));
				}
			}
		}
		
		return N;
	}

// (Remove and add node) move
	public static Vector<Movement> Neighbors4(Vector<Integer>[] solution,double fitness, double[][] DistanceMatrix, int n, int p,int c, double alpha) {
		 Vector<Movement> N = new Vector<>();
	     String type = "Remove/add node";
		double delta;
		for (int i = 0; i < p; i++) {
        	if (solution[i].size() > 1) {
				for (int j = 0; j < solution[i].size(); j++) {
					for (int h = 0; h < p; h++) {
						if (i != h){
							if(solution[h].size() < c ) {
								for (int k = 0; k <= solution[h].size(); k++) {
									  delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,j,h,k);
									  N.add(new Movement(type, i,j,h,k,(fitness + delta)));
								 }
		                        }
						 }else {

								for (int k = 0; k < solution[h].size(); k++) {
									if(j != k) {
									    delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,j,h,k);
										N.add(new Movement(type, i,j,h,k,(fitness + delta)));
									 }
								 }
						 }
					}
				}
			}
		}
         
		return N;
	}

// Swap two non hub extern
	public static Vector<Movement> Neighbors5(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p, int c,double alpha) {
		Vector<Movement> N = new Vector<>();
		String type = "Swap two non-hub extern";
		double delta;

		for (int i = 0; i < p; i++) {
			if (solution[i].size() > 1) {
				for (int j = 1; j < solution[i].size(); j++) {
					for (int h = i + 1; h < p; h++) {
						if (solution[h].size() > 1) {
							for (int k = 1; k < solution[h].size(); k++) {
								delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,j,h,k);
								N.add(new Movement(type, i,j,h,k, (fitness + delta)));
								
		                   }

						}
					}
				}
			}
		}
		return N;
		}
	//swap hub and non hub extern
	public static Vector<Movement> Neighbors6(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p, int c,double alpha) {
		 Vector<Movement> N = new Vector<>();
			String type = "swap hub and non hub extern";
			double delta;
            for (int i = 0; i < p; i++) {
				for (int h = 0; h < p; h++) {
					if(solution[h].size()>1 && i!=h) {
						for (int k = 1; k < solution[h].size(); k++) {
							delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,0,h,k);
							N.add(new Movement(type, i,0,h,k,(fitness + delta)));
							
						}
					}
				}
			}
			return N;
			}
	//swap two vector
	public static Vector<Movement> Neighbors7(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p, int c,double alpha) {
			Vector<Movement> N = new Vector<>();
			String type = "Swap two local route";
			double delta;

			for (int i = 0; i < p-1; i++) {
				for (int h = (i + 1); h < p; h++) {
					delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,0,h,0);
					N.add(new Movement(type, i,0, h, 0, (fitness + delta)));
					
				}
	        }
			return N;
		}
	//remove add Vector
	public static Vector<Movement> Neighbors8(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p, int c,double alpha) {
		Vector<Movement> N = new Vector<>();
		String type = "remove/add local route";
		double delta;
		for (int i = 0; i < p; i++) {
			for (int h = 0; h < p; h++) {
				if(i!=h){
				  delta = CalculateDelta(type,p,alpha,solution,DistanceMatrix,i,0,h,0);
				  N.add(new Movement(type, i,0, h, 0, (fitness + delta)));
				 
				}
		    }
	     }
		return N;

		}
	
	public static Vector<Movement> All_Neighbors(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p, int c,
			double alpha) {
		Vector<Movement> All_neighbors = new Vector<>();
		Vector<Movement> N1 = Neighbors1(solution,fitness, DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N2 = Neighbors2(solution,fitness, DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N3 = Neighbors3(solution,fitness, DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N4 = Neighbors4(solution, fitness,DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N5 = Neighbors5(solution,fitness, DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N6 = Neighbors6(solution,fitness, DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N7 = Neighbors7(solution,fitness, DistanceMatrix, n, p, c, alpha);
		Vector<Movement> N8 = Neighbors8(solution,fitness, DistanceMatrix, n, p, c, alpha);
		All_neighbors.addAll(N1);
		All_neighbors.addAll(N2);
		All_neighbors.addAll(N3);
		All_neighbors.addAll(N4);
		All_neighbors.addAll(N5);
		All_neighbors.addAll(N6);
		All_neighbors.addAll(N7);
		All_neighbors.addAll(N8);
		Collections.sort(All_neighbors, (m1, m2) -> Double.compare(m1.fitness, m2.fitness));
		return All_neighbors;
	}

	
	public static Vector<Integer>[] Simple_local_search(Vector<Integer>[] solution, double[][] DistanceMatrix , int n, int p, int c,double alpha) {
		Vector<Integer>[] S = copy(solution,p);
        Vector<Integer>[] S_best = copy(solution,p);
		double fitness, fitness_best;
		boolean stop = false;
        fitness = Fitness(S_best, DistanceMatrix, n, p, alpha);
        fitness_best = fitness;
        
		while (!stop ) {
            Vector<Movement> N = new Vector<>();
			N = All_Neighbors(S,fitness, DistanceMatrix, n, p, c, alpha);
			Movement m = N.get(0);
			fitness = m.fitness;
			S = Apply_move(S, m);
			stop = equals(S, S_best, p);// if(equals == true) -> stop=true;
			if (fitness_best > fitness) {
				//System.out.println(" fitness : "+fitness);
				S_best = copy(S,p);
                fitness_best = fitness;
			}
			
        }
		return S_best;
	}
	
	public static Boolean check(Vector<Double> list_tabou ,double fitness){
		boolean checkTabou = false;
		for(int ipl = 0; ipl < list_tabou.size(); ipl++){
			if(Math.abs(list_tabou.get(ipl) - fitness) < 0.000001 ) {
				checkTabou = true;
				return checkTabou;
			}       
		}
		
	return checkTabou;
	}
	
    public static Vector<Integer>[] Tabu_Search(Vector<Integer>[] solution, double[][] DistanceMatrix, int n, int p, int C,double alpha) {
		Vector<Integer>[] S = copy(solution,p);
		Vector<Integer>[] S_best = copy(solution,p);
		Vector<Double> list_tabou = new Vector<>();
		Vector<Movement> N = new Vector<>();
		Movement m;
		double fitness=0, fitness_best;
		fitness = Fitness(S_best, DistanceMatrix, n, p, alpha);
        fitness_best = fitness;
		int iteration_Number =1000, max_size_list = 20;
		boolean checkTabou = false;
		
		while (iteration_Number > 0) {

			N .clear();
			N = All_Neighbors(S,fitness, DistanceMatrix, n, p, C, alpha);// Generates a set of neighbor solutions (candidate moves)
			m = N.get(0);
			fitness = m.fitness;
			
			do { // Check if the current solution's fitness is in the tabu list
				checkTabou = check( list_tabou , fitness);
				if(checkTabou) {
					N.remove(0);
					m = N.get(0);
					fitness = m.fitness;
				}
			}while (checkTabou) ;
			// Update the tabu list with the new solution
			if (list_tabou.size() < max_size_list) {
				list_tabou.add(fitness);
			
			} else {
				if (list_tabou.size() == max_size_list) {
					list_tabou.remove(0);
					list_tabou.add(fitness);
				}
			}
			
            S = Apply_move(S, m);
			if (fitness_best > fitness) {
				//System.out.println(" fitness : "+fitness+" iteration : "+iteration_Number);
				S_best = copy(S,p);
                fitness_best = fitness;
			}

			iteration_Number--;
		}
		return S_best;
	}
   
	public static solution_fitness Apply_All_Neighbors(Vector<Integer>[] solution,double fitness,double[][] DistanceMatrix, int n, int p,int C, double alpha) {
		Vector<Movement> N = new Vector<>();
		Vector<Integer> move = new Vector<>();
		int numbre_move, index;
		Movement m;
        for (int i = 1; i <= 8; i++) {
			move.add(i);
		}
       Random rand = new Random();
       
		while (!move.isEmpty()) {
			N.clear(); 
            index = rand.nextInt(move.size());
			numbre_move = move.get(index);
			move.remove(index);

			switch (numbre_move) {
			case 1:
				N.addAll(Neighbors1(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 2:
				N.addAll(Neighbors2(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 3:
				N.addAll(Neighbors3(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 4:
				N.addAll(Neighbors4(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 5:
				N.addAll(Neighbors5(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 6:
				N.addAll(Neighbors6(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 7:
				N.addAll(Neighbors7(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			case 8:
				N.addAll(Neighbors8(solution,fitness,DistanceMatrix, n, p, C, alpha));
				break;
			}
            if (!N.isEmpty()) {
				Collections.sort(N, (m1, m2) -> Double.compare(m1.fitness, m2.fitness));
				m = N.get(0); 
				solution = Apply_move(solution, m);
				fitness = m.fitness;
				
			}
        }
        return new solution_fitness(solution,fitness);
	}

	public static Vector<Integer>[] Neighborhood_Search_with_Random_Selection (Vector<Integer>[] solution, double[][] DistanceMatrix, int n, int p, int c,double alpha) {

		Vector<Integer>[] S = copy(solution,p);
	    Vector<Integer>[] S_best = copy(solution,p);
		double fitness=0, fitness_best;
		fitness = Fitness(S_best, DistanceMatrix, n, p, alpha);
        fitness_best = fitness;
		int iteration_Number = 0;
		solution_fitness SF;
        while ( iteration_Number<1000) {
            SF = Apply_All_Neighbors(S,fitness, DistanceMatrix, n, p, c, alpha);
			S = SF.solution;
			fitness = SF.fitness;
			if (fitness_best  > fitness ) {
				S_best = copy(S,p);
                fitness_best = fitness;
				//System.out.println(" new found  " + fitness_best);
			}
            iteration_Number++;
			
		}
		return S_best;
	}
	
	
				
    public static void main(String[] args) {
        try {
            TSPData tspData = ReadTSPFile("C:\\Users\\lenovo\\eclipse-workspace\\HLRP\\instance\\burma14.tsp");
            HLRP HLRP = new HLRP();
            int n, p=0, C=0,scenario=0,y;
			double alpha=-1,resultDouble;
			n = tspData.dimension;
			Vector<Integer>[] solution = new Vector[p]; 
			Vector<Integer>[] s0 = new Vector[p];
  			Vector<Integer>[] s1 = new Vector[p];
  			Vector<Integer>[] s2 = new Vector[p];
  			Vector<Integer>[] s3 = new Vector[p];
  		
          
          try(Scanner in = new Scanner(System.in)){
    				while(scenario == 0) {
    					System.out.println("choose a scenario!");
    					System.out.println("Enter 1 for scenario ST:");
    					System.out.println("Enter 2 for scenario SL:");
    					System.out.println("Enter 3 for scenario SQ:");
    					scenario = in.nextInt();
    					if(scenario != 1 && scenario != 2 && scenario != 3) {
    						scenario = 0;
    					}
    				}
    				
    				while(alpha == -1) {
    					System.out.println("choose  alpha!");
    					System.out.println("Enter 1 for alpha 0.2");
    					System.out.println("Enter 2 for alpha 0.4");
    					System.out.println("Enter 3 for alpha 0.6");
    					System.out.println("Enter 4 for alpha 0.8");
    					y = in.nextInt();
    					switch (y) {
    					case 1:
    						alpha = 0.2;
    					break;
    					case 2:
    						alpha = 0.4;
    					break;
    					case 3:
    						alpha = 0.6;
    					break;
    					case 4:
    						alpha = 0.8;
    					break;
    					default:
    						System.out.println("erreur");
    						alpha = -1;
    					break;
    					}
    				}
    			}
    			
    			switch (scenario) {
    				case 1:
    					System.out.println("scenario ST: p = 0.2n and C = (n/p)");
    					resultDouble = Math.ceil((double)0.2*n);//exemple :Math.ceil(3.4) => 4.0
    					p = (int) resultDouble;
    					resultDouble = Math.ceil((double) n / p);
    					C = (int) resultDouble;
    					
    					break;
    				case 2:
    					System.out.println("scenario SL: p = 0.2n and C =1.8*(n/p)");
    					resultDouble = Math.ceil((double)0.2*n);
    					p = (int) resultDouble;
    					resultDouble = Math.ceil((double)n / p);
    				    resultDouble = resultDouble*(1.8);
    					C = (int) resultDouble;
    					
    					break;
    				case 3:
    					System.out.println("scenario SQ: p = C = sqrt(n)");
    					resultDouble = Math.sqrt(n);
    					resultDouble = Math.ceil((double)resultDouble);
    					p = (int) resultDouble;
    					C=p;
    					break;
    					
    			}
    			
    			
    		System.out.println("p = " + p + ", c =" + C+" alpha = "+alpha);
    	
    	
    		System.out.println("random_solution:");
    		solution = random_solution1(n,p, C);
            System.out.println(Fitness(solution, tspData.distanceMatrix, n,p,alpha));
    	    HLRP.to_show(solution,p,scenario,alpha);
    	    
    	    long startTime1 = System.currentTimeMillis();
    	    System.out.println("local_search:");
    	    s0 = Simple_local_search(solution, tspData.distanceMatrix, n, p, C, alpha);
    	    System.out.println(Fitness(s0, tspData.distanceMatrix, n, p, alpha));
  			HLRP.to_show(s0,p,scenario,alpha);
  			long endTime1 = System.currentTimeMillis();
  			double durationInSeconds1 = (endTime1 - startTime1) / 1000.0;
  			System.out.println("Temps d'exécution : " + durationInSeconds1 + " secondes");
  			 
  					 
  			long startTime2 = System.currentTimeMillis();
  			System.out.println("Tabu Search:");
  			s1 = Tabu_Search(solution, tspData.distanceMatrix, n, p, C, alpha);
  			System.out.println(Fitness(s1, tspData.distanceMatrix, n, p, alpha));
  			HLRP.to_show(s1, p,scenario,alpha);
  			long endTime2 = System.currentTimeMillis();
  			double durationInSeconds2 = (endTime2 - startTime2) / 1000.0;
  			System.out.println("Temps d'exécution : " + durationInSeconds2 + " secondes");
  			
  			long startTime3 = System.currentTimeMillis();
  			System.out.println("Neighborhood Search with Random Selection:");
  			s2 = Neighborhood_Search_with_Random_Selection(solution, tspData.distanceMatrix, n, p, C, alpha);
  			System.out.println(Fitness(s2, tspData.distanceMatrix, n, p, alpha));
  			HLRP.to_show(s2, p,scenario,alpha);
  			long endTime3 = System.currentTimeMillis();
  			double durationInSeconds3 = (endTime3 - startTime3) / 1000.0;
  			System.out.println("Temps d'exécution : " + durationInSeconds3 + " secondes");
  			
  			/*long startTime4 = System.currentTimeMillis();
  			System.out.println("partial_destruction:");
  		    s3 = partial_destruction(solution, tspData.distanceMatrix, n, p, C, alpha);
  		   System.out.println( Fitness(s3, tspData.distanceMatrix, n, p, alpha));
  			HLRP.to_show(s3, p,scenario,alpha);
  			long endTime4 = System.currentTimeMillis();
  			double durationInSeconds4 = (endTime4 - startTime4) / 1000.0;
  			System.out.println("Temps d'exécution : " + durationInSeconds4 + " secondes");*/
  			
     } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

