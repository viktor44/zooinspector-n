/**
 * This java file is copyright by Daniele Piras ("danielepiras80", no email known) released under the
 * Apache Software License 2.0. It has been downloaded in december 2009 from the CVS web interface
 * of the sourceforge project http://sourceforge.net/projects/jtoaster/ . The web interface to CVS
 * is not available anymore on sourceforge.
 *
 */

/**
 * Java Toaster is a java utility class for your swing applications
 * that show an animate box coming from the bottom of your screen
 * with a notification message and/or an associated image
 * (like msn online/offline notifications).
 *
 * Toaster panel in windows system follow the taskbar; So if
 * the taskbar is into the bottom the panel coming from the bottom
 * and if the taskbar is on the top then the panel coming from the top.
 *
 * This is a simple example of utilization:
 *
 * import com.nitido.utils.toaster.*;
 * import javax.swing.*;
 *
 * public class ToasterTest
 * {
 *
 *  public static void main(String[] args)
 *  {
 *   // Initialize toaster manager...
 *   Toaster toasterManager = new Toaster();
 *
 *   // Show a simple toaster
 *   toasterManager.showToaster( new ImageIcon( "mylogo.gif" ), "A simple toaster with an image" );
 *  }
 * }
 */
package com.nitido.utils.toaster;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to show tosters in multiplatform
 */
@Slf4j
public class Toaster {

	/** Width of the toster */
	@Getter
	@Setter
	private int toasterWidth = 300;

	/** Height of the toster */
	@Getter
	@Setter
	private int toasterHeight = 80;

	/** Step for the toaster */
	@Getter
	@Setter
	private int step = 20;

	/** Step time */
	@Getter
	@Setter
	private int stepTime = 20;

	/** Show time */
	@Getter
	@Setter
	private int displayTime = 3000;

	/** Current number of toaster... */
	private int currentNumberOfToaster = 0;

	/** Last opened toaster */
	private int maxToaster = 0;

	/** Max number of toasters for the sceen */
	private int maxToasterInSceen;

	/** Font used to display message */
	@Getter
	@Setter
	private Font toasterMessageFont;

	/** Color for border */
	@Getter
	@Setter
	private Color borderColor;

	/** Color for toaster */
	@Getter
	@Setter
	private Color toasterColor;

	/** Set message color */
	@Getter
	@Setter
	private Color messageColor;

	/** Set the margin */
	@Getter
	@Setter
	int margin;

	/** Flag that indicate if use alwaysOnTop or not. method always on top start only SINCE JDK 5 ! */
	boolean useAlwaysOnTop = true;

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor to initialized toaster component...
	 */
	public Toaster() {
		// Set default font...
		toasterMessageFont = new Font("Arial", Font.BOLD, 12);
		// Border color
		borderColor = new Color(245, 153, 15);
		toasterColor = Color.WHITE;
		messageColor = Color.BLACK;
		useAlwaysOnTop = true;
		// Verify AlwaysOnTop Flag...
		try {
		  JWindow.class.getMethod("setAlwaysOnTop", new Class[] { Boolean.class } );
		}
		catch (Exception e) {
			useAlwaysOnTop = false;
		}
	}

	/**
	 * Class that rappresent a single toaster
	 */
	class SingleToaster extends JWindow {
		private static final long serialVersionUID = 1L;

		/** Label to store Icon */
		private JLabel iconLabel = new JLabel();

		/** Text area for the message */
		private JTextArea message = new JTextArea();

		/***
		 * Simple costructor that initialized components...
		 */
		public SingleToaster() {
			initComponents();
		}

		/***
		 * Function to initialized components
		 */
		private void initComponents() {
			setSize(toasterWidth, toasterHeight);
			message.setFont( getToasterMessageFont() );
			JPanel externalPanel = new JPanel(new BorderLayout(1, 1));
			externalPanel.setBackground( getBorderColor() );
			JPanel innerPanel = new JPanel(new BorderLayout( getMargin(), getMargin() ));
			innerPanel.setBackground( getToasterColor() );
			message.setBackground( getToasterColor() );
			message.setMargin( new Insets( 2,2,2,2 ) );
			message.setLineWrap( true );
			message.setWrapStyleWord( true );

			EtchedBorder etchedBorder = (EtchedBorder) BorderFactory.createEtchedBorder();
			externalPanel.setBorder(etchedBorder);

			externalPanel.add(innerPanel);
			message.setForeground( getMessageColor() );
			innerPanel.add(iconLabel, BorderLayout.WEST);
			innerPanel.add(message, BorderLayout.CENTER);
			getContentPane().add(externalPanel);
		}


		/***
		 * Start toaster animation...
		 */
		public void animate() {
			(new Animation(this)).start();
		}
	}

	/***
	 * Class that manage the animation
	 */
	class Animation extends Thread {
		private SingleToaster toaster;

		public Animation(SingleToaster toaster) {
			this.toaster = toaster;
		}

		/**
		 * Animate vertically the toaster. The toaster could be moved from bottom
		 * to upper or to upper to bottom
		 * @param posx
		 * @param fromy
		 * @param toy
		 * @throws InterruptedException
		 */
		protected void animateVertically( int posx, int fromY, int toY ) throws InterruptedException {
			toaster.setLocation( posx, fromY );
			if ( toY < fromY ) {
				for (int i = fromY; i > toY; i -= step) {
					toaster.setLocation(posx, i);
					Thread.sleep(stepTime);
				}
			}
			else {
				for (int i = fromY; i < toY; i += step) {
					toaster.setLocation(posx, i);
					Thread.sleep(stepTime);
				}
			}
			toaster.setLocation( posx, toY );
		}

        @Override
		public void run() {
			try {
				boolean animateFromBottom = true;
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				Rectangle screenRect = ge.getMaximumWindowBounds();

				int screenHeight = screenRect.height;

				int startYPosition;
				int stopYPosition;

				if ( screenRect.y > 0 ) {
				  animateFromBottom = false; // Animate from top!
				}

				maxToasterInSceen = screenHeight / toasterHeight;

				int posx = (int) screenRect.width - toasterWidth - 1;

				toaster.setLocation(posx, screenHeight);
				toaster.setVisible(true);
				if ( useAlwaysOnTop ) {
				  toaster.setAlwaysOnTop(true);
				}

				if ( animateFromBottom ) {
					startYPosition = screenHeight;
					stopYPosition = startYPosition - toasterHeight - 1;
					if ( currentNumberOfToaster > 0 ) {
						stopYPosition = stopYPosition - ( maxToaster % maxToasterInSceen * toasterHeight );
					}
					else {
						maxToaster = 0;
					}
				}
				else {
					startYPosition = screenRect.y - toasterHeight;
					stopYPosition = screenRect.y;

					if ( currentNumberOfToaster > 0 ) {
						stopYPosition = stopYPosition + ( maxToaster % maxToasterInSceen * toasterHeight );
					}
					else {
						maxToaster = 0;
					}
				}

				currentNumberOfToaster++;
				maxToaster++;

				animateVertically( posx, startYPosition, stopYPosition );
				Thread.sleep(displayTime);
				animateVertically( posx, stopYPosition, startYPosition );

				currentNumberOfToaster--;
				toaster.setVisible(false);
				toaster.dispose();
			} 
			catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * Show a toaster with the specified message and the associated icon.
	 */
	public void showToaster(Icon icon, String msg) {
	    SingleToaster singleToaster = new SingleToaster();
	    if ( icon != null ) {
	      singleToaster.iconLabel.setIcon( icon );
	    }
	    singleToaster.message.setText( msg );
		singleToaster.animate();
	}

	/**
	 * Show a toaster with the specified message.
	 */
	public void showToaster( String msg ) {
		showToaster( null, msg );
	}
}
