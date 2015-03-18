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
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;

import com.sun.media.jai.widget.DisplayJAI;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;

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
	private ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptorList;
	private String[] descriptorNames;
	
	private int displayMode=SEGMENTATION_DISPLAY;

	
	// UI Variable
	private JPanel root;
	private JScrollPane scroll;
	private JPanel rightPanel;
	private JPanel displayPanel;
	private JPanel treeCutPanel;
	private JRadioButton segmentationButton;
	private JRadioButton frontierOnlyButton;
	private JRadioButton frontierOverImageButton;
	private JRadioButton meanValueButton;
	private JRadioButton originalButton;
	private ButtonGroup displayButtons;
	private JLabel alphaText;
	private JSlider alphaSlider;
	private JLabel[] descriptorText;
	private JSlider[] descriptorSlider;
	
	public AlphaTreeView(AlphaTree alphaTree) throws InstantiationException, IllegalAccessException
	{
		this.alphaTree=alphaTree;
		this.segmentedImage=alphaTree.getCCImage();
		this.descriptorList=alphaTree.getDescriptorList();
		descriptorNames = new String[descriptorList.size()];
		for(int i=0;i<descriptorList.size();i++)
		{
			descriptorNames[i] = ((AlphaTreeNodeDescriptor)descriptorList.get(i).newInstance()).getDescriptorName();
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
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		root.add(rightPanel, BorderLayout.EAST);
		
		displayPanel=new JPanel();
		displayPanel.setLayout(new GridLayout(5,1,0,0));
		displayPanel.setBorder(new TitledBorder("Display"));
		rightPanel.add(displayPanel, BorderLayout.NORTH);
		
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
		treeCutPanel.setLayout(new GridLayout(2*(descriptorList.size()+1),1,0,0));
		treeCutPanel.setBorder(new TitledBorder("Cut parameters"));
		rightPanel.add(treeCutPanel, BorderLayout.SOUTH);
		
		alphaSlider = new JSlider(0,alphaTree.getMaxAlpha(),0);
		alphaText = new JLabel("Alpha : "+alphaSlider.getValue());
		treeCutPanel.add(alphaText);
		treeCutPanel.add(alphaSlider);
		
		double[] maxValues = alphaTree.getMaxDescriptorValues();
		descriptorSlider = new JSlider[descriptorList.size()];
		descriptorText = new JLabel[descriptorList.size()];
		for(int i=0;i<descriptorList.size();i++)
		{
			descriptorSlider[i] = new JSlider(0,(int)maxValues[i],(int)maxValues[i]);
			descriptorText[i] = new JLabel(descriptorNames[i]+" : "+descriptorSlider[i].getValue());
			treeCutPanel.add(descriptorText[i]);
			treeCutPanel.add(descriptorSlider[i]);
		}
		
		//Implement listener
		segmentationButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=SEGMENTATION_DISPLAY; makeDisplayedImage();}});
		meanValueButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=MEAN_VALUES_DISPLAY; makeDisplayedImage();}});
		frontierOnlyButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=FRONTIERS_ONLY_DISPLAY; makeDisplayedImage();}});
		frontierOverImageButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=FRONTIERS_OVER_IMAGE_DISPLAY; makeDisplayedImage();}});
		originalButton.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) { displayMode=ORIGINAL_DISPLAY; makeDisplayedImage();}});
		scroll.addMouseListener(this);
		
		alphaSlider.addChangeListener(this);
		for(int i=0;i<descriptorList.size();i++)
		{
			descriptorSlider[i].addChangeListener(this);
		}
		
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
		for(int i=0;i<descriptorList.size();i++)
		{
			descriptorText[i].setText(descriptorNames[i]+" : "+descriptorSlider[i].getValue());
		}
		long t=System.currentTimeMillis();
		double[] descriptorValues=new double[descriptorList.size()];
		for(int i=0;i<descriptorValues.length;i++)
		{
			descriptorValues[i]=descriptorSlider[i].getValue();
		}
		segmentedImage = alphaTree.getSegmentationFromCut(alphaSlider.getValue(),descriptorValues);		
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

	

}
