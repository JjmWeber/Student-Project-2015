package alphaTree.descriptor;

public class AlphaTreeNodeDescriptorOmega extends AlphaTreeNodeDescriptor {
	
	private int[] min;
	private int[] max;
	private int omega;


	public AlphaTreeNodeDescriptorOmega()
	{
		min=new int[3];
		max=new int[3];
		for(int i=0;i<3;i++)
		{
			min[i]=Integer.MAX_VALUE;
			max[i]=Integer.MIN_VALUE;
		}
		omega=-1;
	}
	
	@Override
	public void addPixel(int[] values) {
		for(int i=0;i<3;i++)
		{
			if(min[i]>values[i])
				min[i]=values[i];
			if(max[i]<values[i])
				max[i]=values[i];
			if(max[i]-min[i]>omega)
				omega=max[i]-min[i];
		}
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorOmega descOmega= (AlphaTreeNodeDescriptorOmega) descriptor;
		for(int i=0;i<3;i++)
		{
			if(min[i]>descOmega.min[i])
			min[i]=descOmega.min[i];
			if(max[i]<descOmega.max[i])
				max[i]=descOmega.max[i];
			if(max[i]-min[i]>omega)
				omega=max[i]-min[i];
		}
		
	}

	@Override
	public boolean check(double value) {
		return value>=omega;
	}

	@Override
	public double getValue() {
		return omega;
	}

	@Override
	public String getDescriptorName() {
		return "Omega";
	}
	
	public AlphaTreeNodeDescriptor clone()
	{
		AlphaTreeNodeDescriptorOmega clone = new AlphaTreeNodeDescriptorOmega();
		for(int b=0;b<3;b++)
		{
			clone.min[b]=min[b];
			clone.max[b]=max[b];
		}
		clone.omega=omega;
		return clone;
	}

}
