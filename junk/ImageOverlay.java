import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
/*from   w  w  w. j av a2  s  .c  om*/
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImageOverlay {

  public static void main(String[] args) throws Exception {
    URL urlImage1 = new URL("http://123emoji.com/wp-content/uploads/2017/08/sticker-15-229.png");
    URL urlImage2 = new URL("https://i.pinimg.com/originals/03/a2/cb/03a2cbb7070a5f2d4372f749bbce0816.png");

    final Image bgImage = ImageIO.read(urlImage1);
    final Image fgImage = ImageIO.read(urlImage2);
    
    int w = fgImage.getWidth(null);
    int h = fgImage.getHeight(null);
    //final BufferedImage bgImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

    //Draw the two pictures, terrain and water, on to each other using this
    final BufferedImage finalImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = finalImage.createGraphics();
    g.drawImage(bgImage, 0, 0, null);
    g.drawImage(fgImage, 0, 0, null);
    g.dispose();

    Runnable r = new Runnable() {
      @Override
      public void run() {
        JPanel gui = new JPanel(new GridLayout(1, 0, 5, 5));

        gui.add(new JLabel(new ImageIcon(bgImage)));
        gui.add(new JLabel(new ImageIcon(fgImage)));
        gui.add(new JLabel(new ImageIcon(finalImage)));

        JOptionPane.showMessageDialog(null, gui);
      }
    };
    SwingUtilities.invokeLater(r);
  }
}
