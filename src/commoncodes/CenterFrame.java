package commoncodes;

import java.awt.*;
import javax.swing.*;
public class CenterFrame
{
	public CenterFrame(JInternalFrame f)
	{
		Dimension ss=Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation((ss.width-f.getWidth())/2,((ss.height-f.getHeight())/2)-50);
	}
}


