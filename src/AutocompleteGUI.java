/*************************************************************************
 * @author Matthew Drabick, adapted by Austin Lu for COMPSCI 201 Autocomplete,
 * 
 * Interactive GUI used to demonstrate the Autocomplete data type.
 *
 *   * Reads a list of terms and weights from a file, specified as a command-line argument.
 *
 *   * As the user types in a text box, display the top-k terms that start with the text that
 *     the user types.
 *
 *   * Displays the result in a browser if the user selects a term (by pressing enter or
 *     clicking a selection).
 *
 * BUG: Selections don't autoupdate if user enter character into text box without typing it
 *      (e.g., by selecting it from Mac OS X Character Viewer). 
 * BUG: Completion list disappears when user clicks to browse.
 *         
 *     
 * FEATURE: add more vertical space between items in suggestion box
 * FEATURE: option to display weights?
 * 
 * UPDATE: Modified to display weights on the left of auto-complete box
 *  
 *  % java AutocompleteGUI cities.txt 10
 *
 *************************************************************************/
/* Don't modify anything under this line */

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;

@SuppressWarnings("serial")
public class AutocompleteGUI extends JFrame {
	private static int DEF_WIDTH = 600;
	private static int DEF_HEIGHT = 400;
	private static String searchURL = "https://www.google.com/search?q=";

	public static final String CHARSET = "UTF-8";
	public static final Locale LOCALE = Locale.US;

	// display top k results
	private final int k;
	private final String autocompletorClassName;

	public AutocompleteGUI(String fileName, int k, String className) {
		this.k = k;
		this.autocompletorClassName = className;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Autocomplete");
		setLocationRelativeTo(null);
		Container content = getContentPane();
		GroupLayout layout = new GroupLayout(content);
		content.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		final AutocompletePanel ap = new AutocompletePanel(fileName);
		JButton searchButton = new JButton("Search Google");
		// searchButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		searchButton.addMouseListener(new MouseListener() {
			// Need a bunch of unimplemented reports
			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				searchOnline(ap.getSelectedText());
			}
		});
		JLabel textLabel = new JLabel("Type text:");
		textLabel.setBorder(BorderFactory.createEmptyBorder(1, 4, 0, 0));
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE)
				.addComponent(ap, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE)
				.addComponent(searchButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(
				layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(textLabel).addComponent(ap).addComponent(searchButton)));
		setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
		pack();
	}

	private class AutocompletePanel extends JPanel {
		private final JTextField searchText;
		private Autocompletor auto;
		private String[] results = new String[k];
		private JList<String> suggestions;

		// keep these two values in sync! - used to keep the listbox the same
		// width as the textfield
		private final int DEF_COLUMNS = 30;
		private final String suggListLen = "<b>Harry Potter and the Deathly Hallows: Part 1 (2010)</b>";

		public AutocompletePanel(String filename) {
			super();

			// read in the data
			Scanner in;
			try {
				in = new Scanner(new File(filename), CHARSET);
				in.useLocale(LOCALE);
				int N = Integer.parseInt(in.nextLine());
				String[] terms = new String[N];
				double[] weights = new double[N];
				for (int i = 0; i < N; i++) {
					String line = in.nextLine();
					int tab = line.indexOf('\t');
					weights[i] = Double.parseDouble(line.substring(0, tab).trim());
					terms[i] = line.substring(tab + 1);
				}
				// create the autocomplete object
				auto = (Autocompletor) Class.forName(autocompletorClassName)
						.getDeclaredConstructor(String[].class, double[].class).newInstance(terms, weights);

			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
				System.exit(1);
			} catch (FileNotFoundException e2) {
				System.out.println("Cannot read file " + filename);
				System.exit(1);

			}

			GroupLayout layout = new GroupLayout(this);
			this.setLayout(layout);
			searchText = new JTextField(DEF_COLUMNS);
			searchText.setMaximumSize(
					new Dimension(searchText.getMaximumSize().width, searchText.getPreferredSize().height));
			searchText.getInputMap().put(KeyStroke.getKeyStroke("UP"), "none");
			searchText.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "none");
			searchText.addFocusListener(new FocusListener() {
				@Override
				public void focusGained(FocusEvent e) {
					int pos = searchText.getText().length();
					searchText.setCaretPosition(pos);
				}

				public void focusLost(FocusEvent e) {
				}
			});
			JPanel searchTextPanel = new JPanel();
			searchTextPanel.add(searchText);
			searchTextPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
			searchTextPanel.setLayout(new GridLayout(1, 1));
			suggestions = new JList<String>(results);
			suggestions.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			suggestions.setVisible(false);
			suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			suggestions.setMaximumSize(
					new Dimension(searchText.getMaximumSize().width, suggestions.getPreferredSize().height));
			// set to make equal to the width of the textfield
			suggestions.setPrototypeCellValue(suggListLen);

			suggestions.setFont(suggestions.getFont().deriveFont(Font.PLAIN, 13));
			Action makeSelection = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					if (!suggestions.isSelectionEmpty()) {
						String selection = (String) suggestions.getSelectedValue();
						selection = selection.replaceAll("\\<.*?>", "");
						searchText.setText(selection);
						getSuggestions(selection);
					}
					searchOnline(searchText.getText());
				}
			};
			Action moveSelectionUp = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (suggestions.getSelectedIndex() >= 0) {
						suggestions.requestFocusInWindow();
						suggestions.setSelectedIndex(suggestions.getSelectedIndex() - 1);
					}
				}
			};
			Action moveSelectionDown = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					if (suggestions.getSelectedIndex() != results.length) {
						suggestions.requestFocusInWindow();
						suggestions.setSelectedIndex(suggestions.getSelectedIndex() + 1);
					}
				}
			};
			Action moveSelectionUpFocused = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					if (suggestions.getSelectedIndex() == 0) {
						suggestions.clearSelection();
						searchText.requestFocusInWindow();
						//int pos = searchText.getText().length();
						// searchText.select(pos, pos);
						searchText.setSelectionEnd(0);

					} else if (suggestions.getSelectedIndex() >= 0) {
						suggestions.setSelectedIndex(suggestions.getSelectedIndex() - 1);
					}
				}
			};
			suggestions.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"),
					"moveSelectionUp");
			suggestions.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"),
					"moveSelectionDown");
			suggestions.getActionMap().put("moveSelectionUp", moveSelectionUp);
			suggestions.getActionMap().put("moveSelectionDown", moveSelectionDown);
			suggestions.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "makeSelection");
			suggestions.getInputMap().put(KeyStroke.getKeyStroke("UP"), "moveSelectionUpFocused");
			suggestions.getActionMap().put("moveSelectionUpFocused", moveSelectionUpFocused);
			suggestions.getActionMap().put("makeSelection", makeSelection);
			JPanel suggestionsPanel = new JPanel();
			suggestionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			suggestionsPanel.add(suggestions);
			suggestionsPanel.setLayout(new GridLayout(1, 1));
			this.setMaximumSize(new Dimension(searchText.getMaximumSize().width, this.getPreferredSize().height));
			suggestions.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					JList<?> theList = (JList<?>) mouseEvent.getSource();
					if (mouseEvent.getClickCount() >= 1) {
						int index = theList.locationToIndex(mouseEvent.getPoint());
						if (index >= 0) {
							String selection = getSelectedText();
							searchText.setText(selection);
							String text = searchText.getText();
							getSuggestions(text);
							searchOnline(searchText.getText());
						}
					}
				}
			});
			searchText.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
					JTextField txtSrc = (JTextField) e.getSource();
					String text = txtSrc.getText();
					getSuggestions(text);
				}

				@Override
				public void keyTyped(KeyEvent e) {
				}
			});
			searchText.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String selection = getSelectedText();
					searchText.setText(selection);
					getSuggestions(selection);
					searchOnline(searchText.getText());
				}

			});
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(searchTextPanel, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(suggestionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE))

			);
			layout.setVerticalGroup(
					layout.createSequentialGroup().addComponent(searchTextPanel).addComponent(suggestionsPanel));
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

		/**
		 * Makes a call to the implementation of Autocomplete to get suggestions
		 * for the currently entered text.
		 * 
		 * @param text
		 *            string to search for
		 */
		public void getSuggestions(String text) {
			text = text.trim();
			if (text.equals("")) {
				suggestions.clearSelection();
				suggestions.setVisible(false);
			} else {
				int textLen = text.length();
				Queue<String> resultQ = new LinkedList<String>();
				Queue<Double> ds = new LinkedList<Double>();
				for (String term : auto.topMatches(text.toLowerCase(), k)) {
					resultQ.add(term);
					ds.add(auto.weightOf(term));
				}
				if (!resultQ.isEmpty()) {
					results = new String[resultQ.size()];
					for (int i = 0; i < results.length; i++) {
						results[i] = resultQ.remove();
						/*
						 * Modified to include the weights of each term and a
						 * delimiter "|" to ensure that the search does not
						 * include the weight.
						 */
						results[i] = "<html>" + results[i].substring(0, textLen) + "<b>" + results[i].substring(textLen)
								+ "</b>" + "|<span style=\"color:#C0C0C0;\">" + String.format("%.0f",ds.remove()) + "</span></html>";
					}
					suggestions.setListData(results);
					suggestions.setVisible(true);
					// suggestions.setSelectedIndex(0); // Pressing enter
					// automatically selects the first one
					// if nothing has been
				} else {
					// No suggestions
					suggestions.setVisible(false);
					suggestions.clearSelection();
				}
			}
		}

		public String getSelectedText() {
			if (!suggestions.isSelectionEmpty()) {
				String selection = (String) suggestions.getSelectedValue();
				int split = selection.indexOf("|");
				// Exclude weight from search string
				selection = selection.substring(0, split); 
				selection = selection.replaceAll("\\<.*?>", "");
				return selection;
			} else
				return getSearchText();
		}

		public String getSearchText() {
			return searchText.getText();
		}
	}

	/**
	 * Creates a URI from the user-defined string and searches the web with the
	 * selected search engine Opens the default web browser (or a new tab if it
	 * is already open)
	 * 
	 * @param s
	 *            string to search online for
	 */
	private void searchOnline(String s) {
		URI searchAddress = null;
		try {
			URI tempAddress = new URI(searchURL + s.trim().replace(' ', '+'));
			searchAddress = new URI(tempAddress.toASCIIString()); // hack to
																	// handle
																	// Unicode
		} catch (URISyntaxException e2) {
			e2.printStackTrace();
			return;
		}
		try {
			Desktop.getDesktop().browse(searchAddress);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
