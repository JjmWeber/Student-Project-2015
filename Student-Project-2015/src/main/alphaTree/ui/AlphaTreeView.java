package main.alphaTree.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.File;
import java.util.ArrayList;

import javax.media.jai.RasterFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import main.alphaTree.util.LabelsToColorByMeanValue;
import main.alphaTree.util.LabelsToRandomColors;
import main.alphaTree.util.FrontiersFromSegmentation;
import main.alphaTree.util.DrawFrontiersOnImage;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageSave;



public class AlphaTreeView extends JFrame implements ChangeListener, MouseListener{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5772987510355903491L;
	
	private static final int SEGMENTATION_DISPLAY = 1;
	private static final int FRONTIERS_ONLY_DISPLAY = 2;
	private static final int FRONTIERS_OVER_IMAGE_DISPLAY = 3;
	private static final int MEAN_VALUES_DISPLAY = 4;
	private static final int ORIGINAL_DISPLAY = 5;

	private AlphaTree alphaTree;
	
	private IntegerImage segmentedImage;
	private ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptorList;
	private ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptorList;
	
	private String[] cutDescriptorNames;
	private String[] filterDescriptorNames;
	
	private int nbCutDescriptors=0;
	private int nbFilterDescriptors=0;
	
	private int displayMode=SEGMENTATION_DISPLAY;

	
	// UI Variable
	private JPanel root;
	private JScrollPane scroll;
	private JPanel rightPanel;
	private JPanel displayPanel;
	private JPanel parametersPanel;
	private JPanel treeCutPanel;
	private JPanel nodeFilteringPanel;
	private JPanel alphaPanel;
	private JRadioButton segmentationButton;
	private JRadioButton frontierOnlyButton;
	private JRadioButton frontierOverImageButton;
	private JRadioButton meanValueButton;
	private JRadioButton originalButton;
	private ButtonGroup displayButtons;
	private JLabel alphaText;
	private JSlider alphaSlider;
	private CutPanel[] cutPanels;
	private FilterPanel[] filterPanels;
	


	
	
	public AlphaTreeView(AlphaTree alphaTree) throws InstantiationException, IllegalAccessException
	{
		this.alphaTree=alphaTree;
		this.segmentedImage=alphaTree.getCCImage();
		this.cutDescriptorList=alphaTree.getCutDescriptorList();
		this.filterDescriptorList=alphaTree.getFilterDescriptorList();
		
		nbCutDescriptors = cutDescriptorList.size();
		nbFilterDescriptors = filterDescriptorList.size();
		
		cutDescriptorNames = new String[nbCutDescriptors];
		filterDescriptorNames = new String[nbFilterDescriptors];

		for(int i=0;i<nbCutDescriptors;i++)
		{
			cutDescriptorNames[i] = ((AlphaTreeNodeCutDescriptor)cutDescriptorList.get(i).newInstance()).getDescriptorName();
		}
		for(int i=0;i<nbFilterDescriptors;i++)
		{
			filterDescriptorNames[i] = ((AlphaTreeNodeFilterDescriptor)filterDescriptorList.get(i).newInstance()).getDescriptorName();
		}
		
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle("Alpha-Tree Viewer");
		
		root = new JPanel();

		Toolkit k = Toolkit.getDefaultToolkit();
		Dimension tailleEcran = k.getScreenSize();
		root.setPreferredSize(new Dimension(Math.min(tailleEcran.width - 3, alphaTree.getOriginalImage().getXDim() + 250), Math.min(tailleEcran.height - 81, alphaTree.getOriginalImage().getYDim() + 81)));
		
		root.setLayout(new BorderLayout());
		this.setContentPane(root);
		scroll = new JScrollPane(null,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		root.add(scroll, BorderLayout.CENTER);
		
		rightPanel = new JPanel(new BorderLayout());
		root.add(rightPanel, BorderLayout.EAST);
		
		displayPanel=new JPanel();
		displayPanel.setLayout(new GridLayout(5,1,0,0));
		displayPanel.setBorder(new TitledBorder("Display"));
		rightPanel.add(displayPanel, BorderLayout.NORTH);
		
		parametersPanel=new JPanel(new BorderLayout());
		rightPanel.add(parametersPanel,BorderLayout.SOUTH);
		
		segmentationButton = new JRadioButton("Segmentation",true);
		meanValueButton = new JRadioButton("Mean Values");
		frontierOnlyButton = new JRadioButton("Frontiers only");
		frontierOverImageButton = new JRadioButton("Frontiers over image");
		originalButton = new JRadioButton("Original image");
		
		displayPanel.add(segmentationButton);
		displayPanel.add(meanValueButton);
		displayPanel.add(frontierOnlyButton);
		displayPanel.add(frontierOverImageButton);
		displayPanel.add(originalButton);
		
		displayButtons = new ButtonGroup();
		displayButtons.add(segmentationButton);
		displayButtons.add(meanValueButton);
		displayButtons.add(frontierOnlyButton);
		displayButtons.add(frontierOverImageButton);
		displayButtons.add(originalButton);
		
		
		
		
		treeCutPanel = new JPanel();
		treeCutPanel.setLayout(new GridLayout(nbCutDescriptors+1,1,0,0));
		treeCutPanel.setBorder(new TitledBorder("Cut parameters"));
		parametersPanel.add(treeCutPanel,BorderLayout.NORTH);
		
		nodeFilteringPanel = new JPanel();
		nodeFilteringPanel.setLayout(new GridLayout(nbFilterDescriptors,1,0,0));
		nodeFilteringPanel.setBorder(new TitledBorder("Filtering parameters"));
		parametersPanel.add(nodeFilteringPanel,BorderLayout.SOUTH);
		
		
		alphaSlider = new JSlider(0,alphaTree.getMaxAlpha(),0);
		alphaText = new JLabel("Alpha : "+alphaSlider.getValue());
		alphaPanel = new JPanel(new GridLayout(2,1,0,0));
		alphaPanel.add(alphaText);
		alphaPanel.add(alphaSlider);
		treeCutPanel.add(alphaPanel);
		
		double[] cutMaxValues = alphaTree.getMaxCutDescriptorValues();
		cutPanels = new CutPanel[nbCutDescriptors];
		for(int i=0;i<nbCutDescriptors;i++)
		{
			cutPanels[i] = new CutPanel((Class<? extends AlphaTreeNodeCutDescriptor>) cutDescriptorList.get(i), cutMaxValues[i], cutDescriptorNames[i],this);
			treeCutPanel.add(cutPanels[i]);
		}
		double[] filterMaxValues = alphaTree.getMaxFilterDescriptorValues();
		filterPanels = new FilterPanel[nbFilterDescriptors];
		for(int i=0;i<nbFilterDescriptors;i++)
		{
			filterPanels[i] = new FilterPanel((Class<? extends AlphaTreeNodeFilterDescriptor>) filterDescriptorList.get(i), filterMaxValues[i], filterDescriptorNames[i],this);
			nodeFilteringPanel.add(filterPanels[i]);
		}
		

		
		//Implement listener
		segmentationButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=SEGMENTATION_DISPLAY; makeDisplayedImage();}});
		meanValueButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=MEAN_VALUES_DISPLAY; makeDisplayedImage();}});
		frontierOnlyButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=FRONTIERS_ONLY_DISPLAY; makeDisplayedImage();}});
		frontierOverImageButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=FRONTIERS_OVER_IMAGE_DISPLAY; makeDisplayedImage();}});
		originalButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=ORIGINAL_DISPLAY; makeDisplayedImage();}});
		scroll.addMouseListener(this);
		
		alphaSlider.addChangeListener(this);

	
		
		makeDisplayedImage();
		pack();
		setVisible(true);
	}
	
	//Treatment of Grey-level case for original image
	//While our display is in color
	private ByteImage getOriginalImageInColor()
	{
		ByteImage img;
		if(alphaTree.getOriginalImage().getBDim()==3)
		{
			img=(ByteImage)alphaTree.getOriginalImage();
		}
		else
		{
			img= new ByteImage(alphaTree.getOriginalImage().getXDim(),alphaTree.getOriginalImage().getYDim(),1,1,3);
			for(int b=0;b<3;b++)
			{
				img.setImage4D(alphaTree.getOriginalImage(), b, Image.B);
			}
		}
		return img;
	}
	
	private ByteImage makeDisplayedImage()
	{
		long t=System.currentTimeMillis();
		//Compute image
		int xDim = alphaTree.getOriginalImage().getXDim();
		int yDim = alphaTree.getOriginalImage().getYDim();
		int bDim = 3;
		ByteImage img=null;
		switch(displayMode)
		{
			case SEGMENTATION_DISPLAY : img=(ByteImage) LabelsToRandomColors.exec(segmentedImage);
										break;
			case MEAN_VALUES_DISPLAY : 	img=LabelsToColorByMeanValue.exec(segmentedImage, getOriginalImageInColor());
										break;
			case FRONTIERS_ONLY_DISPLAY : 	BooleanImage frontiers=FrontiersFromSegmentation.exec(segmentedImage);
											img= new ByteImage(xDim,yDim,1,1,bDim);
											for(int index=0;index<frontiers.size();index++)
											{
												boolean frontier = frontiers.getPixelBoolean(index);
												int colorIndex= index*bDim;
												img.setPixelBoolean(colorIndex, frontier);
												img.setPixelBoolean(colorIndex+1, frontier);
												img.setPixelBoolean(colorIndex+2, frontier);
											}
											break;
			case FRONTIERS_OVER_IMAGE_DISPLAY : BooleanImage frontierS=FrontiersFromSegmentation.exec(segmentedImage);
												img=(ByteImage) DrawFrontiersOnImage.exec(getOriginalImageInColor(), frontierS);												
												break;
			default :					img=getOriginalImageInColor();				
										break;
		
		}
		
		int imgSize=img.size();
		
		//Convert image to SWING compatible format
		DataBufferByte dbb;
		SampleModel s;
		Raster r;
		BufferedImage bimg = null;		
		int[] bandOffsets = { 0, 1, 2 };		
		byte[] byteVal = new byte[imgSize];
		for(int i =0;i<imgSize;i++)
		{
			byteVal[i]=(byte)img.getPixelByte(i);
		}		
		dbb = new DataBufferByte(byteVal, byteVal.length);
		s = RasterFactory.createPixelInterleavedSampleModel(
			DataBuffer.TYPE_BYTE, xDim, yDim, 3, 3
				* xDim, bandOffsets);
		r = RasterFactory.createWritableRaster(s, dbb, new Point(0, 0));
		bimg = new BufferedImage(xDim, yDim, BufferedImage.TYPE_3BYTE_BGR);
		bimg.setData(r);
		int vValue = scroll.getVerticalScrollBar().getValue();
		int hValue = scroll.getHorizontalScrollBar().getValue();
		scroll.setViewportView(new DisplayJAI(bimg));
		scroll.getVerticalScrollBar().setValue(vValue);
		scroll.getHorizontalScrollBar().setValue(hValue);
		t=System.currentTimeMillis()-t;
		System.out.println("Image maked in "+t+"ms.");
		
		return img;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		alphaText.setText("Alpha : "+alphaSlider.getValue());
		double[] cutDescriptorValues=new double[nbCutDescriptors];
		for(int i=0;i<nbCutDescriptors;i++)
		{
			cutPanels[i].updateDisplay();
			cutDescriptorValues[i]=cutPanels[i].slider.getValue();
		}
		double[] minFilterDescriptorValues=new double[nbFilterDescriptors];
		double[] maxFilterDescriptorValues=new double[nbFilterDescriptors];
		for(int i=0;i<nbFilterDescriptors;i++)
		{
			filterPanels[i].updateDisplay();
			minFilterDescriptorValues[i]=filterPanels[i].minSlider.getValue();
			maxFilterDescriptorValues[i]=filterPanels[i].maxSlider.getValue();
		}
		long t=System.currentTimeMillis();
		segmentedImage = alphaTree.getSegmentationFromCutAndFiltering(alphaSlider.getValue(),cutDescriptorValues, minFilterDescriptorValues, maxFilterDescriptorValues);		
		t=System.currentTimeMillis()-t;
		System.out.println("Segmentation computed in "+t+"ms.");
		
		makeDisplayedImage();		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
			//Click on right button
			if(e.getButton() == MouseEvent.BUTTON3)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Picture save");

				int returnVal = chooser.showSaveDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("You save the picture here : "
						+ chooser.getCurrentDirectory() + File.separator
						+ chooser.getSelectedFile().getName());

					ImageSave.exec(makeDisplayedImage(), chooser.getCurrentDirectory()
						+ File.separator + chooser.getSelectedFile().getName());
				}
			}
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private class CutPanel extends JPanel{
		Class<? extends AlphaTreeNodeCutDescriptor> descriptorClass;
		JSlider slider;
		JLabel label;
		String name;
		
		
		public CutPanel(Class<? extends AlphaTreeNodeCutDescriptor> descriptorClass, double max, String name, AlphaTreeView parent)
		{
			super(new GridLayout(2,1,0,0));
			this.descriptorClass=descriptorClass;
			this.name=name;
			slider = new JSlider(0, (int)max, (int)max);
			label = new JLabel(name+" : "+slider.getValue());
			this.add(label);
			this.add(slider);
			slider.addChangeListener(parent);
		}
		
		public void updateDisplay()
		{
			label.setText(name+" : "+slider.getValue());
		}
		
	}
	
	private class FilterPanel extends JPanel{
		Class<? extends AlphaTreeNodeFilterDescriptor> descriptorClass; 
		JSlider minSlider;
		JSlider maxSlider;
		JLabel label;
		String name;
		
		public FilterPanel(Class<? extends AlphaTreeNodeFilterDescriptor> descriptorClass, double max, String name, AlphaTreeView parent)
		{
			super(new GridLayout(3,1,0,0));
			this.descriptorClass=descriptorClass;
			this.name=name;
			minSlider = new JSlider(0, (int)max, 0);
			maxSlider = new JSlider(0, (int)max, (int)max);
			label = new JLabel(name+" in ["+minSlider.getValue()+","+maxSlider.getValue()+"]");
			this.add(label);
			this.add(minSlider);
			this.add(maxSlider);
			minSlider.addChangeListener(parent);
			maxSlider.addChangeListener(parent);
		}
		
		public void updateDisplay()
		{
			label.setText(name+" in ["+minSlider.getValue()+","+maxSlider.getValue()+"]");
		}
	}

	

}
