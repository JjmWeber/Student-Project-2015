package superpixels;


public class MyUF {
	int[] father;
	int[] height;

	public MyUF(int [] father){
		this.father = father;
		System.out.println("longeur de la forêt : "+father.length);
		height = new int [father.length];
		for(int i = 0; i<father.length;i++)
			height[i] = 1;
	}

	public void union(int pixelZone1, int pixelZone2){
		int rootZone1 = FindWithCompression(pixelZone1);
		int rootZone2 = FindWithCompression(pixelZone2);
		if( rootZone1 == rootZone2) return;
		if(height[rootZone1] > height[rootZone2]){
			father[rootZone2] = rootZone1;
			height[rootZone1] = height[rootZone1]+height[rootZone2];
		}else{
			father[rootZone1] = rootZone2;
			height[rootZone2] = height[rootZone1]+height[rootZone2];			
		}
	}

	public int find(int x){//we are looking for the zone where the pixel p is, i.e. we must locate its root
		//System.out.println("x = "+ x);
		//System.out.println("father[x] = "+father[x]);
		while(father[x] != x){
			x = father[x];
		}
		return x;
	}

	public int FindWithCompression(int x){
		//normal treatment
		int root = find(x);
		//and before to return the result we arrange a bit the things
		while(x != root){
			int intermediateNode = father[x];
			father[x] = root; //intermediateNode is now directly attached to root
			x = intermediateNode;
		}
		return root;
	}
}
