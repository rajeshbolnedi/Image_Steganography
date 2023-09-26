import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import javax.swing.filechooser.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import java.io.File;
// import java.io.FileInputStream;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
// import javax.swing.ImageIcon;


class ServerSideGui extends JFrame implements ActionListener {
	JButton display;
	JTextArea message;
	ServerSocket connection;
	Socket socket_var;
	DataInputStream inputStream;
	BufferedImage image = null;
	JLabel l;
	String path;
	JButton open;
	JButton decode;
	JLabel sourceImage_label;

	public ServerSideGui() {
		open = new JButton("Open");
		decode = new JButton("Decode");
		display = new JButton("Display");
		sourceImage_label = new JLabel();
		message = new JTextArea(10, 10);
		l = new JLabel();
		decode.addActionListener(this);
		open.addActionListener(this);
		display.addActionListener(this);
		add(display);
		add(sourceImage_label);
		add(message);
		add(open);
		add(decode);
		add(l);
		setLayout(new FlowLayout());
		setVisible(true);
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			connection = new ServerSocket(3636);
			socket_var = connection.accept();
			inputStream = new DataInputStream(socket_var.getInputStream());
		} catch (Exception st) {
			System.out.println("Socket timed out!");
		}
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		if (e.getSource() == display) {
			try {
				byte[] sizeAr = new byte[4];
				inputStream.read(sizeAr);
				int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
				byte[] imageAr = new byte[size];
				inputStream.read(imageAr);
				image = ImageIO.read(new ByteArrayInputStream(imageAr));
				System.out.println(
						"Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
				ImageIO.write(image, "png", new File("D://rk//facebook_logo_ref.png"));
			} catch (Exception ex) {
                System.out.println("yo");
				System.out.println(ex);
			}
		}
		if (e.getSource() == open) {
			int response = fc.showOpenDialog(null);
			if (response == JFileChooser.APPROVE_OPTION) {
				l.setText(fc.getSelectedFile().getAbsolutePath());
				path = fc.getSelectedFile().getAbsolutePath();
			} else {
				l.setText("the user cancelled the operation");
			}
			try {
				image = ImageIO.read(new File(path));
				sourceImage_label.setIcon(new ImageIcon(image));
			} catch (Exception exce) {
				System.out.println("Exception");
			}
		}
		if (e.getSource() == decode) {
			decodeMessage();
		}
	}

	private void decodeMessage() {
		if (image == null) {
			JOptionPane.showMessageDialog(null, "first open a picture");
			return;
		}
		DWT2 obj = new DWT2();
		String secret = obj.extractText(image);
		System.out.println(secret);
		message.setText(secret);
	}

	// private void resetInterface() {
	// 	message.setText("");
	// 	image = null;
	// 	this.validate();
	// }
}

class ServerSide {
	public static void main(String[] args) throws Exception {
		ServerSideGui ss = new ServerSideGui();
	}
}
