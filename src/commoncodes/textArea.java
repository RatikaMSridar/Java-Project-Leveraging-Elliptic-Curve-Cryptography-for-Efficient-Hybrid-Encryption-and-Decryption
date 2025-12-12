

package commoncodes;

import java.awt.*;
import javax.swing.*;

public class textArea extends JInternalFrame
{
    private static JTextArea txtArea;
    //private static JLabel label = new JLabel("Current  Status  of  The  Project");

    public textArea()
    {        
        super("Current  Status  of  The  Project", true, false, true, false);
        setBackground(Color.DARK_GRAY);
        txtArea = new JTextArea(5, 20);
        //txtArea.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(txtArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        txtArea.setFont(new Font("Serif", Font.BOLD, 16));
        txtArea.setEditable(false);
        getContentPane().add(scrollPane);
        setSize(1015, 200);
        setVisible(true);
    }

    public static void setTxt(Object obj)
    {
        txtArea.setText(txtArea.getText() + obj + "\n");
    }
}
