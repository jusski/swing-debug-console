import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("rawtypes")
public class Debug extends JComponent
{
    private static class DebugString
    {
        String string;
        int x;
        int y;
        boolean button = false;
        boolean isButton()
        {
            return button;
        }
        boolean isString()
        {
            return !button;
        }
    }

    private static volatile ArrayList<DebugString> debugStrings = new ArrayList<>();
    private static ArrayList<DebugString> offScreenDebugStrings = new ArrayList<>();
    private static Map desktopHints;

    private static volatile JFrame frame;

    private static boolean initialized = false;

    static int currentRow = 1;
    static int rowHeight;
    static int charWidth;
    static int ascent;
    static int descent;
    
    static volatile int mouseX = 0;
    static volatile int mouseY = 0;

   
    static
    {
        desktopHints = (Map)Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(Font.decode("Consolas"));
        rowHeight = fontMetrics.getHeight();
        charWidth = fontMetrics.getMaxAdvance();
        ascent = fontMetrics.getAscent();
        descent = fontMetrics.getDescent();
        
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        
        Font mono = Font.decode("Consolas");
        g2d.setFont(mono);
        g2d.addRenderingHints(desktopHints);
        
        List<DebugString> strings = debugStrings.stream().filter(DebugString::isString).collect(Collectors.toList());
        for(DebugString string : strings)
        {
            g2d.drawString(string.string, string.x, string.y);
        }
        
        List<DebugString> buttons = debugStrings.stream().filter(DebugString::isButton).collect(Collectors.toList());
        g2d.setColor(Color.BLUE.brighter());
        for(DebugString button : buttons)
        {
            g2d.drawString(button.string, button.x, button.y);
        }
    }

    public static void renderFrame() throws InvocationTargetException, InterruptedException
    {
        if(!initialized)
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    frame = new JFrame();
                    frame.add(new Debug());
                    frame.setSize(200, 200);frame.setUndecorated(true);
                    frame.addMouseListener(new MouseAdapter()
                    {
                        public void mousePressed(MouseEvent e)
                        {
                            mouseX = e.getX();
                            mouseY = e.getY();
                        }
                    });
                    frame.addMouseMotionListener(new MouseMotionAdapter()
                    {
                        public void mouseDragged(MouseEvent e)
                        {
                            frame.setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
                        }
                    });
                    frame.addKeyListener(new KeyAdapter()
                    {
                        public void keyPressed(KeyEvent e)
                        {
                           if(e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
                        }
                    });
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setVisible(true);
                }
            });
            initialized = true;
        }
        debugStrings = offScreenDebugStrings;
        offScreenDebugStrings = new ArrayList<DebugString>(debugStrings.size());
        frame.repaint();
        currentRow = 1;
    }
    
    public static void println(int x, String string)
    {
        DebugString debugString = new DebugString();
        debugString.string = string;
        debugString.x = x;
        debugString.y = rowHeight * currentRow++;

        offScreenDebugStrings.add(debugString);
    }
    
    public static void println(int x, String string, boolean isButton)
    {
        DebugString debugString = new DebugString();
        debugString.string = string;
        debugString.x = x;
        debugString.y = rowHeight * currentRow++;
        debugString.button = isButton;

        offScreenDebugStrings.add(debugString);
    }

    public static void println(String string)
    {
       println(0, string);
    }  
    
    public static void println(String string, double number)
    {
        println(String.format("%s%.2f", string, number));
        
    }
    
    public static void println(String s1, double n1, String s2, double n2)
    {
        println(String.format("%s%.2f %s%.2f", s1, n1, s2, n2));
        
    }
    
    public static void println(String string, int number)
    {
        println(String.format("%s%d", string, number));
        
    }

    public static void printf(int x, int y, String string)
    {
        DebugString debugString = new DebugString();
        debugString.string = string;
        debugString.x = x * charWidth;
        debugString.y = y * rowHeight;

        offScreenDebugStrings.add(debugString);
    }

    public static void printf(int x, int y, String format, Object... args)
    {
        printf(x, y, String.format(format, args));
    }
    private static ActiveString activeString = null;
    private static class ActiveString
    {
        int baseline;
        int width;
        public ActiveString(int baseline, int width)
        {
            this.baseline = baseline;
            this.width = width;
        }
    }
    
    public static boolean button(String string)
    {
        boolean result = false;
        int baseline = Debug.currentRow * Debug.rowHeight;
        int width = string.length() * Debug.charWidth;
        
        Debug.println(0, string, true);
        if(Debug.mouseX < width &&
           Debug.mouseY > (baseline - ascent) &&
           Debug.mouseY < (baseline + descent))
        {
            Debug.mouseX = 0; Debug.mouseY = 0;
            if(activeString != null && 
               activeString.baseline == baseline)
            {
                activeString = null;
                return false;
            }
            if(activeString == null)
            {
                activeString = new ActiveString(baseline, width);
            }
          
            result = true;
        }
        
        if(activeString != null &&
           activeString.baseline == baseline)
        {
            result = true;
        }
        
        return result;
    }
    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        while(true)
        {
            if(Debug.button("button"))
            {
                Debug.println(10, String.format("%.2f", Math.random()));
                Debug.println(10, "too important too miss");
            }
            
            Table table = Table.createTable("c1", "c2", "c3");
           
            table.printColumn(1, 0, Math.random());
            table.printColumn(1, 1, Math.random());
            table.printColumn(1, 2, Math.random());
           
            table.printColumn(2, 1, Math.random());
            table.printColumn(2, 2, Math.random());
            
            table.printColumn(3, 0, "Summary");
            table.printColumn(3, 2, Math.random());
            
            if(Debug.button(String.format("button2 %.2f", Math.rint(Math.random()))))
            {
                Debug.println("easy: ", 1, "number: ", -1);
                Debug.println("good api ", Math.random());
            }
            Debug.println("bad number " + Math.random());
            Debug.println("Debug counter: ", Math.random());
            Debug.println("number: ", Math.random(), "another: ", Math.random());
            Debug.println("");
            Debug.println("Press Esc to quit.");
            Debug.renderFrame();
            Thread.sleep(100);
        }

    }

    

}
