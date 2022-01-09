import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class AnimatedBeetle
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
               JFrame frame = new JFrame();
               JLabel label = new JLabel();
               ImageIcon icon = new ImageIcon(this.getClass().getResource("71.gif"));
               label.setIcon(icon);
               frame.add(label);
               frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               frame.pack();
               frame.setVisible(true);
                
            }
        });
    }
}
