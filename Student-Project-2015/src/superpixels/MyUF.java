package superpixels;


public class MyUF {
	int[] father;
	int[] height;

	public MyUF(int length){
		father = new int[length];
		height = new int [length];
		for(int i = 0; i<length;i++)
			height[i] = 1;
	}

	public void union(int pixelZone1, int pixelZone2){
		int rootZone1 = find(pixelZone1);
		int rootZone2 = find(pixelZone2);
		if( rootZone1 == rootZone2) return;
		if(height[rootZone1] > height[rootZone2]){
			father[rootZone2] = rootZone1;
			height[rootZone1] = height[rootZone1]+height[rootZone2];
		}else{
			father[rootZone1] = rootZone2;
			height[rootZone2] = height[rootZone1]+height[rootZone2];			
		}

		father[rootZone1] = rootZone2;

	}

	public int find(int x){//we are looking for the zone where the pixel p is, i.e. we must locate its root
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
